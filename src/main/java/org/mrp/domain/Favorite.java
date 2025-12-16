package org.mrp.domain;

import java.time.LocalDateTime;

public class Favorite {
    private int id;
    private int entryId;
    private int userId;
    private LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Favorite{" +
                "id=" + id +
                ", entryID=" + entryId +
                ", userID=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }

    public static FavoriteBuilder builder(){
        return new FavoriteBuilder();
    }

    public static class FavoriteBuilder {
        Favorite favorite = new Favorite();

        public FavoriteBuilder id(int id) {
            favorite.id = id;
            return this;
        }

        public FavoriteBuilder entryId(int entryId) {
            favorite.entryId = entryId;
            return this;
        }

        public FavoriteBuilder userId(int userId) {
            favorite.userId = userId;
            return this;
        }

        public FavoriteBuilder createdAt(LocalDateTime createdAt) {
            favorite.createdAt = createdAt;
            return this;
        }

        public Favorite build(){
            return favorite;
        }
    }
}
