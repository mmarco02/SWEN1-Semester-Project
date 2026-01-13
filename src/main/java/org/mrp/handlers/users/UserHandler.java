package org.mrp.handlers.users;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.http.HttpStatus;
import org.mrp.service.utils.PathUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mrp.service.utils.HttpUtils.sendResponse;

public class UserHandler {
    private static final Pattern profilePattern =
            PathUtils.createPatternFromTemplate("/api/users/{id}/profile");

    private static final Pattern ratingsPattern =
            PathUtils.createPatternFromTemplate("/api/users/{id}/ratings");

    private static final Pattern favoritesPattern =
            PathUtils.createPatternFromTemplate("/api/users/{id}/favorites");

    public static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        Matcher profileMatcher = profilePattern.matcher(path);
        Matcher ratingsMatcher = ratingsPattern.matcher(path);
        Matcher favoritesMatcher = favoritesPattern.matcher(path);

        if (profileMatcher.matches()) {
            int userId = Integer.parseInt(profileMatcher.group(1));
            UserProfileHandler.handle(exchange, userId);
        } else if (ratingsMatcher.matches()) {
            int userId = Integer.parseInt(ratingsMatcher.group(1));
            UserRatingsHandler.handle(exchange, userId);
        } else if (favoritesMatcher.matches()) {
            int userId = Integer.parseInt(favoritesMatcher.group(1));
            UserFavoritesHandler.handle(exchange, userId);
        } else {
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND.getDescription(), "text/plain");
        }
    }
}
