package org.apache.rat.analysis.matchers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.rat.analysis.IHeaderMatcher;
import org.junit.Test;

public class NotMatcherTest {

    @Test
    public void testTrue() {
        IHeaderMatcher enclosed = mock(IHeaderMatcher.class);
        when(enclosed.matches(any())).thenReturn(true);
        NotMatcher matcher = new NotMatcher(enclosed);
        assertFalse( matcher.matches("dummy"));
    }
    
    @Test
    public void testFalse() {
        IHeaderMatcher enclosed = mock(IHeaderMatcher.class);
        when(enclosed.matches(any())).thenReturn(false);
        NotMatcher matcher = new NotMatcher(enclosed);
        assertTrue( matcher.matches("dummy"));
    }
}
