package com.github.artemboldirew.lmska.bibernate.db;

import com.github.artemboldirew.lmska.b2.B2Database;

import java.util.List;
import java.util.Map;

public final class B2Client implements DbClient {
    private final B2Database database;

    public B2Client(B2Database database) {
        this.database = database;
    }

    @Override
    public List<Map<String, Object>> query(String sql, Object... params) {
        return database.execute(sql, params).rows();
    }
    @Override
    public int update(String sql, Object... params) {
        return database.execute(sql, params).affectedRows();
    }
    @Override
    public void close() {
    }
}
