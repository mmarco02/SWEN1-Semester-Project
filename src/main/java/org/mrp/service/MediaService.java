package org.mrp.service;

import org.mrp.domain.MediaEntry;
import org.mrp.domain.MediaType;
import org.mrp.domain.Rating;
import org.mrp.domain.User;
import org.mrp.persistence.implemenatations.MediaEntryRepository;
import org.mrp.persistence.implemenatations.RatingRepository;

import java.util.List;
import java.util.Optional;

public class MediaService {

    private final MediaEntryRepository mediaEntryRepository;
    private final RatingRepository ratingRepository;

    public MediaService(MediaEntryRepository mediaEntryRepository,
                        RatingRepository ratingRepository) {
        this.mediaEntryRepository = mediaEntryRepository;
        this.ratingRepository = ratingRepository;
    }

    public MediaEntry createMediaEntry(String title, String description, MediaType mediaType,
                                       Integer releaseYear, List<String> genres, int age, User creator) {
        MediaEntry entry = MediaEntry.builder()
                .title(title)
                .description(description)
                .mediaType(mediaType)
                .releaseYear(releaseYear)
                .genres(genres)
                .age(age)
                .createdByUserId(creator.getId())
                .build();

        mediaEntryRepository.save(entry);
        return entry;
    }

    public boolean updateMediaEntry(int entryId, String title, String description, MediaType mediaType,
                                    Integer releaseYear, List<String> genres, int age, User editor) {
        Optional<MediaEntry> entryOpt = mediaEntryRepository.findById(entryId);
        if (entryOpt.isEmpty()) {
            return false;
        }

        MediaEntry entry = entryOpt.get();

        if (entry.getCreatedByUserId() != editor.getId()) {
            return false;
        }

        entry.setTitle(title);
        entry.setDescription(description);
        entry.setMediaType(mediaType);
        entry.setReleaseYear(releaseYear);
        entry.setGenres(genres);
        entry.setAge(age);

        mediaEntryRepository.update(entry);
        return true;
    }

    public Optional<MediaEntry> getMediaEntryById(int entryId) {
        return mediaEntryRepository.findById(entryId);
    }

    public List<MediaEntry> getAllMediaEntries() {
        return mediaEntryRepository.findAll();
    }

    public List<MediaEntry> getMediaEntriesByUser(int userId) {
        return mediaEntryRepository.findByUserId(userId);
    }

    public boolean deleteMediaEntry(int entryId, User deleter) {
        Optional<MediaEntry> entryOpt = mediaEntryRepository.findById(entryId);
        if (entryOpt.isEmpty()) {
            return false;
        }

        MediaEntry entry = entryOpt.get();

        if (entry.getCreatedByUserId() != deleter.getId()) {
            return false;
        }

        mediaEntryRepository.deleteById(entryId);
        return true;
    }

    public List<MediaEntry> getMediaEntriesByGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            return List.of();
        }
        return mediaEntryRepository.findByGenre(genre);
    }

    public Optional<Rating> rateMediaEntry(int entryId, int userId, int starValue, String comment) {
        if (starValue < 1 || starValue > 5) {
            return Optional.empty();
        }

        //check if entry exists
        Optional<MediaEntry> entryOpt = mediaEntryRepository.findById(entryId);
        if (entryOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<Rating> existingRating = ratingRepository.findByEntryAndUser(entryId, userId);

        Rating rating;
        if (existingRating.isPresent()) {
            rating = existingRating.get();
            rating.setStarValue(starValue);
            rating.setComment(comment);
            ratingRepository.update(rating);
        } else {
            rating = Rating.builder()
                    .mediaEntryId(entryId)
                    .userId(userId)
                    .starValue(starValue)
                    .comment(comment)
                    .build();
            ratingRepository.save(rating);
        }

        double average = ratingRepository.calculateAverageRating(entryId);
        mediaEntryRepository.updateAverageRating(entryId, average);

        return Optional.of(rating);
    }

    public Optional<Rating> getRating(int ratingId) {
        return ratingRepository.findById(ratingId);
    }

    public List<Rating> getRatingsByEntry(int entryId) {
        return ratingRepository.findByEntryId(entryId);
    }

    public List<Rating> getRatingsByUser(int userId) {
        return ratingRepository.findByUserId(userId);
    }

    public boolean updateRating(int ratingId, int score, String comment, User editor) {
        if (score < 1 || score > 5) {
            return false;
        }

        Optional<Rating> ratingOpt = ratingRepository.findById(ratingId);
        if (ratingOpt.isEmpty()) {
            return false;
        }

        Rating rating = ratingOpt.get();

        // check if editor is the rating owner
        if (rating.getUserId() != editor.getId()) {
            return false;
        }

        rating.setStarValue(score);
        rating.setComment(comment);
        ratingRepository.update(rating);

        // update average rating for the media entry
        double average = ratingRepository.calculateAverageRating(rating.getMediaEntryId());
        mediaEntryRepository.updateAverageRating(rating.getMediaEntryId(), average);

        return true;
    }

    public boolean deleteRating(int ratingId, User deleter) {
        Optional<Rating> ratingOpt = ratingRepository.findById(ratingId);
        if (ratingOpt.isEmpty()) {
            return false;
        }

        Rating rating = ratingOpt.get();

        // check if deleter is the rating owner
        if (rating.getUserId() != deleter.getId()) {
            return false;
        }

        int entryId = rating.getMediaEntryId();
        ratingRepository.deleteById(ratingId);

        // update average rating for the media entry
        double average = ratingRepository.calculateAverageRating(entryId);
        mediaEntryRepository.updateAverageRating(entryId, average);

        return true;
    }

    public double getUserAverageRating(int userId) {
        List<Rating> userRatings = ratingRepository.findByUserId(userId);
        if (userRatings.isEmpty()) {
            return 0.0;
        }

        double sum = userRatings.stream()
                .mapToDouble(Rating::getStarValue)
                .sum();

        return sum / userRatings.size();
    }
}