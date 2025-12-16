package org.mrp.domain;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class MediaEntry extends JsonObject {
    private int id;
    private String title;
    private String description;
    private MediaType mediaType;
    private Integer releaseYear;
    private List<String> genres = new ArrayList<>();
    private int age;
    private double averageRating;
    private int createdByUserId;

    public MediaEntry() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    @Override
    public String toString() {
        return "MediaEntry{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", mediaType=" + mediaType +
                ", releaseYear=" + releaseYear +
                ", genres=" + genres +
                ", age=" + age +
                ", averageRating=" + averageRating +
                ", createdByUserId=" + createdByUserId +
                '}';
    }

    public static MediaEntryBuilder builder() {
        return new MediaEntryBuilder();
    }

    public static class MediaEntryBuilder {
        MediaEntry mediaEntry = new MediaEntry();

        public MediaEntryBuilder id(int id) {
            mediaEntry.id = id;
            return this;
        }

        public MediaEntryBuilder title(String title) {
            mediaEntry.title = title;
            return this;
        }

        public MediaEntryBuilder description(String description) {
            mediaEntry.description = description;
            return this;
        }

        public MediaEntryBuilder mediaType(MediaType mediaType) {
            mediaEntry.mediaType = mediaType;
            return this;
        }

        public MediaEntryBuilder releaseYear(Integer releaseYear) {
            mediaEntry.releaseYear = releaseYear;
            return this;
        }

        public MediaEntryBuilder genres(List<String> genres) {
            mediaEntry.genres = genres;
            return this;
        }

        public MediaEntryBuilder age(int age) {
            mediaEntry.age = age;
            return this;
        }

        public MediaEntryBuilder averageRating(double averageRating) {
            mediaEntry.averageRating = averageRating;
            return this;
        }

        public MediaEntryBuilder createdByUserId(int createdByUserId) {
            mediaEntry.createdByUserId = createdByUserId;
            return this;
        }

        public MediaEntry build() {
            return mediaEntry;
        }
    }
}
