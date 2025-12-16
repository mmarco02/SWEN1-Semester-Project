package org.mrp.handlers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.domain.User;
import org.mrp.http.HttpStatus;
import org.mrp.persistence.DatabaseConnection;
import org.mrp.persistence.implemenatations.TokenRepository;
import org.mrp.persistence.implemenatations.UserProfileRepository;
import org.mrp.persistence.implemenatations.UserRepository;
import org.mrp.service.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

import static org.mrp.service.Utils.HttpUtils.sendResponse;

public class UserRegisterHandler {
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

            Map<String, String> requestData = null;
            try {
                requestData = mapper.readValue(requestBody, Map.class);
                String username = requestData.get("username");
                String password = requestData.get("password");

                User existingUser = userService.getUserByUsername(username);
                if (existingUser != null) {
                    sendResponse(exchange, HttpStatus.CONFLICT.getCode(), "Username already exists", "text/plain");
                    return;
                }

                User user = User.builder()
                        .username(username)
                        .password(password)
                        .build();

                userService.registerUser(user);

                String response = "{\"message\": \"User registered\"}";
                sendResponse(exchange, HttpStatus.OK.getCode(), response, "application/json");
            } catch (Exception e) {
                String response = "{\"error\": \"Invalid JSON format\"}";
                sendResponse(exchange, HttpStatus.BAD_REQUEST.getCode(), response, "application/json");
            }
        } else {
            sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(), HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }
}