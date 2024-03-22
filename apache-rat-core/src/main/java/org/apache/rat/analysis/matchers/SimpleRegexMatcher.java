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

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.ConfigComponent;
import org.apache.rat.config.parameters.Description;

/**
 * A simple regular expression matching IHeaderMatcher
 */
@ConfigComponent(type=Component.Type.Matcher, name="regex", desc="Performs a regex match using the enclosed the text")
public class SimpleRegexMatcher extends AbstractSimpleMatcher {
    private final Pattern pattern;

    /**
     * Constructs a regex pattern matcher with a unique random id and the specified
     * Regex pattern.
     *
     * @param pattern the pattern to match. Pattern will only match a single line
     * from the input stream.
     */
    public SimpleRegexMatcher(Pattern pattern) {
        this(null, pattern);
    }

    /**
     * Constructs a regex pattern matcher with a unique random id and the specified
     * Regex pattern.
     *
     * @param id the id for this matcher
     * @param pattern the pattern to match. Pattern will only match a single line
     * from the input stream.
     */
    public SimpleRegexMatcher(String id, Pattern pattern) {
        super(id);
        this.pattern = pattern;
    }

    @ConfigComponent(type=Component.Type.Text, name="", desc="", parameterType=Pattern.class)
    private String getPattern() {
        return pattern.pattern();
    }

    @Override
    public boolean doMatch(String line) {
        return pattern.matcher(line).find();
    }
}
