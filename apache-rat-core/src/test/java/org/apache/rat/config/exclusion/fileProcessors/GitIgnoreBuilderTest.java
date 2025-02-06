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
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FSInfoTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public class GitIgnoreBuilderTest extends AbstractIgnoreBuilderTest {

    @Test
    public void processExampleFileTest() throws IOException {
        try {
            String[] lines = {
                    "# somethings",
                    "!thingone", "thing*", System.lineSeparator(),
                    "# some fish",
                    "**/fish", "*_fish",
                    "# some colorful directories",
                    "red/", "blue/*/"};
            List<String> matches = Arrays.asList("some/things", "some/fish", "another/red_fish");

            List<String> notMatches = Arrays.asList("some/thingone", "thingone");

            writeFile(".gitignore", Arrays.asList(lines));

            assertCorrect(new GitIgnoreBuilder(), matches, notMatches);
        } finally {
            System.getProperties().remove("FSInfo");
        }
    }

    // see https://git-scm.com/docs/gitignore
    @ParameterizedTest
    @MethodSource("modifyEntryData")
    public void modifyEntryTest(DocumentName.FSInfo fsInfo, String source, String expected) {
        GitIgnoreBuilder underTest = new GitIgnoreBuilder();
        DocumentName testName = DocumentName.builder(fsInfo).setName("GitIgnoreBuilderTest").setBaseName("testDir").build();
        List<MatcherSet> matcherSets = new ArrayList<>();
        Optional<String> entry = underTest.modifyEntry(matcherSets::add, testName, source);

        if (source.endsWith("/")) {
            assertThat(entry).isNotPresent();
            assertThat(matcherSets).hasSize(1);
            DocumentNameMatcher matcher = matcherSets.get(0).createMatcher();
            assertThat(matcher.toString()).isEqualTo(expected);
        } else {
            assertThat(entry.get()).isEqualTo(expected);
        }
    }

    private static Stream<Arguments> modifyEntryData() {
        List<Arguments> lst = new ArrayList<>();
        for (DocumentName.FSInfo fsInfo : FSInfoTest.TEST_SUITE) {
            lst.add(Arguments.of(fsInfo, "\\#filename", "**/#filename"));

            lst.add(Arguments.of(fsInfo, "!#filename", "!**/#filename"));
            lst.add(Arguments.of(fsInfo, "\\#filename", "**/#filename"));
            lst.add(Arguments.of(fsInfo, "!#filename", "!**/#filename"));
            lst.add(Arguments.of(fsInfo, "/filename", "filename"));
            lst.add(Arguments.of(fsInfo, "file/name", "file/name"));
            lst.add(Arguments.of(fsInfo, "/file/name", "file/name"));
            lst.add(Arguments.of(fsInfo, "filename", "**/filename"));
            lst.add(Arguments.of(fsInfo, "filename/", "not(and(isDirectory, **/filename))"));
            lst.add(Arguments.of(fsInfo, "/filename/", "not(and(isDirectory, filename))"));
            // inclusion by itself becomes nothing.
            lst.add(Arguments.of(fsInfo, "!filename/", "TRUE"));
        }
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
        List<MatcherSet> matcherSets = underTest.build(documentName);
        DocumentNameMatcher matcher = MatcherSet.merge(matcherSets).createMatcher();

        DocumentName candidate = DocumentName.builder()
                .setName("/home/claude/apache/creadur-rat/apache-rat-core/target/test-classes/GitIgnoreBuilderTest/src/dir1/file1.log")
                .setBaseName("home/claude/apache/creadur-rat/apache-rat-core/target/test-classes/GitIgnoreBuilderTest/src/").build();
        System.out.println("Decomposition for "+candidate);

        assertThat(matcher.toString()).isEqualTo("matcherSet(or('included dir1/.gitignore', 'included .gitignore'), or('excluded dir1/.gitignore', **/.gitignore, 'excluded .gitignore'))");

        List<String> notMatching = Arrays.asList("README.txt", "dir1/dir1.md", "dir2/dir2.txt", "dir3/file3.log", "dir1/file1.log");

        List<String> matching = Arrays.asList(".gitignore", "root.md", "dir1/.gitignore", "dir1/dir1.txt",  "dir2/dir2.md", "dir3/dir3.log");

        assertCorrect(matcherSets, documentName.getBaseDocumentName(), matching, notMatching);
    }
}
