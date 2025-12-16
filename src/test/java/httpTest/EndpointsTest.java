package httpTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mrp.http.HttpStatus;
import org.mrp.http.Server;
import org.mrp.persistence.DatabaseConnection;

import javax.xml.crypto.Data;
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
    private static int createdRatingId;
    private static int createdMediaIdForRating;

    @BeforeAll
    static void setup() {
        try {
            TEST_USER = "testUser" + System.currentTimeMillis();

            // start server if not already running
            server = new Server(8080);
            if (!server.isRunning()) {
                DatabaseConnection.initDatabase();
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
    void registerUserWithValidData_ShouldReturn201() throws Exception {
        HttpResponse<String> response = TestSetup.registerUser(TEST_USER, TEST_PASSWORD);

        System.out.println("Register Response: " + response.body());

        assertEquals(201, response.statusCode());
        String body = response.body();
        assertTrue(body.contains("\"userId\""));
        assertTrue(body.contains("1"));
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
    void updateUserProfile_ShouldWork() throws Exception {
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
    void updateMediaEntry_ShouldWork() throws Exception {
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
        HttpResponse<String> allResponse = TestSetup.getMediaEntriesWithFilters(null, token);
        System.out.println("All Media: " + allResponse.body());

        Map<String, String> filters = new HashMap<>();
        filters.put("title", "Updated");
        filters.put("mediaType", "MOVIE");
        filters.put("releaseYear", "2024");
        filters.put("ageRestriction", "16");

        HttpResponse<String> response = TestSetup.getMediaEntriesWithFilters(filters, token);

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

        HttpResponse<String> response = TestSetup.getMediaEntriesWithFilters(filters, token);

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

        HttpResponse<String> response = TestSetup.getMediaEntriesWithFilters(filters, token);

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
    void rateMediaEntry_ShouldWork() throws Exception {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("stars", 5);
        ratingData.put("comment", "Good movie");

        HttpResponse<String> response = TestSetup.rateMediaEntry(token, createdMediaId, ratingData);

        System.out.println("Rate Media Response: " + response.body());

        assertEquals(201, response.statusCode());

        JsonNode jsonNode = mapper.readTree(response.body());
        assertTrue(jsonNode.has("id"));
        int ratingId = jsonNode.get("id").asInt();
        assertEquals(5, jsonNode.get("starValue").asInt());
        assertEquals("Good movie", jsonNode.get("comment").asText());
        assertTrue(jsonNode.has("updatedAt"));

        createdRatingId = ratingId;
    }

    @Test
    @Order(16)
    void rateMediaEntryWithInvalidStars_ShouldReturn404() throws Exception {
        if (createdMediaIdForRating == 0) {
            Map<String, Object> mediaData = new HashMap<>();
            mediaData.put("title", "Another Movie for Rating");
            mediaData.put("description", "Another test movie");
            mediaData.put("mediaType", "MOVIE");
            mediaData.put("releaseYear", 2024);
            mediaData.put("genres", new String[]{"Action"});
            mediaData.put("ageRestriction", 12);

            HttpResponse<String> createResponse = TestSetup.createMediaEntry(token, mediaData);
            JsonNode mediaNode = mapper.readTree(createResponse.body());
            createdMediaIdForRating = mediaNode.get("id").asInt();
        }

        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("stars", 6);
        ratingData.put("comment", "Test comment");

        HttpResponse<String> response = TestSetup.rateMediaEntry(token, createdMediaIdForRating, ratingData);

        System.out.println("Rate with Invalid Stars Response: " + response.body());

        assertEquals(404, response.statusCode());
    }

    @Test
    @Order(17)
    void rateMediaEntryWithMissingComment_ShouldReturn400() throws Exception {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("stars", 4);

        HttpResponse<String> response = TestSetup.rateMediaEntry(token, createdMediaIdForRating, ratingData);
        System.out.println("Rate with Missing Comment Response: " + response.body());

        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(18)
    void rateNonExistentMediaEntry_ShouldReturn404() throws Exception {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("stars", 3);
        ratingData.put("comment", "Test comment");

        HttpResponse<String> response = TestSetup.rateMediaEntry(token, 99999, ratingData);

        System.out.println("Rate Non-Existent Media Response: " + response.body());

        assertEquals(404, response.statusCode());
    }

    @Test
    @Order(19)
    void rateMediaEntryWithoutToken_ShouldReturn401() throws Exception {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("stars", 4);
        ratingData.put("comment", "Test comment");

        HttpResponse<String> response = TestSetup.rateMediaEntry("invalid-token", createdMediaIdForRating, ratingData);
        System.out.println("Rate without Valid Token Response: " + response.body());

        assertEquals(401, response.statusCode());
    }

    @Test
    @Order(20)
    void updateRating_ShouldWork() throws Exception {
        if (createdRatingId == 0) {
            Map<String, Object> ratingData = new HashMap<>();
            ratingData.put("stars", 3);
            ratingData.put("comment", "Initial rating");

            HttpResponse<String> rateResponse = TestSetup.rateMediaEntry(token, createdMediaIdForRating, ratingData);
            JsonNode ratingNode = mapper.readTree(rateResponse.body());
            createdRatingId = ratingNode.get("id").asInt();
        }

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("stars", 4);
        updateData.put("comment", "Updated review - actually better than I thought!");

        HttpResponse<String> response = TestSetup.updateRating(token, createdRatingId, updateData);

        System.out.println("Update Rating Response: " + response.body());

        assertEquals(200, response.statusCode());

        JsonNode jsonNode = mapper.readTree(response.body());
        assertEquals(createdRatingId, jsonNode.get("ratingId").asInt());
        assertEquals(4, jsonNode.get("stars").asInt());
        assertEquals("Updated review - actually better than I thought!", jsonNode.get("comment").asText());
    }

    @Test
    @Order(21)
    void updateRatingWithInvalidData_ShouldReturn400() throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("stars", null);
        updateData.put("comment", "");

        HttpResponse<String> response = TestSetup.updateRating(token, createdRatingId, updateData);

        System.out.println("Update Rating with Invalid Data Response: " + response.body());

        assertEquals(400, response.statusCode());
    }

    @Test
    @Order(22)
    void updateNonExistentRating_ShouldReturn404() throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("stars", 5);
        updateData.put("comment", "Great!");

        HttpResponse<String> response = TestSetup.updateRating(token, 99999, updateData);

        System.out.println("Update Non-Existent Rating Response: " + response.body());
        assertEquals(404, response.statusCode());
    }

    @Test
    @Order(23)
    void updateRatingWithoutPermission_ShouldReturn403() throws Exception {
        String otherUser = "otherUser" + System.currentTimeMillis();
        TestSetup.registerUser(otherUser, TEST_PASSWORD);
        HttpResponse<String> loginResponse = TestSetup.loginUser(otherUser, TEST_PASSWORD);
        JsonNode loginNode = mapper.readTree(loginResponse.body());
        String otherToken = loginNode.get("token").asText();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("stars", 1);
        updateData.put("comment", "Trying to update someone else's rating");

        HttpResponse<String> response = TestSetup.updateRating(otherToken, createdRatingId, updateData);

        System.out.println("Update Rating without Permission Response: " + response.body());
        assertEquals(403, response.statusCode());
    }

    @Test
    @Order(24)
    void updateRatingWithoutToken_ShouldReturn401() throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("stars", 2);
        updateData.put("comment", "Unauthorized update attempt");

        HttpResponse<String> response = TestSetup.updateRating("invalid-token", createdRatingId, updateData);

        System.out.println("Update Rating without Token Response: " + response.body());

        assertEquals(401, response.statusCode());
    }

    @Test
    @Order(25)
    void createDuplicateRating_ShouldWork() throws Exception {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("stars", 5);
        ratingData.put("comment", "Changed my mind, it's perfect!");

        HttpResponse<String> response = TestSetup.rateMediaEntry(token, createdMediaIdForRating, ratingData);

        System.out.println("Duplicate Rating Response: " + response.body());
        assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
    }

    @Test
    @Order(26)
    void verifyAverageRatingCalculation() throws Exception {
        HttpResponse<String> mediaResponse = TestSetup.getMediaEntryById(token, createdMediaIdForRating);

        System.out.println("Media with Average Rating: " + mediaResponse.body());

        assertEquals(200, mediaResponse.statusCode());

        JsonNode jsonNode = mapper.readTree(mediaResponse.body());
        assertTrue(jsonNode.has("averageRating"));

        double averageRating = jsonNode.get("averageRating").asDouble();
        System.out.println("Calculated Average Rating: " + averageRating);

        assertTrue(averageRating >= 0 && averageRating <= 5);
    }

    @Test
    @Order(27)
    void deleteMediaEntry_ShouldWork() throws Exception {
        HttpResponse<String> getResponse = TestSetup.getMediaEntryById(token, createdMediaId);
        assertEquals(200, getResponse.statusCode(), "Media entry should exist before deletion");

        HttpResponse<String> deleteResponse = TestSetup.deleteMediaEntry(token, createdMediaId);

        System.out.println("Delete Media Response: " + deleteResponse.body());

        assertEquals(200, deleteResponse.statusCode());
        String body = deleteResponse.body();
        assertTrue(body.contains("Media entry deleted successfully"));

        HttpResponse<String> verifyResponse = TestSetup.getMediaEntryById(token, createdMediaId);
        assertEquals(404, verifyResponse.statusCode(), "Media entry should not exist after");

        HttpResponse<String> allMediaResponse = TestSetup.getMediaEntriesWithFilters(null, token);
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
    @Order(28)
    void deleteNonExistentMediaEntry_ShouldReturn404() throws Exception {
        HttpResponse<String> deleteResponse = TestSetup.deleteMediaEntry(token, 99999);

        System.out.println("Delete Non-Existent Media Response: " + deleteResponse.body());

        assertEquals(404, deleteResponse.statusCode());
    }

    @Test
    @Order(29)
    void deleteMediaEntryWithInvalidToken_ShouldReturn401() throws Exception {
        HttpResponse<String> deleteResponse = TestSetup.deleteMediaEntry("invalid-token", createdMediaId);

        System.out.println("Delete with Invalid Token Response: " + deleteResponse.body());

        assertEquals(401, deleteResponse.statusCode());
    }
}