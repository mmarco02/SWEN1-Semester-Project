package org.mrp.handlers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.domain.Favorite;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.mrp.service.utils.HttpUtils.sendJsonResponse;
import static org.mrp.service.utils.HttpUtils.sendResponse;

public class UserFavoritesHandler {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static MediaService mediaService;
    private static UserService userService;
    private static RatingService ratingService;
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

        MediaEntryRepository mediaEntryRepository = new MediaEntryRepository(connection);
        UserRepository userRepository = new UserRepository(connection);
        UserProfileRepository userProfileRepository = new UserProfileRepository(connection);
        TokenRepository tokenRepository = new TokenRepository(connection);
        RatingRepository ratingRepository = new RatingRepository(connection);
        LikeRepository likeRepository = new LikeRepository(connection);
        FavoriteRepository favoriteRepository = new FavoriteRepository(connection);

        ratingService = new RatingService(ratingRepository, likeRepository);
        userService = new UserService(userRepository, userProfileRepository, tokenRepository);
        mediaService = new MediaService(mediaEntryRepository, ratingRepository);
        favoriteService = new FavoriteService(favoriteRepository);
    }

    public static void handle(HttpExchange exchange, int userId) throws IOException {
        if (exchange.getRequestMethod().equals("GET")) {
            handleGetUserFavorites(exchange, userId);
        } else {
            sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(),
                    HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }

    private static void handleGetUserFavorites(HttpExchange exchange, int userId) throws IOException {
        Optional<User> userOpt = userService.validateBearerToken(exchange);
        if(userOpt.isEmpty()){
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
            return;
        }

        List<Favorite> favoriteList = favoriteService.findByUserId(userId).stream()
                .sorted(Comparator.comparing(favorite -> favorite.getId()))
                .toList();
        //favoriteList.forEach(System.out::println);

        if(favoriteList.isEmpty()) {
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                    "No Entries for this User", "text/plain");
        }
        sendJsonResponse(exchange, HttpStatus.OK.getCode(), favoriteList);
    }


}