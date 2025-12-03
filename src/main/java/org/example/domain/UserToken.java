package org.example.domain;

import java.time.LocalDateTime;

public record UserToken(int userId, String username, LocalDateTime expiresAt) {

}
