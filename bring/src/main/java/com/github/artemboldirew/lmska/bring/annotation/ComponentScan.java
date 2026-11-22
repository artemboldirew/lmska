package com.github.artemboldirew.lmska.bring.annotation;

import java.lang.annotation.*;

/**
 * Аннотация дающая точку, ниже которой будут сканироваться кллассы
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentScan {
    String value() default "";
}
