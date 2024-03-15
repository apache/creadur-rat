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
import org.apache.rat.config.parameters.DescriptionImpl;

import org.apache.rat.analysis.IHeaders;

/**
 * A simple regular expression matching IHeaderMatcher
 */
public class SimpleRegexMatcher extends AbstractSimpleMatcher {
    private final Pattern pattern;

    private Description[] children = {
            new DescriptionImpl(Type.Text, "", "The regex pattern to match", this::getPattern) };

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

    private String getPattern() {
        return pattern.pattern();
    }

    @Override
    public boolean matches(IHeaders headers) {
        return pattern.matcher(headers.raw()).find();
    }

    @Override
    public Description getDescription() {
        return new IHeaderMatcher.MatcherDescription(this, "regex", "Performs a regex match on the text")
                .addChildren(children);
    }
}
