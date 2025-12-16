package org.mrp.service;

import org.mrp.domain.Rating;
import org.mrp.domain.User;
import org.mrp.persistence.implemenatations.RatingRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

//TODO: implement later
public class RatingService {
    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
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

    public Rating createRating(Integer stars, String comment) {
        Rating rating = new Rating();
        rating.setStarValue(stars);
        rating.setComment(comment);
        rating.setUpdatedAt(Timestamp.from(Instant.now()));
        ratingRepository.save(rating);

        return rating;
    }

    public Optional<Rating> getRatingById(int id) {
        return ratingRepository.findById(id);
    }
}
