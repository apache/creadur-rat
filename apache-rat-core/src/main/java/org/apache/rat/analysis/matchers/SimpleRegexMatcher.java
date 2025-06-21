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
package org.apache.rat.analysis.matchers;

import java.util.regex.Pattern;

import org.apache.rat.analysis.IHeaders;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.ConfigComponent;

/**
 * A simple regular expression matching IHeaderMatcher
 */
@ConfigComponent(type = ComponentType.MATCHER, name = "regex",
        desc = "Performs a regular expression match using the enclosed the text.  " +
                "This is a relatively slow matcher.")
public class SimpleRegexMatcher extends AbstractHeaderMatcher {
    /**
     * The regular expression pattern to match.
     */
    @ConfigComponent(type = ComponentType.PARAMETER, desc = "The pattern to match", name = "expression", parameterType = String.class)
    private final Pattern pattern;

    /**
     * Constructs a regex pattern matcher with a unique random id and the specified
     * Regex pattern.
     *
     * @param id the id for this matcher, may be null
     * @param pattern the pattern to match. Pattern will only match a single line
     * from the input stream.
     */
    public SimpleRegexMatcher(final String id, final Pattern pattern) {
        super(id);
        this.pattern = pattern;
    }

    /**
     * Gets the expression of the underlying matching pattern.
     * @return the underlying pattern's construction expression.
     */
    public String getExpression() {
        // called by reflection
        return pattern.pattern();
    }

    @Override
    public boolean matches(final IHeaders headers) {
        return pattern.matcher(headers.raw()).find();
    }
}
