package com.github.artemboldirew.lmska.b2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Table {
    final List<String> columns;
    final String primaryKey;
    final Map<Object, Map<String, Object>> rows = new LinkedHashMap<>();
    long nextRowId;

    Table(List<String> columns, String primaryKey) {
        this.columns = columns;
        this.primaryKey = primaryKey;
    }
}
