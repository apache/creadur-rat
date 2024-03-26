package org.apache.rat.configuration.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.apache.rat.analysis.matchers.FullTextMatcher;
import org.apache.rat.analysis.matchers.SimpleTextMatcher;
import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.junit.jupiter.api.Test;

public class TextBuilderTest {

    Map<String, String> attributes;

    TextBuilderTest() {
        attributes = new HashMap<>();
        attributes.put("id", "IDValue");
    }

    @Test
    public void fullTextMatcherTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        TextBuilder underTest = new TextBuilder();
        attributes.put("id", "IDValue");

        Description description = DescriptionBuilder.buildMap(underTest.builtClass());
        description.setChildren(underTest, attributes);
        description.setUnlabledText(underTest, "example text");

        SimpleTextMatcher m = underTest.build();
        assertEquals("example text", m.getText());
        assertEquals("IDValue", m.getId());
        assertEquals(FullTextMatcher.class, m.getClass());

        boolean foundText = false;
        for (Description d : description.childrenOfType(Component.Type.Unlabled)) {
            if (!d.isCollection()) {
                assertEquals("example text", d.getter(m.getClass()).invoke(m));
                foundText = true;
            }
        }
        assertTrue(foundText);
        foundText = false;
        for (Description d : description.childrenOfType(Component.Type.Parameter)) {
            if (!d.isCollection()) {
                assertEquals("IDValue", d.getter(m.getClass()).invoke(m));
                foundText = true;
            }
        }
        assertTrue(foundText);

    }

    @Test
    public void simpleTextMatcherTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        TextBuilder underTest = new TextBuilder();

        Description description = DescriptionBuilder.buildMap(underTest.builtClass());
        description.setChildren(underTest, attributes);
        description.setUnlabledText(underTest, "exampletext");

        SimpleTextMatcher m = underTest.build();
        assertEquals("exampletext", m.getText());
        assertEquals(SimpleTextMatcher.class, m.getClass());

        boolean foundText = false;
        for (Description d : description.childrenOfType(Component.Type.Unlabled)) {
            if (!d.isCollection()) {
                assertEquals("exampletext", d.getter(m.getClass()).invoke(m));
                foundText = true;
            }
        }
        assertTrue(foundText);
        foundText = false;
        for (Description d : description.childrenOfType(Component.Type.Parameter)) {
            if (!d.isCollection()) {
                assertEquals("IDValue", d.getter(m.getClass()).invoke(m));
                foundText = true;
            }
        }
        assertTrue(foundText);
    }

}
