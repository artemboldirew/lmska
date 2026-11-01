package com.github.artemboldirew.lmska.util;


import com.github.artemboldirew.lmska.bring.BeanDefinition;

import java.util.*;


import java.util.*;


import java.util.*;

public class BeanResolver {

    public List<BeanDefinition> resolve(List<BeanDefinition> definitions) {
        Set<Class<?>> registered = new HashSet<>();
        for (BeanDefinition definition : definitions) {
            if (!registered.add(definition.getClazz())) {
                throw new IllegalStateException("Duplicate bean: " + definition.getClazz().getName());
            }
        }

        for (BeanDefinition definition : definitions) {
            List<BeanDefinition> dependencies = new ArrayList<>();
            for (Class<?> parameter : definition.getParameters()) {
                dependencies.add(findDependency(parameter, definitions));
            }
            definition.resolveDependencies(dependencies);
        }

        List<BeanDefinition> sorted = new ArrayList<>();
        Set<BeanDefinition> visited = new HashSet<>();
        Set<BeanDefinition> visiting = new HashSet<>();

        for (BeanDefinition definition : definitions) {
            visit(definition, visited, visiting, sorted);
        }

        return List.copyOf(sorted);
    }

    private BeanDefinition findDependency(Class<?> type, List<BeanDefinition> definitions) {
        List<BeanDefinition> candidates = definitions.stream().filter(d -> type.isAssignableFrom(d.getClazz())).toList();
        if (candidates.size() != 1) {
            throw new IllegalStateException("Expected one bean for " + type.getName() + ", found: " + candidates.size());
        }
        return candidates.getFirst();
    }

    private void visit(BeanDefinition definition, Set<BeanDefinition> visited, Set<BeanDefinition> visiting, List<BeanDefinition> sorted) {
        if (visited.contains(definition)) {
            return;
        }
        if (!visiting.add(definition)) {
            throw new IllegalStateException("Circular dependency: " + definition.getClazz().getName());
        }
        for (BeanDefinition dependency : definition.getDependencies()) {
            visit(dependency, visited, visiting, sorted);
        }
        visiting.remove(definition);
        visited.add(definition);
        sorted.add(definition);
    }
}
