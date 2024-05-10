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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;


public class TextUtils {
    public static final String[] EMPTY = {};

    /**
     * Asserts a regular expression pattern is in a string
     *
     * @param pattern the pattern to match.
     * @param target  the string to match.
     */
    public static void assertPatternInTarget(String pattern, String target) {
        assertTrue(
                isMatching(pattern, target), () -> "Target does not match string: " + pattern + "\n" + target);
    }

    /**
     * Asserts a regular expression pattern is not in a string
     *
     * @param pattern the pattern to match.
     * @param target  the string to match.
     */
    public static void assertPatternNotInTarget(String pattern, String target) {
        assertFalse(
                isMatching(pattern, target), () -> "Target matches the pattern: " + pattern + "\n" + target);
    }

    /**
     * Returns {@code true} if a regular expression pattern is in a string
     *
     * @param pattern the pattern to match.
     * @param target  the string to match.
     */
    public static boolean isMatching(final String pattern, final String target) {
        return Pattern.compile(pattern, Pattern.MULTILINE).matcher(target).find();
    }

    /**
     * Asserts that a string is contained within another string.
     * @param find The string to find.
     * @param target The string to search.
     */
    public static void assertContains(final String find, final String target) {
        assertTrue(
                target.contains(find), () -> "Target does not contain the text: " + find + "\n" + target);
    }

    /**
     * Asserts that a string is not contained within another string.
     * @param find The string to find.
     * @param target The string to search.
     */
    public static void assertNotContains(final String find, final String target) {
        assertFalse(
                target.contains(find), () -> "Target does contain the text: " + find + "\n" + target);
    }
}
