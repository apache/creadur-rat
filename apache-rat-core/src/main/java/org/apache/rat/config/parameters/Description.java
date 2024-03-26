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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.Component.Type;
import org.apache.rat.license.ILicense;

/**
 * A description of a component.
 */
public class Description {
    /** The type of component this describes */
    private final Type type;
    /** The common name for the component.  Set by ConfigComponent.name() or class/field name. */
    private final String name;
    /** The description for the component */
    private final String desc;
    /** The class of the getter/setter parameter */
    private final Class<?> childClass;
    /** True if the getter/setter expects a collection of childClass objects */ 
    private final boolean isCollection;
    /** a map of name to Description for all the components that are children the described component */
    private final Map<String, Description> children;

    /**
     * Constructor.
     * @param type the type of the component.
     * @param name the name of the component.
     * @param desc the description of the component.
     * @param isCollection true if the getter/setter expects a collection
     * @param childClass the class for expected for the getter/setter.
     * @param children the collection of descriptions for all the components that are children the described component.
     */
    public Description(Type type, String name, String desc, boolean isCollection, Class<?> childClass,
            Collection<Description> children) {
        this.type = type;
        this.name = name;
        this.desc = desc;
        this.isCollection = isCollection;
        this.childClass = childClass;
        this.children = new TreeMap<>();
        if (children != null) {
            children.forEach(d -> {
                this.children.put(d.name, d);
            });
        }
    }

    /**
     * Constructor
     * @param configComponent the configuration component
     * @param isCollection the collection flag.
     * @param childClass the type of object that the method getter/setter expects.
     * @param children the collection of descriptions for all the components that are children the described component.
     */
    public Description(ConfigComponent configComponent, boolean isCollection, Class<?> childClass,
            Collection<Description> children) {
        this(configComponent.type(), configComponent.name(), configComponent.desc(), isCollection, childClass,
                children);
    }

    /**
     * Gets the type of the component.
     * @return the component type.
     */
    public Type getType() {
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
     * Get the class of the objcts for the getter/setter.
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
     * Gets the string parameter value. if this description has no value it should
     * return null.
     * @return the string value (default returns an empty string.
     */
    public String getParamValue(Object o) {
        if (isCollection) {
            return null;
        }
        try {
            Object val = getter(o.getClass()).invoke(o);
            return val == null ? null : val.toString();
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            System.err.format("Can not retrieve value for %s from %s\n", name, o.getClass());
            e.printStackTrace();
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
    public Collection<Description> childrenOfType(Type type) {
        return children.values().stream().filter(d -> d.type == type).collect(Collectors.toList());
    }

    private String methodName(String prefix) {
        return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Returns the getter for the component in the specified class.
     * @param clazz the Class to get the getter from.
     * @return the getter Method.
     * @throws NoSuchMethodException if the class does not have the getter.
     * @throws SecurityException if the getter can not be accessed.
     */
    public Method getter(Class<?> clazz) throws NoSuchMethodException, SecurityException {
        return clazz.getMethod(methodName("get"));
    }

    
    /**
     * Returns the setter for the component in the specified class.
     * Notes:
     * <ul>
     * <li>Licence can not be set in components.  They are top level components.</li>
     * <li>Matcher expects an "add" method that accepts an IHeaderMatcher.Builder.</li>
     * <li>Parameter expects a {@code set(String)} method.</li>
     * <li>Unlabled expects a {@code set(String)} method.</li>
     * <li>BuilderParam expects a {@code set} method that takes a {@code childeClass} argument.</li>
     * </ul>
     * 
     * @param clazz the Class to get the getter from, generally a Builder class..
     * @return the getter Method.
     * @throws NoSuchMethodException if the class does not have the getter.
     * @throws SecurityException if the getter can not be accessed.
     */
    public Method setter(Class<?> clazz) throws NoSuchMethodException, SecurityException {
        switch (type) {
        case License:
            throw new NoSuchMethodException("Can not set a License as a child");
        case Matcher:
            return clazz.getMethod("add", IHeaderMatcher.Builder.class);
        case Parameter:
        case Unlabled:
            return clazz.getMethod(methodName("set"), String.class);
        case BuilderParam:
            return clazz.getMethod(methodName("set"), childClass);
        }
        // should not happen
        throw new IllegalStateException("Type " + type + " not valid.");
    }

    /**
     * Sets the children of values in the builder.
     * Sets the parameters to the values specified in the map.  Only children that accept 
     * string arguments should be specified.
     * @param builder The Matcher builder to set the values in.
     * @param attributes a Map of parameter names to values.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public void setChildren(IHeaderMatcher.Builder builder, Map<String, String> attributes)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            Description d = getChildren().get(entry.getKey());
            if (d == null) {
                // TODO replace this with a logging message
                System.err.println(String.format("%s does not define a Description.  Missing ConfigComponent annotations.", entry.getKey()));
            } else {
                d.setter(builder.getClass()).invoke(builder, entry.getValue());
            }
        }
    }

    /**
     * Sets the first Unlabled item that takes a string argument
     * @param builder The Matcher builder to set the value in.
     * @param value the value.
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public void setUnlabledText(IHeaderMatcher.Builder builder, String value) throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Collection<Description> unlabled = childrenOfType(Component.Type.Unlabled);
        for (Description d : unlabled) {
            if (!d.isCollection() && d.childClass == String.class) {
                d.setter(builder.getClass()).invoke(builder, value);
            }
        }
    }

    @Override
    public String toString() {
        return toString(0);
    }

    private String toString(int indent) {
        char[] spaces = new char[indent];
        Arrays.fill(spaces, ' ');
        String padding = String.copyValueOf(spaces);
        String top = String.format("%sDescription[ t:%s n:%s c:%s %s\n%s   %s] ", padding, type, name, isCollection,
                childClass, padding, desc);
        if (children.isEmpty()) {
            return top;
        }
        StringBuilder sb = new StringBuilder(top);
        for (Description child : children.values()) {
            sb.append("\n").append(child.toString(indent + 2));
        }
        return sb.toString();
    }
}