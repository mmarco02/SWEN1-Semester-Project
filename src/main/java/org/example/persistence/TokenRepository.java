package org.example.persistence;

import org.example.domain.UserToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
            statement.setTimestamp(1, userToken.);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserToken> findById(String s) {
        return Optional.empty();
    }

    @Override
    public List<UserToken> findAll() {
        return List.of();
    }

    @Override
    public void deleteById(String s) {

    }

    @Override
    public void deleteAll() {

    }
}
