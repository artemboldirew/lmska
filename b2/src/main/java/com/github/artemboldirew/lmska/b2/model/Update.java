package com.github.artemboldirew.lmska.b2.model;

import java.util.Map;

public record Update(String table, Map<String, Expr> changes, Condition where) implements Statement {
}
