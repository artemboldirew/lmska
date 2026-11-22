package com.github.artemboldirew.lmska.bibernate.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PostgresClient implements DbClient {
    private final Connection connection;

    public PostgresClient(String url, String username, String password) {
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect to PostgreSQL", e);
        }
    }

    /**
     * Выполняет SELECT с параметрами и собирает строки результата
     */
    @Override
    public List<Map<String, Object>> query(String sql, Object... params) {
        try (PreparedStatement stmt = prepare(sql, params); ResultSet rs = stmt.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) row.put(meta.getColumnLabel(i), rs.getObject(i));
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) {
            throw new IllegalStateException("SQL query failed: " + sql, e);
        }
    }

    @Override
    public int update(String sql, Object... params) {
        try (PreparedStatement stmt = prepare(sql, params)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("SQL update failed: " + sql, e);
        }
    }

    /**
     * Подставляет параметры в подготовленный запрос
     * При ошибке закрывает его
     */
    private PreparedStatement prepare(String sql, Object[] params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        try {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            return stmt;
        } catch (SQLException | RuntimeException e) {
            stmt.close();
            throw e;
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not close connection", e);
        }
    }
}
