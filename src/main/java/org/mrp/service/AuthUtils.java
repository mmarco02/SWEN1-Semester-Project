package org.mrp.service;

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

    public static boolean isValidTokenFormat(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // Basic format validation: should contain a hyphen (username-uuid)
        return token.contains("-");
    }
}