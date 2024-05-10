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
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.ConfigComponent;

/**
 * A simple text matching IHeaderMatcher implementation.
 */
@ConfigComponent(type = ComponentType.MATCHER, name = "text", desc = "Matches the enclosed text")
public class SimpleTextMatcher extends AbstractHeaderMatcher {
    
    @ConfigComponent(type = ComponentType.PARAMETER, name = "simpleText", desc = "The text to match", required=true)
    private final String simpleText;

    /**
     * Constructs the simple text matcher for the simple string.
     *
     * @param simpleText The pattern to match. Will only match a single line from
     * the input stream.
     */
    public SimpleTextMatcher(String simpleText) {
        this(null, simpleText);
    }

    /**
     * Constructs the simple text matcher for the simple string.
     *
     * @param id The id for this matcher.
     * @param simpleText The pattern to match. Will only match a single line from
     * the input stream.
     */
    public SimpleTextMatcher(String id, String simpleText) {
        super(id);
        if (StringUtils.isBlank(simpleText)) {
            throw new IllegalArgumentException("Simple text may not be null, empty or blank");
        }
        this.simpleText = simpleText;
    }

    public String getSimpleText() {
        return this.simpleText;
    }

    @Override
    public boolean matches(IHeaders headers) {
        return headers.raw().contains(simpleText);
    }
}
