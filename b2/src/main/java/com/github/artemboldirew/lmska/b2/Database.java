package com.github.artemboldirew.lmska.b2;

public interface Database extends AutoCloseable {
    SqlResult execute(String sql, Object... params);

    @Override
    default void close() {
    }
}