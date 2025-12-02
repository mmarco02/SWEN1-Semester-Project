package org.example.http;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.example.domain.User;
import org.example.domain.UserProfile;
import org.example.persistence.DatabaseConnection;
import org.example.persistence.UserProfileRepository;
import org.example.persistence.UserRepository;
import org.example.service.HashUtils;
import org.example.service.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.Map;

import static org.example.service.HttpUtils.sendResponse;

public class Server {
    private HttpServer server;
    private final int port;
    private final UserService userService = new UserService(
            new UserRepository(DatabaseConnection.getConnection()),
            new UserProfileRepository(DatabaseConnection.getConnection())
    );

    public Server(int port) throws SQLException {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Auth routes
        server.createContext("/api/users/register", new UserRegisterHandler());
        server.createContext("/api/users/login", new UserLoginHandler());

        // User routes with path variables
        server.createContext("/api/users/", new UserRouter());

        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port " + port);
    }

    class UserRouter implements HttpHandler {
        private final Pattern profilePattern = Pattern.compile("^/api/users/([^/]+)/profile$");
        private final Pattern ratingsPattern = Pattern.compile("^/api/users/([^/]+)/ratings$");
        private final Pattern favoritesPattern = Pattern.compile("^/api/users/([^/]+)/favorites$");

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            Matcher profileMatcher = profilePattern.matcher(path);
            Matcher ratingsMatcher = ratingsPattern.matcher(path);
            Matcher favoritesMatcher = favoritesPattern.matcher(path);

            if (profileMatcher.matches()) {
                int userId = Integer.parseInt(profileMatcher.group(1));
                new UserProfileHandler().handle(exchange, userId);
            } else if (ratingsMatcher.matches()) {
                int userId = Integer.parseInt(ratingsMatcher.group(1));
                new UserRatingsHandler().handle(exchange, userId);
            } else if (favoritesMatcher.matches()) {
                int userId = Integer.parseInt(favoritesMatcher.group(1));
                new UserFavoritesHandler().handle(exchange, userId);
            } else {
                sendResponse(exchange, 404, "Not Found", "text/plain");
            }
        }
    }

    class UserRegisterHandler implements HttpHandler {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                //System.out.println("Registration data: " + requestBody);

                Map<String, String> requestData = null;
                try {
                    requestData = mapper.readValue(requestBody, Map.class);
                    String username = requestData.get("username");
                    String password = requestData.get("password");

                    //System.out.println("Username: " + username);
                    //System.out.println("Password: " + password);

                    User user = User.builder()
                            .username(username)
                            .password(password)
                            .build();

                    userService.registerUser(user);

                    String response = "{\"message\": \"User registered\"}";
                    sendResponse(exchange, 201, response, "application/json");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println(e.getStackTrace());
                    String response = "{\"error\": \"Invalid JSON format\"}";
                    sendResponse(exchange, 400, response, "application/json");
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }


    class UserLoginHandler implements HttpHandler {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String, String> requestData = null;
                    requestData = mapper.readValue(requestBody, Map.class);
                    String username = requestData.get("username");
                    String password = requestData.get("password");

                    User user = userService.getUserByUsername(username);
                    if (user == null) {
                        sendResponse(exchange, 401, "Invalid credentials", "text/plain");
                        return;
                    }

                    boolean isLoginValid = HashUtils.verify(
                            password,
                            user.getPassword(),
                            user.getSalt());

                    String errorResponse = "{\"message\": \"Login failes\"}";

                    if (!isLoginValid) {
                        sendResponse(exchange, 401, errorResponse, "text/plain");
                        return;
                    }

                    String response = "{\"message\": \"Login successful\"}";

                    sendResponse(exchange, 200, response, "application/json");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }


    class UserProfileHandler {
        private final ObjectMapper mapper = new ObjectMapper();

        public void handle(HttpExchange exchange, int userId) throws IOException {
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


    static class UserRatingsHandler {
        public void handle(HttpExchange exchange, int userId) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = String.format("");
                sendResponse(exchange, 200, response, "application/json");
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }


    static class UserFavoritesHandler {
        public void handle(HttpExchange exchange, int userId) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = String.format("");
                sendResponse(exchange, 200, response, "application/json");
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped");
        }
    }

    public static void run() throws SQLException, IOException {
        Server server = new Server(8080);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        System.out.println("Press Ctrl+C to stop the server...");
    }
}