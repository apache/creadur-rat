package org.apache.rat.inspector;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * An interface that explains what the matcher operates on.
 */
public interface Inspector {
    
    public enum Type { License, Matcher, Parameter, Text };
    
    public Type getType();
    
    /** 
     * Gets the common name for the matcher. (e.g. 'text', 'spdx', etc.)
     * @return
     */
    String getCommonName();
    
    /**
     * Gets the string parameter value if this inspector has no 
     * @return the string value (default returns an empty string.
     */
    default String getParamValue() { return ""; }

    /**
     * Gets a map of the parameters that the object contains.  For example Copyright has 'start', 'stop', and 'owner'
     * parameters.  Some IHeaderMatchers have simple text values (e.g. 'regex' or 'text' types) these should list
     * an unnamed parameter (empty string) with the text value.
     * @return the map of parameters to the objects that represent them.
     */
    default Collection<Inspector> getChildren() { return Collections.emptyList(); }
    
    /**
     * Get all the children of a specific type
     * @param type the type to return
     * @return the colleciton of children of the specified type.
     */
    default Collection<Inspector> childrenOfType(Type type) {
        return getChildren().stream().filter(i -> i.getType() == type).collect(Collectors.toList());
    }
}