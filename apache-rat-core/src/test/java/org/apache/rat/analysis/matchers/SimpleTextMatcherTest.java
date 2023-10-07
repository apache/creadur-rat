package org.apache.rat.analysis.matchers;

import static org.junit.Assert.assertEquals;

import org.apache.rat.analysis.IHeaderMatcher.State;
import org.junit.Before;
import org.junit.Test;

public class SimpleTextMatcherTest {

    SimpleTextMatcher target;

    @Before
    public void setup() {
        target = new SimpleTextMatcher("hello");
    }

    @Test
    public void testMatch() {
        assertEquals(State.i, target.currentState());
        assertEquals(State.i, target.matches("what in the world"));
        assertEquals(State.i, target.currentState());
        assertEquals(State.t, target.matches("hello world"));
        assertEquals(State.t, target.currentState());
        assertEquals(State.t, target.finalizeState());
        assertEquals(State.t, target.currentState());
        target.reset();
        assertEquals(State.i, target.currentState());
    }

    @Test
    public void testNoMatch() {
        assertEquals(State.i, target.currentState());
        assertEquals(State.i, target.matches("what in the world"));
        assertEquals(State.i, target.currentState());
        assertEquals(State.i, target.matches("hell o'there"));
        assertEquals(State.i, target.currentState());
        assertEquals(State.f, target.finalizeState());
        assertEquals(State.f, target.currentState());
        target.reset();
        assertEquals(State.i, target.currentState());
    }

    @Test
    public void testTrueIsAlwaysTrue() {
        assertEquals(State.i, target.currentState());
        assertEquals(State.t, target.matches("hello world"));
        assertEquals(State.t, target.currentState());
        assertEquals(State.t, target.matches("A non matching line"));
        assertEquals(State.t, target.currentState());
        assertEquals(State.t, target.finalizeState());
        assertEquals(State.t, target.currentState());
        target.reset();
        assertEquals(State.i, target.currentState());
    }

    @Test
    public void testIndeterminent() {
        target = new SimpleTextMatcher("not a match");
        assertEquals(State.i, target.currentState());
        assertEquals(State.i, target.matches("hello world"));
        assertEquals(State.i, target.currentState());
        assertEquals(State.i, target.matches("A non matching line"));
        assertEquals(State.i, target.currentState());
        assertEquals(State.f, target.finalizeState());
        assertEquals(State.f, target.currentState());
        target.reset();
        assertEquals(State.i, target.currentState());
    }
}
