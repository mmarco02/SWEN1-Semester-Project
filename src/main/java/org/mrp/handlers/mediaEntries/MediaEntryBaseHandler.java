package org.mrp.handlers.mediaEntries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.domain.MediaEntry;
import org.mrp.domain.MediaType;
import org.mrp.domain.User;
import org.mrp.http.HttpStatus;
import org.mrp.persistence.*;
import org.mrp.persistence.implemenatations.*;
import org.mrp.service.MediaService;
import org.mrp.service.RatingService;
import org.mrp.service.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mrp.service.Utils.HttpUtils.sendJsonResponse;
import static org.mrp.service.Utils.HttpUtils.sendResponse;

public class MediaEntryBaseHandler {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static MediaService mediaService;
    private static UserService userService;

    static {
        try {
            initializeServices();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize services", e);
        }
    }

    private static void initializeServices() throws SQLException {
        Connection connection = DatabaseConnection.getConnection();

        UserRepository userRepository = new UserRepository(connection);
        UserProfileRepository userProfileRepository = new UserProfileRepository(connection);
        TokenRepository tokenRepository = new TokenRepository(connection);
        RatingRepository ratingRepository = new RatingRepository(connection);
        LikeRepository likeRepository = new LikeRepository(connection);
        MediaEntryRepository mediaEntryRepository = new MediaEntryRepository(connection);

        userService = new UserService(userRepository, userProfileRepository, tokenRepository);
        mediaService = new MediaService(mediaEntryRepository, ratingRepository);
    }

    public static void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGetMedia(exchange);
                break;
            case "POST":
                handleCreateMedia(exchange);
                break;
            default:
                sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(),
                        HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }

    private static void handleGetMedia(HttpExchange exchange) throws IOException {
        try {
            Optional<User> userOpt = userService.validateBearerToken(exchange);
            if(userOpt.isEmpty()){
                sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                        HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
                return;
            }

            Map<String, String> queryParams = parseQueryParams(exchange);
            List<MediaEntry> allEntries = mediaService.getAllMediaEntries();
            List<MediaEntry> filteredEntries = applyFilters(allEntries, queryParams);

            List<MediaEntry> resultList = new ArrayList<>(filteredEntries);
            applySorting(resultList, queryParams.get("sortBy"));

            sendJsonResponse(exchange, HttpStatus.OK.getCode(), resultList);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "Error fetching media entries", "text/plain");
        }
    }

    private static Map<String, String> parseQueryParams(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = new java.util.HashMap<>();

        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    params.put(pair[0], pair[1]);
                }
            }
        }
        return params;
    }

    private static List<MediaEntry> applyFilters(List<MediaEntry> entries, Map<String, String> filters) {
        //validate filters
        if (filters.containsKey("releaseYear")) {
            try {
                Integer.parseInt(filters.get("releaseYear"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("releaseYear not Integer");
            }
        }

        if (filters.containsKey("ageRestriction")) {
            try {
                int age = Integer.parseInt(filters.get("ageRestriction"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ageRestriction must be an integer.");
            }
        }

        if (filters.containsKey("rating")) {
            try {
                double rating = Double.parseDouble(filters.get("rating"));
                if (rating < 0.0 || rating > 5.0) {
                    throw new IllegalArgumentException("rating must be between 0.0 and 5.0.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("rating must be a number");
            }
        }

        if (filters.containsKey("mediaType")) {
            String type = filters.get("mediaType").toUpperCase();
            try {
                MediaType.valueOf(type);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("mediaType must be one of: MOVIE, SERIES, GAME");
            }
        }

        //apply filters
        return entries.stream().filter(entry -> {
            boolean passes = true;

            if (filters.containsKey("title")) {
                String title = filters.get("title").toLowerCase();
                if (entry.getTitle() == null || !entry.getTitle().toLowerCase().contains(title)) {
                    //System.out.println("Filtered out by title: " + entry.getTitle());
                    passes = false;
                }
            }

            if (passes && filters.containsKey("genre")) {
                String genre = filters.get("genre").toLowerCase();
                if (entry.getGenres() == null || entry.getGenres().isEmpty()) {
                    //System.out.println("Filtered out by genre (no genres): " + entry.getTitle());
                    passes = false;
                } else {
                    boolean hasGenre = entry.getGenres().stream()
                            .anyMatch(g -> g != null && g.toLowerCase().contains(genre));
                    if (!hasGenre) {
                        passes = false;
                    }
                }
            }

            if (passes && filters.containsKey("mediaType")) {
                String type = filters.get("mediaType").toUpperCase();
                if (entry.getMediaType() == null || !entry.getMediaType().name().equals(type)) {
                    //System.out.println("Filtered out by mediaType: " + entry.getMediaType());
                    passes = false;
                }
            }

            if (passes && filters.containsKey("releaseYear")) {
                int year = Integer.parseInt(filters.get("releaseYear"));
                if (entry.getReleaseYear() == null || entry.getReleaseYear() != year) {
                    //System.out.println("Filtered out by releaseYear: " + entry.getReleaseYear() + " != " + year);
                    passes = false;
                }
            }

            if (passes && filters.containsKey("ageRestriction")) {
                int age = Integer.parseInt(filters.get("ageRestriction"));
                if (entry.getAge() > age) {
                    //System.out.println("Filtered out by ageRestriction: entry age=" + entry.getAge() + ", filter age=" + age);
                    passes = false;
                }
            }

            if (passes && filters.containsKey("rating")) {
                double rating = Double.parseDouble(filters.get("rating"));
                if (entry.getAverageRating() < rating) {
                    //System.out.println("Filtered out by rating: entry rating=" + entry.getAverageRating() + ", filter rating=" + rating);
                    passes = false;
                }
            }

            if (passes) {
                //System.out.println("Entry passes all filters: " + entry.getTitle());
            }

            return passes;
        }).collect(Collectors.toList());
    }

    private static void applySorting(List<MediaEntry> entries, String sortBy) {
        if (sortBy == null) return;

        switch (sortBy.toLowerCase()) {
            case "title":
                entries.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
                break;
            case "score":
            case "rating":
                entries.sort((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()));
                break;
            case "releaseyear":
                entries.sort((a, b) -> Integer.compare(b.getReleaseYear(), a.getReleaseYear()));
                break;
            default:
        }
    }

    private static void handleCreateMedia(HttpExchange exchange) throws IOException {
        Optional<User> userOpt = userService.validateBearerToken(exchange);
        if (userOpt.isEmpty()) {
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            Map<String, Object> requestData = mapper.readValue(requestBody, Map.class);

            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            String mediaTypeStr = (String) requestData.get("mediaType");
            Integer releaseYear = (Integer) requestData.get("releaseYear");
            List<String> genres = requestData.get("genres") instanceof List ? (List<String>) requestData.get("genres") : null;
            Integer ageRestriction = (Integer) requestData.get("ageRestriction");

            if (title == null || title.trim().isEmpty() ||
                    mediaTypeStr == null || releaseYear == null || ageRestriction == null) {
                sendResponse(exchange, HttpStatus.BAD_REQUEST.getCode(),
                        "Missing required fields: title, mediaType, releaseYear, ageRestriction", "text/plain");
                return;
            }

            try {
                MediaType mediaType = MediaType.valueOf(mediaTypeStr.toUpperCase());

                MediaEntry newEntry = mediaService.createMediaEntry(
                        title,
                        description != null ? description : "",
                        mediaType,
                        releaseYear,
                        genres != null ? genres : List.of(),
                        ageRestriction,
                        userOpt.get()
                );

                MediaEntry savedEntry = mediaService.getMediaEntryById(newEntry.getId()).orElse(null);

                sendJsonResponse(exchange, HttpStatus.CREATED.getCode(), savedEntry);
            } catch (IllegalArgumentException e) {
                sendResponse(exchange, HttpStatus.BAD_REQUEST.getCode(),
                        "Invalid media type. Must be one of: MOVIE, SERIES, GAME", "text/plain");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpStatus.BAD_REQUEST.getCode(),
                    "Invalid request format", "text/plain");
        }
    }
}