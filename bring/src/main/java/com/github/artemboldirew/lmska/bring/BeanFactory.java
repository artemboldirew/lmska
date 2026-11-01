package com.github.artemboldirew.lmska.bring;


import com.github.artemboldirew.lmska.util.BeanResolver;

import java.lang.reflect.Constructor;
import java.util.*;

public class BeanFactory {
    private final Map<BeanDefinition, Object> beans = new HashMap<>();

    public void initialize(List<BeanDefinition> definitions) {
        for (BeanDefinition definition : definitions) {
            Object[] args = definition.getDependencies().stream().map(dependency -> {
                Object bean = beans.get(dependency);
                if (bean == null) {
                    throw new IllegalStateException("Dependency not initialized: " + dependency.getClazz().getName());
                }
                return bean;
            }).toArray();

            try {
                Constructor<?> constructor = definition.getConstructor();
                constructor.setAccessible(true);
                Object bean = constructor.newInstance(args);
                beans.put(definition, bean);

            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create bean: " + definition.getClazz().getName(), e);
            }
        }
    }

    public <T> T getBean(Class<T> type) {
        List<Object> matches = beans.values().stream().filter(type::isInstance).toList();
        if (matches.size() != 1) {
            throw new IllegalStateException("Expected one bean for: " + type.getName());
        }

        return type.cast(matches.get(0));
    }
}