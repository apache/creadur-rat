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
 * A matcher that performs a logical {@code AND} across all the contained
 * matchers.
 */
@ConfigComponent(type = ComponentType.MATCHER, name = "all", desc = "A collection of matchers that will report true if any enclosed matcher is true.")
public class AndMatcher extends AbstractMatcherContainer {

    /**
     * Constructs the AndMatcher with the specified id and enclosed collection.
     *
     * @param id the to use. If null or an empty string a unique random id will be
     * created.
     * @param enclosed the enclosed collection.
     * @param resource the name of the resource the collection was read from if any. May be null.
     */
    public AndMatcher(final String id, final Collection<? extends IHeaderMatcher> enclosed, final String resource) {
        super(id, enclosed, resource);
    }

    /**
     * Constructs the AndMatcher with an unique random id and the enclosed
     * collection.
     *
     * @param enclosed the enclosed collection.
     * @param resource the name of the resource the collection was read from if any. May be null.
     */
    public AndMatcher(final Collection<? extends IHeaderMatcher> enclosed, final String resource) {
        this(null, enclosed, resource);
    }

    @Override
    public boolean matches(final IHeaders headers) {
        for (IHeaderMatcher matcher : getEnclosed()) {
            if (!matcher.matches(headers)) {
                return false;
            }
        }
        return true;
    }
}
