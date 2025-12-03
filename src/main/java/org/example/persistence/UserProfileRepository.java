package org.example.persistence;

import org.example.domain.UserProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserProfileRepository extends BaseRepository<UserProfile, Integer> {
    public UserProfileRepository(Connection connection) {
        super(connection);
    }

    @Override
    public void save(UserProfile userProfile) {
        String sql = "INSERT INTO UserProfiles (Profile_User_ID, Email, FavoriteGenre) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userProfile.getUserId());
            statement.setString(2, userProfile.getEmail());
            statement.setString(3, userProfile.getFavoriteGenre());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserProfile> findById(Integer profileId) {
        String sql = "SELECT * FROM UserProfiles WHERE Profile_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, profileId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                UserProfile userProfile = UserProfile.builder()
                        .id(rs.getInt("Profile_ID"))
                        .userId(rs.getInt("Profile_User_ID"))
                        .email(rs.getString("Email"))
                        .favoriteGenre(rs.getString("FavoriteGenre"))
                        .build();
                return Optional.of(userProfile);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<UserProfile> findByUserId(Integer userId) {
        String sql = "SELECT * FROM UserProfiles WHERE Profile_User_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                UserProfile userProfile = UserProfile.builder()
                        .id(rs.getInt("Profile_ID"))
                        .userId(rs.getInt("Profile_User_ID"))
                        .email(rs.getString("Email"))
                        .favoriteGenre(rs.getString("FavoriteGenre"))
                        .build();
                return Optional.of(userProfile);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<UserProfile> findByUsername(String username) {
        String sql = "SELECT up.* FROM UserProfiles up " +
                "LEFT JOIN Users u ON up.Profile_User_ID = u.User_ID " +
                "WHERE u.Username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                UserProfile userProfile = UserProfile.builder()
                        .userId(rs.getInt("Profile_User_ID"))
                        .email(rs.getString("Email"))
                        .favoriteGenre(rs.getString("FavoriteGenre"))
                        .build();
                return Optional.of(userProfile);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserProfile> findAll() {
        String sql = "SELECT * FROM UserProfiles";
        List<UserProfile> userProfiles = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                UserProfile userProfile = UserProfile.builder()
                        .userId(rs.getInt("Profile_User_ID"))
                        .email(rs.getString("Email"))
                        .favoriteGenre(rs.getString("FavoriteGenre"))
                        .build();
                userProfiles.add(userProfile);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return userProfiles;
    }

    @Override
    public void deleteById(Integer integer) {
        String sql = "DELETE FROM UserProfiles WHERE Profile_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, integer);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM UserProfiles";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(UserProfile userProfile) {
        String sql = "UPDATE UserProfiles SET Email = ?, FavoriteGenre = ? WHERE Profile_User_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userProfile.getEmail());
            statement.setString(2, userProfile.getFavoriteGenre());
            statement.setInt(3, userProfile.getUserId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateById(UserProfile userProfile) {
        String sql = "UPDATE UserProfiles SET Email = ?, FavoriteGenre = ? WHERE Profile_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userProfile.getEmail());
            statement.setString(2, userProfile.getFavoriteGenre());
            statement.setInt(3, userProfile.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}