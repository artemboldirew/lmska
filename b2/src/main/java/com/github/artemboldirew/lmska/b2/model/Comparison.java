package com.github.artemboldirew.lmska.b2.model;

public record Comparison(Expr left, Operator operator, Expr right) implements Condition {
}
