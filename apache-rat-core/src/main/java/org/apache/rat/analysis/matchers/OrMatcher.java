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

import java.util.Collection;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.ConfigComponent;

/**
 * A matcher that performs a logical {@code OR} across all the contained matchers.
 */
@ConfigComponent(type = ComponentType.MATCHER, name = "any", desc = "A collection of matchers in which all enclosed " +
        "matchers have to be true for the matcher to report true.")
public class OrMatcher extends AbstractMatcherContainer {

    /**
     * Constructs the matcher from the enclosed matchers.
     *
     * @param enclosed the enclosed matchers.
     * @param resource the name of the resource if the collection of matchers was read from a file or resource. May be {@code null}.
     */
    public OrMatcher(final Collection<? extends IHeaderMatcher> enclosed, final String resource) {
        this(null, enclosed, resource);
    }

    /**
     * Constructs the matcher with the specified id from the enclosed matchers.
     *
     * @param id the id to use.
     * @param enclosed the enclosed matchers.
     * @param resource the name of the resource if the collection of matchers was read from a file or resource. May be {@code null}.
     */
    public OrMatcher(final String id, final Collection<? extends IHeaderMatcher> enclosed, final String resource) {
        super(id, enclosed, resource);
    }

    @Override
    public boolean matches(final IHeaders headers) {
        for (IHeaderMatcher matcher : getEnclosed()) {
            if (matcher.matches(headers)) {
                return true;
            }
        }
        return false;
    }
}
