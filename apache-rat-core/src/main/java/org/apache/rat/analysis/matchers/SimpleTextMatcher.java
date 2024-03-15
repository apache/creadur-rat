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

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.DescriptionImpl;

/**
 * A simple text matching IHeaderMatcher implementation.
 */
public class SimpleTextMatcher extends AbstractSimpleMatcher {
    private final String pattern;

    private Description[] children = { new DescriptionImpl(Type.Text, "", "The text to match", this::getSimpleText) };

    /**
     * Constructs the simple text matcher for the simple string.
     * 
     * @param pattern The pattern to match.  Will only match a single line from the input stream.
     */
    public SimpleTextMatcher(String pattern) {
        this(null, pattern);
    }

    /**
     * Constructs the simple text matcher for the simple string.
     * 
     * @param id The id for this matcher.
     * @param pattern The pattern to match.  Will only match a single line from the input stream.
     */
    public SimpleTextMatcher(String id, String pattern) {
        super(id);
        if (StringUtils.isBlank(pattern)) {
            throw new IllegalArgumentException("Pattern may not be null, empty or blank");
        }
        this.pattern = pattern;
    }

    private String getSimpleText() {
        return this.pattern;
    }

    @Override
    public boolean matches(IHeaders headers) {
        return headers.raw().contains(pattern);
    }

    @Override
    public Description getDescription() {
        return new IHeaderMatcher.MatcherDescription(this, "text", "Matches text statement").addChildren(children);
    }
}
