package org.mrp.persistence.implemenatations;

import org.mrp.domain.UserToken;
import org.mrp.persistence.BaseRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TokenRepository extends BaseRepository<UserToken, String> {

    public TokenRepository(Connection connection) {
        super(connection);
    }

    @Override
    public void save(UserToken userToken) {
        String sql = "INSERT INTO UserTokens (Token, User_ID, Created_At) VALUES (?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, userToken.token());
            statement.setInt(2, userToken.userId());
            statement.setTimestamp(3, userToken.createdAt());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserToken> findById(String tokenString) {
        String sql = "SELECT Token, User_ID, Created_At FROM UserTokens WHERE Token = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tokenString);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                UserToken userToken = UserToken.builder()
                        .token(rs.getString("Token"))
                        .userId(rs.getInt("User_ID"))
                        .createdAt(rs.getTimestamp("Created_At"))
                        .build();
                return Optional.of(userToken);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserToken> findAll() {
        String sql = "SELECT Token, User_ID, Created_At FROM UserTokens";
        List<UserToken> tokens = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                UserToken userToken = UserToken.builder()
                        .token(rs.getString("Token"))
                        .userId(rs.getInt("User_ID"))
                        .createdAt(rs.getTimestamp("Created_At"))
                        .build();
                tokens.add(userToken);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tokens;
    }

    @Override
    public void deleteById(String token) {
        String sql = "DELETE FROM UserTokens WHERE Token = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM UserTokens";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteByUserId(Integer userId) {
        String sql = "DELETE FROM UserTokens WHERE User_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<UserToken> findByUserId(Integer userId) {
        String sql = "SELECT Token, User_ID, Created_At FROM UserTokens WHERE User_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            UserToken userToken = null;
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                userToken = UserToken.builder()
                        .token(rs.getString("Token"))
                        .userId(rs.getInt("User_ID"))
                        .createdAt(rs.getTimestamp("Created_At"))
                        .build();
                return Optional.of(userToken);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}