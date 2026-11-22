package com.github.artemboldirew.lmska.bibernate.db;

import com.github.artemboldirew.lmska.b2.B2Database;

/**
 * Все менеджеры этой фабрики используют общий экземпляр B2 в памяти
 */
public final class B2ClientFactory implements DbClientFactory {
    private final B2Client client = new B2Client(new B2Database());

    @Override
    public DbClient open() {
        return client;
    }

    @Override
    public void prepareTable(String createTableSql) {
        client.update(createTableSql);
    }
}
