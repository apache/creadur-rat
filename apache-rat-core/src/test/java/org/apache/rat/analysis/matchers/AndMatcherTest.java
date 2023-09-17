package org.apache.rat.analysis.matchers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.rat.analysis.IHeaderMatcher;
import org.junit.Test;

public class AndMatcherTest {

    @Test
    public void standardTest() {
        IHeaderMatcher one = mock(IHeaderMatcher.class);
        IHeaderMatcher two = mock(IHeaderMatcher.class);
        when(one.matches(any())).thenReturn( true ).thenReturn(false);
        when(two.matches(any())).thenReturn( false ).thenReturn(true);
        
        AndMatcher target = new AndMatcher( "Testing", Arrays.asList( one, two ));
        assertFalse( target.matches("hello"));
        assertTrue( target.matches("world"));
    }
}
