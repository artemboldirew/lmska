package com.github.artemboldirew.lmska.b2;

import com.github.artemboldirew.lmska.b2.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SqlParser {
    // помогает разделить запрос
    private static final Pattern TOKEN = Pattern.compile(
            "\\s+|'(?:''|[^'])*'|[A-Za-z_]\\w*|\\d+(?:\\.\\d+)?|>=|<=|!=|<>|[(),.*;=<>?\\-]");
    private static final Set<String> KEYWORDS = Set.of(
            "SELECT", "FROM", "WHERE", "JOIN", "ON", "ORDER", "BY", "ASC", "DESC",
            "AS", "AND", "OR", "NOT", "INSERT", "INTO", "VALUES", "UPDATE", "SET",
            "DELETE", "CREATE", "TABLE", "PRIMARY", "KEY", "NULL", "TRUE", "FALSE");
    private static final Set<String> UNSUPPORTED = Set.of(
            "LIKE", "LEFT", "RIGHT", "INNER", "OUTER", "IS", "GROUP", "HAVING", "LIMIT", "OFFSET",
            "DISTINCT", "UNION", "IN", "BETWEEN", "EXISTS", "RETURNING");

    private List<String> tokens;
    private int at, parameterAt;
    private Object[] params;

    /**
     * Разбирает SQL-команду и проверяет, что все её части и параметры использованы
     */
    Statement parse(String sql, Object... parameters) {
        tokens = tokenize(sql);
        params = parameters.clone();
        at = parameterAt = 0;
        for (String token : tokens)
            if (!token.startsWith("'") && UNSUPPORTED.contains(token.toUpperCase(Locale.ROOT)))
                throw new UnsupportedOperationException("Unsupported SQL keyword: " + token);

        Statement result;
        if (take("SELECT")) result = select();
        else if (take("CREATE")) result = create();
        else if (take("INSERT")) result = insert();
        else if (take("UPDATE")) result = update();
        else if (take("DELETE")) result = delete();
        else throw new IllegalArgumentException("Unknown SQL statement");
        take(";");
        if (!tokens.get(at).equals("<EOF>")) throw new IllegalArgumentException("Unexpected token: " + tokens.get(at));
        if (parameterAt != params.length) throw new IllegalArgumentException("Unused parameters");
        return result;
    }

    /**
     * Разбирает SELECT с фильтром и сортировкой
     * В JOIN поддерживается только равенство столбцов
     */
    private Select select() {
        List<SelectItem> columns = new ArrayList<>();
        do {
            Expr expr = expression();
            columns.add(new SelectItem(expr, take("AS") ? name() : optionalAlias()));
        } while (take(","));
        expect("FROM");
        TableRef from = table();
        List<Join> joins = new ArrayList<>();
        while (take("JOIN")) {
            TableRef right = table();
            expect("ON");
            Condition condition = condition();
            if (!(condition instanceof Comparison c && c.operator() == Operator.EQ
                    && c.left() instanceof Column && c.right() instanceof Column))
                throw new UnsupportedOperationException("JOIN ON requires column = column");
            joins.add(new Join(right, condition));
        }
        Condition where = take("WHERE") ? condition() : null;
        List<Order> orders = new ArrayList<>();
        if (take("ORDER")) {
            expect("BY");
            do {
                Expr expr = expression();
                if (!(expr instanceof Column)) throw new IllegalArgumentException("ORDER BY requires a column");
                boolean ascending = !take("DESC");
                if (ascending) take("ASC");
                orders.add(new Order(expr, ascending));
            } while (take(","));
        }
        return new Select(columns, from, joins, where, orders);
    }

    /**
     * Разбирает CREATE TABLE
     * Типы столбцов читает, но при записи данных их не проверяет
     */
    private CreateTable create() {
        expect("TABLE");
        String table = name();
        expect("(");
        List<String> columns = new ArrayList<>();
        String pk = null;
        do {
            String column = name();
            columns.add(column);
            if (isName(tokens.get(at)) && !tokens.get(at).equalsIgnoreCase("PRIMARY")) {
                name();
                if (take("(")) {
                    integer();
                    if (take(",")) integer();
                    expect(")");
                }
            }
            if (take("PRIMARY")) {
                expect("KEY");
                if (pk != null) throw new IllegalArgumentException("Only one primary key is supported");
                pk = column;
            }
        } while (take(","));
        expect(")");
        return new CreateTable(table, columns, pk);
    }

    /**
     * Разбирает INSERT и проверяет число значений в каждой строке
     */
    private Insert insert() {
        expect("INTO");
        String table = name();
        expect("(");
        List<String> columns = new ArrayList<>();
        do {
            columns.add(name());
        } while (take(","));
        expect(")");
        expect("VALUES");
        List<List<Expr>> rows = new ArrayList<>();
        do {
            expect("(");
            List<Expr> values = new ArrayList<>();
            do {
                values.add(literal());
            } while (take(","));
            expect(")");
            if (values.size() != columns.size()) throw new IllegalArgumentException("INSERT column count mismatch");
            rows.add(values);
        } while (take(","));
        return new Insert(table, columns, rows);
    }

    /**
     * Разбирает UPDATE
     * Один столбец нельзя указать в SET дважды
     */
    private Update update() {
        String table = name();
        expect("SET");
        Map<String, Expr> changes = new LinkedHashMap<>();
        do {
            String column = name();
            expect("=");
            if (changes.putIfAbsent(column, literal()) != null) throw new IllegalArgumentException("Duplicate column: " + column);
        } while (take(","));
        return new Update(table, changes, take("WHERE") ? condition() : null);
    }

    private Delete delete() {
        expect("FROM");
        String table = name();
        return new Delete(table, take("WHERE") ? condition() : null);
    }

    private Condition condition() {
        return or();
    }
    private Condition or() {
        Condition result = and();
        while (take("OR")) result = new Logical(result, LogicOp.OR, and());
        return result;
    }
    private Condition and() {
        Condition result = not();
        while (take("AND")) result = new Logical(result, LogicOp.AND, not());
        return result;
    }
    /**
     * Разбирает NOT, скобки и сравнения
     * Приоритет операций: NOT, затем AND, затем OR
     */
    private Condition not() {
        if (take("NOT")) return new Not(not());
        if (take("(")) {
            Condition result = condition();
            expect(")");
            return result;
        }
        Expr left = expression();
        Operator op;
        if (take("=")) op = Operator.EQ;
        else if (take("!=") || take("<>")) op = Operator.NE;
        else if (take(">=")) op = Operator.GE;
        else if (take("<=")) op = Operator.LE;
        else if (take(">")) op = Operator.GT;
        else if (take("<")) op = Operator.LT;
        else throw new IllegalArgumentException("Expected comparison operator");
        return new Comparison(left, op, expression());
    }

    private TableRef table() {
        String table = name();
        String alias = take("AS") ? name() : optionalAlias();
        return new TableRef(table, alias == null ? table : alias);
    }
    private String optionalAlias() {
        return isName(tokens.get(at)) && !KEYWORDS.contains(tokens.get(at).toUpperCase(Locale.ROOT)) ? next() : null;
    }
    private Expr literal() {
        Expr e = expression();
        if (!(e instanceof Literal)) throw new IllegalArgumentException("Expected a literal or ?");
        return e;
    }
    /**
     * Читает значение, столбец или *
     * Параметр ? берёт из переданного массива, не вставляя его в текст SQL
     */
    private Expr expression() {
        if (take("?")) {
            if (parameterAt == params.length) throw new IllegalArgumentException("Missing parameter");
            return new Literal(params[parameterAt++]);
        }
        if (take("*")) return new Star(null);
        if (take("-")) return new Literal(number("-" + next()));
        String token = next();
        if (token.startsWith("'")) return new Literal(token.substring(1, token.length() - 1).replace("''", "'"));
        // если токен это число
        if (token.matches("\\d+(?:\\.\\d+)?")) return new Literal(number(token));
        if (token.equalsIgnoreCase("NULL")) return new Literal(null);
        if (token.equalsIgnoreCase("TRUE")) return new Literal(true);
        if (token.equalsIgnoreCase("FALSE")) return new Literal(false);
        if (!isName(token)) throw new IllegalArgumentException("Expected value or column, got: " + token);
        if (take("(")) throw new UnsupportedOperationException("SQL functions are unsupported: " + token);
        if (take(".")) return take("*") ? new Star(token) : new Column(token, name());
        return new Column(null, token);
    }
    private Number number(String text) {
        try {
            return text.contains(".") ? new BigDecimal(text) : Long.valueOf(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number: " + text);
        }
    }
    private int integer() {
        String token = next();
        if (!token.matches("\\d+")) throw new IllegalArgumentException("Expected integer");
        return Integer.parseInt(token);
    }
    private String name() {
        String token = next();
        if (!isName(token) || KEYWORDS.contains(token.toUpperCase(Locale.ROOT)))
            throw new IllegalArgumentException("Expected identifier, got: " + token);
        return token;
    }
    private static boolean isName(String s) {
        return s.matches("[A-Za-z_]\\w*");
    }

    // проверяет совпадает ли текущий токен с ожидаемым
    private boolean take(String text) {
        if (tokens.get(at).equalsIgnoreCase(text)) {
            at++;
            return true;
        }
        return false;
    }
    private void expect(String text) {
        if (!take(text)) throw new IllegalArgumentException("Expected " + text + ", got " + tokens.get(at));
    }

    private String next() {
        return tokens.get(at++);
    }

    /**
     * Разбивает SQL на токены
     * Неизвестные символы вызывают ошибку
     */
    private static List<String> tokenize(String sql) {
        List<String> result = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(sql);
        for (int i = 0; i < sql.length();) {
            matcher.region(i, sql.length());
            if (!matcher.lookingAt()) throw new IllegalArgumentException("Unsupported SQL at: " + sql.substring(i));
            String token = matcher.group();
            if (!token.isBlank()) result.add(token);
            i = matcher.end();
        }
        result.add("<EOF>");
        return result;
    }
}
