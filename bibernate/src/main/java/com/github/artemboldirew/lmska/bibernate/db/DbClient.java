package com.github.artemboldirew.lmska.bibernate.db;

import java.util.List;
import java.util.Map;

/**
 * Общие методы для работы с B2 и PostgreSQL
 */
public interface DbClient extends AutoCloseable {
    List<Map<String, Object>> query(String sql, Object... params);
    int update(String sql, Object... params);
    @Override
    void close();
}
