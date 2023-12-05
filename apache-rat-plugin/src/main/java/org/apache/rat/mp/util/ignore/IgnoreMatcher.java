package org.apache.rat.mp.util.ignore;

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

import java.util.Optional;

public interface IgnoreMatcher {
    /**
     * Checks if the file matches the stored expressions.
     * @param filename The filename to be checked
     * @return empty: not matched, True: must be ignored, False: it must be UNignored
     */
    Optional<Boolean> isIgnoredFile(String filename);

    /**
     * Returns {@code true} if this IgnoreMatcher contains no rules.
     * @return {@code true} if this IgnoreMatcher contains no rules
     */
    boolean isEmpty();
}
