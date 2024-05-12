package org.apache.rat;

import org.apache.commons.cli.Option;
import org.apache.rat.utils.DefaultLog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * Reporting methods for deprecated objects.
 */
public class DeprecationReporter {
    /**
     * Deprecated Command line option consumer.
     */
    public static Consumer<Option> LOG_DEPRECATED = o -> {
        StringBuilder buff = new StringBuilder();
        if (o.getOpt() != null) {
            buff.append("-").append(o.getOpt());
            if (o.getLongOpt() != null) {
                buff.append(", --").append(o.getLongOpt());
            }
        } else {
            buff.append("--").append(o.getLongOpt());
        }
        DefaultLog.getInstance().warn(format("Option [%s] used.  %s", buff, o.getDeprecated().toString()));
    };

    /**
     * Log Deprecated class use.
     * @param clazz the Deprecated class to log
     */
    public static void logDeprecated(Class<?> clazz) {
        if (clazz.getAnnotation(Deprecated.class) != null) {
            StringBuilder sb = new StringBuilder(format("Deprecated class used: %s ", clazz));
            Info info = clazz.getAnnotation(Info.class);
            if (info != null) {
                if (info.forRemoval()) {
                    sb.append("  Scheduled for removal");
                    if (!info.since().isEmpty()) {
                        sb.append( " since ").append(info.since());
                    }
                    sb.append(".");
                } else if (!info.since().isEmpty()) {
                    sb.append(" Deprecated since ").append(info.since()).append(".");
                }
                if (!info.use().isEmpty()) {
                    sb.append(" Use ").append(info.use()).append(" instead.");
                }
            }
            DefaultLog.getInstance().warn(sb.toString());
        }
    }

    /**
     * Annotation to provide deprecation information for Java8.
     * TODO remove this when Java 8 no longer supported.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info {
        /**
         * The common name for the component. If not specified the name of the field or class is used.
         * @return the component name.
         */
        String since() default "";

        /**
         * The description of the component.
         * @return the component description.
         */
        boolean forRemoval() default false;
        /**
         * The component type
         * @return the component type.
         */
        String use() default "";
    }
}
