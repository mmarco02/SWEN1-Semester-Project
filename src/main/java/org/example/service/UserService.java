package org.example.service;

import org.example.domain.User;
import org.example.domain.UserProfile;
import org.example.domain.UserToken;
import org.example.persistence.TokenRepository;
import org.example.persistence.UserProfileRepository;
import org.example.persistence.UserRepository;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    private final TokenRepository tokenRepository;

    private static final long TOKEN_EXPIRATION_HOURS = 24;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.tokenRepository = tokenRepository;
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

    public Optional<String> loginUser(String username, String plainPassword) {
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
            UserToken userToken = new UserToken(token, user.getId(), LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));
            tokenRepository.save(userToken);

            return Optional.of(token);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    public boolean logout(String token) {
        return tokenRepository.deleteById(token);
    }

    public Optional<User> validateToken(String token) {
        UserToken tokenInfo = tokenRepository.findById(token);

        if (tokenInfo == null) {
            return Optional.empty();
        }

        if (tokenInfo.createdAt().isBefore(LocalDateTime.now())) {
            tokenRepository.deleteById(token);
            return Optional.empty();
        }

        return userRepository.findById(tokenInfo.userId());
    }

    public boolean hasPermission(String token, int userId) {
        UserToken userToken = tokenRepository.findById(token);
        if (userToken == null) {
            return false;
        }

        if (userToken.createdAt().isBefore(LocalDateTime.now())) {
            tokenRepository.deleteById(token);
            return false;
        }

        return userToken.userId() == userId;
    }

    public Optional<UserProfile> getUserProfileById(int userId) {
        return userProfileRepository.findByUserId(userId);
    }

    public String generateToken(String username) {
        // generate token in format: "username-randomUUID"
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return username + "-" + uuid;
    }

    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
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

        if (email != null) {
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
}