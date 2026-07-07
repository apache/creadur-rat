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
package org.apache.rat.config.exclusion;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.DocumentName;
import org.apache.rat.report.xml.writer.XmlWriter;
import org.apache.rat.utils.StandardXmlFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.apache.rat.document.FSInfoTest.OSX;
import static org.apache.rat.document.FSInfoTest.UNIX;
import static org.apache.rat.document.FSInfoTest.WINDOWS;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ExclusionProcessorTest {

    @TempDir
    private static Path tempDir;

    private void testParseExclusion(DocumentName basedir, DocumentNameMatcher nameMatcher, DocumentName name, boolean expected) {
        assertThat(nameMatcher.matches(name)).as(() -> format("Failed on [%s %s]%n%s", basedir, name, dump(nameMatcher, name))).isEqualTo(expected);
    }

    private String dump(DocumentNameMatcher nameMatcher, DocumentName name) {
        StringBuilder sb = new StringBuilder();
        nameMatcher.decompose(name).forEach(s -> sb.append(s).append("\n"));
        return sb.toString();
    }

    private DocumentName mkName(DocumentName baseDir, String pth) throws IOException {
        DocumentName result = baseDir.resolve(ExclusionUtils.convertSeparator(pth, "/", baseDir.getDirectorySeparator()));
        DocumentName mocked = spy(result);

        String fn = result.localized(FileSystems.getDefault().getSeparator());
        File file = tempDir.resolve(fn.substring(1)).toFile();
        File parent = file.getParentFile();
        if (parent.exists() && !parent.isDirectory() && !parent.delete()) {
            fail(() -> "Unable to delete parent: " + parent);
        }
        if (!parent.exists() && !parent.mkdirs()) {
            fail((() -> "Unable to create parent: " + parent));
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else {
                FileUtils.delete(file);
            }
        }
        if (!file.createNewFile()) {
            fail(() -> "Unable to create file: " + file);
        }
        when(mocked.asFile()).thenReturn(file);
        return mocked;
    }

    @ParameterizedTest
    @MethodSource("getDocumentNames")
    void defaultTest(DocumentName basedir) throws IOException {
        ExclusionProcessor p = new ExclusionProcessor();
        testParseExclusion(basedir, p.getNameMatcher(basedir), mkName(basedir, "hello"), true);
    }

    @ParameterizedTest
    @MethodSource("getDocumentNames")
    void addExcludedCollectionTest(DocumentName basedir) throws IOException {
        ExclusionProcessor p = new ExclusionProcessor().addExcludedCollection(StandardCollection.MISC);
        // "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*"
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir,"hello"), true);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir,"hello~"), false);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir, "#hello#"), false);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir, ".#hello"), false);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir, "%hello%"), false);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir, "._hello"), false);
    }

    @ParameterizedTest
    @MethodSource("getDocumentNames")
    void addExcludedAndIncludedCollectionTest(DocumentName basedir) throws IOException {
        ExclusionProcessor p = new ExclusionProcessor().addExcludedCollection(StandardCollection.MISC)
                .addIncludedCollection(StandardCollection.HIDDEN_FILE);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir,"hello"), true);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir, "hello~"), false);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir, "#hello#"), false);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir, ".#hello"), true);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir, "%hello%"), false);
        testParseExclusion(basedir, p.getNameMatcher(basedir),  mkName(basedir, "._hello"), true);
    }

    private void assertExclusions(DocumentName basedir, String pattern, Map<String,Boolean> expectedMap) throws IOException {
        String[] paths = {"a/b/foo", "b/foo", "foo", "foo/x", "foo/x/y", "b/foo/x",
                "b/foo/x/y", "a/b/foo/x", "a/b/foo/x/y"};
        ExclusionProcessor p = new ExclusionProcessor().addExcludedPatterns(Collections.singletonList(pattern));
        DocumentNameMatcher pathMatcher = p.getNameMatcher(basedir);
        for (String pth : paths) {
            Boolean expected = expectedMap.get(pth);
            if (expected == null) {
                throw new RuntimeException("Missing expected value for " + pth + " in pattern " + pattern);
            }
            testParseExclusion(basedir, pathMatcher, mkName(basedir, pth), expected);
        }
    }

    @ParameterizedTest
    @MethodSource("getDocumentNames")
    void addExcludedPatternsTest(DocumentName basedir) throws IOException {
        Map<String,Boolean> expectedMap = new HashMap<>();

        expectedMap.put("a/b/foo", true);
        expectedMap.put("b/foo", true);
        expectedMap.put("foo", false);
        expectedMap.put("foo/x", true);
        expectedMap.put("foo/x/y", true);
        expectedMap.put("b/foo/x", true);
        expectedMap.put("b/foo/x/y", true);
        expectedMap.put("a/b/foo/x", true);
        expectedMap.put("a/b/foo/x/y",true);
        assertExclusions(basedir, "foo", expectedMap);

        expectedMap.clear();
        expectedMap.put("a/b/foo", true);
        expectedMap.put("b/foo", true);
        expectedMap.put("foo", true);
        expectedMap.put("foo/x", false);
        expectedMap.put("foo/x/y", true);
        expectedMap.put("b/foo/x", true);
        expectedMap.put("b/foo/x/y", true);
        expectedMap.put("a/b/foo/x", true);
        expectedMap.put("a/b/foo/x/y",true);
        assertExclusions(basedir, "foo/*", expectedMap);

        expectedMap.clear();
        expectedMap.put("a/b/foo", true);
        expectedMap.put("b/foo", true);
        expectedMap.put("foo", false);
        expectedMap.put("foo/x", false);
        expectedMap.put("foo/x/y", false);
        expectedMap.put("b/foo/x", true);
        expectedMap.put("b/foo/x/y", true);
        expectedMap.put("a/b/foo/x", true);
        expectedMap.put("a/b/foo/x/y",true);
        assertExclusions(basedir, "foo/**", expectedMap);

        expectedMap.clear();
        expectedMap.put("a/b/foo", true);
        expectedMap.put("b/foo", false);
        expectedMap.put("foo", true);
        expectedMap.put("foo/x", true);
        expectedMap.put("foo/x/y", true);
        expectedMap.put("b/foo/x", true);
        expectedMap.put("b/foo/x/y", true);
        expectedMap.put("a/b/foo/x", true);
        expectedMap.put("a/b/foo/x/y",true);
        assertExclusions(basedir, "*/foo", expectedMap);

        expectedMap.clear();
        expectedMap.put("a/b/foo", true);
        expectedMap.put("b/foo", true);
        expectedMap.put("foo", true);
        expectedMap.put("foo/x", true);
        expectedMap.put("foo/x/y", true);
        expectedMap.put("b/foo/x", false);
        expectedMap.put("b/foo/x/y", true);
        expectedMap.put("a/b/foo/x", true);
        expectedMap.put("a/b/foo/x/y",true);
        assertExclusions(basedir, "*/foo/*", expectedMap);

        expectedMap.clear();
        expectedMap.put("a/b/foo", true);
        expectedMap.put("b/foo", false);
        expectedMap.put("foo", true);
        expectedMap.put("foo/x", true);
        expectedMap.put("foo/x/y", true);
        expectedMap.put("b/foo/x", false);
        expectedMap.put("b/foo/x/y", false);
        expectedMap.put("a/b/foo/x", true);
        expectedMap.put("a/b/foo/x/y",true);
        assertExclusions(basedir, "*/foo/**", expectedMap);

        expectedMap.clear();
        expectedMap.put("a/b/foo", false);
        expectedMap.put("b/foo", false);
        expectedMap.put("foo", false);
        expectedMap.put("foo/x", true);
        expectedMap.put("foo/x/y", true);
        expectedMap.put("b/foo/x", true);
        expectedMap.put("b/foo/x/y", true);
        expectedMap.put("a/b/foo/x", true);
        expectedMap.put("a/b/foo/x/y",true);
        assertExclusions(basedir, "**/foo", expectedMap);

        expectedMap.clear();
        expectedMap.put("a/b/foo", true);
        expectedMap.put("b/foo", true);
        expectedMap.put("foo", true);
        expectedMap.put("foo/x", false);
        expectedMap.put("foo/x/y", true);
        expectedMap.put("b/foo/x", false);
        expectedMap.put("b/foo/x/y", true);
        expectedMap.put("a/b/foo/x", false);
        expectedMap.put("a/b/foo/x/y",true);
        assertExclusions(basedir, "**/foo/*", expectedMap);

        expectedMap.clear();
        expectedMap.put("a/b/foo", false);
        expectedMap.put("b/foo", false);
        expectedMap.put("foo", false);
        expectedMap.put("foo/x", false);
        expectedMap.put("foo/x/y", false);
        expectedMap.put("b/foo/x", false);
        expectedMap.put("b/foo/x/y", false);
        expectedMap.put("a/b/foo/x", false);
        expectedMap.put("a/b/foo/x/y",false);
        assertExclusions(basedir, "**/foo/**", expectedMap);
    }

    private static Stream<Arguments> getDocumentNames() {
        List<Arguments> lst = new ArrayList<>();

        DocumentName.Builder builder = DocumentName.builder().setName("default");
        lst.add(Arguments.of(builder.setBaseName(builder.directorySeparator()).build()));

        builder = DocumentName.builder(WINDOWS).setName("windows");
        lst.add(Arguments.of(builder.setBaseName(builder.directorySeparator()).build()));

        builder = DocumentName.builder(UNIX).setName("unix");
        lst.add(Arguments.of(builder.setBaseName(builder.directorySeparator()).build()));

        builder = DocumentName.builder(OSX).setName("osx");
        lst.add(Arguments.of(builder.setBaseName(builder.directorySeparator()).build()));

        return lst.stream();
    }

    @Test
    void addNullIncludedMatcherTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(underTest.getIncludedPaths()).isEmpty();
                underTest.addIncludedMatcher(null);
                assertThat(underTest.getIncludedPaths()).isEmpty();
                underTest.addIncludedMatcher(DocumentNameMatcher.MATCHES_ALL)
                        .addIncludedMatcher(null);
        assertThat(underTest.getIncludedPaths()).hasSize(1);
        List<String> actualPaths = underTest.getIncludedPaths().stream().map(DocumentNameMatcher::toString).toList();
        assertThat(actualPaths).containsExactly(DocumentNameMatcher.MATCHES_ALL.toString());
    }

    @Test
    void addNullExcludedMatcherTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(underTest.getExcludedPaths()).isEmpty();
        underTest.addExcludedMatcher(null);
        assertThat(underTest.getExcludedPaths()).isEmpty();
        underTest.addExcludedMatcher(DocumentNameMatcher.MATCHES_ALL)
                .addExcludedMatcher(null);
        assertThat(underTest.getExcludedPaths()).hasSize(1);
        List<String> actualPaths = underTest.getExcludedPaths().stream().map(DocumentNameMatcher::toString).toList();
        assertThat(actualPaths).containsExactly(DocumentNameMatcher.MATCHES_ALL.toString());
    }

    @Test
    void addFileProcessor() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(underTest.getFileProcessors()).isEmpty();
        underTest.addFileProcessor(null);
        assertThat(underTest.getFileProcessors()).isEmpty();
        underTest.addFileProcessor(StandardCollection.HIDDEN_FILE)
                .addFileProcessor(null);
        assertThat(underTest.getFileProcessors()).containsExactly(StandardCollection.HIDDEN_FILE);
    }

    @Test
    void addNullIncludedPatternTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(underTest.getIncludedPatterns()).isEmpty();
        underTest.addIncludedPatterns(null);
        assertThat(underTest.getIncludedPatterns()).isEmpty();
        underTest.addIncludedPatterns(Collections.emptyList());
        assertThat(underTest.getIncludedPatterns()).isEmpty();
        underTest.addIncludedPatterns(List.of("hello/world"))
        .addIncludedPatterns(null)
                .addIncludedPatterns(Collections.emptyList());
        assertThat(underTest.getIncludedPatterns()).containsExactly("hello/world");
    }

    @Test
    void addNullIncludedCollectionTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(underTest.getIncludedCollections()).isEmpty();
        underTest.addIncludedCollection(null);
        assertThat(underTest.getIncludedCollections()).isEmpty();
        underTest.addIncludedCollection(StandardCollection.HIDDEN_FILE)
                .addIncludedCollection(null);
        assertThat(underTest.getIncludedCollections()).containsExactly(StandardCollection.HIDDEN_FILE);
    }

    @Test
    void addNullExcludedCollectionTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(underTest.getExcludedCollections()).isEmpty();
        underTest.addExcludedCollection(null);
        assertThat(underTest.getExcludedCollections()).isEmpty();
        underTest.addExcludedCollection(StandardCollection.HIDDEN_FILE)
                .addExcludedCollection(null);
        assertThat(underTest.getExcludedCollections()).containsExactly(StandardCollection.HIDDEN_FILE);
    }

    @Test
    void addNullExcludedPatternTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(underTest.getExcludedPatterns()).isEmpty();
        underTest.addExcludedPatterns(null);
        assertThat(underTest.getExcludedPatterns()).isEmpty();
        underTest.addExcludedPatterns(Collections.emptyList());
        assertThat(underTest.getExcludedPatterns()).isEmpty();
        underTest.addExcludedPatterns(List.of("hello/world"))
                .addExcludedPatterns(null)
                .addExcludedPatterns(Collections.emptyList());
        assertThat(underTest.getExcludedPatterns()).containsExactly("hello/world");
    }

    public static void assertSame(ExclusionProcessor actual, ExclusionProcessor expected) {
        assertThat(actual.getExcludedCollections()).containsExactlyElementsOf(expected.getExcludedCollections());
        assertThat(actual.getFileProcessors()).containsExactlyElementsOf(expected.getFileProcessors());
        assertThat(actual.getIncludedCollections()).containsExactlyElementsOf(expected.getIncludedCollections());
        assertThat(actual.getExcludedPatterns()).containsExactlyElementsOf(expected.getExcludedPatterns());
        assertThat(actual.getIncludedPatterns()).containsExactlyElementsOf(expected.getIncludedPatterns());

        // paths only match on name.  deserialized paths are not functional.
        List<String> actualPaths = actual.getExcludedPaths().stream().map(DocumentNameMatcher::toString).toList();
        List<String> expectedPaths = expected.getExcludedPaths().stream().map(DocumentNameMatcher::toString).toList();
        assertThat(actualPaths).as("excluded paths").containsExactlyElementsOf(expectedPaths);

        actualPaths = actual.getIncludedPaths().stream().map(DocumentNameMatcher::toString).toList();
        expectedPaths = expected.getIncludedPaths().stream().map(DocumentNameMatcher::toString).toList();
        assertThat(actualPaths).as("included paths").containsExactlyElementsOf(expectedPaths);
    }

    @ParameterizedTest
    @MethodSource("serdeTestData")
    void serdeTest(ExclusionProcessor underTest) throws IOException, SAXException {
        StringWriter stringWriter = new StringWriter();
        try (XmlWriter writer = new XmlWriter(stringWriter)) {
            underTest.serde().serialize(writer);
        }
        Document document = StandardXmlFactory.documentBuilder().parse(new ByteArrayInputStream(stringWriter.toString().getBytes(StandardCharsets.UTF_8)));
        ExclusionProcessor actual = new ExclusionProcessor();
        actual.serde().deserialize(document.getElementsByTagName("ExclusionProcessor").item(0));
        assertSame(actual, underTest);
    }

    static List<ExclusionProcessor> serdeTestData() {
        List<ExclusionProcessor> tests = new ArrayList<>();
        tests.add(new ExclusionProcessor());

        // single exclusions
        tests.add(new ExclusionProcessor()
        .addExcludedPatterns(List.of("pattern/**")));

        tests.add(new ExclusionProcessor()
       .addExcludedCollection(StandardCollection.BAZAAR));


        tests.add(new ExclusionProcessor()
        .addExcludedMatcher(DocumentNameMatcher.MATCHES_ALL));

        // single inclusions
        tests.add(new ExclusionProcessor()
        .addIncludedPatterns(List.of("pattern/**")));

        tests.add(new ExclusionProcessor()
        .addIncludedCollection(StandardCollection.BAZAAR));

        tests.add(new ExclusionProcessor()
        .addIncludedMatcher(DocumentNameMatcher.MATCHES_NONE));

        // full population
        tests.add(new ExclusionProcessor()
        .addExcludedPatterns(List.of("pattern/**", "pattern2/**"))
        .addExcludedCollection(StandardCollection.BAZAAR)
        .addExcludedCollection(StandardCollection.MISC)
        .addExcludedMatcher(DocumentNameMatcher.MATCHES_ALL)
        .addIncludedPatterns(List.of("**/pattern3", "**/pattern4"))
                .addIncludedCollection(StandardCollection.ARCH)
                .addIncludedCollection(StandardCollection.BITKEEPER)
        .addIncludedMatcher(DocumentNameMatcher.MATCHES_NONE));

        return tests;
    }

    @Test
    void reportExclusionsTest() throws IOException {
        ExclusionProcessor underTest = new ExclusionProcessor()
                .addExcludedPatterns(List.of("pattern/**", "pattern2/**"))
                .addExcludedCollection(StandardCollection.BAZAAR)
                .addExcludedCollection(StandardCollection.MISC)
                .addFileProcessor(StandardCollection.HIDDEN_FILE)
                .addExcludedMatcher(DocumentNameMatcher.MATCHES_ALL)
                .addIncludedPatterns(List.of("**/pattern3", "**/pattern4"))
                .addIncludedCollection(StandardCollection.ARCH)
                .addIncludedCollection(StandardCollection.BITKEEPER)
                .addIncludedMatcher(DocumentNameMatcher.MATCHES_NONE);

        StringWriter writer = new StringWriter();
        underTest.reportExclusions(writer);
        String actual = writer.toString();

        assertThat(actual).containsPattern("Excluding patterns:[^$]+\\Qpattern/**\\E")
                .containsPattern("Excluding patterns:[^$]+\\Qpattern2/**\\E")
                .containsPattern("Including patterns:[^$]+\\Q**/pattern3\\E")
                .containsPattern("Including patterns:[^$]+\\Q**/pattern4\\E")
                .contains("Excluding " + StandardCollection.BAZAAR + " collection.")
                .contains("Excluding " + StandardCollection.MISC + " collection.")
                .contains("Including " + StandardCollection.ARCH + " collection.")
                .contains("Including " + StandardCollection.BITKEEPER + " collection.")
                .contains("Processing exclude file from " + StandardCollection.HIDDEN_FILE)
                .contains("Excluding " + DocumentNameMatcher.MATCHES_ALL + ".")
                .contains("Including " + DocumentNameMatcher.MATCHES_NONE + ".");
    }

    @Test
    void getNameMatcherTest() {
        ExclusionProcessor underTest = new ExclusionProcessor()
                .addExcludedPatterns(List.of("pattern/**", "pattern2/**"))
                .addExcludedCollection(StandardCollection.BAZAAR)
                .addExcludedCollection(StandardCollection.MISC)
                .addFileProcessor(StandardCollection.HIDDEN_FILE)
                .addExcludedMatcher(DocumentNameMatcher.MATCHES_ALL)
                .addIncludedPatterns(List.of("**/pattern3", "**/pattern4"))
                .addIncludedCollection(StandardCollection.ARCH)
                .addIncludedCollection(StandardCollection.BITKEEPER)
                .addIncludedMatcher(DocumentNameMatcher.MATCHES_NONE);

        assertThat(underTest.getLastMatcherBaseDir()).isNull();
        assertThat(underTest.getLastMatcher()).isNull();
        DocumentName base = DocumentName.builder(new File(".")).build();

        DocumentNameMatcher matcher = underTest.getNameMatcher(base);
        assertThat(underTest.getLastMatcherBaseDir()).isEqualTo(base);
        assertThat(underTest.getLastMatcher()).isEqualTo(matcher);

        DocumentNameMatcher matcher2 = underTest.getNameMatcher(base);
        assertThat(underTest.getLastMatcherBaseDir()).isEqualTo(base);
        assertThat(underTest.getLastMatcher()).isEqualTo(matcher);
        assertThat(underTest.getLastMatcherBaseDir()).isEqualTo(base);
        assertThat(matcher2).isEqualTo(matcher);
    }
}
