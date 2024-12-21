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
package org.apache.rat.config.exclusion.fileProcessors;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class GitIgnoreBuilderTest extends AbstractIgnoreBuilderTest {

    @Test
    public void processExampleFileTest() throws IOException {
        String[] lines = {
                "# somethings",
                "!thingone", "thing*", System.lineSeparator(),
                "# some fish",
                "**/fish", "*_fish",
                "# some colorful directories",
                "red/", "blue/*/"};

        List<String> expected = Arrays.asList("some/things", "some/fish", "another/red_fish");

        List<String> ignored = Arrays.asList("some/thinggone");

        // "thingone",
        writeFile(".gitignore", Arrays.asList(lines));

        GitIgnoreBuilder processor = new GitIgnoreBuilder();
        MatcherSet matcherSet = processor.build(baseName);
        assertThat(matcherSet.includes()).isPresent();
        assertThat(matcherSet.excludes()).isPresent();

        DocumentNameMatcher matcher = matcherSet.includes().orElseThrow(() -> new IllegalStateException("How?"));
        for (String name : expected) {
            DocumentName docName = baseName.resolve(name);
            assertThat(matcher.matches(docName)).as(docName.getName()).isTrue();
        }

        matcher = matcherSet.excludes().orElseThrow(() -> new IllegalStateException("How?"));
        for (String name : ignored) {
            DocumentName docName = baseName.resolve(name);
            assertThat(matcher.matches(docName)).as(docName.getName()).isTrue();
        }
    }

    // see https://git-scm.com/docs/gitignore
    @ParameterizedTest
    @MethodSource("modifyEntryData")
    public void modifyEntryTest(String source, String expected) {
        GitIgnoreBuilder underTest = new GitIgnoreBuilder();
        DocumentName testName = DocumentName.builder().setName("GitIgnoreBuilderTest").setBaseName("testDir").build();
        if (source.endsWith("/")) {
            assertThat(underTest.modifyEntry(testName, source)).isEqualTo(null);
            assertThat(underTest.getIncluded().toString()).isEqualTo(expected);
        } else {
            assertThat(underTest.modifyEntry(testName, source)).isEqualTo(expected);
        }
    }

    private static Stream<Arguments> modifyEntryData() {
        List<Arguments> lst = new ArrayList<>();

        lst.add(Arguments.of("\\#filename", "**/#filename"));

        lst.add(Arguments.of("!#filename", "!**/#filename"));
        lst.add(Arguments.of("\\#filename", "**/#filename"));
        lst.add(Arguments.of("!#filename", "!**/#filename"));
        lst.add(Arguments.of("/filename", "filename"));
        lst.add(Arguments.of("file/name", "file/name"));
        lst.add(Arguments.of("/file/name", "file/name"));
        lst.add(Arguments.of("filename", "**/filename"));
        lst.add(Arguments.of("filename/", "and(isDirectory, **/filename)"));
        lst.add(Arguments.of("/filename/", "and(isDirectory, filename)"));

        return lst.stream();
    }

    @Test
    public void test_RAT_335() {
        GitIgnoreBuilder underTest = new GitIgnoreBuilder();
        URL url = GitIgnoreBuilderTest.class.getClassLoader().getResource("RAT_355/src/");
        File file = new File(url.getFile());

        DocumentName documentName = DocumentName.builder(file).build();
        MatcherSet matcherSet = underTest.build(documentName);
        assertThat(matcherSet.excludes()).isPresent();
        assertThat(matcherSet.includes()).isPresent();
        DocumentNameMatcher matcher = matcherSet.includes().orElseThrow(() -> new IllegalStateException("How?"));
        assertThat(matcher.toString()).isEqualTo("or(/dir1/.gitignore, /.gitignore)");

        DocumentName name = documentName.resolve("subdir/file1.log" );
        assertThat(matcher.matches(name)).isTrue();
        name = documentName.resolve("subdir/joe.txt" );
        assertThat(matcher.matches(name)).isFalse();
        name = documentName.resolve("subdir/file1.log" );
        assertThat(matcher.matches(name)).isTrue();
        name = documentName.resolve("subdir/joe.md" );
        assertThat(matcher.matches(name)).isTrue();

        name = documentName.resolve("dir1/joe.txt" );
        assertThat(matcher.matches(name)).isTrue();
        name = documentName.resolve("dir1/file1.md" );
        assertThat(matcher.matches(name)).isTrue();

        matcher = matcherSet.excludes().orElseThrow(() -> new IllegalStateException("How?"));
        assertThat(matcher.toString()).isEqualTo("or(/dir1/.gitignore, /.gitignore)");

        name = documentName.resolve("dir1/dir1.md" );
        assertThat(matcher.matches(name)).isTrue();
        name = documentName.resolve("subdir/dir1.md" );
        assertThat(matcher.matches(name)).isFalse();
        name = documentName.resolve("dir1/file1.log" );
        assertThat(matcher.matches(name)).isTrue();
        name = documentName.resolve("subdir/file1.log" );
        assertThat(matcher.matches(name)).isTrue();
    }
}
