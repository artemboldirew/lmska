package com.github.artemboldirew.lmska.bibernate;

import com.github.artemboldirew.lmska.bibernate.context.EntityEntry;
import com.github.artemboldirew.lmska.bibernate.context.EntityKey;
import com.github.artemboldirew.lmska.bibernate.context.PersistenceContext;
import com.github.artemboldirew.lmska.bibernate.db.DbClient;
import com.github.artemboldirew.lmska.bibernate.mapping.EntityMapper;
import com.github.artemboldirew.lmska.bibernate.mapping.EntityMetadata;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public final class EntityManagerImpl implements EntityManager {
    private final EntityManagerFactory factory;
    private final DbClient db;
    private final EntityMapper mapper = new EntityMapper();
    private final PersistenceContext context = new PersistenceContext();
    private boolean closed;

    EntityManagerImpl(EntityManagerFactory factory, DbClient db) {
        this.factory = factory;
        this.db = db;
    }

    /**
     * Ищет сущность в контексте или базе
     * Сохраняет её в контексте до загрузки связей, чтобы избежать зацикливания
     */
    @Override
    public <T> T find(Class<T> type, Object id) {
        checkOpen();
        EntityMetadata meta = factory.metadata(type);
        Object key = mapper.convert(id, meta.idType());
        EntityEntry cached = context.get(type, key);
        if (cached != null) return cached.removed() ? null : type.cast(cached.entity());

        String columns = String.join(", ", meta.selectColumns());
        List<Map<String, Object>> rows = db.query("SELECT " + columns + " FROM " + meta.table() + " WHERE " + meta.idColumn() + " = ?", key);
        if (rows.isEmpty()) return null;
        Map<String, Object> row = rows.getFirst();
        T entity = mapper.fromRow(type, meta, row);


        EntityEntry entry = new EntityEntry(entity, meta, new LinkedHashMap<>(row), false);
        context.put(type, meta.id(entity), entry);
        loadRelations(entity, meta, row);
        entry.synchronizedWith(mapper.values(entity, meta));
        return entity;
    }

    /**
     * Загружает связанные объекты и списки
     * Если запись по ссылке не найдена, выдаёт ошибку
     */
    private void loadRelations(Object entity, EntityMetadata meta, Map<String, Object> row) {
        for (var relation : meta.manyToOne().entrySet()) {
            Object id = row.get(relation.getValue());
            Object related = id == null ? null : find(relation.getKey().getType(), id);
            if (id != null && related == null) throw new IllegalStateException("Broken reference " + meta.table() + "." + relation.getValue() + " = " + id);
            set(relation.getKey(), entity, related);
        }
        for (var relation : meta.oneToMany().entrySet()) {
            Class<?> childType = meta.collectionType(relation.getKey());
            EntityMetadata child = factory.metadata(childType);
            try {
                Field ownerField = childType.getDeclaredField(relation.getValue());
                String joinColumn = child.manyToOne().get(ownerField);
                List<Map<String, Object>> ids = db.query("SELECT " + child.idColumn() + " FROM " + child.table() + " WHERE " + joinColumn + " = ?", meta.id(entity));
                List<Object> children = new ArrayList<>();
                for (Map<String, Object> result : ids) children.add(find(childType, result.get(child.idColumn())));
                set(relation.getKey(), entity, children);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static void set(Field field, Object entity, Object value) {
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Добавляет новую сущность в контекст
     * Запись в базу произойдёт при вызове flush
     */
    @Override
    public void persist(Object entity) {
        checkOpen();
        EntityMetadata meta = factory.metadata(entity.getClass());
        Object id = requireId(meta, entity);
        EntityEntry existing = context.get(meta.type(), id);
        if (existing != null) {
            if (existing.entity() != entity || existing.removed()) throw new IllegalStateException("ID is already managed: " + id);
            return;
        }
        context.put(meta.type(), id, new EntityEntry(entity, meta, null, true));
    }

    @Override
    public void remove(Object entity) {
        checkOpen();
        EntityMetadata meta = factory.metadata(entity.getClass());
        EntityEntry entry = context.get(meta.type(), requireId(meta, entity));
        if (entry == null || entry.entity() != entity) throw new IllegalArgumentException("remove() requires a managed entity");
        entry.remove();
    }

    /**
     * Сохраняет добавления, изменения и удаления
     * У существующих записей обновляет только изменённые поля
     */
    @Override
    public void flush() {
        checkOpen();
        for (var managed : context.entries()) {
            EntityKey key = managed.getKey();
            EntityEntry entry = managed.getValue();
            EntityMetadata meta = entry.metadata();
            Map<String, Object> values = mapper.values(entry.entity(), meta);
            if (!Objects.equals(meta.id(entry.entity()), key.id())) throw new IllegalStateException("Changing @Id is not supported: " + meta.type().getName());

            if (entry.removed()) {
                if (!entry.fresh()) {
                    int changed = db.update("DELETE FROM " + meta.table() + " WHERE " + meta.idColumn() + " = ?", key.id());
                    if (changed != 1) throw new IllegalStateException("Could not delete entity with id=" + key.id());
                }
                context.delete(key.type(), key.id());
            } else if (entry.fresh()) {
                String columns = String.join(", ", values.keySet());
                String placeholders = String.join(", ", Collections.nCopies(values.size(), "?"));
                db.update("INSERT INTO " + meta.table() + " (" + columns + ") VALUES (" + placeholders + ")", values.values().toArray());
                entry.synchronizedWith(values);
            } else if (!values.equals(entry.snapshot())) {
                List<String> changed = values.keySet().stream().filter(col -> !col.equals(meta.idColumn()))
                        .filter(col -> !Objects.equals(values.get(col), entry.snapshot().get(col))).toList();
                if (!changed.isEmpty()) {
                    String setters = changed.stream().map(col -> col + " = ?").collect(Collectors.joining(", "));
                    List<Object> params = new ArrayList<>();
                    changed.forEach(col -> params.add(values.get(col)));
                    params.add(key.id());
                    int affected = db.update("UPDATE " + meta.table() + " SET " + setters + " WHERE " + meta.idColumn() + " = ?", params.toArray());
                    if (affected != 1) throw new IllegalStateException("Could not update entity with id=" + key.id());
                }
                entry.synchronizedWith(values);
            }
        }
    }

    @Override
    public List<Map<String, Object>> query(String sql, Object... params) {
        checkOpen();
        return db.query(sql, params);
    }

    private Object requireId(EntityMetadata meta, Object entity) {
        Object id = meta.id(entity);
        if (id == null) throw new IllegalArgumentException("@Id must be set: " + meta.type().getName());
        return id;
    }

    private void checkOpen() {
        if (closed) throw new IllegalStateException("EntityManager is closed");
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            context.clear();
            db.close();
        }
    }
}
