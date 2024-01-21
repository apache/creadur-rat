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

/**
 * An abstract IHeaderMatcher that does simple matching.  Implementations need to implement the {@code doMatch(String)}
 * method to perform the actual matching.  All handling of the {@code State} is managed by this class.  By default the 
 * {@code finalizeState()} method will convert {@code State.i} to {@code State.f}.
 */
public abstract class AbstractSimpleMatcher extends AbstractHeaderMatcher {
    private State lastState;

    /**
     * Constructs the AbstractSimpleMatcher with the specified id.
     * If the id is null or an empty string a unique random id will be generated.
     * @param id the Id to use.  May be null.
     */
    protected AbstractSimpleMatcher(String id) {
        super(id);
        this.lastState = State.i;
    }

    /**
     * Performs the actual match test.
     * @param line the line to check.
     * @return {@code true} if the line matches, {@code false} otherwise.
     */
    abstract protected boolean doMatch(String line);

    @Override
    public final State matches(String line) {
        if (lastState == State.t) {
            return lastState;
        }
        if (line != null && doMatch(line)) {
            lastState = State.t;
        }
        return lastState;
    }

    @Override
    public void reset() {
        lastState = State.i;
    }

    @Override
    public State finalizeState() {
        if (lastState == State.i) {
            lastState = State.f;
        }
        return lastState;
    }

    @Override
    public final State currentState() {
        return lastState;
    }
}
