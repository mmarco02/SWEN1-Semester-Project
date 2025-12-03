package org.example.handlers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.example.domain.User;
import org.example.http.HttpStatus;
import org.example.persistence.DatabaseConnection;
import org.example.persistence.TokenRepository;
import org.example.persistence.UserProfileRepository;
import org.example.persistence.UserRepository;
import org.example.service.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

import static org.example.service.HttpUtils.sendResponse;

public class UserLoginHandler {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final UserService userService;

    static {
        try {
            userService = new UserService(
                    new UserRepository(DatabaseConnection.getConnection()),
                    new UserProfileRepository(DatabaseConnection.getConnection()),
                    new TokenRepository(DatabaseConnection.getConnection())
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void handle(HttpExchange exchange) throws IOException {

        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> requestData = mapper.readValue(requestBody, Map.class);
            String username = requestData.get("username");
            String password = requestData.get("password");

            User user = userService.getUserByUsername(username);
            if (user == null) {
                sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(), "Invalid credentials", "text/plain");
                return;
            }

            String token = userService.loginUser(username, password).orElse(null);

            String errorResponse = "{\"message\": \"Login failed\"}";

            if (token == null || token.isEmpty()) {
                sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(), errorResponse, "text/plain");
                return;
            }

            String response = "{\"message\": \"Login successful\" , \"token\": \"" + token + "\"}";

            sendResponse(exchange, HttpStatus.OK.getCode(), response, "application/json");
        } else {
            sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(), HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }
}
