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
package org.apache.rat.document.impl.guesser;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class GuessUtils {
    
    private static final String[] SEPARATORS = {"/", "\\"};
    
    /**
     * Converts name to upper case and strips any path.
     * @param name not null
     * @return not null
     */
    public static String normalise(final String name) {
        String result = name.toUpperCase(Locale.US);
        final int lastSeparatorIndex = StringUtils.lastIndexOfAny(result, SEPARATORS);
        final int length = result.length();
        if (lastSeparatorIndex >= 0 && lastSeparatorIndex < length) {
            result = result.substring(lastSeparatorIndex + 1);
        }
        return result;
    }
}
