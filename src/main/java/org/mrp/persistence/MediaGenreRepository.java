package org.mrp.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaGenreRepository {
    private final Connection connection;

    public MediaGenreRepository(Connection connection) {
        this.connection = connection;
    }

    public void saveGenresForEntry(int entryId, List<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO MediaGenres (Entry_ID, Genre) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String genre : genres) {
                pstmt.setInt(1, entryId);
                pstmt.setString(2, genre);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save genres for entry " + entryId, e);
        }
    }

    public List<String> findGenresByEntryId(int entryId) {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT Genre FROM MediaGenres WHERE Entry_ID = ? ORDER BY Genre";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                genres.add(rs.getString("Genre"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find genres for entry " + entryId, e);
        }
        return genres;
    }

    public void deleteGenresForEntry(int entryId) {
        String sql = "DELETE FROM MediaGenres WHERE Entry_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete genres for entry " + entryId, e);
        }
    }

    public void updateGenresForEntry(int entryId, List<String> newGenres) {
        // first delete existing genres
        deleteGenresForEntry(entryId);
        saveGenresForEntry(entryId, newGenres);
    }
}