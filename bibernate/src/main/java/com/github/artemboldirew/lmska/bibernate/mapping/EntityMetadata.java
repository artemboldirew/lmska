package com.github.artemboldirew.lmska.bibernate.mapping;

import com.github.artemboldirew.lmska.bibernate.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.*;

/**
 * Хранит данные о полях, ключах и связях сущности
 */
public final class EntityMetadata {
    private final Class<?> type;
    private final String table;
    private final Field idField;
    private final Map<Field, String> columns = new LinkedHashMap<>();
    private final Map<Field, String> manyToOne = new LinkedHashMap<>();
    private final Map<Field, String> oneToMany = new LinkedHashMap<>();
    private final Map<Field, Field> relatedIds = new LinkedHashMap<>();
    private final Map<Field, Class<?>> collectionTypes = new LinkedHashMap<>();

    /**
     * Проверяет класс сущности и собирает данные о его столбцах и связях
     */
    public EntityMetadata(Class<?> type) {
        if (!type.isAnnotationPresent(Entity.class)) throw new IllegalArgumentException("Missing @Entity: " + type.getName());
        this.type = type;
        Table annotation = type.getAnnotation(Table.class);
        table = identifier(annotation != null && !annotation.name().isBlank() ? annotation.name() : type.getSimpleName().toLowerCase(Locale.ROOT));

        Field key = null;
        for (Field field : type.getDeclaredFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) continue;
            if (Modifier.isFinal(field.getModifiers())) throw new IllegalArgumentException("Final fields are unsupported: " + field);
            field.setAccessible(true);

            ManyToOne many = field.getAnnotation(ManyToOne.class);
            OneToMany one = field.getAnnotation(OneToMany.class);
            if (many != null && one != null) throw new IllegalArgumentException("Conflicting relationship annotations: " + field);

            if (one != null) {
                if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class)) throw new IllegalArgumentException("Invalid @OneToMany field: " + field);
                if (field.getType() != List.class || !(field.getGenericType() instanceof ParameterizedType generic)
                        || generic.getActualTypeArguments().length != 1 || !(generic.getActualTypeArguments()[0] instanceof Class<?> child)) {
                    throw new IllegalArgumentException("@OneToMany requires List<SomeEntity>: " + field);
                }
                if (!child.isAnnotationPresent(Entity.class)) throw new IllegalArgumentException("@OneToMany target is not @Entity: " + child.getName());
                try {
                    Field inverse = child.getDeclaredField(one.mappedBy());
                    if (!inverse.isAnnotationPresent(ManyToOne.class) || inverse.getType() != type) {
                        throw new IllegalArgumentException("mappedBy must refer to @ManyToOne of " + type.getName());
                    }
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException("Unknown mappedBy field: " + one.mappedBy(), e);
                }
                oneToMany.put(field, one.mappedBy());
                collectionTypes.put(field, child);
                continue;
            }

            if (many != null) {
                if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class)) throw new IllegalArgumentException("Invalid @ManyToOne field: " + field);
                if (!field.getType().isAnnotationPresent(Entity.class)) throw new IllegalArgumentException("@ManyToOne target is not @Entity: " + field);
                Field relatedId = findIdField(field.getType());
                if (!supported(relatedId.getType())) throw new IllegalArgumentException("Unsupported related @Id type: " + relatedId);
                relatedId.setAccessible(true);
                String name = identifier(many.joinColumn().isBlank() ? field.getName() + "_id" : many.joinColumn());
                checkUnique(name);
                manyToOne.put(field, name);
                relatedIds.put(field, relatedId);
                continue;
            }

            if (!supported(field.getType())) throw new IllegalArgumentException("Unsupported field type: " + field);
            Column column = field.getAnnotation(Column.class);
            String name = identifier(column != null && !column.name().isBlank() ? column.name() : field.getName());
            checkUnique(name);
            columns.put(field, name);
            if (field.isAnnotationPresent(Id.class)) {
                if (key != null) throw new IllegalArgumentException("Multiple @Id in " + type.getName());
                key = field;
            }
        }
        if (key == null) throw new IllegalArgumentException("Missing @Id in " + type.getName());
        idField = key;
        try {
            type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Entity requires no-arg constructor: " + type.getName());
        }
    }

    public String table() {
        return table;
    }
    public String idColumn() {
        return columns.get(idField);
    }
    public Class<?> idType() {
        return idField.getType();
    }
    public Class<?> type() {
        return type;
    }
    public Map<Field, String> columns() {
        return Collections.unmodifiableMap(columns);
    }
    public Map<Field, String> manyToOne() {
        return Collections.unmodifiableMap(manyToOne);
    }
    public Map<Field, String> oneToMany() {
        return Collections.unmodifiableMap(oneToMany);
    }
    public Class<?> collectionType(Field field) {
        return collectionTypes.get(field);
    }

    public List<String> selectColumns() {
        List<String> result = new ArrayList<>(columns.values());
        result.addAll(manyToOne.values());
        return result;
    }

    public Object id(Object entity) {
        try {
            return idField.get(entity);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Получает ID связанного объекта
     * Для пустой ссылки возвращает null, а для объекта без ID — ошибку
     */
    public Object relatedId(Field relation, Object referencedEntity) {
        if (referencedEntity == null) return null;
        Field key = relatedIds.get(relation);
        try {
            Object id = key.get(referencedEntity);
            if (id == null) throw new IllegalArgumentException("Referenced entity has no @Id: " + relation);
            return id;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Составляет CREATE TABLE по полям сущности и её связям
     */
    public String createTableSql() {
        StringJoiner defs = new StringJoiner(", ");
        for (var column : columns.entrySet()) {
            defs.add(column.getValue() + " " + sqlType(column.getKey().getType()) + (column.getKey() == idField ? " PRIMARY KEY" : ""));
        }
        for (var relation : manyToOne.entrySet()) {
            defs.add(relation.getValue() + " " + sqlType(relatedIds.get(relation.getKey()).getType()));
        }
        return "CREATE TABLE " + table + " (" + defs + ")";
    }

    private void checkUnique(String name) {
        if (columns.containsValue(name) || manyToOne.containsValue(name)) throw new IllegalArgumentException("Duplicate column: " + name);
    }

    /**
     * Ищет поле с Id
     * В сущности должно быть ровно одно такое поле
     */
    private static Field findIdField(Class<?> target) {
        Field found = null;
        for (Field field : target.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Id.class)) continue;
            if (found != null) throw new IllegalArgumentException("Multiple @Id in " + target.getName());
            found = field;
        }
        if (found == null) throw new IllegalArgumentException("Missing @Id in " + target.getName());
        return found;
    }

    private static String identifier(String name) {
        if (!name.matches("[A-Za-z_][A-Za-z0-9_]*")) throw new IllegalArgumentException("Invalid SQL identifier: " + name);
        return name.toLowerCase(Locale.ROOT);
    }

    private static boolean supported(Class<?> type) {
        return type == UUID.class || type == String.class || type == boolean.class || type == Boolean.class
                || type == int.class || type == Integer.class || type == long.class || type == Long.class
                || type == double.class || type == Double.class || type == float.class || type == Float.class || type == BigDecimal.class;
    }

    /**
     * Подбирает SQL-тип по Java-типу
     * Для строк используется VARCHAR(255)
     */
    private static String sqlType(Class<?> type) {
        if (type == UUID.class) return "UUID";
        if (type == String.class) return "VARCHAR(255)";
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (type == int.class || type == Integer.class) return "INT";
        if (type == long.class || type == Long.class) return "BIGINT";
        if (type == double.class || type == Double.class) return "FLOAT8";
        if (type == float.class || type == Float.class) return "REAL";
        if (type == BigDecimal.class) return "DECIMAL(20,6)";
        throw new IllegalArgumentException("Unsupported SQL field type: " + type.getName());
    }
}
