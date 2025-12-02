package org.example.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class UserProfile extends JsonObject {
    private int id;
    private int userId;
    private String email;
    private String favouriteGenre;

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

    public String getFavouriteGenre() {
        return favouriteGenre;
    }

    public void setFavouriteGenre(String favouriteGenre) {
        this.favouriteGenre = favouriteGenre;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + id +
                ", userId=" + userId +
                ", email='" + email + '\'' +
                ", favouriteGenre='" + favouriteGenre + '\'' +
                '}';
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

        public UserProfileBuilder favouriteGenre(String favouriteGenre) {
            userProfile.favouriteGenre = favouriteGenre;
            return this;
        }

        public UserProfile build() {
            return userProfile;
        }
    }
}
