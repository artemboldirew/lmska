package com.github.artemboldirew.lmska.b2.model;

import java.util.List;

public record Insert(String table, List<String> columns, List<List<Expr>> rows) implements Statement {
}
