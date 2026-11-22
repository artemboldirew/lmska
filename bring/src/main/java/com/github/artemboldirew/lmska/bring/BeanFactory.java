package com.github.artemboldirew.lmska.bring;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Создаёт бины с учётом их зависимостей
 */
public class BeanFactory {
    private final Map<BeanDefinition, Object> beans = new HashMap<>();
    private final List<AutoCloseable> resources = new ArrayList<>();

    /**
     * Создаёт бины и запоминает, какие из них нужно закрыть
     */
    public void initialize(List<BeanDefinition> definitions) {
        for (BeanDefinition definition : definitions) {
            // Не создаем уже созданные бины
            if (beans.containsKey(definition)) {
                continue;
            }

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
                if (bean instanceof AutoCloseable resource) resources.add(resource);

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

        return type.cast(matches.getFirst());
    }

    public void registerSingleton(BeanDefinition definition, Object bean) {
        if (!definition.getClazz().isInstance(bean)) {
            throw new IllegalArgumentException("Invalid bean type: " + definition.getClazz().getName());
        }

        if (beans.putIfAbsent(definition, bean) != null) {
            throw new IllegalStateException("Bean already registered: " + definition.getClazz().getName());
        }
    }

    /**
     * Закрывает ресурсы всех бинов, даже если при закрытии одного возникла ошибка
     */
    public void close() {
        IllegalStateException failure = null;
        for (int i = resources.size() - 1; i >= 0; i--) {
            try {
                resources.get(i).close();
            } catch (Exception e) {
                if (failure == null) failure = new IllegalStateException("Failed to close beans", e);
                else failure.addSuppressed(e);
            }
        }
        resources.clear();
        if (failure != null) throw failure;
    }
}
