/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

import org.apache.commons.cli.Option;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * Reporting methods for deprecated objects.
 */
public final class DeprecationReporter {

    /**
     * Deprecated Command line option consumer.
     */
    private DeprecationReporter() {
        // DO NOT INSTANTIATE
    }

    /**
     * The consumer that is used for deprecation reporting.
     */
    private static Consumer<Option> consumer = getDefault();

    /**
     * Get the default reporter.
     * @return The default reporter.
     */
    public static Consumer<Option> getDefault() {
        return  o -> {
            StringBuilder buff = new StringBuilder();
            if (o.getOpt() != null) {
                buff.append("-").append(o.getOpt());
                if (o.getLongOpt() != null) {
                    buff.append(", --").append(o.getLongOpt());
                }
            } else {
                buff.append("--").append(o.getLongOpt());
            }
            DefaultLog.getInstance().warn(format("Option [%s] used. %s", buff, o.getDeprecated().toString()));
        };
    }

    /**
     * Creates the consumer that will log usage of deprecated operations to the default log.
     * @return The consumer that will log usage of deprecated operations to the default log.
     */
    public static Consumer<Option> getLogReporter() {
        return consumer;
    }

    /**
     * Sets the consumer that will do the reporting.
     * @param consumer The consumer that will do the reporting.
     */
    public static void setLogReporter(final Consumer<Option> consumer) {
        DeprecationReporter.consumer = consumer;
    }

    /**
     * Rests the consumer to the default consumer.
     */
    public static void resetLogReporter() {
        DeprecationReporter.consumer = getDefault();
    }

    /**
     * Log Deprecated class use.
     * @param clazz the Deprecated class to log
     */
    public static void logDeprecated(final Class<?> clazz) {
        if (clazz.getAnnotation(Deprecated.class) != null) {
            String name = format("Deprecated class used: %s ", clazz);
            Info info = clazz.getAnnotation(Info.class);
            if (info == null) {
                DefaultLog.getInstance().warn(formatEntry(name, "", false, ""));
            } else {
                DefaultLog.getInstance().warn(formatEntry(format(name, clazz), info.since(), info.forRemoval(), info.use()));
            }
        }
    }

    private static String formatEntry(final String prefix, final String since, final boolean forRemoval, final String use) {
        StringBuilder sb = new StringBuilder("Deprecated " + prefix);
        if (forRemoval) {
            sb.append(" Scheduled for removal");
            if (!since.isEmpty()) {
                sb.append(" since ").append(since);
            }
            sb.append(".");
        } else if (!since.isEmpty()) {
            sb.append(" Deprecated since ").append(since).append(".");
        }
        if (!use.isEmpty()) {
            sb.append(" Use ").append(use).append(" instead.");
        }
        return sb.toString();
    }

    /**
     * Log Deprecated class use.
     * @param name The name of the deprecated tag
     * @param since The version where the deprecation was declared.
     * @param forRemoval If {@code true} then tag is scheduled for removal.
     * @param use What to use instead.
     */
    public static void logDeprecated(final String name, final String since, final boolean forRemoval, final String use) {
        DefaultLog.getInstance().warn(formatEntry(name, since, forRemoval, use));
    }

    /**
     * Annotation to provide deprecation information for Java8.
     * TODO remove this when Java 8 no longer supported.
     */
    @Target({ElementType.TYPE})
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
         * The component type.
         * @return the component type.
         */
        String use() default "";
    }
}
