package org.mrp.persistence;

import org.mrp.domain.Rating;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class RatingRepository extends BaseRepository<Rating,Integer> {
    public RatingRepository(Connection connection) {
        super(connection);
    }

    @Override
    public void save(Rating entity) {

    }

    @Override
    public Optional<Rating> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public List<Rating> findAll() {
        return List.of();
    }

    @Override
    public void deleteById(Integer integer) {

    }

    @Override
    public void deleteAll() {

    }
}
