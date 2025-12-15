package org.mrp.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class UserProfile extends JsonObject {
    private int id;
    private int userId;
    private String email;
    private String favoriteGenre;

    public UserProfile() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    public void setFavoriteGenre(String favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + id +
                ", userId=" + userId +
                ", email='" + email + '\'' +
                ", favoriteGenre='" + favoriteGenre + '\'' +
                '}';
    }

    @Override
    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("id", id);
        jsonMap.put("userId", userId);
        jsonMap.put("email", email != null ? email : "");
        jsonMap.put("favoriteGenre", favoriteGenre != null ? favoriteGenre : "");

        return mapper.writeValueAsString(jsonMap);
    }

    public static UserProfileBuilder builder() {
        return new UserProfileBuilder();
    }

    public static class UserProfileBuilder {
        private UserProfile userProfile = new UserProfile();

        public UserProfileBuilder id(int id) {
            userProfile.id = id;
            return this;
        }

        public UserProfileBuilder userId(int userId) {
            userProfile.userId = userId;
            return this;
        }

        public UserProfileBuilder email(String email) {
            userProfile.email = email;
            return this;
        }

        public UserProfileBuilder favoriteGenre(String favoriteGenre) {
            userProfile.favoriteGenre = favoriteGenre;
            return this;
        }

        public UserProfile build() {
            return userProfile;
        }
    }
}
