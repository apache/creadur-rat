package org.apache.rat.config.parameters;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


public interface Component {
    public enum Type { License, Matcher, Parameter, Text };

    /**
     * Returns the component Description.
     * @return the component description.
     */
    Description getDescription();

    interface Description {
        public Type getType();
        
        /** 
         * Gets the common name for the matcher. (e.g. 'text', 'spdx', etc.)
         * @return The common name for the item being inspected.
         */
        String getCommonName();
        
        /**
         * Gets the description of descriptive text for the component.
         * @return the descriptive text;
         */
        String getDescription();
        
        /**
         * Gets the string parameter value if this inspector has no 
         * @return the string value (default returns an empty string.
         */
        String getParamValue();
    
        /**
         * Gets a map of the parameters that the object contains.  For example Copyright has 'start', 'stop', and 'owner'
         * parameters.  Some IHeaderMatchers have simple text values (e.g. 'regex' or 'text' types) these should list
         * an unnamed parameter (empty string) with the text value.
         * @return the map of parameters to the objects that represent them.
         */
        default Collection<Description> getChildren() { return Collections.emptyList(); }
        
        /**
         * Get all the children of a specific type
         * @param type the type to return
         * @return the collection of children of the specified type.
         */
        default Collection<Description> childrenOfType(Type type) {
            return getChildren().stream().filter(i -> i.getType() == type).collect(Collectors.toList());
        }
    }

}
