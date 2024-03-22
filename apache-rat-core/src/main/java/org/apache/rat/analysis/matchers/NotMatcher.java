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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.ConfigChildren;
import org.apache.rat.config.parameters.ConfigComponent;
import org.apache.rat.config.parameters.Description;

/**
 * An IHeaderMatcher that reverses the result of an enclosed matcher.
 */
@ConfigComponent(type=Component.Type.Matcher, name="not", desc="Negates the enclosed matcher.")
public class NotMatcher extends AbstractHeaderMatcher {

    private final IHeaderMatcher enclosed;

    /**
     * Create the matcher with the enclosed matcher.
     *
     * @param enclosed the enclosed matcher
     */
    public NotMatcher(IHeaderMatcher enclosed) {
        this(null, enclosed);
    }

    /**
     * Create the matcher with the enclosed matcher and id.
     *
     * @param id the id for this matcher.
     * @param enclosed the enclosed matcher
     */
    public NotMatcher(String id, IHeaderMatcher enclosed) {
        super(id);
        Objects.requireNonNull(enclosed, "enclosed matcher may not be null");
        this.enclosed = enclosed;
    }
    
    @ConfigChildren(parameterType=IHeaderMatcher.class)
    public List<IHeaderMatcher> getEnclosed() {
        return Arrays.asList(enclosed);
    }

    @Override
    public State matches(String line) {
        enclosed.matches(line);
        return currentState();
    }

    @Override
    public void reset() {
        enclosed.reset();
    }

    @Override
    public State finalizeState() {
        enclosed.finalizeState();
        return currentState();
    }

    @Override
    public State currentState() {
        switch (enclosed.currentState()) {
        case t:
            return State.f;
        case f:
            return State.t;
        default:
        case i:
            return State.i;
        }
    }

}
