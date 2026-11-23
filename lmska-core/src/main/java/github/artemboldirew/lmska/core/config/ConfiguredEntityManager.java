package github.artemboldirew.lmska.core.config;

import com.github.artemboldirew.lmska.bibernate.EntityManager;
import com.github.artemboldirew.lmska.bibernate.EntityManagerFactory;
import com.github.artemboldirew.lmska.bring.Environment;
import com.github.artemboldirew.lmska.bring.annotation.Component;
import github.artemboldirew.lmska.core.entity.Category;
import github.artemboldirew.lmska.core.entity.Task;
import github.artemboldirew.lmska.core.entity.TaskAttempt;
import github.artemboldirew.lmska.core.entity.User;


import java.util.UUID;
import java.util.List;
import java.util.Map;

/**
 * Создаёт EntityManager с настройками из Bring
 */
@Component
public final class ConfiguredEntityManager implements EntityManager {
    private final EntityManagerFactory factory;
    private final EntityManager delegate;

    /**
     * Открывает базу и заранее создаёт таблицы B2 через поиск сущностей
     */
    public ConfiguredEntityManager(Environment environment) {
        String url = environment.getProperty("bibernate.db.url");
        if (url == null || url.isBlank()) throw new IllegalArgumentException("Missing bibernate.db.url");
        factory = EntityManagerFactory.create(url, environment.getProperty("bibernate.db.username"), environment.getProperty("bibernate.db.password"));
        EntityManager manager = factory.createEntityManager();
        try {

            manager.find(User.class, UUID.randomUUID());
            manager.find(Category.class, UUID.randomUUID());
            manager.find(Task.class, UUID.randomUUID());
            manager.find(TaskAttempt.class, UUID.randomUUID());
        } catch (RuntimeException e) {
            manager.close();
            factory.close();
            throw e;
        }
        delegate = manager;
    }

    @Override
    public <T> T find(Class<T> type, Object id) {
        return delegate.find(type, id);
    }
    @Override
    public void persist(Object entity) {
        delegate.persist(entity);
    }
    @Override
    public void remove(Object entity) {
        delegate.remove(entity);
    }
    @Override
    public void flush() {
        delegate.flush();
    }
    @Override
    public List<Map<String, Object>> query(String sql, Object... params) {
        return delegate.query(sql, params);
    }

    @Override
    public void close() {
        try {
            delegate.close();
        } finally {
            factory.close();
        }
    }
}
