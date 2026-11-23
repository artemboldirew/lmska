package com.github.artemboldirew.lmska.b2.model;

import java.util.List;

public record Select(List<SelectItem> select, TableRef from, List<Join> joins, Condition where, List<Order> orderBy) implements Statement {
}
