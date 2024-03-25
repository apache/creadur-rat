package org.apache.rat.config.parameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigComponent {
    /**
     * The common name for the component.
     */
    String name() default "";

    /**
     * The description of the component.
     */
    String desc();
    /**
     * The component type
     */
    Component.Type type();
    
    Class<?> parameterType() default String.class;
}
