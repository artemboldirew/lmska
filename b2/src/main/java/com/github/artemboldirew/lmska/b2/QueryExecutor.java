package com.github.artemboldirew.lmska.b2;

import com.github.artemboldirew.lmska.b2.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class QueryExecutor {
    private final B2Database db;
    QueryExecutor(B2Database db) {
        this.db = db;
    }

    /**
     * Выполняет SELECT: соединяет таблицы, применяет фильтр, выбирает столбцы и сортирует строки
     */
    List<Map<String, Object>> execute(Select query) {
        List<TableRef> tables = new ArrayList<>();
        tables.add(query.from());
        List<Map<String, Object>> rows = rows(query.from());
        for (Join join : query.joins()) {
            if (tables.stream().anyMatch(t -> t.alias().equals(join.table().alias())))
                throw new IllegalArgumentException("Duplicate alias: " + join.table().alias());
            tables.add(join.table());
            List<Map<String, Object>> joined = new ArrayList<>();
            for (var left : rows) for (var right : rows(join.table())) {
                Map<String, Object> merged = new LinkedHashMap<>(left);
                merged.putAll(right);
                if (matches(join.on(), merged)) joined.add(merged);
            }
            rows = joined;
        }
        rows.removeIf(row -> !matches(query.where(), row));
        List<ProjectedRow> result = new ArrayList<>();
        for (var row : rows) result.add(new ProjectedRow(row, project(row, query.select(), tables)));
        result.sort((a, b) -> {
            for (Order order : query.orderBy()) {
                Object x = orderValue(order.expression(), a), y = orderValue(order.expression(), b);
                int cmp = x == y ? 0 : x == null ? 1 : y == null ? -1 : compare(x, y);
                if (cmp != 0) return order.ascending() ? cmp : -cmp;
            }
            return 0;
        });
        return result.stream().map(ProjectedRow::projected).toList();
    }

    boolean matches(Condition condition, Map<String, Object> row) {
        return condition == null || Boolean.TRUE.equals(test(condition, row));
    }

    /**
     * Проверяет условие SQL
     * Результат null означает «неизвестно», такая строка в выборку не попадёт
     */
    private Boolean test(Condition condition, Map<String, Object> row) {
        return switch (condition) {
            case Comparison c -> {
                Object a = value(c.left(), row), b = value(c.right(), row);
                if (a == null || b == null) yield null;
                int cmp = compare(a, b);
                yield switch (c.operator()) {
                    case EQ -> cmp == 0;
                    case NE -> cmp != 0;
                    case GT -> cmp > 0;
                    case GE -> cmp >= 0;
                    case LT -> cmp < 0;
                    case LE -> cmp <= 0;
                };
            }
            case Not n -> {
                Boolean v = test(n.condition(), row);
                yield v == null ? null : !v;
            }
            case Logical l -> {
                Boolean a = test(l.left(), row), b = test(l.right(), row);
                if (l.operator() == LogicOp.AND) {
                    if (Boolean.FALSE.equals(a) || Boolean.FALSE.equals(b)) yield false;
                    yield a == null || b == null ? null : Boolean.TRUE;
                }
                if (Boolean.TRUE.equals(a) || Boolean.TRUE.equals(b)) yield true;
                yield a == null || b == null ? null : Boolean.FALSE;
            }
        };
    }

    private List<Map<String, Object>> rows(TableRef ref) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var source : db.snapshot(ref.table()).rows()) {
            Map<String, Object> row = new LinkedHashMap<>();
            source.forEach((key, v) -> row.put(ref.alias() + "." + key, v));
            result.add(row);
        }
        return result;
    }

    /**
     * Собирает выбранные столбцы, включая *
     * Повторять имена в результате нельзя
     */
    private Map<String, Object> project(Map<String, Object> row,
                                        List<SelectItem> items, List<TableRef> tables) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (SelectItem item : items) {
            if (item.expression() instanceof Star star) {
                if (item.alias() != null) throw new IllegalArgumentException("Cannot alias *");
                if (star.qualifier() != null && tables.stream().noneMatch(t -> t.alias().equals(star.qualifier())))
                    throw new IllegalArgumentException("Unknown alias: " + star.qualifier());
                for (var entry : row.entrySet()) {
                    String qualifier = entry.getKey().substring(0, entry.getKey().indexOf('.'));
                    if (star.qualifier() != null && !star.qualifier().equals(qualifier)) continue;
                    String name = tables.size() == 1 && star.qualifier() == null
                            ? entry.getKey().substring(entry.getKey().indexOf('.') + 1) : entry.getKey();
                    result.put(name, entry.getValue());
                }
            } else {
                String name = item.alias() != null ? item.alias() :
                        item.expression() instanceof Column c ? c.name() : "value";
                if (result.containsKey(name)) throw new IllegalArgumentException("Duplicate column: " + name);
                result.put(name, value(item.expression(), row));
            }
        }
        return result;
    }

    private Object orderValue(Expr expr, ProjectedRow result) {
        if (expr instanceof Column c && c.qualifier() == null && result.projected().containsKey(c.name()))
            return result.projected().get(c.name());
        return value(expr, result.original());
    }

    /**
     * Получает значение или находит столбец
     * Если имя есть в нескольких таблицах, выдаёт ошибку
     */
    private Object value(Expr expr, Map<String, Object> row) {
        if (expr instanceof Literal l) return l.value();
        if (!(expr instanceof Column c)) throw new IllegalArgumentException("Expected a column or literal");
        if (c.qualifier() != null) {
            String key = c.qualifier() + "." + c.name();
            if (!row.containsKey(key)) throw new IllegalArgumentException("Unknown column: " + key);
            return row.get(key);
        }
        Object value = null;
        int found = 0;
        for (var entry : row.entrySet()) if (entry.getKey().endsWith("." + c.name())) {
            value = entry.getValue();
            found++;
        }
        if (found != 1) throw new IllegalArgumentException(
                found == 0 ? "Unknown column: " + c.name() : "Ambiguous column: " + c.name());
        return value;
    }

    /**
     * Сравнивает числа, строки, UUID и логические значения
     * Числа сравнивает через BigDecimal
     */
    private static int compare(Object a, Object b) {
        if (a instanceof Number na && b instanceof Number nb)
            return new BigDecimal(na.toString()).compareTo(new BigDecimal(nb.toString()));
        if (a instanceof String sa && b instanceof String sb) return sa.compareTo(sb);
        if (a instanceof Boolean ba && b instanceof Boolean bb) return ba.compareTo(bb);
        if (a instanceof UUID ua && b instanceof UUID ub) return ua.toString().compareTo(ub.toString());
        throw new IllegalArgumentException("Cannot compare " + a.getClass() + " with " + b.getClass());
    }
}
