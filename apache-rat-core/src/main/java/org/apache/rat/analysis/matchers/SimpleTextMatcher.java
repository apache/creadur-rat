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
package org.apache.rat.analysis.matchers;

import org.apache.commons.lang3.StringUtils;

/**
 * @since Rat 0.8
 */
public class SimpleTextMatcher extends AbstractSimpleMatcher {
    private String pattern;

    /**
     * Creates a pattern based license with full documentation.
     * 
     * @param pLicenseFamilyCategory
     * @param pLicenseFamilyName
     * @param pNotes
     * @param pPatterns
     */
    public SimpleTextMatcher(String pattern) {
        this(null, pattern);
    }

    public SimpleTextMatcher(String id, String pattern) {
        super(id);
        if (StringUtils.isBlank(pattern)) {
            throw new IllegalArgumentException("Pattern may not be null, empty or blank");
        }
        this.pattern = pattern;
    }

    @Override
    public boolean doMatch(String line) {
        return line.contains(pattern);
    }
}
