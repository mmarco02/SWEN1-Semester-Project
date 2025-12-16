package httpTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mrp.http.Server;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndpointsTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static String TEST_USER;
    private static final String TEST_PASSWORD = "testPassword123";
    private static String token;
    private static int userId;
    private static Server server;
    private static int createdMediaId;

    @BeforeAll
    static void setup() {
        try {
            TEST_USER = "testUser" + System.currentTimeMillis();

            // start server if not already running
            server = new Server(8080);
            if (!server.isRunning()) {
                server.start();
                Thread.sleep(2000); // give server time to start
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void cleanup() {
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }

    @Test
    @Order(1)
    void registerUserWithValidData_ShouldReturn200() throws Exception {
        HttpResponse<String> response = TestSetup.registerUser(TEST_USER, TEST_PASSWORD);

        System.out.println("Register Response: " + response.body());

        assertEquals(200, response.statusCode());
        String body = response.body();
        assertTrue(body.contains("\"message\""));
        assertTrue(body.contains("User registered"));
    }

    @Test
    @Order(2)
    void loginWithValidCredentials_ShouldReturnToken() throws Exception {
        HttpResponse<String> response = TestSetup.loginUser(TEST_USER, TEST_PASSWORD);

        System.out.println("Login Response: " + response.body());

        assertEquals(200, response.statusCode());
        String body = response.body();
        assertTrue(body.contains("token"));

        JsonNode jsonNode = mapper.readTree(body);
        token = jsonNode.get("token").asText();
        assertNotNull(token);
        assertFalse(token.isEmpty());

        System.out.println("Token: " + token);
        userId = 1;
    }

    @Test
    @Order(3)
    void createMediaEntry_ShouldReturnCreatedEntry() throws Exception {
        Map<String, Object> mediaData = new HashMap<>();
        mediaData.put("title", "Test Movie");
        mediaData.put("description", "A test movie description");
        mediaData.put("mediaType", "MOVIE");
        mediaData.put("releaseYear", 2023);
        mediaData.put("genres", new String[]{"Action", "Drama"});
        mediaData.put("ageRestriction", 13);

        HttpResponse<String> response = TestSetup.createMediaEntry(token, mediaData);

        System.out.println("Create Media Response: " + response.body());

        assertEquals(201, response.statusCode());

        JsonNode jsonNode = mapper.readTree(response.body());
        assertTrue(jsonNode.has("id"));
        createdMediaId = jsonNode.get("id").asInt();

        userId = jsonNode.get("createdByUserId").asInt();
        System.out.println("Actual User ID from media creation: " + userId);

        assertEquals("Test Movie", jsonNode.get("title").asText());
        assertEquals("MOVIE", jsonNode.get("mediaType").asText());
        assertEquals(2023, jsonNode.get("releaseYear").asInt());
    }

    @Test
    @Order(4)
    void updateUserProfile_ShouldReturnSuccess() throws Exception {
        HttpResponse<String> response = TestSetup.updateProfile(token, userId,
                "test@example.com", "Action");

        System.out.println("Update Profile Response: " + response.body());

        assertEquals(200, response.statusCode());
        String body = response.body();
        assertTrue(body.contains("Profile updated successfully"));
        assertTrue(body.contains("test@example.com"));
        assertTrue(body.contains("Action"));
    }

    @Test
    @Order(5)
    void getUserProfile_ShouldReturnUpdatedProfile() throws Exception {
        HttpResponse<String> response = TestSetup.getProfile(token, userId);

        System.out.println("Get Profile Response: " + response.body());

        assertEquals(200, response.statusCode());
        String body = response.body();

        JsonNode jsonNode = mapper.readTree(body);
        assertEquals("test@example.com", jsonNode.get("email").asText());
        assertEquals("Action", jsonNode.get("favoriteGenre").asText());
    }

    @Test
    @Order(6)
    void updateMediaEntry_ShouldReturnSuccess() throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("title", "Updated Test Movie");
        updateData.put("description", "Updated description");
        updateData.put("mediaType", "MOVIE");
        updateData.put("releaseYear", 2024);
        updateData.put("genres", new String[]{"Action", "Thriller"});
        updateData.put("ageRestriction", 16);

        HttpResponse<String> response = TestSetup.updateMediaEntry(token, createdMediaId, updateData);

        System.out.println("Update Media Response: " + response.body());

        assertEquals(200, response.statusCode());
        String body = response.body();
        assertTrue(body.contains("Media entry updated successfully"));
    }

    @Test
    @Order(7)
    void getMediaEntryById_ShouldReturnUpdatedEntry() throws Exception {
        HttpResponse<String> response = TestSetup.getMediaEntryById(token, createdMediaId);

        System.out.println("Get Media by ID Response: " + response.body());

        assertEquals(200, response.statusCode());

        JsonNode jsonNode = mapper.readTree(response.body());
        assertEquals("Updated Test Movie", jsonNode.get("title").asText());
        assertEquals(2024, jsonNode.get("releaseYear").asInt());
    }

    @Test
    @Order(8)
    void getMediaEntriesWithFilters_ShouldReturnFilteredResults() throws Exception {
        HttpResponse<String> allResponse = TestSetup.getMediaEntriesWithFilters(null);
        System.out.println("All Media: " + allResponse.body());

        Map<String, String> filters = new HashMap<>();
        filters.put("title", "Updated");
        filters.put("mediaType", "MOVIE");
        filters.put("releaseYear", "2024");
        filters.put("ageRestriction", "16");

        HttpResponse<String> response = TestSetup.getMediaEntriesWithFilters(filters);

        System.out.println("Filtered Media Response: " + response.body());

        assertEquals(200, response.statusCode());

        JsonNode jsonNode = mapper.readTree(response.body());
        assertTrue(jsonNode.isArray());

        boolean found = false;
        for (JsonNode entry : jsonNode) {
            if (entry.get("id").asInt() == createdMediaId) {
                found = true;
                assertEquals("Updated Test Movie", entry.get("title").asText());
                break;
            }
        }
        System.out.println("Found entry in filtered results: " + found);
        assertTrue(found, "Created media entry should be in filtered results");
    }

    @Test
    @Order(9)
    void getMediaEntriesWithGenreFilter_ShouldWork() throws Exception {
        Map<String, String> filters = new HashMap<>();
        filters.put("genre", "Action");

        HttpResponse<String> response = TestSetup.getMediaEntriesWithFilters(filters);

        System.out.println("Genre Filter Response: " + response.body());

        assertEquals(200, response.statusCode());

        JsonNode jsonNode = mapper.readTree(response.body());
        assertTrue(jsonNode.isArray());
    }

    @Test
    @Order(10)
    void getMediaEntriesWithRatingFilter_ShouldWork() throws Exception {
        Map<String, String> filters = new HashMap<>();
        filters.put("rating", "0.0");
        filters.put("sortBy", "rating");

        HttpResponse<String> response = TestSetup.getMediaEntriesWithFilters(filters);

        System.out.println("Rating Filter Response: " + response.body());

        assertEquals(200, response.statusCode());

        JsonNode jsonNode = mapper.readTree(response.body());
        assertTrue(jsonNode.isArray());
    }

    @Test
    @Order(11)
    void setMediaEntryAsFavorite_ShouldWork() throws Exception {
        HttpResponse<String> response = TestSetup.favoriteMedia(token, createdMediaId);

        assertEquals(201, response.statusCode());
    }

    @Test
    @Order(12)
    void setNonExistentMediaEntryAsFavorite_ShouldReturn404() throws Exception {
        HttpResponse<String> response = TestSetup.favoriteMedia(token, 999);

        assertEquals(404, response.statusCode());
    }

    @Test
    @Order(13)
    void unfavoriteNonExistentMediaEntry_ShouldReturn404() throws  Exception {
        HttpResponse<String> response = TestSetup.unfavoriteMedia(token, 999);

        assertEquals(404, response.statusCode());
    }

    @Test
    @Order(14)
    void unfavoriteValidMediaEntry_ShouldWork() throws Exception{
        HttpResponse<String> response = TestSetup.unfavoriteMedia(token, createdMediaId);

        assertEquals(200, response.statusCode());
    }

    @Test
    @Order(15)
    void deleteMediaEntry_ShouldReturnSuccess() throws Exception {
        HttpResponse<String> getResponse = TestSetup.getMediaEntryById(token, createdMediaId);
        assertEquals(200, getResponse.statusCode(), "Media entry should exist before deletion");

        HttpResponse<String> deleteResponse = TestSetup.deleteMediaEntry(token, createdMediaId);

        System.out.println("Delete Media Response: " + deleteResponse.body());

        assertEquals(200, deleteResponse.statusCode());
        String body = deleteResponse.body();
        assertTrue(body.contains("Media entry deleted successfully"));

        HttpResponse<String> verifyResponse = TestSetup.getMediaEntryById(token, createdMediaId);
        assertEquals(404, verifyResponse.statusCode(), "Media entry should not exist after");

        HttpResponse<String> allMediaResponse = TestSetup.getMediaEntriesWithFilters(null);
        JsonNode jsonNode = mapper.readTree(allMediaResponse.body());
        boolean stillExists = false;
        for (JsonNode entry : jsonNode) {
            if (entry.get("id").asInt() == createdMediaId) {
                stillExists = true;
                break;
            }
        }
        assertFalse(stillExists, "Media entry should not be in the list");
    }

    @Test
    @Order(16)
    void deleteNonExistentMediaEntry_ShouldReturnError() throws Exception {
        HttpResponse<String> deleteResponse = TestSetup.deleteMediaEntry(token, 99999);

        System.out.println("Delete Non-Existent Media Response: " + deleteResponse.body());

        assertTrue(deleteResponse.statusCode() == 403 || deleteResponse.statusCode() == 404,
                "Should return 403 or 404 for non-existent media entry");
    }

    @Test
    @Order(17)
    void deleteMediaEntryWithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        HttpResponse<String> deleteResponse = TestSetup.deleteMediaEntry("invalid-token", createdMediaId);

        System.out.println("Delete with Invalid Token Response: " + deleteResponse.body());

        assertEquals(401, deleteResponse.statusCode(), "Should return 401 Unauthorized with invalid token");
    }
}