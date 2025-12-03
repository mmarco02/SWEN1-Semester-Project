package org.example.handlers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.example.domain.UserProfile;
import org.example.persistence.DatabaseConnection;
import org.example.persistence.TokenRepository;
import org.example.persistence.UserProfileRepository;
import org.example.persistence.UserRepository;
import org.example.service.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static org.example.service.HttpUtils.sendResponse;

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
        switch (exchange.getRequestMethod()) {
            case "GET":
                Optional<UserProfile> userProfile = userService.getUserProfileById(userId);
                UserProfile userProfileObj = userProfile.orElse(null);
                System.out.println(userProfileObj);
                if(userProfileObj == null) {
                    sendResponse(exchange, 404, "Not Found", "text/plain");
                    return;
                }
                String response = userProfileObj.toJson();
                System.out.println(response);
                sendResponse(exchange, 200, response, "application/json");
                break;

            case "PUT":
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("Update profile for user " + userId + ": " + requestBody);
                Optional<UserProfile> userProfileToUpdate = userService.getUserProfileById(userId);
                UserProfile userProfileToUpdateObj = userProfileToUpdate.orElse(null);
                if(userProfileToUpdateObj == null) {
                    sendResponse(exchange, 404, "Not Found", "text/plain");
                    return;
                }
                Map<String, String> requestValues = mapper.readValue(requestBody, Map.class);
                String email = requestValues.get("email");
                String favoriteGenre = requestValues.get("favoriteGenre");

                userService.updateUserProfile(userProfileToUpdateObj.getUserId(), email, favoriteGenre);

                String updateResponse = String.format("""
                            {
                                "message": "Profile updated successfully",
                                "userId": "%s"
                            }
                            """, userId);
                sendResponse(exchange, 200, updateResponse, "application/json");
                break;

            default:
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
        }
    }
}
