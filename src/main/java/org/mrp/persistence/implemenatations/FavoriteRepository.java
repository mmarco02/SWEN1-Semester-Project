package org.mrp.persistence.implemenatations;

import org.mrp.domain.Favorite;
import org.mrp.persistence.BaseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FavoriteRepository extends BaseRepository<Favorite, Integer> {

    public FavoriteRepository(Connection connection) {
        super(connection);
    }

    @Override
    public void save(Favorite favorite) {
        String sql = """
            INSERT INTO FavoriteMedia (Entry_ID, User_ID)
            VALUES (?, ?) RETURNING Favorite_ID, Created_At
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, favorite.getEntryId());
            statement.setInt(2, favorite.getUserId());

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int favoriteId = rs.getInt("Favorite_ID");
                Timestamp createdAt = rs.getTimestamp("Created_At");
                favorite.setId(favoriteId);
                favorite.setCreatedAt(createdAt);
            } else {
                throw new RuntimeException("Failed to save favorite - no data returned");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save favorite", e);
        }
    }

    @Override
    public Optional<Favorite> findById(Integer favoriteId) {
        String sql = """
                SELECT Favorite_ID, Entry_ID, User_ID, Created_At
                FROM FavoriteMedia
                WHERE Favorite_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, favoriteId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToFavorite(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find favorite by id", e);
        }
        return Optional.empty();
    }

    public Optional<Favorite> findByUserAndEntry(int userId, int entryId) {
        String sql = """
                SELECT Favorite_ID, Entry_ID, User_ID, Created_At
                FROM FavoriteMedia
                WHERE User_ID = ? AND Entry_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, entryId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToFavorite(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find favorite by user and entry", e);
        }
        return Optional.empty();
    }

    public List<Favorite> findByUserId(int userId) {
        List<Favorite> favorites = new ArrayList<>();
        String sql = """
                SELECT Favorite_ID, Entry_ID, User_ID, Created_At
                FROM FavoriteMedia
                WHERE User_ID = ?
                ORDER BY Created_At DESC
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                favorites.add(mapResultSetToFavorite(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find favorites by user id", e);
        }
        return favorites;
    }

    public List<Favorite> findByEntryId(int entryId) {
        List<Favorite> favorites = new ArrayList<>();
        String sql = """
                SELECT Favorite_ID, Entry_ID, User_ID, Created_At
                FROM FavoriteMedia
                WHERE Entry_ID = ?
                ORDER BY Created_At DESC
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entryId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                favorites.add(mapResultSetToFavorite(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find favorites by entry id", e);
        }
        return favorites;
    }

    @Override
    public List<Favorite> findAll() {
        List<Favorite> favorites = new ArrayList<>();
        String sql = """
                SELECT Favorite_ID, Entry_ID, User_ID, Created_At
                FROM FavoriteMedia
                ORDER BY Created_At DESC
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                favorites.add(mapResultSetToFavorite(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all favorites", e);
        }
        return favorites;
    }

    @Override
    public void deleteById(Integer favoriteId) {
        String sql = "DELETE FROM FavoriteMedia WHERE Favorite_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, favoriteId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete favorite by id", e);
        }
    }

    public boolean deleteByUserAndEntry(int userId, int entryId) {
        String sql = "DELETE FROM FavoriteMedia WHERE User_ID = ? AND Entry_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, entryId);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete favorite by user and entry", e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM FavoriteMedia";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all favorites", e);
        }
    }

    private Favorite mapResultSetToFavorite(ResultSet rs) throws SQLException {
        return Favorite.builder()
                .id(rs.getInt("Favorite_ID"))
                .entryId(rs.getInt("Entry_ID"))
                .userId(rs.getInt("User_ID"))
                .createdAt(rs.getTimestamp("Created_At"))
                .build();
    }
}