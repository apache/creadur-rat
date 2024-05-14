/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;
import org.apache.rat.configuration.builders.AbstractBuilder;

/**
 * A class to track the Matcher Builders as they are defined.  Matchers may be defined in multiple configuration files
 * this method tracks them so that they can be referenced across the configuration files.
 */
public final class MatcherBuilderTracker {

    /** The instance of the BuildTracker. */
    public static MatcherBuilderTracker INSTANCE;

    private final Map<String, Class<? extends AbstractBuilder>> matcherBuilders;

    private static synchronized MatcherBuilderTracker instance() {
        if (INSTANCE == null) {
            INSTANCE = new MatcherBuilderTracker();
            Defaults.init();
        }
        return INSTANCE;
    }

    /**
     * Adds a builder to the tracker.
     * If the {@code name} is null then the builder class name simple is used with the "Builder" suffix removed.
     * @param className the Class name for the builder.
     * @param name the short name for the builder. 
     */
    public static void addBuilder(final String className, final String name) {
        instance().addBuilderImpl(className, name);
    }

    /**
     * Get the matching builder for the name.
     * @param name The name of the builder.
     * @return the builder for that name.
     */
    public static AbstractBuilder getMatcherBuilder(final String name) {
        Class<? extends AbstractBuilder> clazz = instance().matcherBuilders.get(name);
        if (clazz == null) {
            StringBuilder sb = new StringBuilder(System.lineSeparator()).append("Valid builders").append(System.lineSeparator());
            instance().matcherBuilders.keySet().forEach(x -> sb.append(x).append(System.lineSeparator()));
            sb.append("ERROR MSG").append(System.lineSeparator());
            throw new ConfigurationException(sb.append("No matcher builder named ").append(name).toString());
        }
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new ConfigurationException(
                    String.format("Can not instantiate matcher builder named %s (%s)", name, clazz.getName()), e);
        }
    }

    private MatcherBuilderTracker() {
        matcherBuilders = new HashMap<>();
    }
    

    /**
     * Gets a collection of classes that are recognized as builders.
     * @return the collection of builder classes
     */
    public Collection<Class<? extends AbstractBuilder>> getClasses() {
        return Collections.unmodifiableCollection(matcherBuilders.values());
    }

    private void addBuilderImpl(final String className, String name) {
        Objects.requireNonNull(className, "className may not be null");
        Class<?> clazz;
        try {
            clazz = getClass().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
        if (AbstractBuilder.class.isAssignableFrom(clazz)) {
            @SuppressWarnings("unchecked")
            Class<? extends AbstractBuilder> candidate = (Class<? extends AbstractBuilder>) clazz;
            if (StringUtils.isBlank(name)) {
                name = candidate.getSimpleName();
                if (!name.endsWith("Builder")) {
                    throw new ConfigurationException(
                            "name is required, or " + candidate.getName() + " must end with 'Builder'");
                }
                name = name.substring(0, name.lastIndexOf("Builder"));
                if (StringUtils.isBlank(name)) {
                    throw new ConfigurationException("Last segment of " + candidate.getName()
                            + " may not be 'Builder', but must end in 'Builder'");
                }
                name = WordUtils.uncapitalize(name);
            }
            matcherBuilders.put(name, candidate);
        } else {
            throw new ConfigurationException("Class " + clazz.getName() + " does not extend " + AbstractBuilder.class);
        }
    }
}
