package org.example.handlers.users;

import com.sun.net.httpserver.HttpExchange;
import org.example.http.HttpStatus;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.service.HttpUtils.sendResponse;

public class UserHandler {
    private static final Pattern profilePattern = Pattern.compile("^/api/users/([^/]+)/profile$");
    private static final Pattern ratingsPattern = Pattern.compile("^/api/users/([^/]+)/ratings$");
    private static final Pattern favoritesPattern = Pattern.compile("^/api/users/([^/]+)/favorites$");

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
            UserFavoritesBaseHandler.handle(exchange, userId);
        } else {
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND.getDescription(), "text/plain");
        }
    }
}
