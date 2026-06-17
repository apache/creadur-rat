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

import org.apache.rat.ConfigurationException;
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

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class XMLConfigurationReaderTest {

    public static final String[] EXPECTED_IDS = {"AL", "BSD-3", "CDDL1", "GPL", "MIT", "OASIS",
            "W3C", "W3CD"};

    public static final String[] APPROVED_IDS = {"AL", "BSD-3", "CDDL1", "MIT", "OASIS",
            "W3C", "W3CD"};

    public static final String[] EXPECTED_LICENSES = {"AL1.0", "AL1.1", "AL2.0", "BSD-3", "DOJO", "TMF", "CDDL1", "ILLUMOS", "GPL1", "GPL2",
            "GPL3", "MIT", "OASIS", "W3C", "W3CD"};

    @Test
    void approvedLicenseIdTest() throws URISyntaxException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = XMLConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml");
        assertThat(url).isNotNull();
        reader.read(url.toURI());

        Collection<String> readCategories = reader.approvedLicenseId();
        assertThat(readCategories.toArray(new String[readCategories.size()]))
                .containsExactly(APPROVED_IDS);
    }

    @Test
    void LicensesTest() throws URISyntaxException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = XMLConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml");
        reader.read(url.toURI());
        assertThat(reader.readLicenses().stream().map(IHeaderMatcher::getId).toArray(String[]::new))
                .containsExactly(EXPECTED_LICENSES);
    }

    @Test
    void LicenseFamiliesTest() throws URISyntaxException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = XMLConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml");
        reader.read(url.toURI());

        assertThat(reader.readFamilies().stream().map(x -> x.getFamilyCategory().trim()).toArray(String[]::new))
                .containsExactly(EXPECTED_IDS);
    }

    private void checkMatcher(String name, Class<? extends AbstractBuilder> clazz) {
        AbstractBuilder builder = MatcherBuilderTracker.getMatcherBuilder(name);
        assertThat(builder).isNotNull();
        assertThat(clazz.isAssignableFrom(builder.getClass())).as(() -> name + " is not an instanceof " + clazz.getName())
                .isTrue();
    }

    @Test
    void checkSystemMatcherTest() throws URISyntaxException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URI uri = XMLConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml").toURI();
        assertThat(uri).isNotNull();
        reader.read(uri);
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
    void descriptionTest() throws SecurityException, URISyntaxException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URI uri = XMLConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml").toURI();
        assertThat(uri).isNotNull();
        reader.read(uri);
        reader.readMatcherBuilders();

        IHeaderMatcher.Builder builder = MatcherBuilderTracker.getMatcherBuilder("copyright");
        Description desc = DescriptionBuilder.buildMap(builder.getClass());
        assertThat(desc).as(() -> "did not build description for 'copyright'").isNotNull();
        assertThat(desc.getCommonName()).isEqualTo("copyright");
        assertThat(desc.getType()).isEqualTo(ComponentType.MATCHER);
        assertThat(desc.isCollection()).isFalse();
        assertThat(desc.getChildType()).isNull();
        assertThat(desc.getDescription()).isEqualTo("A matcher that matches Copyright text. Uses regular expressions and so should only " +
                "be used when looking for copyrights with specific patterns that are not caught by a standard text " +
                "matcher. This matcher will match \"(C)\", \"copyright\", or \"©\". (text is not case sensitive). " +
                "It will also match things like Copyright (c) joe 1995 as well as Copyright (C) 1995 joe and " +
                "Copyright (C) joe 1995.");
        assertThat(desc.getChildren()).hasSize(4);
        assertThat(desc.getChildren()).containsKey("end");
        assertThat(desc.getChildren()).containsKey("start");
        assertThat(desc.getChildren()).containsKey("owner");
        assertThat(desc.getChildren()).containsKey("id");
    }

    @Test
    void checkWithXXETest() throws SecurityException {
        final String contents = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE user [
                    <!ENTITY xxe SYSTEM "file:///etc/passwd">
                ]>
                <user>
                    <name>John Doe</name>
                    <bio>&xxe;</bio>
                </user>
                """;

        final XMLConfigurationReader reader = new XMLConfigurationReader();
        final StringReader xml = new StringReader(contents);

       assertThatThrownBy(()->reader.read(xml))
               .isInstanceOf(ConfigurationException.class)
               .hasMessageContaining("Unable to read inputSource");
    }

}
