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
package org.apache.rat.testhelpers;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.rat.analysis.matchers.AbstractHeaderMatcher;

/**
 * An Matcher for testing.
 */
public class TestingMatcher extends AbstractHeaderMatcher {
    private State lastState;
    private final boolean[] initialResults;
    private Queue<Boolean> results;
    public State finalState = State.f;

    /**
     * Constructs a matcher with an ID of "dfltMtch" that does not match anyting.
     */
    public TestingMatcher() {
        this("dfltMtch", false);
    }

    /**
     * Constructs a matcher with the specified id and matching result.
     * @param id the ID for this matcher
     * @param result if {@code true} will match everything, otherwise it matches nothing.
     */
    public TestingMatcher(boolean result) {
        this("dfltMtch", result);
    }
    /**
     * Constructs a matcher with the specified id and matching result.
     * @param id the ID for this matcher
     * @param result if {@code true} will match everything, otherwise it matches nothing.
     */
    public TestingMatcher(String id, boolean result) {
        this(id, new boolean[] { result });
    }

    /**
     * Constructs a matcher with the specified ID that returns the matching values in order.
     * Will throw NPE if more {@code matches()} are called than there are results.
     * @param id the id of the matcher.
     * @param results the result for each call to match.
     */
    public TestingMatcher(String id, boolean... results) {
        super(id);
        initialResults = results;
        this.results = new LinkedList<>();
        reset();
    }

    @Override
    public final State matches(String line) {
        if (lastState == State.t) {
            return lastState;
        }
        if (line != null && results.poll()) {
            lastState = State.t;
        }
        return lastState;
    }

    @Override
    public void reset() {
        lastState = State.i;
        results.clear();
        for (boolean b : initialResults) {
            this.results.add(b);
        }
    }

    @Override
    public State finalizeState() {
        if (lastState == State.i) {
            lastState = finalState;
        }
        return lastState;
    }

    @Override
    public final State currentState() {
        return lastState;
    }
}