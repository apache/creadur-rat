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

package org.apache.rat.config.parameters;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.ImplementationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicense;

import static java.lang.String.format;

/**
 * Builds Description objects for the various component instances.
 */
public final class DescriptionBuilder {
    private DescriptionBuilder() {
        // do not instantiate
    }

    /**
     * Create the description for the object.
     * The object must have a ConfigComponent annotation or {@code null} will be returned.
     * @param object the object to process.
     * @return the Description of the object.
     */
    public static Description build(final Object object) {
        if (object instanceof ILicense) {
            ILicense license = (ILicense) object;
            Class<?> clazz = object.getClass();
            ConfigComponent configComponent = clazz.getAnnotation(ConfigComponent.class);
            if (configComponent == null || configComponent.type() != ComponentType.LICENSE) {
                throw new ConfigurationException(
                        format("Licenses must have License type specified in ConfigComponent annotation. Annotation missing or incorrect in %s", clazz));
            }
            List<Description> children = getConfigComponents(object.getClass());
            return new Description(ComponentType.LICENSE, license.getId(), license.getName(), false, null, children, false);
        }
        return buildMap(object.getClass());
    }

    private static String fixupMethodName(final Method method) {
        String name = method.getName();
        if (name.startsWith("get") || name.startsWith("set") || name.startsWith("add")) {
            if (name.length() > 3) {
                return WordUtils.uncapitalize(name.substring(3));
            }
        }
        throw new ImplementationException(format("'%s' is not a recognized method name", name));
    }
    /**
     * Build the list of descriptions for children of the class.
     * @param clazz source class.
     * @return the Descriptions of the child elements.
     */
    static List<Description> getConfigComponents(final Class<?> clazz) {
        if (clazz == null || clazz == String.class || clazz == Object.class) {
            return Collections.emptyList();
        }
        List<Description> result = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            ConfigComponent configComponent = field.getAnnotation(ConfigComponent.class);
            if (configComponent != null) {
                String name = StringUtils.isBlank(configComponent.name()) ? field.getName() : configComponent.name();
                Class<?> childClazz = configComponent.parameterType() == void.class ? field.getType()
                        : configComponent.parameterType();
                boolean isCollection = Iterable.class.isAssignableFrom(field.getType());

                Description desc = new Description(configComponent.type(), name, configComponent.desc(), isCollection,
                        childClazz, getConfigComponents(childClazz), configComponent.required());
                result.add(desc);
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            ConfigComponent configComponent = method.getAnnotation(ConfigComponent.class);
            if (configComponent != null) {
                String name = StringUtils.isBlank(configComponent.name()) ? fixupMethodName(method) : configComponent.name();
                Class<?> childClazz = configComponent.parameterType() == void.class ? method.getReturnType()
                        : configComponent.parameterType();
                boolean isCollection = Iterable.class.isAssignableFrom(method.getReturnType());

                Description desc = new Description(configComponent.type(), name, configComponent.desc(), isCollection,
                        childClazz, getConfigComponents(childClazz), configComponent.required());
                result.add(desc);
            }
        }
        result.addAll(getConfigComponents(clazz.getSuperclass()));
        Arrays.stream(clazz.getInterfaces()).forEach(c -> result.addAll(getConfigComponents(c)));
        return result;
    }

    private static ConfigComponent findConfigComponent(final Class<?> clazz) {
        if (clazz == null || clazz == String.class || clazz == Object.class) {
            return null;
        }
        ConfigComponent configComponent = clazz.getAnnotation(ConfigComponent.class);
        return configComponent == null ? findConfigComponent(clazz.getSuperclass()) : configComponent;
    }

    public static Class<?> getBuiltClass(final Class<? extends IHeaderMatcher.Builder> clazz) {
        try {
            MatcherBuilder matcherBuilder = clazz.getAnnotation(MatcherBuilder.class);
            if (matcherBuilder == null) {
                return clazz.getMethod("build").getReturnType();
            } else {
                return matcherBuilder.value();
            }
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("the 'build' method of the Builder interface must always be public");
        }
    }

    /**
     * Create a description for a class.
     * @param clazz the class to build the description for.
     * @return the Description of the class or null if no ConfigComponent annotation was found on the class.
     */
    public static Description buildMap(final Class<?> clazz) {
        if (clazz == IHeaderMatcher.class) {
            throw new ImplementationException("'clazz' parameter must not be IHeaderMatcher.class but may be a child of it");
        }
        Class<?> workingClass = IHeaderMatcher.Builder.class.isAssignableFrom(clazz)
                    ? getBuiltClass((Class<IHeaderMatcher.Builder>) clazz)
                    : clazz;

        ConfigComponent configComponent = findConfigComponent(workingClass);
        if (configComponent == null) {
            return null;
        }
        List<Description> children = getConfigComponents(workingClass);

        return new Description(configComponent, false, null, children);
    }
}
