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

import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.BuilderParams;
import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.utils.DefaultLog;

/**
 * A description of a component.
 */
public class Description {
    /** The type of component this describes */
    private final ComponentType type;
    /**
     * The common name for the component. Set by ConfigComponent.name() or
     * class/field name.
     */
    private final String name;
    /** The description for the component */
    private final String desc;
    /** The class of the getter/setter parameter */
    private final Class<?> childClass;
    /** True if the getter/setter expects a collection of childClass objects */
    private final boolean isCollection;
    /** True if this component is required. */
    private final boolean required;
    /**
     * A map of name to Description for all the components that are children of the
     * described component.
     */
    private final Map<String, Description> children;

    /**
     * Constructor.
     * @param type the type of the component.
     * @param name the name of the component.
     * @param desc the description of the component.
     * @param isCollection true if the getter/setter expects a collection
     * @param childClass the class for expected for the getter/setter.
     * @param children the collection of descriptions for all the components that
     * are children of the described component.
     * @param required If {@code true} the component is required.
     */
    public Description(final ComponentType type, final String name, final String desc, final boolean isCollection,
                       final Class<?> childClass, final Collection<Description> children, final boolean required) {
        this.type = type;
        this.name = name;
        this.desc = desc;
        this.isCollection = isCollection;
        this.required = required;
        if (type == ComponentType.BUILD_PARAMETER) {
            Method m;
            try {
                m = BuilderParams.class.getMethod(name);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new ConfigurationException(format("'%s' is not a valid BuildParams method", name));
            }
            this.childClass = m.getReturnType();
        } else {
            this.childClass = childClass;
        }
        this.children = new TreeMap<>();
        if (children != null) {
            children.forEach(d -> this.children.put(d.name, d));
        }
    }

    /**
     * Constructor
     * @param configComponent the configuration component
     * @param isCollection the collection flag.
     * @param childClass the type of object that the method getter/setter expects.
     * @param children the collection of descriptions for all the components that
     * are children the described component.
     */
    public Description(final ConfigComponent configComponent, final boolean isCollection, final Class<?> childClass,
                       final Collection<Description> children) {
        this(configComponent.type(), configComponent.name(), configComponent.desc(), isCollection, childClass, children,
                configComponent.required());
    }

    /**
     * Get the canBeChild flag.
     * @return {@code true} if this item can be a child of the containing item.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Gets the type of the component.
     * @return the component type.
     */
    public ComponentType getType() {
        return type;
    }

    /**
     * Get the isCollection flag.
     * @return true if this is a collection.
     */
    public boolean isCollection() {
        return isCollection;
    }

    /**
     * Get the class of the objects for the getter/setter methods.
     * @return the getter/setter param class.
     */
    public Class<?> getChildType() {
        return childClass;
    }

    /**
     * Gets the common name for the matcher. (e.g. 'text', 'spdx', etc.) May not be
     * null.
     * @return The common name for the item being inspected.
     */
    public String getCommonName() {
        return name;
    }

    /**
     * Gets the description of descriptive text for the component. May be an empty
     * string or null.
     * @return the descriptive text;
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Retrieve the value of the described parameter from the specified object.
     * If the parameter is a collection return {@code null}.
     * @param object the object that contains the value.
     * @return the string value.
     */
    public String getParamValue(final Object object) {
        if (isCollection) {
            return null;
        }
        try {
            Object val = getter(object.getClass()).invoke(object);
            return val == null ? null : val.toString();
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            DefaultLog.getInstance().error(format("Can not retrieve value for '%s' from %s%n", name, object.getClass().getName()),
                    e);
            return null;
        }
    }

    /**
     * Gets a map of the parameters that the object contains. For example Copyright
     * has 'start', 'stop', and 'owner' parameters. Some IHeaderMatchers have simple
     * text values (e.g. 'regex' or 'text' types) these should list an unnamed
     * parameter (empty string) with the text value.
     * @return the map of parameters to the objects that represent them.
     */
    public Map<String, Description> getChildren() {
        return children;
    }

    /**
     * Get all the children of a specific type
     * @param type the type to return
     * @return the collection of children of the specified type.
     */
    public Collection<Description> childrenOfType(final ComponentType type) {
        return filterChildren(d -> d.getType() == type);
    }

    /**
     * Gets a filtered collection of the child descriptions.
     * @param filter the filter to apply to the child descriptions.
     * @return the collection of children that matche the filter.
     */
    public Collection<Description> filterChildren(final Predicate<Description> filter) {
        return children.values().stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * Generate a method name for this description.
     * @param prefix the start of the method name (e.g. "set", "get" )
     * @return the method name.
     */
    public String methodName(final String prefix) {
        return prefix + StringUtils.capitalize(name);
    }

    /**
     * Returns the getter for the component in the specified class.
     * @param clazz the Class to get the getter from.
     * @return the getter Method.
     * @throws NoSuchMethodException if the class does not have the getter.
     * @throws SecurityException if the getter can not be accessed.
     */
    public Method getter(final Class<?> clazz) throws NoSuchMethodException, SecurityException {
        return clazz.getMethod(methodName("get"));
    }

    /**
     * Returns the setter for the component in the specified class. Notes:
     * <ul>
     * <li>License can not be set in components. They are top level components.</li>
     * <li>Matcher expects an "add" method that accepts an
     * IHeaderMatcher.Builder.</li>
     * <li>Parameter expects a {@code set(String)} method.</li>
     * <li>Unlabeled expects a {@code set(String)} method.</li>
     * <li>BuilderParam expects a {@code set} method that takes a
     * {@code childeClass} argument.</li>
     * </ul>
     * @param clazz the Class to get the getter from, generally a Builder class..
     * @return the getter Method.
     * @throws NoSuchMethodException if the class does not have the getter.
     * @throws SecurityException if the getter can not be accessed.
     */
    public Method setter(final Class<?> clazz) throws NoSuchMethodException, SecurityException {
        String methodName = methodName(isCollection ? "add" : "set");
        switch (type) {
        case LICENSE:
            throw new NoSuchMethodException("Can not set a License as a child");
        case MATCHER:
            return clazz.getMethod(methodName, IHeaderMatcher.Builder.class);
        case PARAMETER:
            return clazz.getMethod(methodName,
                    IHeaderMatcher.class.isAssignableFrom(childClass) ? IHeaderMatcher.Builder.class : childClass);
        case BUILD_PARAMETER:
            return clazz.getMethod(methodName, childClass);
        }
        // should not happen
        throw new IllegalStateException("Type " + type + " not valid.");
    }

    private void callSetter(final Description description, final IHeaderMatcher.Builder builder, final String value) {
        try {
            description.setter(builder.getClass()).invoke(builder, value);
        } catch (NoSuchMethodException e) {
            String msg = format("No setter for '%s' on %s", description.getCommonName(),
                    builder.getClass().getCanonicalName());
            DefaultLog.getInstance().error(msg);
            throw new ConfigurationException(msg);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            String msg = format("Unable to call setter for '%s' on %s", description.getCommonName(),
                    builder.getClass().getCanonicalName());
            DefaultLog.getInstance().error(msg, e);
            throw new ConfigurationException(msg, e);
        }
    }

    /**
     * Sets the children of values in the builder. Sets the parameters to the values
     * specified in the map. Only children that accept string arguments should be
     * specified.
     * @param builder The Matcher builder to set the values in.
     * @param attributes a Map of parameter names to values.
     */
    public void setChildren(final IHeaderMatcher.Builder builder, final Map<String, String> attributes) {
        attributes.forEach((key, value) -> setChild(builder, key, value));
    }

    /**
     * Sets the child value in the builder.
     * @param builder The Matcher builder to set the values in.
     * @param name the name of the child to set
     * @param value the value of the parameter.
     */
    public void setChild(final IHeaderMatcher.Builder builder, final String name, final String value) {
        Description d = getChildren().get(name);
        if (d == null) {
            DefaultLog.getInstance().error(format("%s does not define a ConfigComponent for a member %s.",
                    builder.getClass().getCanonicalName(), name));
        } else {
            callSetter(d, builder, value);
        }
    }

    @Override
    public String toString() {
        String childList = children.isEmpty() ? ""
                : children.values().stream().map(Description::getCommonName).collect(Collectors.joining(", "));

        return format("Description[%s t:%s c:%s %s children: [%s]] ", name, type, isCollection, childClass,
                childList);
    }
}
