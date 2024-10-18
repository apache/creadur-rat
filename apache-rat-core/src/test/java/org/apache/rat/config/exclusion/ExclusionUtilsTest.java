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

import org.apache.rat.ConfigurationException;
import org.apache.rat.utils.iterator.ExtendedIterator;
import org.apache.rat.utils.iterator.WrappedIterator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import java.util.function.Predicate;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExclusionUtilsTest {

    @TempDir
    private File testDir;

    private final int fileCount = 0;

    private static final String[] COMMENTS = {
            "# comment that is", "## comment that is", //
            "## comment that is ## ", //
            "     // comment that is ## ", "", //
            "     /** comment that is **/ ", null
    };

    private static final String[] NOT_COMMENTS = {
            "This is a  normal line", "**/ignoreMe/*", "C:\\No Space In FileNames Please"
    };

    /**
     * This method is a known workaround for
     * {@link <a href="https://github.com/junit-team/junit5/issues/2811">junit 5 issue #2811</a> }.
     */
    @AfterEach
    @EnabledOnOs(OS.WINDOWS)
    void cleanUp() {
        System.gc();
    }

    private File createFile(Iterable<String> contents) {
        File f = new File(testDir, "file" + fileCount);
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            for (String content : contents) {
                if (content != null) {
                    pw.println(content);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return f;
    }

    @Test
    public void CommentFilterTest() {
        Predicate<String> filter = ExclusionUtils.commentFilter(ExclusionUtils.COMMENT_PREFIXES);
        for (String comment : COMMENTS) {
            assertFalse(filter.test(comment), () -> format(" '%s' should be a comment", comment));
        }
        for (String comment : NOT_COMMENTS) {
            assertTrue(filter.test(comment), () -> format(" '%s' should not be a comment", comment));
        }
    }

    @Test
    public void fileCommentFilterTest() {
        List<String> notComments = new ArrayList<>(Arrays.asList(NOT_COMMENTS));
        ExtendedIterator<String> iter = WrappedIterator.create(Arrays.asList(COMMENTS).iterator())
                .andThen(Arrays.asList(NOT_COMMENTS).iterator());
        Predicate<String> filter = ExclusionUtils.commentFilter(ExclusionUtils.COMMENT_PREFIXES);
        File f = createFile(() -> iter);
        for (String s : ExclusionUtils.asIterable(f, filter)) {
            assertTrue(notComments.remove(s), () -> format(" '%s' should be a comment", s));
        }
    }

    @Test
    public void parseFromNonExistingFileOrDirectoryOrNull() {
        Predicate<String> filter = ExclusionUtils.commentFilter(ExclusionUtils.COMMENT_PREFIXES);
        assertThrows(ConfigurationException.class, () -> ExclusionUtils.asIterable(new File("./mustNotExist-RAT-171"), filter));
        assertThrows(ConfigurationException.class, () -> ExclusionUtils.asIterable(null, filter));
        assertThrows(ConfigurationException.class, () -> ExclusionUtils.asIterable(new File("."), filter));
    }

    @Test
    public void asIterableWithPredicateTest() {
        String[] contents = { "# comment", "Hello World", "   # comment 2", "Good bye cruel World"};
        String[] comments = { "# comment",  "   # comment 2" };

        File f = createFile(Arrays.asList(contents));
        List<String> found = new ArrayList<>();
        for (String s : ExclusionUtils.asIterable(f, s -> !s.contains("World"))) {
            found.add(s);
        }
        assertArrayEquals(comments, found.toArray());
    }

    @Test
    public void asIterableWithStringTest() {
        String[] contents = { "# comment", "Hello World", "   # comment 2", "Good bye cruel World"};
        String[] nonComments = { "Hello World", "Good bye cruel World" };

        File f = createFile(Arrays.asList(contents));
        List<String> found = new ArrayList<>();
        for (String s : ExclusionUtils.asIterable(f, "#")) {
            found.add(s);
        }
        assertArrayEquals(nonComments, found.toArray());
    }
}
