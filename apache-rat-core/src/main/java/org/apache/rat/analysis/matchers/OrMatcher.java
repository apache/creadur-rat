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
import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.ConfigComponent;
import org.apache.rat.config.parameters.DescriptionBuilder;

/**
 * A matcher that performs a logical {@code OR} across all the contained
 * matchers.
 */
@ConfigComponent(type = Component.Type.Matcher, name = "any", desc = "Returns true if at least one of the enclosed matchers return true.")
public class OrMatcher extends AbstractMatcherContainer {

    private State lastState;

    /**
     * Constructs the matcher from the enclosed matchers.
     *
     * @param enclosed the enclosed matchers.
     */
    public OrMatcher(Collection<? extends IHeaderMatcher> enclosed, String resource) {
        this(null, enclosed, resource);
    }

    /**
     * Constructs the matcher with the specified id from the enclosed matchers.
     *
     * @param id the id to use.
     * @param enclosed the enclosed matchers.
     */
    public OrMatcher(String id, Collection<? extends IHeaderMatcher> enclosed, String resource) {
        super(id, enclosed, resource);
        lastState = State.i;
    }

    @Override
    public State matches(String line) {
        if (lastState == State.t) {
            return State.t;
        }
        for (IHeaderMatcher matcher : enclosed) {
            State s = matcher.matches(line);
            System.out.format("Or testing %s -> %s%n", DescriptionBuilder.build(matcher), s);
            
            switch (s) {
            case t:
                lastState = State.t;
                return lastState;
            case f:
            case i:
                lastState = State.i;
            }
        }
        return lastState;
    }

    @Override
    public State currentState() {
        if (lastState == State.t) {
            return lastState;
        }
        for (IHeaderMatcher matcher : enclosed) {
            switch (matcher.currentState()) {
            case t:
                lastState = State.t;
                return lastState;
            case i:
                lastState = State.i;
                return lastState;
            case f:
                // do nothing;
                break;
            }
        }
        lastState = State.f;
        return lastState;
    }

    @Override
    public void reset() {
        super.reset();
        lastState = State.i;
    }
}
