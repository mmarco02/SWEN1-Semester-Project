package org.example.handlers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.example.domain.User;
import org.example.persistence.DatabaseConnection;
import org.example.persistence.UserProfileRepository;
import org.example.persistence.UserRepository;
import org.example.service.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

import static org.example.service.HttpUtils.sendResponse;

public class UserRegisterHandler {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final UserService userService;

    static {
        try {
            userService = new UserService(new UserRepository(DatabaseConnection.getConnection()), new UserProfileRepository(DatabaseConnection.getConnection()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            Map<String, String> requestData = null;
            try {
                requestData = mapper.readValue(requestBody, Map.class);
                String username = requestData.get("username");
                String password = requestData.get("password");

                User user = User.builder()
                        .username(username)
                        .password(password)
                        .build();

                userService.registerUser(user);

                String response = "{\"message\": \"User registered\"}";
                sendResponse(exchange, 201, response, "application/json");
            } catch (Exception e) {
                String response = "{\"error\": \"Invalid JSON format\"}";
                sendResponse(exchange, 400, response, "application/json");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
        }
    }
}