package org.apache.rat.analysis.matchers;

import static org.junit.Assert.assertEquals;

import org.apache.rat.analysis.IHeaderMatcher.State;
import org.junit.Test;

public class SimpleCopyrightTests {

    CopyrightMatcher target = new CopyrightMatcher(null,null,null);
    
    @Test
    public void testTrueIsAlwaysTrue() {
        
        assertEquals( State.i, target.currentState());
        assertEquals( State.t, target.matches("hello Copyright 1999"));
        assertEquals( State.t, target.currentState());
        assertEquals( State.t, target.matches("A non matching line"));
        assertEquals( State.t, target.currentState());        
        assertEquals( State.t, target.finalizeState()); 
        assertEquals( State.t, target.currentState());
        target.reset();
        assertEquals( State.i, target.currentState());
    }
}
