package org.example.persistence;

import org.example.domain.UserToken;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class TokenRepository extends BaseRepository<UserToken, String> {

    public TokenRepository(Connection connection) {
        super(connection);
    }

    @Override
    public void save(UserToken entity) {

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
