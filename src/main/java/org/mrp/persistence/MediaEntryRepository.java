package org.mrp.persistence;

import org.mrp.domain.MediaEntry;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class MediaEntryRepository extends BaseRepository<MediaEntry, Integer> {
    public MediaEntryRepository(Connection connection) {
        super(connection);
    }

    @Override
    public void save(MediaEntry entity) {

    }

    @Override
    public Optional<MediaEntry> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public List<MediaEntry> findAll() {
        return List.of();
    }

    @Override
    public void deleteById(Integer integer) {

    }

    @Override
    public void deleteAll() {

    }
}
