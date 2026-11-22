package com.github.artemboldirew.lmska.bibernate.db;

import java.util.Objects;

/**
 * Выбирает B2 или PostgreSQL по адресу подключения
 */
public interface DbClientFactory extends AutoCloseable {
    DbClient open();


    default void prepareTable(String createTableSql) {
    }

    static DbClientFactory forUrl(String url, String username, String password) {
        Objects.requireNonNull(url, "Database URL is required");
        String address = url.trim();
        if (address.startsWith("b2:")) return new B2ClientFactory();
        if (address.startsWith("jdbc:postgresql:")) return new PostgresClientFactory(address, username, password);
        throw new IllegalArgumentException("Unsupported database URL: " + url);
    }

    @Override
    default void close() {
    }
}
