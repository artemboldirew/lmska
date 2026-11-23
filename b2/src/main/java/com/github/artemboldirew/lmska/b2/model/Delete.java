package com.github.artemboldirew.lmska.b2.model;

public record Delete(String table, Condition where) implements Statement {
}
