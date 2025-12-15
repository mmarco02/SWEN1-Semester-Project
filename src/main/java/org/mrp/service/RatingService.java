package org.mrp.service;

import org.mrp.persistence.implemenatations.RatingRepository;

//TODO: implement later
public class RatingService {
    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public double calculateAverageRating(int entryId) {
        return ratingRepository.calculateAverageRating(entryId);
    }
}
