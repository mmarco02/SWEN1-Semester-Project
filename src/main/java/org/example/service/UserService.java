package org.example.service;

import org.example.domain.User;
import org.example.persistence.UserRepository;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private final UserRepository userRepository;

    // In-memory token storage (in production, use Redis or database)
    private final Map<String, TokenInfo> activeTokens = new ConcurrentHashMap<>();

    // Token expiration time (24 hours)
    private static final long TOKEN_EXPIRATION_HOURS = 24;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(User user) {
        try {
            HashUtils.HashResult hashResult = HashUtils.hashWithSalt(user.getPassword());
            user.setPassword(hashResult.hash());
            user.setSalt(hashResult.salt());
            userRepository.save(user);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    public Optional<String> login(String username, String plainPassword) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();
        try {
            boolean isValid = HashUtils.verify(plainPassword, user.getPassword(), user.getSalt());
            if (!isValid) {
                return Optional.empty();
            }

            // token generation
            String token = generateToken(username);
            TokenInfo tokenInfo = new TokenInfo(user.getId(), username, Instant.now().plus(TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS));
            activeTokens.put(token, tokenInfo);

            return Optional.of(token);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    public boolean logout(String token) {
        return activeTokens.remove(token) != null;
    }

    public Optional<User> validateToken(String token) {
        TokenInfo tokenInfo = activeTokens.get(token);

        if (tokenInfo == null) {
            return Optional.empty();
        }

        // Check if token is expired
        if (tokenInfo.expiresAt().isBefore(Instant.now())) {
            activeTokens.remove(token);
            return Optional.empty();
        }

        // Get user from database
        return userRepository.findById(tokenInfo.userId());
    }

    public boolean hasPermission(String token, String username) {
        TokenInfo tokenInfo = activeTokens.get(token);
        if (tokenInfo == null) {
            return false;
        }

        // Check if token is expired
        if (tokenInfo.expiresAt().isBefore(Instant.now())) {
            activeTokens.remove(token);
            return false;
        }

        // Check if token belongs to the requested username
        // This ensures users can only access their own resources
        return tokenInfo.username().equals(username);
    }

    public Optional<User> getUserProfile(String username) {
        return userRepository.findByUsername(username);
    }

    private String generateToken(String username) {
        // Generate token in format: "username-randomUUID"
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return username + "-" + uuid + "Token";
    }

    // Clean up expired tokens (call this periodically)
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        activeTokens.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    public User getUserByUsername(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser.orElse(null);
    }

    public User getById(int userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        return  optionalUser.orElse(null);
    }

    // Token information record
    private record TokenInfo(int userId, String username, Instant expiresAt) {}
}