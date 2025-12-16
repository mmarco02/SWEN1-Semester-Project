package org.mrp.service;

import org.mrp.domain.Favorite;
import org.mrp.persistence.implemenatations.FavoriteRepository;

import java.util.List;
import java.util.Optional;

public class FavoriteService {
    private FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }


    public Optional<Favorite> findByUserAndEntry(int id, int entryId) {
        Optional<Favorite> favoriteOpt = favoriteRepository.findByUserAndEntry(id, entryId);
        return favoriteOpt;
    }

    public void save(Favorite favorite) {
        favoriteRepository.save(favorite);
    }

    public boolean deleteByUserAndEntry(int id, int entryId) {
        return favoriteRepository.deleteByUserAndEntry(id, entryId);
    }

    public List<Favorite> findByEntryId(int entryId) {
        return favoriteRepository.findByEntryId(entryId);
    }

    public Optional<Favorite> findById(int favoriteId) {
        return favoriteRepository.findById(favoriteId);
    }

    public List<Favorite> findByUserId(int userId) {
        return favoriteRepository.findByUserId(userId);
    }
}
