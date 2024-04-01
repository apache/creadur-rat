/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.configuration.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.apache.rat.analysis.matchers.FullTextMatcher;
import org.apache.rat.analysis.matchers.SimpleTextMatcher;
import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.XMLConfigurationReader;
import org.apache.rat.license.ILicense;
import org.apache.rat.utils.DefaultLog;
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
        description.setChildren(DefaultLog.INSTANCE, underTest, attributes);
        description.setUnlabledText(DefaultLog.INSTANCE, underTest, "example text");

        SimpleTextMatcher m = underTest.build();
        assertEquals("example text", m.getText());
        assertEquals("IDValue", m.getId());
        assertEquals(FullTextMatcher.class, m.getClass());

        boolean foundText = false;
        for (Description d : description.childrenOfType(Component.Type.Unlabeled)) {
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
        description.setChildren(DefaultLog.INSTANCE, underTest, attributes);
        description.setUnlabledText(DefaultLog.INSTANCE, underTest, "exampletext");

        SimpleTextMatcher m = underTest.build();
        assertEquals("exampletext", m.getText());
        assertEquals(SimpleTextMatcher.class, m.getClass());

        boolean foundText = false;
        for (Description d : description.childrenOfType(Component.Type.Unlabeled)) {
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

    @Test
    public void xmlTest() {
        String configStr = "<rat-config>"
                + "        <families>"
                + "            <family id='newFam' name='my new family' />"
                + "        </families>"
                + "        <licenses>"
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>"
                + "                <text>The text to match</text>"
                + "            </license>"
                + "        </licenses>"
                + "    </rat-config>"
                + "";
        
        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        SortedSet<ILicense> licenses = reader.readLicenses();
        assertEquals(1, licenses.size());
    }
}
