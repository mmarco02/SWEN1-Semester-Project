package org.mrp.handlers.users;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.http.HttpStatus;

import java.io.IOException;

import static org.mrp.service.HttpUtils.sendResponse;

public class UserFavoritesHandler {

    public static void handle(HttpExchange exchange, int userId) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String response = String.format("");
            sendResponse(exchange, HttpStatus.OK.getCode(), response, "application/json");
        } else {
            sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(), HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }
}