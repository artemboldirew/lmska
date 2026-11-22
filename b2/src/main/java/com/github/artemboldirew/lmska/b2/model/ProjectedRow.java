package com.github.artemboldirew.lmska.b2.model;

import java.util.Map;

public record ProjectedRow(Map<String, Object> original, Map<String, Object> projected) {
}
