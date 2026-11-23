package com.github.artemboldirew.lmska.b2;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SqlResult(List<Map<String, Object>> rows, int affectedRows) {
    public SqlResult {
        if (rows != null) {
            rows = rows.stream()
                    .map(row -> Collections.unmodifiableMap(new LinkedHashMap<>(row)))
                    .toList();
        }
    }

    static SqlResult selected(List<Map<String, Object>> rows) {
        return new SqlResult(rows, -1);
    }

    static SqlResult changed(int count) {
        return new SqlResult(null, count);
    }

    @Override
    public List<Map<String, Object>> rows() {
        if (rows == null) throw new IllegalStateException("SQL command did not return rows");
        return rows;
    }

    @Override
    public int affectedRows() {
        if (affectedRows < 0) throw new IllegalStateException("SELECT did not modify rows");
        return affectedRows;
    }
}
