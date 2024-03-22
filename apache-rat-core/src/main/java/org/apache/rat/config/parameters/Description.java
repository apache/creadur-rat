package org.apache.rat.config.parameters;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.rat.config.parameters.Component.Type;

final public class Description {
    
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
}