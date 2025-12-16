package org.mrp.domain;

import java.sql.Timestamp;

public class Rating extends JsonObject {
    private int id;
    private int mediaEntryId;
    private int userId;
    //starValue 1-5
    private int starValue;
    private String comment;
    private Timestamp updatedAt;
    private boolean isConfirmed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMediaEntryId() {
        return mediaEntryId;
    }

    public void setMediaEntryId(int mediaEntryId) {
        this.mediaEntryId = mediaEntryId;
    }

    public int getStarValue() {
        return starValue;
    }

    public void setStarValue(int starValue) {
        this.starValue = starValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", mediaEntryId=" + mediaEntryId +
                ", userId=" + userId +
                ", starValue=" + starValue +
                ", comment='" + comment + '\'' +
                ", updatedAt=" + updatedAt +
                ", isConfirmed=" + isConfirmed +
                '}';
    }

    public static RatingBuilder builder() {
        return new RatingBuilder();
    }

    public static class RatingBuilder {
        Rating rating = new Rating();

        public RatingBuilder id(int id) {
            rating.id = id;
            return this;
        }

        public RatingBuilder userId(int userId) {
            rating.userId = userId;
            return this;
        }

        public RatingBuilder mediaEntryId(int mediaEntryId) {
            rating.mediaEntryId = mediaEntryId;
            return this;
        }

        public RatingBuilder starValue(int starValue) {
            rating.starValue = starValue;
            return this;
        }

        public RatingBuilder comment(String comment) {
            rating.comment = comment;
            return this;
        }

        public RatingBuilder updatedAt(Timestamp updatedAt) {
            rating.updatedAt = updatedAt;
            return this;
        }

        public RatingBuilder isConfirmed(boolean isConfirmed) {
            rating.isConfirmed = isConfirmed;
            return this;
        }

        public Rating build() {
            return rating;
        }
    }
}
