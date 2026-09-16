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
package org.apache.rat.testhelpers;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;

/**
 * Utilities to assert text appears or does not appear in text.
 */
public class TextUtils {
    /** An empty list of strings */
    public static final String[] EMPTY = {};

    /**
     * Asserts a regular expression pattern is in a string.
     *
     * @param pattern the pattern to match.
     * @param target  the string to match.
     * @deprecated use  assertThat(target).containsPattern(pattern)
     */
    @Deprecated
    public static void assertPatternInTarget(String pattern, String target) {
        assertThat(isMatching(pattern, target)).as(() -> format("Target does not match string: %s%n%s", pattern, target))
                        .isTrue();
    }

    /**
     * Asserts a regular expression pattern is not in a string.
     *
     * @param pattern the pattern to match.
     * @param target  the string to match.
     * @deprecated use assertThat(target).dosNotContainPattern(pattern)
     */
    @Deprecated
    public static void assertPatternNotInTarget(String pattern, String target) {
        assertThat(isMatching(pattern, target)).as(() -> format("Target matches the pattern: %s%n%s", pattern, target))
                .isFalse();
    }

    /**
     * Determines if a regular expression pattern is in a string.
     *
     * @param pattern the pattern to match.
     * @param target  the string to match.
     * @return {@code true} if a regular expression pattern is in a string
     * @deprecated use assertThat(target).matches(pattern)
     */
    @Deprecated
    public static boolean isMatching(final String pattern, final String target) {
        return Pattern.compile(pattern, Pattern.MULTILINE).matcher(target).find();
    }

    /**
     * Read given file as UTF-8.
     * @param f File to read from.
     * @return contents of the file as UTF-8.
     * @throws IOException in case of I/O-errors.
     */
    public static String readFile(File f) throws IOException {
        return String.join("\n", IOUtils.readLines(Files.newInputStream(f.toPath()), StandardCharsets.UTF_8));
    }
}
