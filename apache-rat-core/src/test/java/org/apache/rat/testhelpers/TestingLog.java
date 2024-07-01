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

    public void assertContains(String expected) {
        TextUtils.assertContains(expected, captured.toString());
    }

    public void assertNotContains(String expected) {
        TextUtils.assertNotContains(expected, captured.toString());
    }

    public void assertNotContains(String expected, int repetition) {
        TextUtils.assertNotContains(expected, captured.toString(), repetition);
    }

    public void assertContainsPattern(String pattern) {
        TextUtils.assertPatternInTarget(pattern, captured.toString());
    }

    public void assertNotContainsPattern(String pattern) {
        TextUtils.assertPatternNotInTarget(pattern, captured.toString());
    }

    @Override
    public void log(Level level, String msg) {
        captured.append(String.format("%s: %s%n", level, msg));
    }

    public boolean isEmpty() {
        return captured.length() == 0;
    }
}
