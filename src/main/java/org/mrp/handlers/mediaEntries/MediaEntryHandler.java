package org.mrp.handlers.mediaEntries;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.handlers.users.UserProfileHandler;
import org.mrp.http.HttpStatus;
import org.mrp.service.PathUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mrp.service.HttpUtils.sendResponse;

public class MediaEntryHandler {

    private static final Pattern basePattern = PathUtils.createPatternFromTemplate("/api/media");
    private static final Pattern idPattern = PathUtils.createPatternFromTemplate("/api/media/{id}");

    public static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        Matcher idPatterMatcher = idPattern.matcher(path);
        Matcher basePatterMatcher = basePattern.matcher(path);

        if (idPatterMatcher.matches()) {
            MediaEntryBaseHandler.handle(exchange);
        } else if (basePatterMatcher.matches()) {
            int entryId = Integer.parseInt(basePatterMatcher.group(1));
            UserProfileHandler.handle(exchange, entryId);
        } else {
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND.getDescription(), "text/plain");
        }
    }
}
