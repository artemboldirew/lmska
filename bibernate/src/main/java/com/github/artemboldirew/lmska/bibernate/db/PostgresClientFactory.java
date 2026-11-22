package com.github.artemboldirew.lmska.bibernate.db;

/**
 * Создаёт отдельное подключение PostgreSQL для каждого менеджера
 */
public final class PostgresClientFactory implements DbClientFactory {
    private final String url;
    private final String username;
    private final String password;

    public PostgresClientFactory(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public DbClient open() {
        return new PostgresClient(url, username, password);
    }
}
