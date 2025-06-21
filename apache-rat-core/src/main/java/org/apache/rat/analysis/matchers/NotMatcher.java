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

import java.util.Objects;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.ConfigComponent;

/**
 * An IHeaderMatcher that reverses the result of an enclosed matcher.
 */
@ConfigComponent(type = ComponentType.MATCHER, name = "not",
        desc = "A matcher that wraps one matcher and negates its value. Not matchers require that the entire " +
                "header be read before it can report true or false. This may significantly slow processing.")
public class NotMatcher extends AbstractHeaderMatcher {
    /**
     * The enclosed matcher to negate.
     */
    @ConfigComponent(desc = "enclosed Matcher", type = ComponentType.PARAMETER, parameterType = IHeaderMatcher.class, required = true)
    private final IHeaderMatcher enclosed;

    /**
     * Create the matcher with the enclosed matcher and id.
     *
     * @param id the id for this matcher. May be null
     * @param enclosed the enclosed matcher
     */
    public NotMatcher(final String id, final IHeaderMatcher enclosed) {
        super(id);
        Objects.requireNonNull(enclosed, "enclosed matcher may not be null");
        this.enclosed = enclosed;
    }

    public IHeaderMatcher getEnclosed() {
        return enclosed;
    }

    @Override
    public boolean matches(final IHeaders headers) {
        return !enclosed.matches(headers);
    }

    @Override
    public void reset() {
        enclosed.reset();
    }
}
