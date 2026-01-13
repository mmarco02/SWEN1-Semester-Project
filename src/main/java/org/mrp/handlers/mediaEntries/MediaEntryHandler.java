package org.mrp.handlers.mediaEntries;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.http.HttpStatus;
import org.mrp.service.utils.PathUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mrp.service.utils.HttpUtils.sendResponse;

public class MediaEntryHandler {

    private static final Pattern BASE_PATTERN = PathUtils.createPatternFromTemplate("/api/media");
    private static final Pattern ID_PATTERN = PathUtils.createPatternFromTemplate("/api/media/{id}");
    private static final Pattern RATE_PATTERN = PathUtils.createPatternFromTemplate("/api/media/{id}/rate");
    private static final Pattern FAVORITE_PATTERN = PathUtils.createPatternFromTemplate("/api/media/{id}/favorite");

    public static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        //System.out.println("Request method: " + exchange.getRequestMethod());

        Matcher idMatcher = ID_PATTERN.matcher(path);
        Matcher baseMatcher = BASE_PATTERN.matcher(path);
        Matcher rateMatcher = RATE_PATTERN.matcher(path);
        Matcher favoriteMatcher = FAVORITE_PATTERN.matcher(path);

        if (baseMatcher.matches()) {
            MediaEntryBaseHandler.handle(exchange);
        } else if (idMatcher.matches()) {
            int entryId = Integer.parseInt(idMatcher.group(1));
            MediaEntryIdHandler.handle(exchange, entryId);
        } else if (rateMatcher.matches()) {
            int entryId = Integer.parseInt(rateMatcher.group(1));
            MediaEntryRatingsHandler.handle(exchange, entryId);
        } else if (favoriteMatcher.matches()) {
            int entryId = Integer.parseInt(favoriteMatcher.group(1));
            MediaEntryFavoriteHandler.handle(exchange, entryId);
        } else {
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                    HttpStatus.NOT_FOUND.getDescription(), "text/plain");
        }
    }
}