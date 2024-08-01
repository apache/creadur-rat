/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.configuration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.builders.AbstractBuilder;
import org.apache.rat.configuration.builders.AllBuilder;
import org.apache.rat.configuration.builders.AnyBuilder;
import org.apache.rat.configuration.builders.CopyrightBuilder;
import org.apache.rat.configuration.builders.MatcherRefBuilder;
import org.apache.rat.configuration.builders.NotBuilder;
import org.apache.rat.configuration.builders.RegexBuilder;
import org.apache.rat.configuration.builders.SpdxBuilder;
import org.apache.rat.configuration.builders.TextBuilder;
import org.junit.jupiter.api.Test;

public class XMLConfigurationReaderTest {

    public static final String[] EXPECTED_IDS = { "AL", "BSD-3", "CDDL1", "GEN", "GPL1", "GPL2", "GPL3", "MIT", "OASIS",
            "W3C", "W3CD" };

    public static final String[] EXPECTED_LICENSES = { "AL", "ASL", "BSD-3", "DOJO", "TMF", "CDDL1", "ILLUMOS", "GEN", "GPL1", "GPL2",
            "GPL3", "MIT", "OASIS", "W3C", "W3CD" };

    @Test
    public void approvedLicenseIdTest() throws URISyntaxException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = XMLConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml");
        reader.read(url.toURI());

        Collection<String> readCategories = reader.approvedLicenseId();

        assertArrayEquals(EXPECTED_IDS, readCategories.toArray(new String[readCategories.size()]));
    }

    @Test
    public void LicensesTest() throws URISyntaxException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = XMLConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml");
        reader.read(url.toURI());

        Collection<String> readCategories = reader.readLicenses().stream().map(x -> x.getId())
                .collect(Collectors.toList());
        assertArrayEquals(EXPECTED_LICENSES, readCategories.toArray(new String[readCategories.size()]));
    }

    @Test
    public void LicenseFamiliesTest() throws URISyntaxException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = XMLConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml");
        reader.read(url.toURI());

        Collection<String> readCategories = reader.readFamilies().stream().map(x -> x.getFamilyCategory().trim())
                .collect(Collectors.toList());
        assertArrayEquals(EXPECTED_IDS, readCategories.toArray(new String[readCategories.size()]));
    }

    private void checkMatcher(String name, Class<? extends AbstractBuilder> clazz) {
        AbstractBuilder builder = MatcherBuilderTracker.getMatcherBuilder(name);
        assertNotNull(builder);
        assertTrue(clazz.isAssignableFrom(builder.getClass()), () -> name + " is not an instanceof " + clazz.getName());
    }

    @Test
    public void checkSystemMatcherTest() throws URISyntaxException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = XMLConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml");
        reader.read(url.toURI());
        reader.readMatcherBuilders();
        checkMatcher("all", AllBuilder.class);
        checkMatcher("any", AnyBuilder.class);
        checkMatcher("copyright", CopyrightBuilder.class);
        checkMatcher("matcherRef", MatcherRefBuilder.class);
        checkMatcher("not", NotBuilder.class);
        checkMatcher("regex", RegexBuilder.class);
        checkMatcher("spdx", SpdxBuilder.class);
        checkMatcher("text", TextBuilder.class);
    }

    @Test
    public void descriptionTest() throws NoSuchMethodException, SecurityException, URISyntaxException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = XMLConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml");
        reader.read(url.toURI());
        reader.readMatcherBuilders();

        IHeaderMatcher.Builder builder = MatcherBuilderTracker.getMatcherBuilder("copyright");
        Description desc = DescriptionBuilder.buildMap(builder.getClass());
        assertNotNull(desc, () -> "did not build description for 'copyright'");
        assertEquals("copyright", desc.getCommonName());
        assertEquals(ComponentType.MATCHER, desc.getType());
        assertFalse(desc.isCollection());
        assertNull(desc.getChildType());
        assertEquals("Matches copyright statements.", desc.getDescription());
        assertEquals(4, desc.getChildren().size());
        assertTrue(desc.getChildren().containsKey("end"));
        assertTrue(desc.getChildren().containsKey("start"));
        assertTrue(desc.getChildren().containsKey("owner"));
        assertTrue(desc.getChildren().containsKey("id"));
    }
}
