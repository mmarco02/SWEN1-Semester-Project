package org.mrp.service;

import org.mrp.domain.Like;
import org.mrp.domain.Rating;
import org.mrp.domain.User;
import org.mrp.persistence.implemenatations.LikeRepository;
import org.mrp.persistence.implemenatations.RatingRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class RatingService {
    private final RatingRepository ratingRepository;
    private final LikeRepository likeRepository;

    public RatingService(RatingRepository ratingRepository, LikeRepository likeRepository) {
        this.ratingRepository = ratingRepository;
        this.likeRepository = likeRepository;
    }

    public boolean updateRating(int ratingId, Integer stars, String comment, User editor) {
        Optional<Rating> ratingOpt = ratingRepository.findById(ratingId);
        if (ratingOpt.isEmpty()) {
            return false;
        }

        Rating rating = ratingOpt.get();

        if(rating.getUserId() != editor.getId()) {
            return false;
        }

        rating.setStarValue(stars);
        rating.setComment(comment);
        rating.setUpdatedAt(Timestamp.from(Instant.now()));

        ratingRepository.update(rating);
        return true;
    }

    public double calculateAverageRating(int entryId) {
        return ratingRepository.calculateAverageRating(entryId);
    }

    public Rating createRating(int entryId, int userId, Integer stars, String comment) {
        Rating rating = new Rating();
        rating.setMediaEntryId(entryId);
        rating.setUserId(userId);
        rating.setStarValue(stars);
        rating.setComment(comment);
        rating.setUpdatedAt(Timestamp.from(Instant.now()));
        ratingRepository.save(rating);

        return rating;
    }

    public Optional<Rating> getRatingById(int id) {
        return ratingRepository.findById(id);
    }

    public Optional<Like> likeRating(int ratingId, User user) {
        Optional<Rating> ratingOpt = ratingRepository.findById(ratingId);
        if (ratingOpt.isEmpty()) {
            return Optional.empty();
        }

        Rating rating = ratingOpt.get();

        if (rating.getUserId() == user.getId()) {
            return Optional.empty();
        }

        Optional<Like> existingLike = likeRepository.findByRatingAndUser(ratingId, user.getId());
        if (existingLike.isPresent()) {
            // user already liked this rating, return the existing like
            return existingLike;
        }

        Like like = new Like();
        like.setRatingId(ratingId);
        like.setUserId(user.getId());

        likeRepository.save(like);
        return Optional.of(like);
    }

    public boolean unlikeRating(int ratingId, User user) {
        Optional<Rating> ratingOpt = ratingRepository.findById(ratingId);
        if (ratingOpt.isEmpty()) {
            return false;
        }

        Optional<Like> likeOpt = likeRepository.findByRatingAndUser(ratingId, user.getId());
        if (likeOpt.isEmpty()) {
            return false;
        }

        likeRepository.deleteByRatingAndUser(ratingId, user.getId());
        return true;
    }

    public boolean toggleLikeRating(int ratingId, User user) {
        Optional<Rating> ratingOpt = ratingRepository.findById(ratingId);
        if (ratingOpt.isEmpty()) {
            return false;
        }

        Rating rating = ratingOpt.get();

        if (rating.getUserId() == user.getId()) {
            return false;
        }

        Optional<Like> existingLike = likeRepository.findByRatingAndUser(ratingId, user.getId());
        if (existingLike.isPresent()) {
            likeRepository.deleteByRatingAndUser(ratingId, user.getId());
            return false;
        } else {
            Like like = new Like();
            like.setRatingId(ratingId);
            like.setUserId(user.getId());
            likeRepository.save(like);
            return true;
        }
    }

    public int getLikeCountForRating(int ratingId) {
        return likeRepository.getLikeCountForRating(ratingId);
    }

    public boolean hasUserLikedRating(int ratingId, int userId) {
        return likeRepository.hasUserLikedRating(ratingId, userId);
    }

    public Optional<Like> getLikeByRatingAndUser(int ratingId, int userId) {
        return likeRepository.findByRatingAndUser(ratingId, userId);
    }

    public Optional<Rating> confirmRating(Rating rating) {
        rating.setConfirmed(true);
        ratingRepository.update(rating);
        return ratingRepository.findById(rating.getId());
    }
}