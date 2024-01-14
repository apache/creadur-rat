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
    
    public static void assertPatternInOutput(String pPattern, String out) {
        assertTrue(
                isMatching(pPattern, out), ()->"Output does not match string: " + pPattern+"\n"+out);
    }
    
    public static void assertPatternNotInOutput(String pPattern, String out) {
        assertFalse(
                isMatching(pPattern, out), ()->"Output matches the pattern: " + pPattern+"\n"+out);
    }

   public static boolean isMatching(final String pPattern, final String pValue) {
        return Pattern.compile(pPattern, Pattern.MULTILINE).matcher(pValue).find();
    }
}
