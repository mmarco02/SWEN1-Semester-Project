package org.mrp.handlers.ratings;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.http.HttpStatus;
import org.mrp.service.Utils.PathUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mrp.service.Utils.HttpUtils.sendResponse;

public class RatingsHandler {
    private static final Pattern ID_PATTERN = PathUtils.createPatternFromTemplate("/api/ratings/{id}");
    private static final Pattern LIKE_PATTERN = PathUtils.createPatternFromTemplate("/api/ratings/{id}/like");
    private static final Pattern CONFIRM_PATTERN = PathUtils.createPatternFromTemplate("/api/ratings/{id}/confirm");

    public static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        //System.out.println("Request method: " + exchange.getRequestMethod());

        Matcher idMatcher = ID_PATTERN.matcher(path);
        Matcher likeMatcher = LIKE_PATTERN.matcher(path);
        Matcher confirmMatcher = CONFIRM_PATTERN.matcher(path);

        if (idMatcher.matches()) {
            int ratingId = Integer.parseInt(idMatcher.group(1));
            RatingsIdHandler.handle(exchange, ratingId);
        } else if (likeMatcher.matches()) {
            int ratingId = Integer.parseInt(likeMatcher.group(1));
            RatingsLikeHandler.handle(exchange, ratingId);
        } else if (confirmMatcher.matches()) {
            int ratingId = Integer.parseInt(confirmMatcher.group(1));
            RatingsConfirmHandler.handle(exchange, ratingId);
        } else {
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                    HttpStatus.NOT_FOUND.getDescription(), "text/plain");
        }
    }
}
