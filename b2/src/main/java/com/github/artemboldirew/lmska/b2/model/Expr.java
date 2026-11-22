package com.github.artemboldirew.lmska.b2.model;

public sealed interface Expr permits Column, Literal, Star {
}
