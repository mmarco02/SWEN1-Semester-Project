package org.mrp.handlers.mediaEntries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.domain.Favorite;
import org.mrp.domain.MediaEntry;
import org.mrp.domain.User;
import org.mrp.http.HttpStatus;
import org.mrp.persistence.DatabaseConnection;
import org.mrp.persistence.implemenatations.*;
import org.mrp.service.FavoriteService;
import org.mrp.service.MediaService;
import org.mrp.service.RatingService;
import org.mrp.service.UserService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mrp.service.Utils.HttpUtils.sendJsonResponse;
import static org.mrp.service.Utils.HttpUtils.sendResponse;

public class MediaEntryFavoriteHandler {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static UserService userService;
    private static MediaService mediaService;
    private static FavoriteService favoriteService;

    static {
        try {
            initializeServices();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize services", e);
        }
    }

    private static void initializeServices() throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        FavoriteRepository favoriteRepository = new FavoriteRepository(connection);
        MediaEntryRepository mediaEntryRepository = new MediaEntryRepository(connection);
        UserRepository userRepository = new UserRepository(connection);
        TokenRepository tokenRepository = new TokenRepository(connection);
        UserProfileRepository userProfileRepository = new UserProfileRepository(connection);
        RatingRepository ratingRepository = new RatingRepository(connection);

        userService = new UserService(userRepository,userProfileRepository, tokenRepository);
        mediaService = new MediaService(mediaEntryRepository, ratingRepository);
        favoriteService = new FavoriteService(favoriteRepository);


    }

    public static void handle(HttpExchange exchange, int entryId) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGetFavorites(exchange, entryId);
                break;
            case "POST":
                handleAddFavorite(exchange, entryId);
                break;
            case "DELETE":
                handleRemoveFavorite(exchange, entryId);
                break;
            default:
                sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(),
                        HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }

    private static void handleGetFavorites(HttpExchange exchange, int entryId) throws IOException {
        try {
            Optional<User> userOpt = userService.validateBearerToken(exchange);
            if(userOpt.isEmpty()){
                sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                        HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
                return;
            }

            List<Favorite> favorites = favoriteService.findByEntryId(entryId);
            sendJsonResponse(exchange, HttpStatus.OK.getCode(), favorites);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "Error fetching favorites", "text/plain");
        }
    }

    private static void handleAddFavorite(HttpExchange exchange, int entryId) throws IOException {
        Optional<User> userOpt = userService.validateBearerToken(exchange);
        if(userOpt.isEmpty()){
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
            return;
        }

        User user = userOpt.get();

        Optional<MediaEntry> mediaOpt = mediaService.getMediaEntryById(entryId);
        if (mediaOpt.isEmpty()) {
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                    "Media entry not found", "text/plain");
            return;
        }

        Optional<Favorite> existingFavorite = favoriteService.findByUserAndEntry(user.getId(), entryId);
        if (existingFavorite.isPresent()) {
            sendResponse(exchange, HttpStatus.CONFLICT.getCode(),
                    "Media entry is already in favorites", "text/plain");
            return;
        }

        try {
            if(user.getId() != mediaOpt.get().getCreatedByUserId()){
                sendResponse(exchange, HttpStatus.FORBIDDEN.getCode(),
                        HttpStatus.FORBIDDEN.getDescription(), "text/plain");
                return;
            }

            Favorite favorite = Favorite.builder()
                    .entryId(entryId)
                    .userId(user.getId())
                    .build();

            favoriteService.save(favorite);

            Optional<Favorite> savedFavorite = favoriteService.findById(favorite.getId());
            if(savedFavorite.isEmpty()){
                sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                        "Failed to save Favorite", "text/plain");
                return;
            }

            Map<String, String> response = Map.of(
                    "message", "Added to favorites",
                    "favoriteId", String.valueOf(savedFavorite.get().getId())
            );
            sendJsonResponse(exchange, HttpStatus.CREATED.getCode(), response);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "Error adding to favorites", "text/plain");
        }
    }

    private static void handleRemoveFavorite(HttpExchange exchange, int entryId) throws IOException {
        Optional<User> userOpt = userService.validateBearerToken(exchange);
        if (userOpt.isEmpty()) {
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
            return;
        }

        User user = userOpt.get();

        try {
            Optional<MediaEntry> entry = mediaService.getMediaEntryById(entryId);
            if(entry.isEmpty()){
                sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                        "MediaEntry not Found", "text/plain");
                return;
            }

            if(user.getId() != entry.get().getCreatedByUserId()){
                sendResponse(exchange, HttpStatus.FORBIDDEN.getCode(),
                        HttpStatus.FORBIDDEN.getDescription(), "text/plain");
                return;
            }

            boolean removed = favoriteService.deleteByUserAndEntry(user.getId(), entryId);

            if (removed) {
                Map<String, String> response = Map.of("message", "Removed from favorites");
                sendJsonResponse(exchange, HttpStatus.OK.getCode(), response);
            } else {
                sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                        "Favorite not found", "text/plain");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "Error removing from favorites", "text/plain");
        }
    }
}