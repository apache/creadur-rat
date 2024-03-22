package org.apache.rat.config.parameters;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigParameter {
    /**
     * The name of the parameter variable if not same as java variable.
     */
    String name() default "";
}
