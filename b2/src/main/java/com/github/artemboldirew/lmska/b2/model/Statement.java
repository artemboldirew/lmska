package com.github.artemboldirew.lmska.b2.model;

public sealed interface Statement permits Select, CreateTable, Insert, Update, Delete {
}
