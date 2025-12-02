package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
public class HttpUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType)
            throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        // Add CORS headers for preflight requests
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object responseObject)
            throws IOException {
        String jsonResponse = OBJECT_MAPPER.writeValueAsString(responseObject);
        sendResponse(exchange, statusCode, jsonResponse, "application/json");
    }
}