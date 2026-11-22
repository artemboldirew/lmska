package com.github.artemboldirew.lmska.b2.model;

import java.util.List;
import java.util.Map;

public record TableSnapshot(List<String> columns, List<Map<String, Object>> rows) {
}
