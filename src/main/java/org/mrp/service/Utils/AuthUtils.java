package org.mrp.service.Utils;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.util.Optional;

public class AuthUtils {

    public static Optional<String> extractBearerToken(HttpExchange exchange) {
        Headers headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            return Optional.empty();
        }

        // get token authentication bearer
        if (!authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        // Extract token (remove "Bearer ")
        String token = authHeader.substring(7).trim();
        return Optional.of(token);
    }
}