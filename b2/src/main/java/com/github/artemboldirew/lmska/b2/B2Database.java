package com.github.artemboldirew.lmska.b2;

import java.math.BigDecimal;
import java.util.*;
import com.github.artemboldirew.lmska.b2.model.*;

public class B2Database implements Database {
    private final Map<String, Table> tables = new LinkedHashMap<>();
    private final QueryExecutor executor = new QueryExecutor(this);


    /**
     * Разбирает SQL и выполняет нужную команду
     */
    @Override
    public synchronized SqlResult execute(String sql, Object... parameters) {
        Statement command = new SqlParser().parse(sql, parameters);
        return switch (command) {
            case Select select -> SqlResult.selected(executor.execute(select));
            case CreateTable create -> {
                createTable(create.table(), create.columns(), create.primaryKey());
                yield SqlResult.changed(0);
            }
            case Insert insert -> SqlResult.changed(insertRows(insert));
            case Update update -> SqlResult.changed(updateWhere(update));
            case Delete delete -> SqlResult.changed(deleteWhere(delete));
        };
    }

    /**
     * Сначала проверяет все строки, затем добавляет их в таблицу
     * При ошибке ничего не записывает
     */
    private int insertRows(Insert command) {

        Table table = require(command.table());
        List<Map<String, Object>> pending = new ArrayList<>();
        java.util.Set<Object> newKeys = new java.util.HashSet<>();
        for (List<Expr> values : command.rows()) {
            if (values.size() != command.columns().size())
                throw new IllegalArgumentException("INSERT column count mismatch");
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 0; i < values.size(); i++) {
                String col = command.columns().get(i);
                if (row.containsKey(col)) throw new IllegalArgumentException("Duplicate INSERT column: " + col);
                if (!(values.get(i) instanceof Literal literal))
                    throw new UnsupportedOperationException("INSERT supports only literal values");
                row.put(col, literal.value());
            }
            checkColumns(table, row);
            if (table.primaryKey != null) {
                Object id = row.get(table.primaryKey);
                if (id == null) throw new IllegalArgumentException("NULL primary key: " + table.primaryKey);
                Object key = normalizeKey(id);
                if (table.rows.containsKey(key) || !newKeys.add(key))
                    throw new IllegalArgumentException("Duplicate primary key: " + id);
            }
            pending.add(row);
        }
        for (Map<String, Object> row : pending) insert(command.table(), row);
        return pending.size();
    }

    /**
     * Добавляет строку
     * Пустые столбцы заполняет null, а при отсутствии ключа выдаёт номер
     */
    private void insert(String table, Map<String, Object> values) {
        Table t = require(table);
        checkColumns(t, values);
        Map<String, Object> row = new LinkedHashMap<>();
        for (String column : t.columns) row.put(column, values.get(column));
        Object key;
        if (t.primaryKey != null) {
            Object id = row.get(t.primaryKey);
            if (id == null) throw new IllegalArgumentException("NULL primary key: " + t.primaryKey);
            key = normalizeKey(id);
            if (t.rows.containsKey(key)) throw new IllegalArgumentException("Duplicate primary key: " + id);
        } else {
            key = ++t.nextRowId;
        }
        t.rows.put(key, row);
    }

    /**
     * Проверяет названия столбцов и первичный ключ, затем создаёт таблицу
     */
    private void createTable(String name, List<String> columns, String primaryKey) {
        if (tables.containsKey(name)) throw new IllegalArgumentException("Table already exists: " + name);
        if (columns.isEmpty() || new LinkedHashSet<>(columns).size() != columns.size())
            throw new IllegalArgumentException("Empty or duplicate column names");
        String pk = primaryKey;
        if (pk != null && !columns.contains(pk)) throw new IllegalArgumentException("Unknown primary key: " + pk);
        tables.put(name, new Table(List.copyOf(columns), pk));
    }

    /**
     * Обновляет строки, подходящие под условие
     * Первичный ключ менять нельзя
     */
    private int updateWhere(Update query) {
        Table t = require(query.table());
        checkColumns(t, query.changes());
        if (t.primaryKey != null && query.changes().containsKey(t.primaryKey))
            throw new IllegalArgumentException("Primary key updates are unsupported");
        Map<String, Object> changes = new LinkedHashMap<>();
        query.changes().forEach((col, expr) -> {
            if (!(expr instanceof Literal literal))
                throw new UnsupportedOperationException("UPDATE SET supports only literal values");
            changes.put(col, literal.value());
        });
        int count = 0;
        for (Map<String, Object> row : t.rows.values()) {
            if (!executor.matches(query.where(), qualified(query.table(), row))) continue;
            row.putAll(changes);
            count++;
        }
        return count;
    }

    private int deleteWhere(Delete query) {
        Table t = require(query.table());
        int before = t.rows.size();
        t.rows.values().removeIf(row -> executor.matches(
                query.where(), qualified(query.table(), row)));
        return before - t.rows.size();
    }

    private static Map<String, Object> qualified(String table, Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>();
        row.forEach((column, value) -> result.put(table + "." + column, value));
        return result;
    }


    TableSnapshot snapshot(String name) {
        Table t = require(name);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : t.rows.values()) rows.add(new LinkedHashMap<>(row));
        return new TableSnapshot(t.columns, rows);
    }

    private Table require(String table) {
        Table result = tables.get(table);
        if (result == null) throw new IllegalArgumentException("Unknown table: " + table);
        return result;
    }

    private static void checkColumns(Table table, Map<String, ?> values) {
        for (String column : values.keySet()) {
            if (!table.columns.contains(column)) throw new IllegalArgumentException("Unknown column: " + column);
        }
    }

    private static Object normalizeKey(Object value) {
        if (value instanceof Number number)
            return new BigDecimal(number.toString()).stripTrailingZeros();
        return value;
    }
}
