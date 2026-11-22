package com.github.artemboldirew.lmska.b2.model;

public record Logical(Condition left, LogicOp operator, Condition right) implements Condition {
}
