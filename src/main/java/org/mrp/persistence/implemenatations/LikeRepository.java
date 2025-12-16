package org.mrp.persistence.implemenatations;

import org.mrp.domain.Like;
import org.mrp.persistence.BaseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LikeRepository extends BaseRepository<Like, Integer> {

    public LikeRepository(Connection connection) {
        super(connection);
    }

    public void save(Like like) {
        String sql = """
            INSERT INTO RatingLikes (Rating_ID, User_ID)
            VALUES (?, ?) RETURNING Like_ID
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, like.getRatingId());
            statement.setInt(2, like.getUserId());

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int likeId = rs.getInt("Like_ID");
                like.setId(likeId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save like", e);
        }
    }

    public void update(Like like) {
        String sql = """
                UPDATE RatingLikes SET Rating_ID = ?, User_ID = ?
                WHERE Like_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, like.getRatingId());
            statement.setInt(2, like.getUserId());
            statement.setInt(3, like.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update like", e);
        }
    }

    @Override
    public Optional<Like> findById(Integer id) {
        String sql = """
                SELECT Like_ID, Rating_ID, User_ID
                FROM RatingLikes WHERE Like_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                Like like = Like.builder()
                        .id(rs.getInt("Like_ID"))
                        .ratingId(rs.getInt("Rating_ID"))
                        .userId(rs.getInt("User_ID"))
                        .build();

                return Optional.of(like);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find like by id", e);
        }
    }

    public Optional<Like> findByRatingAndUser(int ratingId, int userId) {
        String sql = """
                SELECT Like_ID, Rating_ID, User_ID
                FROM RatingLikes WHERE Rating_ID = ? AND User_ID = ?
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ratingId);
            statement.setInt(2, userId);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                Like like = Like.builder()
                        .id(rs.getInt("Like_ID"))
                        .ratingId(rs.getInt("Rating_ID"))
                        .userId(rs.getInt("User_ID"))
                        .build();

                return Optional.of(like);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find like by rating and user", e);
        }
    }

    @Override
    public List<Like> findAll() {
        List<Like> likes = new ArrayList<>();
        String sql = """
                SELECT Like_ID, Rating_ID, User_ID
                FROM RatingLikes ORDER BY Like_ID
                """;
        try(Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                Like like = Like.builder()
                        .id(rs.getInt("Like_ID"))
                        .ratingId(rs.getInt("Rating_ID"))
                        .userId(rs.getInt("User_ID"))
                        .build();
                likes.add(like);
            }
            return likes;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all likes", e);
        }
    }

    public List<Like> findByRatingId(int ratingId) {
        List<Like> likes = new ArrayList<>();
        String sql = """
                SELECT Like_ID, Rating_ID, User_ID
                FROM RatingLikes WHERE Rating_ID = ? ORDER BY Like_ID
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ratingId);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                Like like = Like.builder()
                        .id(rs.getInt("Like_ID"))
                        .ratingId(rs.getInt("Rating_ID"))
                        .userId(rs.getInt("User_ID"))
                        .build();
                likes.add(like);
            }
            return likes;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find likes by rating", e);
        }
    }

    public List<Like> findByUserId(int userId) {
        List<Like> likes = new ArrayList<>();
        String sql = """
                SELECT Like_ID, Rating_ID, User_ID
                FROM RatingLikes WHERE User_ID = ? ORDER BY Like_ID
                """;
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                Like like = Like.builder()
                        .id(rs.getInt("Like_ID"))
                        .ratingId(rs.getInt("Rating_ID"))
                        .userId(rs.getInt("User_ID"))
                        .build();
                likes.add(like);
            }
            return likes;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find likes by user", e);
        }
    }

    public int getLikeCountForRating(int ratingId) {
        String sql = "SELECT COUNT(*) as like_count FROM RatingLikes WHERE Rating_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ratingId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("like_count");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get like count for rating", e);
        }
        return 0;
    }

    public boolean hasUserLikedRating(int ratingId, int userId) {
        return findByRatingAndUser(ratingId, userId).isPresent();
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM RatingLikes WHERE Like_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete like", e);
        }
    }

    public void deleteByRatingAndUser(int ratingId, int userId) {
        String sql = "DELETE FROM RatingLikes WHERE Rating_ID = ? AND User_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ratingId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete like by rating and user", e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM RatingLikes";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all likes", e);
        }
    }
}