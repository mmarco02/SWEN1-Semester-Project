package org.mrp.domain;

import java.time.LocalDateTime;

public record UserToken(String token, int userId, LocalDateTime createdAt) {
    public String token() {
        return token;
    }

    public int userId() {
        return userId;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "UserToken{" +
                "token='" + token + '\'' +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }

    public static UserTokenBuilder builder() {
        return new UserTokenBuilder();
    }

    public static class UserTokenBuilder {
        private String token;
        private Integer userId;
        private LocalDateTime createdAt;

        public UserTokenBuilder token(String token) {
            this.token = token;
            return this;
        }

        public UserTokenBuilder userId(Integer userId) {
            this.userId = userId;
            return this;
        }

        public UserTokenBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserToken build() {
            if (token == null || userId == null || createdAt == null) {
                throw new IllegalStateException("All fields must be set");
            }
            return new UserToken(token, userId, createdAt);
        }
    }
}