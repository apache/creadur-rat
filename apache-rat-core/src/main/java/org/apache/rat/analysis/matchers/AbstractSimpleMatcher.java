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

public abstract class AbstractSimpleMatcher extends AbstractHeaderMatcher {
    private State lastState;

    public AbstractSimpleMatcher(String id) {
        super(id);
        this.lastState = State.i;
    }

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
