package com.github.artemboldirew.lmska.bibernate;

import com.github.artemboldirew.lmska.bibernate.db.DbClientFactory;
import com.github.artemboldirew.lmska.bibernate.mapping.EntityMetadata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Создаёт менеджеры для одной базы данных
 * Настройки получает от приложения
 */
public final class EntityManagerFactory implements AutoCloseable {
    private final DbClientFactory clients;
    private final Map<Class<?>, EntityMetadata> metadata = new ConcurrentHashMap<>();
    private volatile boolean closed;

    public EntityManagerFactory(String url, String username, String password) {
        this.clients = DbClientFactory.forUrl(url, username, password);
    }

    public static EntityManagerFactory create(String url, String username, String password) {
        return new EntityManagerFactory(url, username, password);
    }

    public EntityManager createEntityManager() {
        if (closed) throw new IllegalStateException("EntityManagerFactory is closed");
        return new EntityManagerImpl(this, clients.open());
    }

    /**
     * Создаёт описание сущности при первом обращении
     * В B2 готовит таблицу, в PostgreSQL таблица уже должна быть
     */
    synchronized EntityMetadata metadata(Class<?> type) {
        if (closed) throw new IllegalStateException("EntityManagerFactory is closed");
        return metadata.computeIfAbsent(type, clazz -> {
            EntityMetadata meta = new EntityMetadata(clazz);
            clients.prepareTable(meta.createTableSql());
            return meta;
        });
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            clients.close();
        }
    }
}
