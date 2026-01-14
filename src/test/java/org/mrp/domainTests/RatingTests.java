package org.mrp.domainTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mrp.domain.Like;
import org.mrp.domain.Rating;
import org.mrp.domain.User;
import org.mrp.persistence.implemenatations.LikeRepository;
import org.mrp.persistence.implemenatations.RatingRepository;
import org.mrp.service.RatingService;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingTests {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private LikeRepository likeRepository;

    private RatingService ratingService;
    private Rating testRating;
    private User testUser;
    private Like testLike;

    @BeforeEach
    void setUp() {
        ratingService = new RatingService(ratingRepository, likeRepository);

        testRating = Rating.builder()
                .id(1)
                .mediaEntryId(100)
                .userId(10)
                .starValue(4)
                .comment("Great movie!")
                .updatedAt(LocalDateTime.now())
                .isConfirmed(false)
                .build();

        testUser = User.builder()
                .id(10)
                .username("reviewer")
                .password("pass")
                .salt("salt")
                .build();

        testLike = Like.builder()
                .id(1)
                .ratingId(1)
                .userId(20)
                .build();
    }

    // Service Tests

    @Test
    void testCreateRatingSuccess() {
        doAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.setId(1);
            return null;
        }).when(ratingRepository).save(any(Rating.class));

        Rating result = ratingService.createRating(100, 10, 4, "Good movie");

        assertNotNull(result);
        verify(ratingRepository, times(1)).save(any(Rating.class));
        assertEquals(100, result.getMediaEntryId());
        assertEquals(10, result.getUserId());
        assertEquals(4, result.getStarValue());
        assertEquals("Good movie", result.getComment());
        assertEquals(1, result.getId()); // ID sollte gesetzt sein
    }

    @Test
    void testCreateRatingWithNullComment() {
        doAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.setId(2);
            return null;
        }).when(ratingRepository).save(any(Rating.class));

        Rating result = ratingService.createRating(100, 10, 4, null);

        assertNotNull(result);
        verify(ratingRepository, times(1)).save(any(Rating.class));
        assertEquals(4, result.getStarValue());
        assertNull(result.getComment());
    }

    @Test
    void testGetRatingByIdSuccess() {
        when(ratingRepository.findById(1)).thenReturn(Optional.of(testRating));

        Optional<Rating> result = ratingService.getRatingById(1);

        assertTrue(result.isPresent());
        assertEquals(testRating, result.get());
    }

    @Test
    void testGetRatingByIdNotFound() {
        when(ratingRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Rating> result = ratingService.getRatingById(999);

        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateRatingSuccess() {
        User editor = User.builder().id(10).build(); // Same user as rating owner
        when(ratingRepository.findById(1)).thenReturn(Optional.of(testRating));

        boolean result = ratingService.updateRating(1, 5, "Updated comment", editor);

        assertTrue(result);
        verify(ratingRepository, times(1)).update(testRating);
        assertEquals(5, testRating.getStarValue());
        assertEquals("Updated comment", testRating.getComment());
        assertNotNull(testRating.getUpdatedAt());
    }

    @Test
    void testUpdateRatingWithNullValues() {
        User editor = User.builder().id(10).build();

        assertThrows(IllegalArgumentException.class, () -> {
            ratingService.updateRating(1, null, "Updated comment", editor);
        });

        verify(ratingRepository, never()).update(any());
    }

    @Test
    void testUpdateRatingFailsWhenNotOwner() {
        User otherUser = User.builder().id(999).build(); // Different user
        when(ratingRepository.findById(1)).thenReturn(Optional.of(testRating));

        boolean result = ratingService.updateRating(1, 5, "Comment", otherUser);

        assertFalse(result);
        verify(ratingRepository, never()).update(any());
    }

    @Test
    void testUpdateRatingFailsWhenRatingNotFound() {
        when(ratingRepository.findById(999)).thenReturn(Optional.empty());

        boolean result = ratingService.updateRating(999, 5, "Comment", testUser);

        assertFalse(result);
        verify(ratingRepository, never()).update(any());
    }

    @Test
    void testLikeRatingSuccess() {
        User liker = User.builder().id(20).build(); // Different from rating owner

        when(ratingRepository.findById(1)).thenReturn(Optional.of(testRating));
        when(likeRepository.findByRatingAndUser(1, 20)).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            Like like = invocation.getArgument(0);
            like.setId(1); // Simuliere ID-Zuweisung
            return null;
        }).when(likeRepository).save(any(Like.class));

        Optional<Like> result = ratingService.likeRating(1, liker);

        assertTrue(result.isPresent());
        verify(likeRepository, times(1)).save(any(Like.class));
        assertEquals(1, result.get().getId());
        assertEquals(1, result.get().getRatingId());
        assertEquals(20, result.get().getUserId());
    }

    @Test
    void testLikeRatingFailsWhenOwnerTriesToLikeOwnRating() {
        when(ratingRepository.findById(1)).thenReturn(Optional.of(testRating));

        Optional<Like> result = ratingService.likeRating(1, testUser);

        assertFalse(result.isPresent());
        verify(likeRepository, never()).save(any());
    }

    @Test
    void testLikeRatingReturnsExistingLike() {
        User liker = User.builder().id(20).build();
        when(ratingRepository.findById(1)).thenReturn(Optional.of(testRating));
        when(likeRepository.findByRatingAndUser(1, 20)).thenReturn(Optional.of(testLike));

        Optional<Like> result = ratingService.likeRating(1, liker);

        assertTrue(result.isPresent());
        assertEquals(testLike, result.get());
        verify(likeRepository, never()).save(any());
    }

    @Test
    void testLikeRatingFailsWhenRatingNotFound() {
        when(ratingRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Like> result = ratingService.likeRating(999, testUser);

        assertFalse(result.isPresent());
        verify(likeRepository, never()).save(any());
    }

    @Test
    void testUnlikeRatingFailsWhenRatingNotFound() {
        when(ratingRepository.findById(999)).thenReturn(Optional.empty());

        boolean result = ratingService.unlikeRating(999, testUser);

        assertFalse(result);
        verify(likeRepository, never()).deleteByRatingAndUser(anyInt(), anyInt());
    }

    @Test
    void testGetLikeCountForRating() {
        when(likeRepository.getLikeCountForRating(1)).thenReturn(5);

        int count = ratingService.getLikeCountForRating(1);

        assertEquals(5, count);
    }

    @Test
    void testHasUserLikedRatingReturnsTrue() {
        when(likeRepository.hasUserLikedRating(1, 20)).thenReturn(true);

        boolean result = ratingService.hasUserLikedRating(1, 20);

        assertTrue(result);
    }

    @Test
    void testHasUserLikedRatingReturnsFalse() {
        when(likeRepository.hasUserLikedRating(1, 99)).thenReturn(false);

        boolean result = ratingService.hasUserLikedRating(1, 99);

        assertFalse(result);
    }

    @Test
    void testGetLikeByRatingAndUser() {
        when(likeRepository.findByRatingAndUser(1, 20)).thenReturn(Optional.of(testLike));

        Optional<Like> result = ratingService.getLikeByRatingAndUser(1, 20);

        assertTrue(result.isPresent());
        assertEquals(testLike, result.get());
    }

    @Test
    void testConfirmRating() {
        Rating unconfirmedRating = Rating.builder()
                .id(2)
                .mediaEntryId(100)
                .userId(10)
                .starValue(3)
                .isConfirmed(false)
                .build();

        when(ratingRepository.findById(2)).thenReturn(Optional.of(unconfirmedRating));

        Optional<Rating> result = ratingService.confirmRating(unconfirmedRating);

        assertTrue(result.isPresent());
        verify(ratingRepository, times(1)).update(unconfirmedRating);
        assertTrue(unconfirmedRating.isConfirmed());
        assertEquals(2, result.get().getId());
    }

    @Test
    void testConfirmRatingAlreadyConfirmed() {
        Rating confirmedRating = Rating.builder()
                .id(3)
                .mediaEntryId(100)
                .userId(10)
                .starValue(3)
                .isConfirmed(true)
                .build();

        when(ratingRepository.findById(3)).thenReturn(Optional.of(confirmedRating));

        Optional<Rating> result = ratingService.confirmRating(confirmedRating);

        assertTrue(result.isPresent());
        verify(ratingRepository, times(1)).update(confirmedRating);
        assertTrue(confirmedRating.isConfirmed()); //should still be true
    }

    @Test
    void testCalculateAverageRating() {
        when(ratingRepository.calculateAverageRating(100)).thenReturn(4.5);

        double average = ratingService.calculateAverageRating(100);

        assertEquals(4.5, average, 0.001);
    }

    @Test
    void testUpdateRatingWithValidStarValues() {
        User editor = User.builder().id(10).build();
        when(ratingRepository.findById(1)).thenReturn(Optional.of(testRating));

        for (int starValue = 1; starValue <= 5; starValue++) {
            boolean result = ratingService.updateRating(1, starValue, "Test", editor);

            assertTrue(result);
            assertEquals(starValue, testRating.getStarValue());
        }
    }

    @Test
    void testUpdateRatingWithInvalidStarValues() {
        User editor = User.builder().id(10).build();

        testRating.setStarValue(4);

        int[] invalidValues = {0, 6, -1, 10};
        for (int starValue : invalidValues) {
            assertThrows(IllegalArgumentException.class, () -> {
                ratingService.updateRating(1, starValue, "Test", editor);
            });

            assertEquals(4, testRating.getStarValue());
        }

        verify(ratingRepository, never()).findById(anyInt());
        verify(ratingRepository, never()).update(any());
    }

    @Test
    void testLikeRatingCreatesLikeWithCorrectValues() {
        User liker = User.builder().id(25).build();
        when(ratingRepository.findById(1)).thenReturn(Optional.of(testRating));
        when(likeRepository.findByRatingAndUser(1, 25)).thenReturn(Optional.empty());

        doAnswer(invocation -> {
            Like like = invocation.getArgument(0);
            like.setId(2);
            return null;
        }).when(likeRepository).save(any(Like.class));

        Optional<Like> result = ratingService.likeRating(1, liker);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getRatingId());
        assertEquals(25, result.get().getUserId());
        assertEquals(2, result.get().getId());
    }
}