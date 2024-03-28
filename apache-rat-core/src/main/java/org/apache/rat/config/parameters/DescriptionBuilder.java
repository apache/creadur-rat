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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.license.ILicense;

public class DescriptionBuilder {
    /* do not instantiate */
    private DescriptionBuilder() {}

    /**
     * Create the description for the object.
     * The object must have a ConfigComponent annotation or null will be returned.
     * @param obj the object to process.
     * @return the Description of the object.
     */
    public static Description build(Object obj) {
        if (obj instanceof ILicense) {
            ILicense license = (ILicense)obj;
            Class<?> clazz = obj.getClass();
            ConfigComponent configComponent = clazz.getAnnotation(ConfigComponent.class);
            if (configComponent == null || configComponent.type() != Component.Type.License) {
                throw new IllegalArgumentException(String.format("Licenses must have License type specified in ConfigComponent annotation.  Annotation missing or incorrect in %s", clazz));
            }
            List<Description> children = getConfigComponents(obj.getClass());
            return new Description(Component.Type.License, license.getId(), license.getName(), false, null, children);
        }
        return buildMap(obj.getClass());
    }

    /**
     * Build the list of descriptions for children of the class.
     * @param clazz
     * @return the Descriptions. of the child elements.
     */
    private static List<Description> getConfigComponents(Class<?> clazz) {
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
                        childClazz, getConfigComponents(childClazz));
                result.add(desc);
            }
        }
        result.addAll(getConfigComponents(clazz.getSuperclass()));
        Arrays.stream(clazz.getInterfaces()).forEach(c -> result.addAll(getConfigComponents(c)));
        return result;
    }

    private static ConfigComponent findConfigComponent(Class<?> clazz) {
        if (clazz == null || clazz == String.class || clazz == Object.class) {
            return null;
        }
        ConfigComponent configComponent = clazz.getAnnotation(ConfigComponent.class);
        return configComponent == null ? findConfigComponent(clazz.getSuperclass()) : configComponent;
    }
    /**
     * Create a description for a class.
     * @param clazz the class to build the description for.
     * @return the Description of the class or null if no ConfigComponent annotation was found on the class.
     */
    public static Description buildMap(Class<?> clazz) {
        ConfigComponent configComponent = findConfigComponent(clazz);
        if (configComponent == null) {
            return null;
        }
        List<Description> children = getConfigComponents(clazz);

        return new Description(configComponent, false, null, children);
    }
}
