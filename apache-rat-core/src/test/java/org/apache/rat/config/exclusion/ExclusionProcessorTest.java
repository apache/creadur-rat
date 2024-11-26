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

import org.apache.commons.io.FileUtils;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.DocumentName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class ExclusionProcessorTest {

    final private static DocumentNameMatcher TRUE = DocumentNameMatcher.MATCHES_ALL;
    final private static DocumentNameMatcher FALSE = DocumentNameMatcher.MATCHES_NONE;
    /** The base directory for the test. */
    @TempDir
    private File basedirFile;
    private DocumentName basedir;

    @BeforeEach
    public void setup() {
        basedir = DocumentName.builder(basedirFile).build();
    }

    private void testParseExclusion(DocumentNameMatcher nameMatcher, DocumentName name, boolean expected) {
        assertThat(nameMatcher.matches(name)).as(() -> format("Failed on [%s %s]", basedir, name)).isEqualTo(expected);
    }

    private DocumentName mkName(String pth) {
        File f = new File(basedirFile, pth);
        try {
            FileUtils.cleanDirectory(basedirFile);
            FileUtils.touch(f);
        } catch (IOException e) {
            fail(e);
        }
        return DocumentName.builder(f).setBaseName(basedir.getBaseName()).build();
    }

    @Test
    public void defaultTest()  {
        ExclusionProcessor p = new ExclusionProcessor();
        testParseExclusion(p.getNameMatcher(basedir), mkName("hello"), true);
    }

    @Test
    public void addExcludedCollectionTest() {
        ExclusionProcessor p = new ExclusionProcessor().addExcludedCollection(StandardCollection.MISC);
        // "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*"
        testParseExclusion(p.getNameMatcher(basedir),  mkName("hello"), true);
        testParseExclusion(p.getNameMatcher(basedir),  mkName("hello~"), false);
        testParseExclusion(p.getNameMatcher(basedir),  mkName("#hello#"), false);
        testParseExclusion(p.getNameMatcher(basedir),  mkName(".#hello"), false);
        testParseExclusion(p.getNameMatcher(basedir),  mkName("%hello%"), false);
        testParseExclusion(p.getNameMatcher(basedir),  mkName("._hello"), false);
    }

    @Test
    public void addExcludedAndIncludedCollectionTest() {
        ExclusionProcessor p = new ExclusionProcessor().addExcludedCollection(StandardCollection.MISC)
                .addIncludedCollection(StandardCollection.HIDDEN_FILE);
        testParseExclusion(p.getNameMatcher(basedir),  mkName("hello"), true);
        testParseExclusion(p.getNameMatcher(basedir),  mkName("hello~"), false);
        testParseExclusion(p.getNameMatcher(basedir),  mkName("#hello#"), false);
        testParseExclusion(p.getNameMatcher(basedir),  mkName(".#hello"), true);
        testParseExclusion(p.getNameMatcher(basedir),  mkName("%hello%"), false);
        testParseExclusion(p.getNameMatcher(basedir),  mkName("._hello"), true);
    }

    private void assertExclusions(String pattern, Map<String,Boolean> expectedMap) {
        String[] paths = {"a/b/foo", "b/foo", "foo", "foo/x", "foo/x/y", "b/foo/x",
                "b/foo/x/y", "a/b/foo/x", "a/b/foo/x/y"};
        ExclusionProcessor p = new ExclusionProcessor().addExcludedPatterns(Collections.singletonList(pattern));
        DocumentNameMatcher pathMatcher = p.getNameMatcher(basedir);
        for (String pth : paths) {
            Boolean expected = expectedMap.get(pth);
            if (expected == null) {
                throw new RuntimeException("Missing expected value for " + pth + " in pattern " + pattern);
            }
            DocumentName dn = mkName(pth);
            testParseExclusion(pathMatcher, mkName(pth), expected);
        }
    }

    @Test
    public void addExcludedPatternsTest() {
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
        assertExclusions("foo", expectedMap);

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
        assertExclusions("foo/*", expectedMap);

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
        assertExclusions("foo/**", expectedMap);

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
        assertExclusions("*/foo", expectedMap);

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
        assertExclusions("*/foo/*", expectedMap);

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
        assertExclusions("*/foo/**", expectedMap);

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
        assertExclusions("**/foo", expectedMap);

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
        assertExclusions("**/foo/*", expectedMap);

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
        assertExclusions("**/foo/**", expectedMap);
    }

    @Test
    public void orTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(DocumentNameMatcher.or(Arrays.asList(TRUE, FALSE)).matches(basedir)).isTrue();
        assertThat(DocumentNameMatcher.or(Arrays.asList(FALSE, TRUE)).matches(basedir)).isTrue();
        assertThat(DocumentNameMatcher.or(Arrays.asList(TRUE, TRUE)).matches(basedir)).isTrue();
        assertThat(DocumentNameMatcher.or(Arrays.asList(FALSE, FALSE)).matches(basedir)).isFalse();
    }

    @Test
    public void andTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(DocumentNameMatcher.and(TRUE, FALSE).matches(basedir)).isFalse();
        assertThat(DocumentNameMatcher.and(FALSE, TRUE).matches(basedir)).isFalse();
        assertThat(DocumentNameMatcher.and(TRUE, TRUE).matches(basedir)).isTrue();
        assertThat(DocumentNameMatcher.and(FALSE, FALSE).matches(basedir)).isFalse();
    }

    @Test
    public void notTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertThat(DocumentNameMatcher.not(TRUE).matches(basedir)).isFalse();
        assertThat(DocumentNameMatcher.not(FALSE).matches(basedir)).isTrue();
    }
}
