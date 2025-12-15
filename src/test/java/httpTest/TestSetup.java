package httpTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class TestSetup {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static HttpResponse<String> registerUser(String username, String password) throws IOException, InterruptedException {
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> loginUser(String username, String password) throws IOException, InterruptedException {
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> getProfile(String token, int userId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/" + userId + "/profile"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> updateProfile(String token, int userId, String email, String favoriteGenre) throws IOException, InterruptedException {
        String json = String.format("{\"email\":\"%s\",\"favoriteGenre\":\"%s\"}", email, favoriteGenre);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/" + userId + "/profile"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> createMediaEntry(String token, Map<String, Object> mediaData) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(mediaData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/media"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> updateMediaEntry(String token, int mediaId, Map<String, Object> mediaData) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(mediaData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/media/" + mediaId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> getMediaEntriesWithFilters(Map<String, String> filters) throws IOException, InterruptedException {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/media");

        if (filters != null && !filters.isEmpty()) {
            urlBuilder.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                if (!first) {
                    urlBuilder.append("&");
                }
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> getMediaEntryById(String token, int mediaId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/media/" + mediaId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> deleteMediaEntry(String token, int mediaId) throws IOException, InterruptedException {
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + "/media/" + mediaId))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        return httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
    }
}