package org.example.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public record UserToken(String token, int userId, Timestamp createdAt) {
    @Override
    public String token() {
        return token;
    }

    @Override
    public int userId() {
        return userId;
    }

    @Override
    public Timestamp createdAt() {
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
