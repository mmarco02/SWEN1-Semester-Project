package org.mrp.handlers.mediaEntries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.http.HttpStatus;
import org.mrp.persistence.DatabaseConnection;
import org.mrp.persistence.implemenatations.*;
import org.mrp.service.MediaService;
import org.mrp.service.RatingService;
import org.mrp.service.UserService;

import java.io.IOException;
import java.sql.SQLException;

import static org.mrp.service.HttpUtils.sendResponse;

public class MediaEntryFavoriteHandler {
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
        var connection = DatabaseConnection.getConnection();

        UserRepository userRepository = new UserRepository(connection);
        UserProfileRepository userProfileRepository = new UserProfileRepository(connection);
        TokenRepository tokenRepository = new TokenRepository(connection);
        RatingRepository ratingRepository = new RatingRepository(connection);
        RatingService ratingService = new RatingService(ratingRepository);
        MediaEntryRepository mediaEntryRepository = new MediaEntryRepository(connection);

        userService = new UserService(userRepository, userProfileRepository, tokenRepository);
        mediaService = new MediaService(mediaEntryRepository, ratingRepository, userService, ratingService);
    }

    public static void handle(HttpExchange exchange, int entryId) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "POST":
                handleAddFavorite(exchange, entryId);
                break;
            case "DEL":
                handleDeleteFavorite(exchange, entryId);
                break;
            default:
                sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(),
                        HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }

    private static void handleAddFavorite(HttpExchange exchange, int entryId) {


    }

    private static void handleDeleteFavorite(HttpExchange exchange, int entryId) {
    }


}
