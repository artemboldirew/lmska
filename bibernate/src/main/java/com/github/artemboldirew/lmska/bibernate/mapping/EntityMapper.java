package com.github.artemboldirew.lmska.bibernate.mapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Переносит значения между объектом и строкой таблицы
 * Связанные объекты загружает EntityManager
 */
public final class EntityMapper {
    public Map<String, Object> values(Object entity, EntityMetadata meta) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (var column : meta.columns().entrySet()) values.put(column.getValue(), read(column.getKey(), entity));
        for (var relation : meta.manyToOne().entrySet()) values.put(relation.getValue(), meta.relatedId(relation.getKey(), read(relation.getKey(), entity)));
        return values;
    }

    /**
     * Создаёт объект и заполняет его поля из строки таблицы
     * Связи здесь не загружаются
     */
    public <T> T fromRow(Class<T> type, EntityMetadata meta, Map<String, Object> row) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            T entity = constructor.newInstance();
            for (var column : meta.columns().entrySet()) {
                if (!row.containsKey(column.getValue())) throw new IllegalArgumentException("Column missing from result: " + column.getValue());
                column.getKey().set(entity, convert(row.get(column.getValue()), column.getKey().getType()));
            }
            return entity;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create " + type.getName(), e);
        }
    }

    /**
     * Приводит значение к типу поля
     * Проверяет null для примитивов и точность преобразования в целое число
     */
    public Object convert(Object value, Class<?> type) {
        if (value == null) {
            if (type.isPrimitive()) throw new IllegalArgumentException("NULL cannot be stored in primitive " + type);
            return null;
        }
        if (type.isInstance(value)) return value;
        if (type == UUID.class && value instanceof String text) return UUID.fromString(text);
        if (value instanceof Number n) {
            if (type == int.class || type == Integer.class) return new BigDecimal(n.toString()).intValueExact();
            if (type == long.class || type == Long.class) return new BigDecimal(n.toString()).longValueExact();
            if (type == float.class || type == Float.class) return n.floatValue();
            if (type == double.class || type == Double.class) return n.doubleValue();
            if (type == BigDecimal.class) return new BigDecimal(n.toString());
        }
        if (type == boolean.class && value instanceof Boolean) return value;
        throw new IllegalArgumentException("Cannot map " + value.getClass().getName() + " to " + type.getName());
    }

    private static Object read(Field field, Object entity) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
