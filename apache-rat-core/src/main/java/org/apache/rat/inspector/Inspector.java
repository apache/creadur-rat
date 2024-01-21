/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */ 
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
     * @return The common name for the item being inspected.
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