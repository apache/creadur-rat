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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaderMatcher.State;
import org.apache.rat.testhelpers.TestingMatcher;
import org.junit.jupiter.api.Test;

public class OrMatcherTest {

    private void assertValues(IHeaderMatcher target, State hello, State world, State finalize) {
        assertEquals(State.i, target.currentState());
        assertEquals( hello, target.matches("hello"), "hello match");
        assertEquals( hello, target.currentState(), "hello current");
        assertEquals(world, target.matches("world"), "world match");
        assertEquals( world, target.currentState(), "world current");
        assertEquals(finalize, target.finalizeState(), "finalize match");
        assertEquals(finalize, target.currentState(), "finalize current");
    }

    @Test
    public void trueTest() {
        IHeaderMatcher one = new TestingMatcher("one", false, false);
        IHeaderMatcher two = new TestingMatcher("two", false, true);
        OrMatcher target = new OrMatcher("Testing", Arrays.asList(one, two));
        assertValues(target, State.i, State.t, State.t);
        target.reset();
        assertEquals(State.i, one.currentState());
        assertEquals(State.i, two.currentState());
        assertEquals(State.i, target.currentState());
    }

    @Test
    public void falseTest() {
        IHeaderMatcher one = new TestingMatcher("one", false, false);
        IHeaderMatcher two = new TestingMatcher("two", false, false);
        OrMatcher target = new OrMatcher("Testing", Arrays.asList(one, two));
        assertValues(target, State.i, State.i, State.f);
        target.reset();
        assertEquals(State.i, one.currentState());
        assertEquals(State.i, two.currentState());
        assertEquals(State.i, target.currentState());
    }

    @Test
    public void indeterminentTest() {
        IHeaderMatcher one = new TestingMatcher("one", false, false);
        IHeaderMatcher two = new TestingMatcher("two", false, false);
        OrMatcher target = new OrMatcher("Testing", Arrays.asList(one, two));
        assertValues(target, State.i, State.i, State.f);
        target.reset();
        assertEquals(State.i, one.currentState());
        assertEquals(State.i, two.currentState());
        assertEquals(State.i, target.currentState());
    }
}
