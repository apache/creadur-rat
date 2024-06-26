/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.commandline;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputArgsTest {

    /** The base directory for the test.  We do not use TempFile because we want the evidence of the run to exist after
     * a failure.*/
    @TempDir
    File baseDir;

    @Test
    public void parseExclusionsTest() {
        final Optional<IOFileFilter> filter = Arg.parseExclusions(DefaultLog.getInstance(), Arrays.asList("", " # foo/bar", "foo", "##", " ./foo/bar"));
        assertThat(filter).isPresent();
        assertThat(filter.get()).isExactlyInstanceOf(OrFileFilter.class);
        assertTrue(filter.get().accept(baseDir, "./foo/bar" ), "./foo/bar");
        assertFalse(filter.get().accept(baseDir, "B.bar"), "B.bar");
        assertTrue(filter.get().accept(baseDir, "foo" ), "foo");
        assertFalse(filter.get().accept(baseDir, "notfoo"), "notfoo");
    }

    /**
     * A parameterized test for file exclusions.
     * @param pattern The pattern to exclude
     * @param expectedPatterns The file filters that are expected to be generated from the pattern
     * @param logEntries the list of expected log entries.
     */
    @ParameterizedTest
    @MethodSource("exclusionsProvider")
    public void testParseExclusions(String pattern, List<IOFileFilter> expectedPatterns, List<String> logEntries) {
        TestingLog log = new TestingLog();
        Optional<IOFileFilter> filter = Arg.parseExclusions(log, Collections.singletonList(pattern));
        if (expectedPatterns.isEmpty()) {
            assertThat(filter).isEmpty();
        } else {
            assertThat(filter).isNotEmpty();
            assertInstanceOf(OrFileFilter.class, filter.get());
            String result = filter.toString();
            for (IOFileFilter expectedFilter : expectedPatterns) {
                TextUtils.assertContains(expectedFilter.toString(), result);
            }
        }
        assertEquals(log.isEmpty(), logEntries.isEmpty());
        for (String logEntry : logEntries) {
            log.assertContains(logEntry);
        }
    }

    /** Provider for the testParseExclusions */
    public static Stream<Arguments> exclusionsProvider() {
        List<Arguments> lst = new ArrayList<>();

        lst.add(Arguments.of( "", Collections.emptyList(), Collections.singletonList("INFO: Ignored 1 lines in your exclusion files as comments or empty lines.")));

        lst.add(Arguments.of( "# a comment", Collections.emptyList(), Collections.singletonList("INFO: Ignored 1 lines in your exclusion files as comments or empty lines.")));

        List<IOFileFilter> expected = new ArrayList<>();
        String pattern = "hello.world";
        expected.add(new RegexFileFilter(pattern));
        expected.add(new NameFileFilter(pattern));
        lst.add(Arguments.of( pattern, expected, Collections.emptyList()));

        expected = new ArrayList<>();
        pattern = "[Hh]ello.[Ww]orld";
        expected.add(new RegexFileFilter(pattern));
        expected.add(new NameFileFilter(pattern));
        lst.add(Arguments.of( pattern, expected, Collections.emptyList()));

        expected = new ArrayList<>();
        pattern = "hell*.world";
        expected.add(new RegexFileFilter(pattern));
        expected.add(new NameFileFilter(pattern));
        expected.add(WildcardFileFilter.builder().setWildcards(pattern).get());
        lst.add(Arguments.of( pattern, expected, Collections.emptyList()));

        // see RAT-265 for issue
        expected = new ArrayList<>();
        pattern = "*.world";
        expected.add(new NameFileFilter(pattern));
        expected.add(WildcardFileFilter.builder().setWildcards(pattern).get());
        lst.add(Arguments.of( pattern, expected, Collections.emptyList()));

        expected = new ArrayList<>();
        pattern = "hello.*";
        expected.add(new NameFileFilter(pattern));
        expected.add(WildcardFileFilter.builder().setWildcards(pattern).get());
        lst.add(Arguments.of( pattern, expected, Collections.emptyList()));

        expected = new ArrayList<>();
        pattern = "?ello.world";
        expected.add(new NameFileFilter(pattern));
        expected.add(WildcardFileFilter.builder().setWildcards(pattern).get());
        lst.add(Arguments.of( pattern, expected, Collections.emptyList()));

        expected = new ArrayList<>();
        pattern = "hell?.world";
        expected.add(new RegexFileFilter(pattern));
        expected.add(new NameFileFilter(pattern));
        expected.add(WildcardFileFilter.builder().setWildcards(pattern).get());
        lst.add(Arguments.of( pattern, expected, Collections.emptyList()));

        expected = new ArrayList<>();
        pattern = "hello.worl?";
        expected.add(new NameFileFilter(pattern));
        expected.add(WildcardFileFilter.builder().setWildcards(pattern).get());
        lst.add(Arguments.of( pattern, expected, Collections.emptyList()));

        return lst.stream();
    }

}
