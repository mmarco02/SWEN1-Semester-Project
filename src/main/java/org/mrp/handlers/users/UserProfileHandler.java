package org.mrp.handlers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.domain.User;
import org.mrp.domain.UserProfile;
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
import java.util.Optional;

import static org.mrp.service.utils.HttpUtils.sendResponse;

public class UserProfileHandler {
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

    public static void handle(HttpExchange exchange, int userId) throws IOException {
        Optional<User> authenticatedUser = userService.validateBearerToken(exchange);
        if (authenticatedUser.isEmpty()) {
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    "Missing or invalid Authorization token", "text/plain");
            return;
        }

        // check if authenticated user has permission to access this User
        User user = authenticatedUser.get();
        if (user.getId() != userId) {
            sendResponse(exchange, HttpStatus.FORBIDDEN.getCode(),
                    "Access denied", "text/plain");
            return;
        }

        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGetProfile(exchange, userId);
                break;

            case "PUT":
                handleUpdateProfile(exchange, userId);
                break;

            default:
                sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(),
                        HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }

    private static void handleGetProfile(HttpExchange exchange, int userId) throws IOException {
        Optional<User> userOpt = userService.validateBearerToken(exchange);
        if(userOpt.isEmpty()){
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
            return;
        }

        Optional<UserProfile> userProfile = userService.getUserProfileById(userId);

        if (userProfile.isEmpty()) {
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                    "User profile not found", "text/plain");
            return;
        }

        String response = userProfile.get().toJson();
        sendResponse(exchange, HttpStatus.OK.getCode(), response, "application/json");
    }

    private static void handleUpdateProfile(HttpExchange exchange, int userId) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Map<String, String> requestValues;
        try {
            requestValues = mapper.readValue(requestBody, Map.class);
        } catch (Exception e) {
            sendResponse(exchange, HttpStatus.BAD_REQUEST.getCode(),
                    "Invalid JSON format", "text/plain");
            return;
        }

        Optional<User> userOpt = userService.validateBearerToken(exchange);
        if(userOpt.isEmpty()){
            sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(),
                    HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
            return;
        }

        String email = requestValues.get("email");
        String favoriteGenre = requestValues.get("favoriteGenre");

        Optional<UserProfile> updatedProfile = userService.updateUserProfile(userId, email, favoriteGenre);

        if (updatedProfile.isEmpty()) {
            sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(),
                    HttpStatus.NOT_FOUND.getDescription(), "text/plain");
            return;
        }

        String response = String.format("""
            {
                "message": "Profile updated successfully",
                "userId": %d,
                "email": "%s",
                "favoriteGenre": "%s"
            }
            """, userId,
                updatedProfile.get().getEmail() != null ? updatedProfile.get().getEmail() : "",
                updatedProfile.get().getFavoriteGenre() != null ? updatedProfile.get().getFavoriteGenre() : "");

        sendResponse(exchange, HttpStatus.OK.getCode(), response, "application/json");
    }
}