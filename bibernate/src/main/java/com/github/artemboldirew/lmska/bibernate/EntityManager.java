package com.github.artemboldirew.lmska.bibernate;

import java.util.List;
import java.util.Map;

public interface EntityManager extends AutoCloseable {
    <T> T find(Class<T> type, Object id);
    void persist(Object entity);
    void remove(Object entity);
    void flush();


    List<Map<String, Object>> query(String sql, Object... params);

    @Override
    void close();
}
