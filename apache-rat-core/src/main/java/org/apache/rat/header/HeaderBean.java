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
package org.apache.rat.header;

import java.util.regex.Pattern;

public class HeaderBean {

    private Pattern headerPattern;
    private boolean match;
    
    public HeaderBean() {
        super();
    }
    
    public HeaderBean(Pattern headerPattern, boolean match) {
        super();
        this.headerPattern = headerPattern;
        this.match = match;
    }

    /**
     * Gets the header pattern to be matched.
     * @return <code>Patter</code>
     * or null when no match is to be made
     */
    public Pattern getHeaderPattern() {
        return headerPattern;
    }
    
    /**
     * Sets the header pattern to be matched.
     * @param headerPattern <code>Patter</code>
     * or null when no match is to be made
     */
    public void setHeaderPattern(Pattern headerPattern) {
        this.headerPattern = headerPattern;
    }
    
    public boolean isMatch() {
        return match;
    }
    public void setMatch(boolean match) {
        this.match = match;
    }
}
