package com.github.artemboldirew.lmska.bring;

import com.github.artemboldirew.lmska.util.BeanResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.List;

public class ApplicationContext {
    private final ComponentScanHandler scanHandler = new ComponentScanHandler();
    private final BeanResolver beanResolver = new BeanResolver();
    private final BeanFactory beanFactory = new BeanFactory();

    private List<BeanDefinition> beanDefinitions = List.of();
    private boolean initialized;

    public void refresh(Class<?> configClass) {
        if (initialized) {
            throw new IllegalStateException("Context already initialized");
        }
        List<BeanDefinition> definitions = scanHandler.handle(configClass);
        List<BeanDefinition> resolved = beanResolver.resolve(definitions);
        beanFactory.initialize(resolved);

        beanDefinitions = List.copyOf(resolved);
        initialized = true;
    }

    public <T> T getBean(Class<T> type) {
        if (!initialized) {
            throw new IllegalStateException("Context is not initialized");
        }
        return beanFactory.getBean(type);
    }

    public List<BeanDefinition> getBeanDefinitions() {
        return beanDefinitions;
    }
}