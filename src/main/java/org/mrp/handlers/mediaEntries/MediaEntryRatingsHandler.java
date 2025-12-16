package org.mrp.handlers.mediaEntries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.domain.MediaEntry;
import org.mrp.domain.Rating;
import org.mrp.domain.User;
import org.mrp.http.HttpStatus;
import org.mrp.persistence.DatabaseConnection;
import org.mrp.persistence.implemenatations.*;
import org.mrp.service.MediaService;
import org.mrp.service.RatingService;
import org.mrp.service.UserService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static org.mrp.service.Utils.HttpUtils.sendJsonResponse;
import static org.mrp.service.Utils.HttpUtils.sendResponse;

public class MediaEntryRatingsHandler {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static MediaService mediaService;
    private static UserService userService;
    private static RatingService ratingService;

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
        LikeRepository likeRepository = new LikeRepository(connection);

        ratingService = new RatingService(ratingRepository, likeRepository);
        userService = new UserService(userRepository, userProfileRepository, tokenRepository);
        mediaService = new MediaService(mediaEntryRepository, ratingRepository);
    }

    public static void handle(HttpExchange exchange, int entryId) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "POST":
                handleRateMedia(exchange, entryId);
                break;
            default:
                sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(),
                        HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }

    private static void handleRateMedia(HttpExchange exchange, int entryId) throws IOException {
        Optional<User> userOpt = userService.validateBearerToken(exchange);
        if(userOpt.isEmpty()){
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes());

        try {
            Map<String, Object> requestData = mapper.readValue(requestBody, Map.class);

            Integer stars = (Integer) requestData.get("stars");
            String comment = (String) requestData.get("comment");

            if(stars == null || comment == null || comment.isEmpty()) {
                sendResponse(exchange, HttpStatus.BAD_REQUEST.getCode(),
                        "Missing required fields: stars, comment", "text/plain");
                return;
            }

            User user = userOpt.get();

            Optional<MediaEntry> entry = mediaService.getMediaEntryById(entryId);
            if(entry.isEmpty()) {
                sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                        "No Media Found for this ID", "text/plain");
                return;
            }

            Optional<Rating> ratingOpt = mediaService.rateMediaEntry(entryId, user.getId(), stars, comment);

            if(ratingOpt.isEmpty()){
                sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                        "Failed to create Rating for MediaEntry", "text/plain");
                return;
            }

            Rating rating = ratingOpt.get();

            Rating savedRating = ratingService.getRatingById(rating.getId()).orElse(null);

            sendJsonResponse(exchange, HttpStatus.CREATED.getCode(),
                    savedRating);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpStatus.BAD_REQUEST.getCode(),
                    HttpStatus.BAD_REQUEST.getDescription(), "text/plain");
        }
    }
}
