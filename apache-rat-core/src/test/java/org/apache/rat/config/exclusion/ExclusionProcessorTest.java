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
import org.apache.rat.document.TraceableDocumentNameMatcher;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.apache.rat.document.TraceableDocumentNameMatcher.TRUE;
import static org.apache.rat.document.TraceableDocumentNameMatcher.FALSE;

public class ExclusionProcessorTest {

    /** The base directory for the test. */
    @TempDir
    private File basedirFile;
    private DocumentName basedir;

    @BeforeEach
    public void setup() {
        basedir = new DocumentName(basedirFile);
    }

    private void testParseExclusion(DocumentNameMatcher nameMatcher, DocumentName name, boolean expected) {
        assertEquals(expected, nameMatcher.matches(name), () -> format("Failed on [%s %s]", basedir, name));
    }

    private DocumentName mkName(String pth) {
        File f = new File(basedirFile, pth);
        try {
            FileUtils.cleanDirectory(basedirFile);
            FileUtils.touch(f);
        } catch (IOException e) {
            fail(e);
        }
        return new DocumentName(f, basedir);
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
            assertEquals(expected, pathMatcher.matches(dn),
                    () -> format("%s failed on [%s]", pattern, pth));
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
        assertTrue(TraceableDocumentNameMatcher.or(Arrays.asList(TRUE, FALSE)).matches(basedir));
        assertTrue(TraceableDocumentNameMatcher.or(Arrays.asList(FALSE, TRUE)).matches(basedir));
        assertTrue(TraceableDocumentNameMatcher.or(Arrays.asList(TRUE, TRUE)).matches(basedir));
        assertFalse(TraceableDocumentNameMatcher.or(Arrays.asList(FALSE, FALSE)).matches(basedir));
    }

    @Test
    public void andTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertFalse(TraceableDocumentNameMatcher.and(TRUE, FALSE).matches(basedir));
        assertFalse(TraceableDocumentNameMatcher.and(FALSE, TRUE).matches(basedir));
        assertTrue(TraceableDocumentNameMatcher.and(TRUE, TRUE).matches(basedir));
        assertFalse(TraceableDocumentNameMatcher.and(FALSE, FALSE).matches(basedir));
    }

    @Test
    public void notTest() {
        ExclusionProcessor underTest = new ExclusionProcessor();
        assertFalse(TraceableDocumentNameMatcher.not(TRUE).matches(basedir));
        assertTrue(TraceableDocumentNameMatcher.not(FALSE).matches(basedir));
    }
}
