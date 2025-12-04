package org.mrp.persistence;

import java.sql.Connection;

public abstract class BaseRepository<T, ID> implements Repository<T, ID> {
    protected final Connection connection;

    public BaseRepository(Connection connection) {
        this.connection = connection;
    }
}