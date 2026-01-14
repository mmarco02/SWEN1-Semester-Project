package org.mrp.domainTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mrp.domain.User;
import org.mrp.domain.UserProfile;
import org.mrp.domain.UserToken;
import org.mrp.persistence.implemenatations.TokenRepository;
import org.mrp.persistence.implemenatations.UserProfileRepository;
import org.mrp.persistence.implemenatations.UserRepository;
import org.mrp.service.UserService;
import org.mrp.service.utils.HashUtils;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UserService userService;

    private User testUser;
    private UserProfile testProfile;
    private UserToken testToken;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userProfileRepository, tokenRepository);

        testUser = User.builder()
                .id(1)
                .username("testuser")
                .password("hashedPassword123")
                .salt("salt123")
                .build();

        testProfile = UserProfile.builder()
                .id(1)
                .userId(1)
                .email("test@example.com")
                .favoriteGenre("Action")
                .build();

        testToken = UserToken.builder()
                .token("testuser-abc123")
                .userId(1)
                .createdAt(Timestamp.from(Instant.now()))
                .build();
    }


    // Domain Tests

    @Test
    void testUserProfileToJsonHandlesNullValues() throws Exception {
        UserProfile profile = UserProfile.builder()
                .id(1)
                .userId(1)
                .email(null)
                .favoriteGenre(null)
                .build();

        String json = profile.toJson();
        assertNotNull(json);
        assertTrue(json.contains("\"email\":\"\""));
        assertTrue(json.contains("\"favoriteGenre\":\"\""));
    }

    // Service Tests

    @Test
    void testRegisterUserHashesPasswordAndCreatesProfile() {
        User newUser = User.builder()
                .id(0)
                .username("newuser")
                .password("plainpassword")
                .build();

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1);
            return null;
        }).when(userRepository).save(any(User.class));

        userService.registerUser(newUser);

        verify(userRepository, times(1)).save(any(User.class));
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getPassword());
        assertNotNull(savedUser.getSalt());
        assertNotEquals("plainpassword", savedUser.getPassword());
        assertEquals(1, savedUser.getId());
    }

    @Test
    void testLoginUserSuccess() throws NoSuchAlgorithmException {
        String plainPassword = "password123";
        HashUtils.HashResult hr = HashUtils.hashWithSalt(plainPassword);
        String hashedPassword = hr.hash();
        String salt = hr.salt();

        User user = User.builder()
                .id(1)
                .username("testuser")
                .password(hashedPassword)
                .salt(salt)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserId(1)).thenReturn(Optional.empty());

        Optional<String> result = userService.loginUser("testuser", plainPassword);

        assertTrue(result.isPresent());
        verify(tokenRepository, times(1)).save(any(UserToken.class));
    }

    @Test
    void testLoginUserFailsWithWrongPassword() throws NoSuchAlgorithmException {
        String password = "password123";
        HashUtils.HashResult hr = HashUtils.hashWithSalt(password);
        User user = User.builder()
                .id(1)
                .username("testuser")
                .password(hr.hash())
                .salt(hr.salt())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<String> result = userService.loginUser("testuser", "wrongpassword");

        assertFalse(result.isPresent());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void testLoginUserFailsWhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<String> result = userService.loginUser("nonexistent", "password");

        assertFalse(result.isPresent());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void testLogoutRemovesToken() {
        userService.logout(testUser);

        verify(tokenRepository, times(1)).deleteByUserId(1);
    }

    @Test
    void testValidateTokenSuccess() {
        when(tokenRepository.findById("validToken")).thenReturn(Optional.of(testToken));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.validateToken("validToken");

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void testValidateTokenFailsWhenTokenExpired() {
        Timestamp oldTimestamp = Timestamp.from(Instant.now().minus(25, ChronoUnit.HOURS));
        UserToken expiredToken = new UserToken("expiredToken", 1, oldTimestamp);

        when(tokenRepository.findById("expiredToken")).thenReturn(Optional.of(expiredToken));

        Optional<User> result = userService.validateToken("expiredToken");

        assertFalse(result.isPresent());
        verify(tokenRepository, times(1)).deleteById("expiredToken");
    }

    @Test
    void testHasPermissionForResourceReturnsTrueForOwner() {
        when(tokenRepository.findById("validToken")).thenReturn(Optional.of(testToken));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        boolean hasPermission = userService.hasPermissionForResource("validToken", 1);

        assertTrue(hasPermission);
    }

    @Test
    void testHasPermissionForResourceReturnsFalseForNonOwner() {
        when(tokenRepository.findById("validToken")).thenReturn(Optional.of(testToken));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        boolean hasPermission = userService.hasPermissionForResource("validToken", 999);

        assertFalse(hasPermission);
    }

    @Test
    void testUpdateUserProfileSuccess() {
        when(userProfileRepository.findByUserId(1)).thenReturn(Optional.of(testProfile));

        Optional<UserProfile> result = userService.updateUserProfile(1, "new@email.com", "Comedy");

        assertTrue(result.isPresent());
        verify(userProfileRepository, times(1)).update(testProfile);
        assertEquals("new@email.com", testProfile.getEmail());
        assertEquals("Comedy", testProfile.getFavoriteGenre());
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        User result = userService.getById(1);

        assertNotNull(result);
        assertEquals(testUser, result);
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        User result = userService.getById(999);

        assertNull(result);
    }

    @Test
    void testUpdateUserProfileNoChanges() {
        when(userProfileRepository.findByUserId(1)).thenReturn(Optional.of(testProfile));

        Optional<UserProfile> result = userService.updateUserProfile(1, null, null);

        assertTrue(result.isPresent());
        assertEquals(testProfile, result.get());
        verify(userProfileRepository, never()).update(any());
    }

    @Test
    void testCleanupExpiredTokens() {
        Timestamp expiredTimestamp = Timestamp.from(Instant.now().minus(25, ChronoUnit.HOURS));
        UserToken expiredToken = new UserToken("expired", 1, expiredTimestamp);

        Timestamp validTimestamp = Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS));
        UserToken validToken = new UserToken("valid", 2, validTimestamp);

        when(tokenRepository.findAll()).thenReturn(List.of(expiredToken, validToken));

        userService.cleanupExpiredTokens();

        verify(tokenRepository, times(1)).deleteById("expired");
    }
}