package com.github.artemboldirew.lmska.common;

import java.util.Set;

public record ClassMetadata(
        String name,
        String superClass,
        Set<String> interfaces,
        Set<String> annotations
) {
    public ClassMetadata {
        interfaces = Set.copyOf(interfaces);
        annotations = Set.copyOf(annotations);
    }
}