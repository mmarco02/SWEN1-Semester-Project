package org.mrp.service;

import com.sun.net.httpserver.HttpExchange;
import org.mrp.domain.MediaEntry;
import org.mrp.domain.User;
import org.mrp.domain.UserProfile;
import org.mrp.domain.UserToken;
import org.mrp.persistence.implemenatations.TokenRepository;
import org.mrp.persistence.implemenatations.UserProfileRepository;
import org.mrp.persistence.implemenatations.UserRepository;
import org.mrp.service.Utils.AuthUtils;
import org.mrp.service.Utils.HashUtils;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
            UserToken userToken = new UserToken(token, user.getId(), Timestamp.from(Instant.now()));
            if(tokenRepository.findByUserId(user.getId()).isPresent()){
                tokenRepository.deleteByUserId(user.getId());
            }
            tokenRepository.save(userToken);

            return Optional.of(token);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    // removes token for user
    public void logout(User user) {
        tokenRepository.deleteByUserId(user.getId());
    }

    public Optional<User> validateBearerToken(HttpExchange exchange) {
        Optional<String> tokenOpt = AuthUtils.extractBearerToken(exchange);
        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }
        return validateToken(tokenOpt.get());
    }

    public Optional<User> validateToken(String token) {
        Optional<UserToken> tokenInfoOpt = tokenRepository.findById(token);

        if (tokenInfoOpt.isEmpty()) {
            return Optional.empty();
        }

        UserToken tokenInfo = tokenInfoOpt.get();

        if (isTokenExpired(tokenInfo)) {
            tokenRepository.deleteById(token);
            return Optional.empty();
        }

        return userRepository.findById(tokenInfo.userId());
    }

    public boolean hasPermissionForResource(String token, int resourceOwnerId) {
        return validateToken(token)
                .map(user -> user.getId() == resourceOwnerId)
                .orElse(false);
    }

    // overload method
    public boolean hasPermissionForResource(HttpExchange exchange, int resourceOwnerId) {
        return validateBearerToken(exchange)
                .map(user -> user.getId() == resourceOwnerId)
                .orElse(false);
    }

    private boolean isTokenExpired(UserToken token) {
        Instant expirationTime = token.createdAt().toInstant()
                .plus(TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS);
        return Instant.now().isAfter(expirationTime);
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
        for (UserToken userToken : tokenRepository.findAll()) {
            if(userToken.createdAt().before(Timestamp.from(Instant.now().plus(TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS)))) {
                tokenRepository.deleteById(userToken.token());
            }
        }
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

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<UserToken> getTokenByUserId(int userId) {
        return tokenRepository.findByUserId(userId);
    }

    public void setFavorite(MediaEntry mediaEntry) {

    }
}