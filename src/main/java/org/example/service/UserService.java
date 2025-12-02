package org.example.service;

import org.example.domain.User;
import org.example.domain.UserProfile;
import org.example.persistence.UserProfileRepository;
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

    private final UserProfileRepository userProfileRepository;
    private final Map<String, TokenInfo> activeTokens = new ConcurrentHashMap<>();
    private static final long TOKEN_EXPIRATION_HOURS = 24;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public void registerUser(User user) {
        try {
            HashUtils.HashResult hashResult = HashUtils.hashWithSalt(user.getPassword());
            user.setPassword(hashResult.hash());
            user.setSalt(hashResult.salt());
            userRepository.save(user);

            UserProfile userProfile = UserProfile.builder()
                    .userId(user.getId())
                    .email("")
                    .favoriteGenre("")
                    .build();

            userProfileRepository.save(userProfile);
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

        if (tokenInfo.expiresAt().isBefore(Instant.now())) {
            activeTokens.remove(token);
            return Optional.empty();
        }

        return userRepository.findById(tokenInfo.userId());
    }

    public boolean hasPermission(String token, String username) {
        TokenInfo tokenInfo = activeTokens.get(token);
        if (tokenInfo == null) {
            return false;
        }

        if (tokenInfo.expiresAt().isBefore(Instant.now())) {
            activeTokens.remove(token);
            return false;
        }

        return tokenInfo.username().equals(username);
    }

    public Optional<UserProfile> getUserProfileById(int userId) {
        return userProfileRepository.findByUserId(userId);
    }

    private String generateToken(String username) {
        // generate token in format: "username-randomUUID"
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return username + "-" + uuid + "Token";
    }

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

    public Optional<UserProfile> updateUserProfile(int userId, String email, String favoriteGenre) {
        Optional<UserProfile> existingProfile = userProfileRepository.findByUserId(userId);

        if (existingProfile.isEmpty()) {
            return Optional.empty();
        }

        UserProfile userProfile = existingProfile.get();
        boolean isUpdated = false;

        if (email != null && !email.trim().isEmpty()) {
            userProfile.setEmail(email.trim());
            isUpdated = true;
        }

        if (favoriteGenre != null) {
            userProfile.setFavoriteGenre(favoriteGenre);
            isUpdated = true;
        }

        if (isUpdated) {
            userProfileRepository.update(userProfile);
            return Optional.of(userProfile);
        }

        return Optional.of(userProfile);
    }

    private record TokenInfo(int userId, String username, Instant expiresAt) {}
}