package com.github.artemboldirew.lmska.bibernate.context;

import com.github.artemboldirew.lmska.bibernate.mapping.EntityMetadata;

import java.util.Map;

public final class EntityEntry {
    private final Object entity;
    private final EntityMetadata metadata;
    private Map<String, Object> snapshot;
    private boolean fresh;
    private boolean removed;

    public EntityEntry(Object entity, EntityMetadata metadata, Map<String, Object> snapshot, boolean fresh) {
        this.entity = entity;
        this.metadata = metadata;
        this.snapshot = snapshot;
        this.fresh = fresh;
    }

    public Object entity() {
        return entity;
    }
    public EntityMetadata metadata() {
        return metadata;
    }
    public Map<String, Object> snapshot() {
        return snapshot;
    }
    public boolean fresh() {
        return fresh;
    }
    public boolean removed() {
        return removed;
    }
    public void remove() {
        removed = true;
    }
    public void synchronizedWith(Map<String, Object> values) {
        snapshot = values;
        fresh = false;
    }
}
