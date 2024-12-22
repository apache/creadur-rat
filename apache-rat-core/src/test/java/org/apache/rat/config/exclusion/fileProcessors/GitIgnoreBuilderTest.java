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
import static org.assertj.core.api.Assertions.not;

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

        List<String> excluded = Arrays.asList("some/things", "some/fish", "another/red_fish");

        List<String> included = Arrays.asList("some/thingone");

        writeFile(".gitignore", Arrays.asList(lines));

        GitIgnoreBuilder processor = new GitIgnoreBuilder();
        MatcherSet matcherSet = processor.build(baseName);
        assertThat(matcherSet.includes()).isPresent();
        assertThat(matcherSet.excludes()).isPresent();

        DocumentNameMatcher matcher = matcherSet.includes().orElseThrow(() -> new IllegalStateException("How?"));
        for (String name : included) {
            DocumentName docName = baseName.resolve(name);
            assertThat(matcher.matches(docName)).as(docName.getName()).isTrue();
        }

        matcher = matcherSet.excludes().orElseThrow(() -> new IllegalStateException("How?"));
        for (String name : excluded) {
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
            assertThat(underTest.modifyEntry(testName, source)).isNotPresent();
            assertThat(underTest.getIncluded().toString()).isEqualTo(expected);
        } else {
            assertThat(underTest.modifyEntry(testName, source)).contains(expected);
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

    private void assertMatches(DocumentName documentName, DocumentNameMatcher matcher, String[] matching, String[] notMatching) {
        for (String test : matching) {
            DocumentName name = documentName.resolve(test);
            assertThat(matcher.matches(name)).as(test).isTrue();
        }
        for (String test: notMatching) {
            DocumentName name = documentName.resolve(test);
            assertThat(matcher.matches(name)).as(test).isFalse();
        }
    }

    @Test
    public void test_RAT_335() {
        GitIgnoreBuilder underTest = new GitIgnoreBuilder();
        URL url = GitIgnoreBuilderTest.class.getClassLoader().getResource("GitIgnoreBuilderTest/src/");
        File file = new File(url.getFile());

        DocumentName documentName = DocumentName.builder(file).build();
        MatcherSet matcherSet = underTest.build(documentName);
        assertThat(matcherSet.excludes()).isPresent();
        assertThat(matcherSet.includes()).isPresent();

        // includes test
        DocumentNameMatcher matcher = matcherSet.includes().orElseThrow(() -> new IllegalStateException("How?"));
        assertThat(matcher.toString()).isEqualTo("or('included dir1/.gitignore', 'included .gitignore')");
        assertMatches(documentName, matcher, new String[]{"subdir/file1.log", "dir1/dir1.md", "dir1/somedir/dir1.md",
                        "dir1/file1.log"},
                 new String[]{"dir1/joe.txt", "subdir/joe.txt", "subdir/joe.md", "dir1/joe.md" });

        // excludes tests
        matcher = matcherSet.excludes().orElseThrow(() -> new IllegalStateException("How?"));
        assertThat(matcher.toString()).isEqualTo("or('excluded dir1/.gitignore', 'excluded .gitignore')");
        assertMatches(documentName, matcher, new String[]{ "dir1/file1.txt", "dir1/somedir/file1.txt", "dir1/file1.log",
                        "dir1/somedir/file1.log", "subdir/dir1.md", "subdir/some.log"},
                new String[]{"subdir/afile.txt" });

    }
}
