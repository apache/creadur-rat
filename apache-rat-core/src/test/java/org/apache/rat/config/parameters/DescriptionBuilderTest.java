package org.apache.rat.config.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.rat.analysis.matchers.AndMatcher;
import org.apache.rat.analysis.matchers.CopyrightMatcher;
import org.junit.jupiter.api.Test;

public class DescriptionBuilderTest {

    @Test
    public void matcherMapBuildTest() {
        
        Description underTest = DescriptionBuilder.buildMap(CopyrightMatcher.class);
        assertEquals(Component.Type.Matcher, underTest.getType());
        assertEquals("copyright", underTest.getCommonName());
        assertNotNull(underTest.getDescription());
        assertNull(underTest.getParamValue());
        assertEquals(4,underTest.getChildren().size());
        assertTrue(underTest.getChildren().containsKey("id"));
        assertTrue(underTest.getChildren().containsKey("start"));
        assertTrue(underTest.getChildren().containsKey("stop"));
        assertTrue(underTest.getChildren().containsKey("owner"));

        underTest = DescriptionBuilder.buildMap(AndMatcher.class);
        assertEquals(Component.Type.Matcher, underTest.getType());
        assertEquals("all", underTest.getCommonName());
        assertNotNull(underTest.getDescription());
        assertNull(underTest.getParamValue());
        assertEquals(2,underTest.getChildren().size());
        assertTrue(underTest.getChildren().containsKey("id"));
        assertTrue(underTest.getChildren().containsKey("enclosed"));
        Description desc = underTest.getChildren().get("enclosed");
        assertEquals(Component.Type.Matcher, underTest.getType());
    }

}
