package org.apache.rat.config.parameters;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.Component.Type;

public class Description {
    
    final Type type;
    final String name;
    final String desc;
    final String value;
    final Map<String,Description> children;
    
    public Description(Type type, String name, String desc, String value, Collection<Description> children) {
        this.type = type;
        this.name = name;
        this.desc = desc;        
        this.value = value;
        this.children = new TreeMap<>();
        if (children != null) {
            children.forEach(d -> {this.children.put(d.name, d);});
        }
    }
    
    public Description(ConfigComponent configComponent, String value, Collection<Description> children) {
        this(configComponent.type(), configComponent.name(), configComponent.desc(), value, children);
    }
    
    /**
     * Gets the type of the object this description decribes.
     * @return the obhect type.
     */
    public Type getType() { return type; }
    
    /** 
     * Gets the common name for the matcher. (e.g. 'text', 'spdx', etc.)
     * May not be null.
     * @return The common name for the item being inspected.
     */
    public String getCommonName() { return name; }
    
    /**
     * Gets the description of descriptive text for the component.
     * May be an empty string or null.
     * @return the descriptive text;
     */
    public String getDescription() { return desc;}
    
    /**
     * Gets the string parameter value.  if this description has no value it should return null.
     * @return the string value (default returns an empty string.
     */
    public String getParamValue() { return value; };

    /**
     * Gets a map of the parameters that the object contains.  For example Copyright has 'start', 'stop', and 'owner'
     * parameters.  Some IHeaderMatchers have simple text values (e.g. 'regex' or 'text' types) these should list
     * an unnamed parameter (empty string) with the text value.
     * @return the map of parameters to the objects that represent them.
     */
    public Map<String,Description> getChildren() { return children; };
    
    /**
     * Get all the children of a specific type
     * @param type the type to return
     * @return the collection of children of the specified type.
     */
    public Collection<Description> childrenOfType(Type type) {
        return children.values().stream().filter(d -> d.type == type).collect(Collectors.toList());
    }
    
    private String methodName(String prefix) {
        return prefix+name.substring(0,1).toUpperCase()+name.substring(1);
    }
    
    public Method getter(Class<?> clazz) throws NoSuchMethodException, SecurityException {
        return clazz.getMethod(methodName("get"));
    }
    
    public Method setter(Class<?> clazz) throws NoSuchMethodException, SecurityException {
        switch (type) {
        case License:
            throw new NoSuchMethodException("Can not set a License as a child");
        case Matcher:
            return clazz.getMethod("add", IHeaderMatcher.Builder.class);
        case Parameter:
            return clazz.getMethod(methodName("set"), String.class);
        case Text:
            return clazz.getMethod("setText", String.class);
        }
        // should not happen
        throw new IllegalStateException("Type "+type+" not valid.");
    }
    

    @Override
    public String toString() {
        return toString(0);
    }
    
    private String toString(int indent) {
        char[] spaces = new char[indent];
        Arrays.fill(spaces, ' ');
        String padding = String.copyValueOf(spaces);
        String top = String.format( "%sDescription[ t:%s n:%s v:%s\n%s   %s] ", padding, type, name, value, padding, desc);
        if (children.isEmpty()) {
            return top;
        }
        StringBuilder sb = new StringBuilder(top);
        for (Description child : children.values()) {
            sb.append("\n").append(child.toString(indent+2));
        }
        return sb.toString();
    }
}