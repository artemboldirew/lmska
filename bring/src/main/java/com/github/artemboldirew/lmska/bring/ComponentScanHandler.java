package com.github.artemboldirew.lmska.bring;

import com.github.artemboldirew.lmska.bring.annotation.Component;
import com.github.artemboldirew.lmska.bring.annotation.ComponentScan;
import com.github.artemboldirew.lmska.common.ClassMetadata;
import com.github.artemboldirew.lmska.common.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;


public class ComponentScanHandler {
    public List<BeanDefinition> handle(Class<?> configClass) {
        ComponentScan scan = findComponentScan(configClass);
        if (scan == null) {
            throw new IllegalStateException("ComponentScan not found: " + configClass.getName());
        }

        String basePackage = scan.value().isEmpty() ? configClass.getPackageName() : scan.value();
        Set<ClassMetadata> metadata = ReflectionUtils.scanClassFiles(basePackage);
        Set<Class<?>> components = ReflectionUtils.findAnnotatedClasses(metadata, Component.class);
        return components.stream().map(BeanDefinition::new).toList();
    }

    private ComponentScan findComponentScan(Class<?> clazz) {
        ComponentScan direct = clazz.getAnnotation(ComponentScan.class);
        if (direct != null) {
            return direct;
        }
        for (Annotation annotation : clazz.getAnnotations()) {
            ComponentScan scan = annotation.annotationType().getAnnotation(ComponentScan.class);

            if (scan != null) {
                return scan;
            }
        }
        return null;
    }
}

