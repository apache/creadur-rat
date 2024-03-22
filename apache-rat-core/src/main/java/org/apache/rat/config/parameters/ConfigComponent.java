package org.apache.rat.config.parameters;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigComponent {
    /**
     * The common name for the component.
     */
    String name();

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
