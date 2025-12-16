package org.mrp.domain;

public class Like extends JsonObject{
    private int id;
    private int ratingId;
    private int userId;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRatingId() {
        return ratingId;
    }

    public void setRatingId(int ratingId) {
        this.ratingId = ratingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", ratingId=" + ratingId +
                ", userId=" + userId +
                '}';
    }

    public static LikeBuilder builder() {
        return new LikeBuilder();
    }

    public static class LikeBuilder {
        Like like = new Like();

        public LikeBuilder id (int id){
            like.id = id;
            return this;
        }

        public LikeBuilder ratingId(int ratingId) {
            like.ratingId = ratingId;
            return this;
        }

        public LikeBuilder userId(int userId) {
            like.userId = userId;
            return this;
        }

        public Like build(){
            return like;
        }

    }
}
