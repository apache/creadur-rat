package org.apache.rat.analysis.matchers;

import static org.junit.Assert.assertEquals;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaderMatcher.State;
import org.junit.Before;
import org.junit.Test;

public class SPDXMatcherTest {

    IHeaderMatcher target = SPDXMatcherFactory.INSTANCE.create("hello");

    @Before
    public void setup() {
        target.reset();
    }

    @Test
    public void testMatch() {
        assertEquals(State.i, target.currentState());
        assertEquals(State.i, target.matches("SPDX-License-Identifier: Apache-2"));
        assertEquals(State.i, target.currentState());
        assertEquals(State.t, target.matches("SPDX-License-Identifier: hello"));
        assertEquals(State.t, target.currentState());
        assertEquals(State.t, target.finalizeState());
        assertEquals(State.t, target.currentState());
        target.reset();
        assertEquals(State.i, target.currentState());
    }

    @Test
    public void testNoMatch() {
        assertEquals(State.i, target.currentState());
        assertEquals(State.i, target.matches("SPDX-License-Identifier: Apache-2"));
        assertEquals(State.i, target.currentState());
        assertEquals(State.i, target.matches("SPDX-License-Identifier: MIT"));
        assertEquals(State.i, target.currentState());
        assertEquals(State.f, target.finalizeState());
        assertEquals(State.f, target.currentState());
        target.reset();
        assertEquals(State.i, target.currentState());
    }

    @Test
    public void testTrueIsAlwaysTrue() {
        assertEquals(State.i, target.currentState());
        assertEquals(State.t, target.matches("SPDX-License-Identifier: hello"));
        assertEquals(State.t, target.currentState());
        assertEquals(State.t, target.matches("SPDX-License-Identifier: Apache-2"));
        assertEquals(State.t, target.currentState());
        assertEquals(State.t, target.finalizeState());
        assertEquals(State.t, target.currentState());
        target.reset();
        assertEquals(State.i, target.currentState());
    }
}
