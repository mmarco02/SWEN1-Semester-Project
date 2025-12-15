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
        String sql = "INSERT INTO MediaRatings (Entry_ID, User_ID, Score) " +
                "VALUES (?, ?, ?) RETURNING Rating_ID, Created_At, Updated_At";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, entity.getMediaEntryId());
            pstmt.setInt(2, entity.getUserId());
            pstmt.setDouble(3, entity.getStarValue());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                entity.setId(rs.getInt("Rating_ID"));
                entity.setTimeStamp(rs.getTimestamp("Created_At").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save rating", e);
        }
    }

    public void update(Rating entity) {
        String sql = "UPDATE MediaRatings SET Score = ?, Updated_At = CURRENT_TIMESTAMP " +
                "WHERE Rating_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, entity.getStarValue());
            pstmt.setInt(2, entity.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update rating", e);
        }
    }

    @Override
    public Optional<Rating> findById(Integer id) {
        String sql = "SELECT Rating_ID, Entry_ID, User_ID, Score, Created_At, Updated_At " +
                "FROM MediaRatings WHERE Rating_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRating(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find rating by id", e);
        }
        return Optional.empty();
    }

    public Optional<Rating> findByEntryAndUser(int entryId, int userId) {
        String sql = "SELECT Rating_ID, Entry_ID, User_ID, Score, Created_At, Updated_At " +
                "FROM MediaRatings WHERE Entry_ID = ? AND User_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

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
        String sql = "SELECT Rating_ID, Entry_ID, User_ID, Score, Created_At, Updated_At " +
                "FROM MediaRatings ORDER BY Created_At DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
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
        String sql = "SELECT Rating_ID, Entry_ID, User_ID, Score, Created_At, Updated_At " +
                "FROM MediaRatings WHERE Entry_ID = ? ORDER BY Created_At DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            ResultSet rs = pstmt.executeQuery();
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
        String sql = "SELECT Rating_ID, Entry_ID, User_ID, Score, Created_At, Updated_At " +
                "FROM MediaRatings WHERE User_ID = ? ORDER BY Created_At DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
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
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete rating", e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM MediaRatings";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all ratings", e);
        }
    }

    public double calculateAverageRating(int entryId) {
        String sql = "SELECT AVG(Score) as average FROM MediaRatings WHERE Entry_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            ResultSet rs = pstmt.executeQuery();
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
        rating.setStarValue(rs.getInt("Score"));
        rating.setTimeStamp(rs.getTimestamp("Created_At").toLocalDateTime());
        return rating;
    }
}