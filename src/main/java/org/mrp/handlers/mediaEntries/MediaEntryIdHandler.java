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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mrp.service.Utils.HttpUtils.sendJsonResponse;
import static org.mrp.service.Utils.HttpUtils.sendResponse;

public class MediaEntryIdHandler {
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

        MediaEntryRepository mediaEntryRepository = new MediaEntryRepository(connection);
        UserRepository userRepository = new UserRepository(connection);
        UserProfileRepository userProfileRepository = new UserProfileRepository(connection);
        TokenRepository tokenRepository = new TokenRepository(connection);
        RatingRepository ratingRepository = new RatingRepository(connection);
        RatingService ratingService = new RatingService(ratingRepository);

        userService = new UserService(userRepository, userProfileRepository, tokenRepository);
        mediaService = new MediaService(mediaEntryRepository, ratingRepository);
    }

    public static void handle(HttpExchange exchange, int entryId) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGetMediaById(exchange, entryId);
                break;
            case "PUT":
                handleUpdateMedia(exchange, entryId);
                break;
            case "DELETE":
                handleDeleteMedia(exchange, entryId);
                break;
            default:
                sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(),
                        HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }

    private static void handleGetMediaById(HttpExchange exchange, int entryId) throws IOException {
        try {
            Optional<MediaEntry> mediaEntry = mediaService.getMediaEntryById(entryId);

            if (mediaEntry.isPresent()) {
                sendJsonResponse(exchange, HttpStatus.OK.getCode(), mediaEntry.get());
            } else {
                sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                        "Media entry not found", "text/plain");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "Error fetching media entry", "text/plain");
        }
    }

    private static void handleUpdateMedia(HttpExchange exchange, int entryId) throws IOException {
        Optional<User> userOpt = userService.validateBearerToken(exchange);
        if (userOpt.isEmpty()) {
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    "Authentication required", "text/plain");
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            Map<String, Object> requestData = mapper.readValue(requestBody, Map.class);

            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            String mediaTypeStr = (String) requestData.get("mediaType");
            Integer releaseYear = (Integer) requestData.get("releaseYear");
            List<String> genres = (List<String>) requestData.get("genres");
            Integer ageRestriction = (Integer) requestData.get("ageRestriction");

            if (title == null || title.trim().isEmpty() ||
                    mediaTypeStr == null || releaseYear == null || ageRestriction == null) {
                sendResponse(exchange, HttpStatus.BAD_REQUEST.getCode(),
                        "Missing required fields: title, mediaType, releaseYear, ageRestriction", "text/plain");
                return;
            }

            try {
                MediaType mediaType = MediaType.valueOf(mediaTypeStr.toUpperCase());
                boolean updated = mediaService.updateMediaEntry(
                        entryId,
                        title,
                        description != null ? description : "",
                        mediaType,
                        releaseYear,
                        genres != null ? genres : List.of(),
                        ageRestriction,
                        userOpt.get()
                );

                if (updated) {
                    Map<String, String> response = Map.of("message", "Media entry updated successfully");
                    sendJsonResponse(exchange, HttpStatus.OK.getCode(), response);
                } else {
                    sendResponse(exchange, HttpStatus.FORBIDDEN.getCode(),
                            "You don't have permission to update this", "text/plain");
                }

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

    private static void handleDeleteMedia(HttpExchange exchange, int entryId) throws IOException {
        Optional<User> userOpt = userService.validateBearerToken(exchange);
        if (userOpt.isEmpty()) {
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
            return;
        }

        try {
            boolean success = mediaService.deleteMediaEntry(entryId, userOpt.get());

            if (success) {
                Map<String, String> response = Map.of("message", "Media entry deleted successfully");
                sendJsonResponse(exchange, HttpStatus.OK.getCode(), response);
            } else {
                sendResponse(exchange, HttpStatus.FORBIDDEN.getCode(),
                        "You don't have permission to delete this", "text/plain");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    HttpStatus.INTERNAL_SERVER_ERROR.getDescription(), "text/plain");
        }
    }
}