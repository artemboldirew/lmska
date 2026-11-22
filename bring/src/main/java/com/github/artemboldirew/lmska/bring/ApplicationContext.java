package com.github.artemboldirew.lmska.bring;

import com.github.artemboldirew.lmska.bring.util.BeanResolver;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Хранит бины приложения и закрывает их ресурсы
 */
public class ApplicationContext implements AutoCloseable {
    private final Environment environment;
    private final ComponentScanHandler scanHandler = new ComponentScanHandler();
    private final BeanResolver beanResolver = new BeanResolver();
    private final BeanFactory beanFactory = new BeanFactory();
    private List<BeanDefinition> beanDefinitions = List.of();
    private boolean initialized;

    public ApplicationContext(Environment environment) {
        this.environment = environment;
    }

    public void refresh(Class<?> configClass) {
        if (initialized) {
            throw new IllegalStateException("Context already initialized");
        }
        List<BeanDefinition> definitions = new ArrayList<>();
        BeanDefinition environmentDefinition = new BeanDefinition(Environment.class);
        definitions.add(environmentDefinition);
        BeanDefinition contextDefinition = new BeanDefinition(ApplicationContext.class);
        definitions.add(contextDefinition);
        definitions.addAll(scanHandler.handle(configClass));
        List<BeanDefinition> resolved = beanResolver.resolve(definitions);
        //environment добавляем отдельно
        beanFactory.registerSingleton(environmentDefinition, environment);
        beanFactory.registerSingleton(contextDefinition, this);
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

    /**
     * Возвращает готовые бины с указанной аннотацией
     */
    public List<Object> getBeansWithAnnotation(Class<? extends Annotation> annotation) {
        if (!initialized) {
            throw new IllegalStateException("Context is not initialized");
        }
        List<Object> beans = new ArrayList<>();
        for (BeanDefinition definition : beanDefinitions) {
            if (definition.getClazz().isAnnotationPresent(annotation)) {
                beans.add(getBean(definition.getClazz()));
            }
        }
        return List.copyOf(beans);
    }

    @Override
    public void close() {
        beanFactory.close();
    }
}
