package org.example.handlers.users;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import static org.example.service.HttpUtils.sendResponse;

public class UserFavoritesBaseHandler {

    public static void handle(HttpExchange exchange, int userId) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String response = String.format("");
            sendResponse(exchange, 200, response, "application/json");
        } else {
            sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
        }
    }
}