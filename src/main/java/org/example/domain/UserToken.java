package org.example.domain;

import java.time.LocalDateTime;

public record UserToken(String token, int userId, LocalDateTime createdAt) {
    @Override
    public String token() {
        return token;
    }

    @Override
    public int userId() {
        return userId;
    }

    @Override
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
}
