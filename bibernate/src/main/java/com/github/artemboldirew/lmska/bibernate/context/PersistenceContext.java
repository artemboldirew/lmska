package com.github.artemboldirew.lmska.bibernate.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PersistenceContext {
    private final Map<EntityKey, EntityEntry> entities = new LinkedHashMap<>();

    public EntityEntry get(Class<?> type, Object id) {
        return entities.get(new EntityKey(type, id));
    }
    public void put(Class<?> type, Object id, EntityEntry entry) {
        entities.put(new EntityKey(type, id), entry);
    }
    public void delete(Class<?> type, Object id) {
        entities.remove(new EntityKey(type, id));
    }
    public Collection<Map.Entry<EntityKey, EntityEntry>> entries() {
        return new ArrayList<>(entities.entrySet());
    }
    public void clear() {
        entities.clear();
    }
}
