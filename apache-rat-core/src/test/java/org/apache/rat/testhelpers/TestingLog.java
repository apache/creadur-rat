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

import org.apache.rat.utils.Log;

/**
 * Log that captures output for later review.
 */
public class TestingLog implements Log {

    private StringBuilder captured = new StringBuilder();

    /**
     * Clears the captured buffer
     */
    public void clear() {
        captured = new StringBuilder();
    }

    /**
     * Gets the captured log entries.
     * @return the log entries
     */
    public String getCaptured() {
        return captured.toString();
    }

    /**
     * Asserts the text was found in the given log entry.
     * @param expected the text to find.
     */
    public void assertContains(String expected) {
        TextUtils.assertContains(expected, captured.toString());
    }

    /**
     * Asserts the text was found exactly n times in the log.
     * @param times the number of times to find the expected text.
     * @param expected the expected test.
     */
    public void assertContainsExactly(int times, String expected) {
        TextUtils.assertContainsExactly(times, expected, getCaptured());
    }

    /**
     * Asserts that the text is not found in the log.
     * @param notExpected the text that should not be in the log.
     */
    public void assertNotContains(String notExpected) {
        TextUtils.assertNotContains(notExpected, captured.toString());
    }


    /**
     * Asserts that a regular expression is found in the log.
     * @param pattern the regular expression to search for.
     */
    public void assertContainsPattern(String pattern) {
        TextUtils.assertPatternInTarget(pattern, captured.toString());
    }

    /**
     * Asserts that a regular expression is not found in the log.
     * @param pattern the regular expression that should not be in the log.
     */
    public void assertNotContainsPattern(String pattern) {
        TextUtils.assertPatternNotInTarget(pattern, captured.toString());
    }

    @Override
    public Level getLevel() {
        return Level.DEBUG;
    }

    @Override
    public void log(Level level, String msg) {
        captured.append(String.format("%s: %s%n", level, msg));
    }

    /**
     * Returns true if the log is empty.
     * @return {@code true} if the log is empty.
     */
    public boolean isEmpty() {
        return captured.length() == 0;
    }
}
