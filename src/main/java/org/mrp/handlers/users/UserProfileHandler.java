package org.mrp.handlers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.mrp.domain.UserProfile;
import org.mrp.http.HttpStatus;
import org.mrp.persistence.DatabaseConnection;
import org.mrp.persistence.TokenRepository;
import org.mrp.persistence.UserProfileRepository;
import org.mrp.persistence.UserRepository;
import org.mrp.service.UserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static org.mrp.service.HttpUtils.sendResponse;

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
                    sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(), "Not Found", "text/plain");
                    return;
                }

                if (!userService.isValidToken(userId)) {
                    sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(), HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
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
                    sendResponse(exchange, HttpStatus.NOT_FOUND.getCode(), "Not Found", "text/plain");
                    return;
                }
                Map<String, String> requestValues = mapper.readValue(requestBody, Map.class);
                String email = requestValues.get("email");
                String favoriteGenre = requestValues.get("favoriteGenre");

                if (!userService.isValidToken(userId)) {
                    sendResponse(exchange, HttpStatus.UNAUTHORIZED.getCode(), HttpStatus.UNAUTHORIZED.getDescription(), "text/plain");
                    return;
                }

                userService.updateUserProfile(userProfileToUpdateObj.getUserId(), email, favoriteGenre);

                String updateResponse = String.format("""
                            {
                                "message": "Profile updated successfully",
                                "userId": "%s"
                            }
                            """, userId);
                sendResponse(exchange, HttpStatus.OK.getCode(), updateResponse, "application/json");
                break;

            default:
                sendResponse(exchange, HttpStatus.METHOD_NOT_ALLOWED.getCode(), HttpStatus.METHOD_NOT_ALLOWED.getDescription(), "text/plain");
        }
    }
}
