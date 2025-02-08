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

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.rat.config.exclusion.plexus.SelectorUtils;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.DocumentName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.apache.rat.document.FSInfoTest.OSX;
import static org.apache.rat.document.FSInfoTest.UNIX;
import static org.apache.rat.document.FSInfoTest.WINDOWS;

public class ExclusionProcessorTest {

    final private static DocumentNameMatcher TRUE = DocumentNameMatcher.MATCHES_ALL;
    final private static DocumentNameMatcher FALSE = DocumentNameMatcher.MATCHES_NONE;

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
        DocumentName mocked = Mockito.spy(result);

        String fn = result.localized(FileSystems.getDefault().getSeparator());
        File file = tempDir.resolve(fn.substring(1)).toFile();
        File parent = file.getParentFile();
        if (parent.exists() && !parent.isDirectory()) {
            parent.delete();
        }
        parent.mkdirs();
        if (file.exists()) {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else {
                FileUtils.delete(file);
            }
        }
        file.createNewFile();
        Mockito.when(mocked.asFile()).thenReturn(file);
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

    @ParameterizedTest
    @MethodSource("getDocumentNames")
    void orTest(DocumentName basedir) {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(DocumentNameMatcher.or(Arrays.asList(TRUE, FALSE)).matches(basedir)).isTrue();
        assertThat(DocumentNameMatcher.or(Arrays.asList(FALSE, TRUE)).matches(basedir)).isTrue();
        assertThat(DocumentNameMatcher.or(Arrays.asList(TRUE, TRUE)).matches(basedir)).isTrue();
        assertThat(DocumentNameMatcher.or(Arrays.asList(FALSE, FALSE)).matches(basedir)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getDocumentNames")
    void andTest(DocumentName basedir) {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(DocumentNameMatcher.and(TRUE, FALSE).matches(basedir)).isFalse();
        assertThat(DocumentNameMatcher.and(FALSE, TRUE).matches(basedir)).isFalse();
        assertThat(DocumentNameMatcher.and(TRUE, TRUE).matches(basedir)).isTrue();
        assertThat(DocumentNameMatcher.and(FALSE, FALSE).matches(basedir)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getDocumentNames")
    void notTest(DocumentName basedir) {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(DocumentNameMatcher.not(TRUE).matches(basedir)).isFalse();
        assertThat(DocumentNameMatcher.not(FALSE).matches(basedir)).isTrue();
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
        lst.add(Arguments.of(builder.setBaseName(builder.directorySeparator()).build()))
        return lst.stream();
    }
}
