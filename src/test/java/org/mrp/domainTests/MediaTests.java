package org.mrp.domainTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mrp.domain.Favorite;
import org.mrp.domain.MediaEntry;
import org.mrp.domain.MediaType;
import org.mrp.domain.Rating;
import org.mrp.domain.User;
import org.mrp.persistence.implemenatations.FavoriteRepository;
import org.mrp.persistence.implemenatations.MediaEntryRepository;
import org.mrp.persistence.implemenatations.RatingRepository;
import org.mrp.service.FavoriteService;
import org.mrp.service.MediaService;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaTests {

    @Mock
    private MediaEntryRepository mediaEntryRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    private MediaService mediaService;
    private FavoriteService favoriteService;
    private MediaEntry testMediaEntry;
    private User testUser;
    private Rating testRating;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(mediaEntryRepository, ratingRepository);
        favoriteService = new FavoriteService(favoriteRepository);

        testUser = User.builder()
                .id(1)
                .username("creator")
                .password("pass")
                .salt("salt")
                .build();

        testMediaEntry = MediaEntry.builder()
                .id(100)
                .title("Inception")
                .description("A mind-bending thriller")
                .mediaType(MediaType.MOVIE)
                .releaseYear(2010)
                .genres(Arrays.asList("Sci-Fi", "Thriller"))
                .age(13)
                .averageRating(4.5)
                .createdByUserId(1)
                .build();

        testRating = Rating.builder()
                .id(50)
                .mediaEntryId(100)
                .userId(2)
                .starValue(5)
                .comment("Amazing!")
                .build();

        testFavorite = Favorite.builder()
                .id(1)
                .entryId(100)
                .userId(1)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Domain Tests

    @Test
    void testMediaEntryBuilderCreatesValidEntry() {
        MediaEntry entry = MediaEntry.builder()
                .id(1)
                .title("The Matrix")
                .description("Virtual reality action")
                .mediaType(MediaType.MOVIE)
                .releaseYear(1999)
                .genres(Arrays.asList("Action", "Sci-Fi"))
                .age(18)
                .averageRating(4.8)
                .createdByUserId(5)
                .build();

        assertEquals("The Matrix", entry.getTitle());
        assertEquals(MediaType.MOVIE, entry.getMediaType());
        assertEquals(1999, entry.getReleaseYear());
        assertEquals(2, entry.getGenres().size());
        assertTrue(entry.getGenres().contains("Action"));
    }

    // MediaService Tests

    @Test
    void testCreateMediaEntrySuccess() {
        doAnswer(invocation -> {
            MediaEntry entry = invocation.getArgument(0);
            entry.setId(100);
            return null;
        }).when(mediaEntryRepository).save(any(MediaEntry.class));

        MediaEntry result = mediaService.createMediaEntry(
                "Inception",
                "A mind-bending thriller",
                MediaType.MOVIE,
                2010,
                Arrays.asList("Sci-Fi", "Thriller"),
                13,
                testUser
        );

        assertNotNull(result);
        verify(mediaEntryRepository, times(1)).save(any(MediaEntry.class));
        assertEquals("Inception", result.getTitle());
        assertEquals(MediaType.MOVIE, result.getMediaType());
        assertEquals(1, result.getCreatedByUserId());
        assertEquals(100, result.getId()); // ID sollte gesetzt sein
    }

    @Test
    void testGetMediaEntryByIdSuccess() {
        when(mediaEntryRepository.findById(100)).thenReturn(Optional.of(testMediaEntry));

        Optional<MediaEntry> result = mediaService.getMediaEntryById(100);

        assertTrue(result.isPresent());
        assertEquals(testMediaEntry, result.get());
    }

    @Test
    void testUpdateMediaEntrySuccessWhenOwner() {
        when(mediaEntryRepository.findById(100)).thenReturn(Optional.of(testMediaEntry));

        boolean result = mediaService.updateMediaEntry(
                100,
                "Inception Updated",
                "Updated description",
                MediaType.MOVIE,
                2010,
                Arrays.asList("Sci-Fi", "Action"),
                13,
                testUser
        );

        assertTrue(result);
        verify(mediaEntryRepository, times(1)).update(testMediaEntry);
        assertEquals("Inception Updated", testMediaEntry.getTitle());
        assertEquals("Updated description", testMediaEntry.getDescription());
        assertEquals(2, testMediaEntry.getGenres().size());
        assertTrue(testMediaEntry.getGenres().contains("Action"));
    }

    @Test
    void testUpdateMediaEntryFailsWhenNotOwner() {
        User otherUser = User.builder().id(999).build(); // Different user
        when(mediaEntryRepository.findById(100)).thenReturn(Optional.of(testMediaEntry));

        boolean result = mediaService.updateMediaEntry(
                100,
                "New Title",
                "New Desc",
                MediaType.MOVIE,
                2020,
                List.of("Drama"),
                18,
                otherUser
        );

        assertFalse(result);
        verify(mediaEntryRepository, never()).update(any());
    }

    @Test
    void testUpdateMediaEntryFailsWhenEntryNotFound() {
        when(mediaEntryRepository.findById(999)).thenReturn(Optional.empty());

        boolean result = mediaService.updateMediaEntry(
                999,
                "Title",
                "Desc",
                MediaType.MOVIE,
                2020,
                List.of("Genre"),
                18,
                testUser
        );

        assertFalse(result);
        verify(mediaEntryRepository, never()).update(any());
    }

    @Test
    void testDeleteMediaEntrySuccessWhenOwner() {
        when(mediaEntryRepository.findById(100)).thenReturn(Optional.of(testMediaEntry));

        boolean result = mediaService.deleteMediaEntry(100, testUser);

        assertTrue(result);
        verify(mediaEntryRepository, times(1)).deleteById(100);
    }

    @Test
    void testDeleteMediaEntryFailsWhenNotOwner() {
        User otherUser = User.builder().id(999).build();
        when(mediaEntryRepository.findById(100)).thenReturn(Optional.of(testMediaEntry));

        boolean result = mediaService.deleteMediaEntry(100, otherUser);

        assertFalse(result);
        verify(mediaEntryRepository, never()).deleteById(anyInt());
    }

    @Test
    void testGetAllMediaEntries() {
        List<MediaEntry> entries = List.of(testMediaEntry);
        when(mediaEntryRepository.findAll()).thenReturn(entries);

        List<MediaEntry> result = mediaService.getAllMediaEntries();

        assertEquals(1, result.size());
        assertEquals(testMediaEntry, result.get(0));
    }

    @Test
    void testRateMediaEntrySuccessNewRating() {
        when(mediaEntryRepository.findById(100)).thenReturn(Optional.of(testMediaEntry));
        when(ratingRepository.findByEntryAndUser(100, 2)).thenReturn(Optional.empty());
        when(ratingRepository.calculateAverageRating(100)).thenReturn(4.5);

        doAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.setId(50);
            return null;
        }).when(ratingRepository).save(any(Rating.class));


        Optional<Rating> result = mediaService.rateMediaEntry(100, 2, 5, "Amazing!");

        assertTrue(result.isPresent());
        verify(ratingRepository, times(1)).save(any(Rating.class));
        verify(mediaEntryRepository, times(1)).updateAverageRating(100, 4.5);
        assertEquals(100, result.get().getMediaEntryId());
        assertEquals(2, result.get().getUserId());
        assertEquals(5, result.get().getStarValue());
        assertEquals("Amazing!", result.get().getComment());
        assertEquals(50, result.get().getId()); // ID sollte gesetzt sein
    }

    @Test
    void testRateMediaEntryUpdatesExistingRating() {
        when(mediaEntryRepository.findById(100)).thenReturn(Optional.of(testMediaEntry));
        when(ratingRepository.findByEntryAndUser(100, 2)).thenReturn(Optional.of(testRating));
        when(ratingRepository.calculateAverageRating(100)).thenReturn(4.7);

        Optional<Rating> result = mediaService.rateMediaEntry(100, 2, 4, "Updated review");

        assertTrue(result.isPresent());
        verify(ratingRepository, times(1)).update(testRating);
        verify(mediaEntryRepository, times(1)).updateAverageRating(100, 4.7);
        assertEquals(4, testRating.getStarValue());
        assertEquals("Updated review", testRating.getComment());
    }

    @Test
    void testRateMediaEntryFailsWithInvalidStarValue() {
        Optional<Rating> result = mediaService.rateMediaEntry(100, 2, 6, "Too high"); // Invalid: 6

        assertFalse(result.isPresent());
        verify(ratingRepository, never()).save(any());
        verify(ratingRepository, never()).update(any());
    }

    @Test
    void testRateMediaEntryFailsWithStarValueZero() {
        Optional<Rating> result = mediaService.rateMediaEntry(100, 2, 0, "Too low"); // Invalid: 0

        assertFalse(result.isPresent());
        verify(ratingRepository, never()).save(any());
        verify(ratingRepository, never()).update(any());
    }

    @Test
    void testRateMediaEntryFailsWhenEntryNotFound() {
        when(mediaEntryRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Rating> result = mediaService.rateMediaEntry(999, 2, 5, "Comment");

        assertFalse(result.isPresent());
        verify(ratingRepository, never()).save(any());
        verify(ratingRepository, never()).update(any());
    }

    @Test
    void testUpdateRatingSuccessWhenOwner() {
        User ratingOwner = User.builder().id(2).build();
        when(ratingRepository.findById(50)).thenReturn(Optional.of(testRating));
        when(ratingRepository.calculateAverageRating(100)).thenReturn(4.0);

        boolean result = mediaService.updateRating(50, 3, "Updated", ratingOwner);

        assertTrue(result);
        verify(ratingRepository, times(1)).update(testRating);
        verify(mediaEntryRepository, times(1)).updateAverageRating(100, 4.0);
        assertEquals(3, testRating.getStarValue());
        assertEquals("Updated", testRating.getComment());
    }

    @Test
    void testUpdateRatingFailsWhenNotOwner() {
        User otherUser = User.builder().id(999).build();
        when(ratingRepository.findById(50)).thenReturn(Optional.of(testRating));

        boolean result = mediaService.updateRating(50, 3, "Updated", otherUser);

        assertFalse(result);
        verify(ratingRepository, never()).update(any());
        verify(mediaEntryRepository, never()).updateAverageRating(anyInt(), anyDouble());
    }

    @Test
    void testUpdateRatingFailsWithInvalidScore() {
        // Arrange
        User ratingOwner = User.builder().id(2).build();

        // Act - Teste verschiedene ungültige Scores
        boolean result1 = mediaService.updateRating(50, 0, "Updated", ratingOwner); // Invalid: 0
        boolean result2 = mediaService.updateRating(50, 6, "Updated", ratingOwner); // Invalid: 6

        // Assert
        assertFalse(result1);
        assertFalse(result2);
        verify(ratingRepository, never()).findById(anyInt());
        verify(ratingRepository, never()).update(any());
        verify(mediaEntryRepository, never()).updateAverageRating(anyInt(), anyDouble());
    }

    @Test
    void testDeleteRatingSuccessWhenOwner() {
        User ratingOwner = User.builder().id(2).build();
        when(ratingRepository.findById(50)).thenReturn(Optional.of(testRating));
        when(ratingRepository.calculateAverageRating(100)).thenReturn(4.0);

        boolean result = mediaService.deleteRating(50, ratingOwner);

        assertTrue(result);
        verify(ratingRepository, times(1)).deleteById(50);
        verify(mediaEntryRepository, times(1)).updateAverageRating(100, 4.0);
    }

    @Test
    void testDeleteRatingFailsWhenNotOwner() {
        User otherUser = User.builder().id(999).build();
        when(ratingRepository.findById(50)).thenReturn(Optional.of(testRating));

        boolean result = mediaService.deleteRating(50, otherUser);

        assertFalse(result);
        verify(ratingRepository, never()).deleteById(anyInt());
        verify(mediaEntryRepository, never()).updateAverageRating(anyInt(), anyDouble());
    }

    @Test
    void testSaveFavorite() {
        favoriteService.save(testFavorite);

        verify(favoriteRepository, times(1)).save(testFavorite);
    }

    @Test
    void testDeleteByUserAndEntrySuccess() {
        when(favoriteRepository.deleteByUserAndEntry(1, 100)).thenReturn(true);

        boolean result = favoriteService.deleteByUserAndEntry(1, 100);

        assertTrue(result);
        verify(favoriteRepository, times(1)).deleteByUserAndEntry(1, 100);
    }

    @Test
    void testDeleteByUserAndEntryFails() {
        when(favoriteRepository.deleteByUserAndEntry(1, 999)).thenReturn(false);

        boolean result = favoriteService.deleteByUserAndEntry(1, 999);

        assertFalse(result);
        verify(favoriteRepository, times(1)).deleteByUserAndEntry(1, 999);
    }

    @Test
    void testSaveFavoriteWithTimestamp() {
        Favorite favoriteWithoutId = Favorite.builder()
                .entryId(100)
                .userId(1)
                .createdAt(LocalDateTime.now())
                .build();

        doAnswer(invocation -> {
            Favorite fav = invocation.getArgument(0);
            fav.setId(1);
            return null;
        }).when(favoriteRepository).save(any(Favorite.class));

        favoriteService.save(favoriteWithoutId);

        verify(favoriteRepository, times(1)).save(favoriteWithoutId);
        assertEquals(1, favoriteWithoutId.getId());
    }
}