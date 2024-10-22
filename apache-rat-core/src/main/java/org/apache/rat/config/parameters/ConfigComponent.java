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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that marks a configuration component.
 */
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigComponent {
    /**
     * The common name for the component. If not specified the name of the field or class is used.
     * @return the component name.
     */
    String name() default "";

    /**
     * The description of the component.
     * @return the component description.
     */
    String desc() default "";

    /**
     * The component type
     * @return the component type.
     */
    ComponentType type();

    /**
     * For collections defines the enclosed type.
     * @return the enclosed type.
     */
    Class<?> parameterType() default void.class;

    /**
     * if {@code true} this component can be child of the containing component
     * @return {@code true} if this component can be child of the containing component
     */
    boolean required() default false;
}
