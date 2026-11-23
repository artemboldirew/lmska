package com.github.artemboldirew.lmska.b2.model;

public sealed interface Condition permits Comparison, Logical, Not {
}
