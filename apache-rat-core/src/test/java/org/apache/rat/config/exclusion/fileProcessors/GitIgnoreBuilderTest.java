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
import java.net.URISyntaxException;
import java.nio.file.Paths;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

            assertCorrect(new GitIgnoreBuilder() {
                @Override
                protected Optional<File> globalGitIgnore() {
                    return Optional.empty();
                }
            }, matches, notMatches);
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
        for (String test : notMatching) {
            DocumentName name = documentName.resolve(test);
            assertThat(matcher.matches(name)).as(test).isFalse();
        }
    }

    @Test
    public void test_RAT_335() throws URISyntaxException {
        GitIgnoreBuilder underTest = new GitIgnoreBuilder() {
            @Override
            protected Optional<File> globalGitIgnore() {
                return Optional.empty();
            }
        };
        URL url = GitIgnoreBuilderTest.class.getClassLoader().getResource("GitIgnoreBuilderTest/src/");
        File file = Paths.get(url.toURI()).toFile();

        DocumentName documentName = DocumentName.builder(file).build();
        List<MatcherSet> matcherSets = underTest.build(documentName);
        DocumentNameMatcher matcher = MatcherSet.merge(matcherSets).createMatcher();

        assertThat(matcher.toString()).isEqualTo("matcherSet(or('included dir1/.gitignore', 'included .gitignore'), or('excluded dir1/.gitignore', **/.gitignore, 'excluded .gitignore'))");

        // files that should be checked:
        List<String> notMatching = Arrays.asList("README.txt", "dir1/dir1.md", "dir2/dir2.txt", "dir3/file3.log", "dir1/file1.log", "local-should-precede-global.xml");

        // files that should be ignored:
        List<String> matching = Arrays.asList(".gitignore", "root.md", "dir1/.gitignore", "dir1/dir1.txt",  "dir2/dir2.md", "dir3/dir3.log", "local-should-precede-global.md");

        assertCorrect(matcherSets, documentName.getBaseDocumentName(), matching, notMatching);
    }

    /**
     * Test that exclusions from a global gitignore are also applied,
     * see <a href="https://issues.apache.org/jira/browse/RAT-473">RAT-473</a> for details.
     */
    @Test
    public void test_global_gitignore() throws URISyntaxException {
        GitIgnoreBuilder underTest = new GitIgnoreBuilder() {
            @Override
            protected Optional<File> globalGitIgnore() {
                URL globalGitIgnoreUrl = GitIgnoreBuilderTest.class.getClassLoader().getResource("GitIgnoreBuilderTest/global-gitignore");
                String globalGitIgnore;
                try {
                    globalGitIgnore = Paths.get(globalGitIgnoreUrl.toURI()).toString();
                    return Optional.of(new File(globalGitIgnore));
                } catch (URISyntaxException e) {
                    System.err.println("Unable to load global gitignore in test from " + globalGitIgnoreUrl + ", due to " + e.getMessage());
                    return Optional.empty();
                }
            }
        };
        URL url = GitIgnoreBuilderTest.class.getClassLoader().getResource("GitIgnoreBuilderTest/src/");
        File file = Paths.get(url.toURI()).toFile();

        DocumentName documentName = DocumentName.builder(file).build();
        List<MatcherSet> matcherSets = underTest.build(documentName);
        DocumentNameMatcher matcher = MatcherSet.merge(matcherSets).createMatcher();

        assertThat(matcher.toString()).isEqualTo("matcherSet(or('included dir1/.gitignore', 'included .gitignore', 'included global gitignore'), or('excluded dir1/.gitignore', **/.gitignore, 'excluded .gitignore', 'excluded global gitignore'))");

        // files that should be checked:
        // "local-should-precede-global.md" should be 'matching'
        // here, but that is a future improvement (RAT-476)
        List<String> notMatching = Arrays.asList("dir1/dir1.md", "dir2/dir2.txt", "dir3/file3.log", "dir1/file1.log", "local-should-precede-global.md", "local-should-precede-global.xml");

        // files that should be ignored:
        List<String> matching = Arrays.asList(".gitignore", "README.txt", "root.md", "dir1/.gitignore", "dir1/dir1.txt", "dir2/dir2.md", "dir3/dir3.log");

        assertCorrect(matcherSets, documentName.getBaseDocumentName(), matching, notMatching);
    }
}
