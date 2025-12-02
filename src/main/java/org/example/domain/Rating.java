package org.example.domain;

import java.time.LocalDateTime;

public class Rating {
    private int id;
    private int userId;
    private int mediaEntryId;
    //starValue 1-5
    private int starValue;
    private String comment;
    private LocalDateTime timeStamp;

    public Rating() {
    }

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

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", userId=" + userId +
                ", mediaEntryId=" + mediaEntryId +
                ", starValue=" + starValue +
                ", comment='" + comment + '\'' +
                ", timeStamp=" + timeStamp +
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

        public RatingBuilder timeStamp(LocalDateTime timeStamp) {
            rating.timeStamp = timeStamp;
            return this;
        }

        public Rating build() {
            return rating;
        }
    }
}
