package org.mrp.persistence.implemenatations;

import org.mrp.domain.MediaEntry;
import org.mrp.domain.MediaType;
import org.mrp.persistence.BaseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MediaEntryRepository extends BaseRepository<MediaEntry, Integer> {
    private final MediaGenreRepository mediaGenreRepository;

    public MediaEntryRepository(Connection connection) {
        super(connection);
        this.mediaGenreRepository = new MediaGenreRepository(connection);
    }

    @Override
    public void save(MediaEntry entity) {
        String sql = """
                INSERT INTO MediaEntries (Title, Description, MediaType, ReleaseYear, Age, Created_By_User_ID)
                VALUES (?, ?, ?, ?, ?, ?) RETURNING Entry_ID, Created_At, Updated_At, AverageRating
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getTitle());
            statement.setString(2, entity.getDescription());
            statement.setString(3, entity.getMediaType().name());
            statement.setInt(4, entity.getReleaseYear());
            statement.setInt(5, entity.getAge());
            statement.setInt(6, entity.getCreatedByUserId());

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int entryId = rs.getInt("Entry_ID");
                entity.setId(entryId);
                // save genres after getting the entry ID
                if (entity.getGenres() != null && !entity.getGenres().isEmpty()) {
                    mediaGenreRepository.saveGenresForEntry(entryId, entity.getGenres());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save media entry", e);
        }
    }

    public void update(MediaEntry entity) {
        String sql = """
                UPDATE MediaEntries SET Title = ?, Description = ?, MediaType = ?,
                ReleaseYear = ?, Age = ?, Updated_At = CURRENT_TIMESTAMP
                WHERE Entry_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getTitle());
            statement.setString(2, entity.getDescription());
            statement.setString(3, entity.getMediaType().name());
            statement.setInt(4, entity.getReleaseYear());
            statement.setInt(5, entity.getAge());
            statement.setInt(6, entity.getId());

            statement.executeUpdate();
            // update genres
            if (entity.getGenres() != null) {
                mediaGenreRepository.updateGenresForEntry(entity.getId(), entity.getGenres());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update media entry", e);
        }
    }

    @Override
    public Optional<MediaEntry> findById(Integer id) {
        String sql = """
                SELECT Entry_ID, Title, Description, MediaType, ReleaseYear, Age,
                AverageRating, Created_By_User_ID, Created_At, Updated_At
                FROM MediaEntries WHERE Entry_ID = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                MediaEntry entry = mapResultSetToMediaEntry(rs);
                // load genres for this entry
                List<String> genres = mediaGenreRepository.findGenresByEntryId(id);
                entry.setGenres(genres);

                return Optional.of(entry);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find media entry by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<MediaEntry> findAll() {
        List<MediaEntry> entries = new ArrayList<>();
        String sql = """
                SELECT Entry_ID, Title, Description, MediaType, ReleaseYear, Age,
                AverageRating, Created_By_User_ID, Created_At, Updated_At
                FROM MediaEntries ORDER BY Created_At DESC
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                MediaEntry entry = mapResultSetToMediaEntry(rs);
                // load genres for each entry
                List<String> genres = mediaGenreRepository.findGenresByEntryId(entry.getId());
                entry.setGenres(genres);

                entries.add(entry);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all media entries", e);
        }
        return entries;
    }

    public List<MediaEntry> findByUserId(int userId) {
        List<MediaEntry> entries = new ArrayList<>();
        String sql = """
                SELECT Entry_ID, Title, Description, MediaType, ReleaseYear, Age,
                AverageRating, Created_By_User_ID, Created_At, Updated_At
                FROM MediaEntries WHERE Created_By_User_ID = ? ORDER BY Created_At DESC
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                MediaEntry entry = mapResultSetToMediaEntry(rs);
                // load genres for each entry
                List<String> genres = mediaGenreRepository.findGenresByEntryId(entry.getId());
                entry.setGenres(genres);

                entries.add(entry);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find media entries by user id", e);
        }
        return entries;
    }

    @Override
    public void deleteById(Integer id) {
        // first delete genres due to foreign keys
        mediaGenreRepository.deleteGenresForEntry(id);

        String sql = "DELETE FROM MediaEntries WHERE Entry_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete media entry", e);
        }
    }

    @Override
    public void deleteAll() {
        // delete all genres first becuase of foreign keys
        String deleteGenresSql = "DELETE FROM MediaGenres";
        String deleteEntriesSql = "DELETE FROM MediaEntries";

        try {
            try (PreparedStatement statement = connection.prepareStatement(deleteGenresSql)) {
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(deleteEntriesSql)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all media entries and genres", e);
        }
    }

    public void updateAverageRating(int entryId, double averageRating) {
        String sql = "UPDATE MediaEntries SET AverageRating = ? WHERE Entry_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, averageRating);
            statement.setInt(2, entryId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update average rating", e);
        }
    }

    public List<MediaEntry> findByGenre(String genre) {
        List<MediaEntry> entries = new ArrayList<>();
        String sql = """
            SELECT DISTINCT e.Entry_ID, e.Title, e.Description, e.MediaType, e.ReleaseYear,
                            e.Age, e.AverageRating, e.Created_By_User_ID, e.Created_At, e.Updated_At
                            FROM MediaEntries e JOIN MediaGenres g 
                                ON e.Entry_ID = g.Entry_ID
                                WHERE g.Genre ILIKE ?
                                ORDER BY e.Created_At DESC
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + genre + "%");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                MediaEntry entry = mapResultSetToMediaEntry(rs);

                // load genres for each entry
                List<String> genres = mediaGenreRepository.findGenresByEntryId(entry.getId());
                entry.setGenres(genres);

                entries.add(entry);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find media entries by genre", e);
        }
        return entries;
    }

    public Optional<MediaEntry> findByRatingId(int ratingId) {
        String sql = """
        SELECT e.Entry_ID, e.Title, e.Description, e.MediaType, e.ReleaseYear,
               e.Age, e.AverageRating, e.Created_By_User_ID, e.Created_At, e.Updated_At
        FROM MediaEntries e
        JOIN MediaRatings r ON e.Entry_ID = r.Entry_ID
        WHERE r.Rating_ID = ?
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ratingId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                MediaEntry entry = mapResultSetToMediaEntry(rs);
                List<String> genres = mediaGenreRepository.findGenresByEntryId(entry.getId());
                entry.setGenres(genres);

                return Optional.of(entry);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find media entry", e);
        }

        return Optional.empty();
    }

    private MediaEntry mapResultSetToMediaEntry(ResultSet rs) throws SQLException {
        MediaEntry entry = new MediaEntry();
        entry.setId(rs.getInt("Entry_ID"));
        entry.setTitle(rs.getString("Title"));
        entry.setDescription(rs.getString("Description"));
        entry.setMediaType(MediaType.valueOf(rs.getString("MediaType")));
        entry.setReleaseYear(rs.getInt("ReleaseYear"));
        entry.setAge(rs.getInt("Age"));
        entry.setAverageRating(rs.getDouble("AverageRating"));
        entry.setCreatedByUserId(rs.getInt("Created_By_User_ID"));
        return entry;
    }
}