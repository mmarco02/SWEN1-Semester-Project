package org.example.persistence;

import org.example.domain.UserProfile;

import java.sql.Connection;

public abstract class BaseRepository<T, ID> implements Repository<T, ID> {
    protected final Connection connection;

    public BaseRepository(Connection connection) {
        this.connection = connection;
    }
}