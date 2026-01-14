package org.mrp.persistence.implemenatations;

import org.mrp.domain.Rating;
import org.mrp.persistence.BaseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RatingRepository extends BaseRepository<Rating, Integer> {
    public RatingRepository(Connection connection) {
        super(connection);
    }

    @Override
    public void save(Rating entity) {
        String sql = """
                INSERT INTO MediaRatings (Entry_ID, User_ID, StarValue, Comment)
                VALUES (?, ?, ?, ?) RETURNING Rating_ID, Updated_At
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entity.getMediaEntryId());
            statement.setInt(2, entity.getUserId());
            statement.setDouble(3, entity.getStarValue());
            statement.setString(4, entity.getComment());

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                entity.setId(rs.getInt("Rating_ID"));
                entity.setUpdatedAt(rs.getTimestamp("Updated_At"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save rating", e);
        }
    }

    public void update(Rating rating) {
        String sql = """
                UPDATE MediaRatings SET StarValue = ?, Comment = ?, Updated_At = CURRENT_TIMESTAMP, Is_Confirmed = ?
                WHERE Rating_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, rating.getStarValue());
            statement.setString(2, rating.getComment());
            statement.setBoolean(3, rating.isConfirmed());
            statement.setInt(4, rating.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update rating", e);
        }
    }

    @Override
    public Optional<Rating> findById(Integer id) {
        String sql = """
                SELECT Rating_ID, Entry_ID, User_ID, StarValue, Comment, Updated_At, Is_Confirmed
                FROM MediaRatings WHERE Rating_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRating(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find rating by id", e);
        }
        return Optional.empty();
    }

    public Optional<Rating> findByEntryAndUser(int entryId, int userId) {
        String sql = """
                SELECT Rating_ID, Entry_ID, User_ID, StarValue, Comment, Updated_At, Is_Confirmed
                FROM MediaRatings WHERE Entry_ID = ? AND User_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entryId);
            statement.setInt(2, userId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRating(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find rating by entry and user", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Rating> findAll() {
        List<Rating> ratings = new ArrayList<>();
        String sql = """
            SELECT Rating_ID, Entry_ID, User_ID, StarValue, Comment, Updated_At, Is_Confirmed
            FROM MediaRatings ORDER BY Updated_At DESC
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                ratings.add(mapResultSetToRating(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all ratings", e);
        }
        return ratings;
    }

    public List<Rating> findByEntryId(int entryId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = """
                SELECT Rating_ID, Entry_ID, User_ID, StarValue, Comment, Updated_At, Is_Confirmed
                FROM MediaRatings WHERE Entry_ID = ? ORDER BY Updated_At DESC
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entryId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                ratings.add(mapResultSetToRating(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ratings by entry", e);
        }
        return ratings;
    }

    public List<Rating> findByUserId(int userId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = """
                SELECT Rating_ID, Entry_ID, User_ID, StarValue, Comment, Updated_At, Is_Confirmed
                FROM MediaRatings WHERE User_ID = ? ORDER BY Updated_At DESC
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                ratings.add(mapResultSetToRating(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ratings by user", e);
        }
        return ratings;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM MediaRatings WHERE Rating_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete rating", e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM MediaRatings";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all ratings", e);
        }
    }

    public double calculateAverageRating(int entryId) {
        String sql = "SELECT AVG(StarValue) as average FROM MediaRatings WHERE Entry_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entryId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getDouble("average");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate average rating", e);
        }
        return 0.0;
    }

    private Rating mapResultSetToRating(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getInt("Rating_ID"));
        rating.setMediaEntryId(rs.getInt("Entry_ID"));
        rating.setUserId(rs.getInt("User_ID"));
        rating.setStarValue(rs.getInt("StarValue"));
        rating.setComment(rs.getString("Comment"));
        rating.setUpdatedAt(rs.getTimestamp("Updated_At"));
        rating.setConfirmed(rs.getBoolean("Is_Confirmed"));
        return rating;
    }
}