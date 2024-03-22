package org.apache.rat.config.parameters;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigChildren {
    String name() default "";
    Class<?> parameterType() default String.class;
}
