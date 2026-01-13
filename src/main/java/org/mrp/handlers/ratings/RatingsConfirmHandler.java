package org.mrp.handlers.ratings;

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

import static org.mrp.service.utils.HttpUtils.sendJsonResponse;
import static org.mrp.service.utils.HttpUtils.sendResponse;

public class RatingsConfirmHandler {
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

    public static void handle(HttpExchange exchange, int ratingId) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "POST":
                handleConfirmRating(exchange, ratingId);
                break;
            default:
                sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(),
                        HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }

    private static void handleConfirmRating(HttpExchange exchange, int ratingId) throws IOException {
        Optional<User> userOpt = userService.validateBearerToken(exchange);
        if(userOpt.isEmpty()){
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
            return;
        }

        Optional<Rating> ratingOpt = ratingService.getRatingById(ratingId);
        if(ratingOpt.isEmpty()){
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                    "Rating not found", "text/plain");
            return;
        }

        Rating rating = ratingOpt.get();

        Optional<MediaEntry> mediaEntryOpt = mediaService.getMediaEntryByRatingId(ratingId);
        if(mediaEntryOpt.isEmpty()){
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                    "Media Entry not found", "text/plain");
            return;
        }

        if(userOpt.get().getId() != mediaEntryOpt.get().getCreatedByUserId()){
            sendResponse(exchange, HttpStatus.FORBIDDEN.getCode(),
                    "Cannot confirm Rating for Media you're not the owner of", "text/plain");
            return;
        }

        Optional<Rating> updatedRatingOpt = ratingService.confirmRating(rating);
        if(updatedRatingOpt.isEmpty()){
            sendResponse(exchange, HttpStatus.BAD_REQUEST.getCode(),
                    "Could not confirm Rating", "text/plain");
            return;
        }

        Rating updatedRating = updatedRatingOpt.get();

        Map<String, Object> response = Map.of(
                "ratingId", updatedRating.getId(),
                "is_confirmed", updatedRating.isConfirmed()
        );

        sendJsonResponse(exchange, HttpStatus.OK.getCode(), response);
    }
}
