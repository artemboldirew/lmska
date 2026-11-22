package com.github.artemboldirew.lmska.bring.annotation;

import java.lang.annotation.*;

/**
 * Аннотация для бинов
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
}
