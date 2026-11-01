package com.github.artemboldirew.lmska.bring;

import com.github.artemboldirew.lmska.common.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

public class BeanDefinition {
    private final Class<?> clazz;
    private final List<Class<?>> parameters;
    private final Set<Class<?>> interfaces;
    private final Constructor<?> constructor;

    public List<BeanDefinition> getDependencies() {
        return dependencies;
    }

    private List<BeanDefinition> dependencies;

    public BeanDefinition(Class<?> clazz) {
        this.clazz = clazz;
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if (constructors.length != 1) {
            throw new IllegalStateException(
                    "Expected one constructor: " + clazz.getName()
            );
        }
        this.constructor = constructors[0];
        this.parameters = List.of(clazz.getConstructors()[0].getParameterTypes());
        this.interfaces = ReflectionUtils.getAllInterfaces(clazz);
    }

    public void resolveDependencies(List<BeanDefinition> dependencies) {
        this.dependencies = List.copyOf(dependencies);
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<Class<?>> getParameters() {
        return parameters;
    }

    public Set<Class<?>> getInterfaces() {
        return interfaces;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }
}
