package com.github.artemboldirew.lmska.b2.model;

import java.util.List;

public record CreateTable(String table, List<String> columns, String primaryKey) implements Statement {
}
