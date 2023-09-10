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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches a typical Copyright header line only based on a regex pattern which
 * allows for one (starting) year or year range, and a configurable copyright
 * owner. <br>
 * <br>
 * The matching is done case insensitive<br>
 * <br>
 * Example supported Copyright header lines, using copyright owner
 * &quot;FooBar&quot;
 * <ul>
 * <li>* Copyright 2010 FooBar. *</li>
 * <li>* Copyright 2010-2012 FooBar. *</li>
 * <li>*copyright 2012 foobar*</li>
 * </ul>
 * <p>
 * Note also that the copyright owner is appended to the regex pattern, so can
 * support additional regex but also requires escaping where needed,<br>
 * e.g. use &quot;FooBar \(www\.foobar\.com\)&quot; for matching &quot;FooBar
 * (www.foobar.com)&quot;
 * </p>
 *
 * @since Rat 0.9
 */
public class CopyrightMatcher extends AbstractHeaderMatcher {

    private static final String COPYRIGHT_PATTERN_DEFN = "(\\b)?\\([Cc]\\)|©|Copyright\\b";
    private static final Pattern COPYRIGHT_PATTERN = Pattern.compile(COPYRIGHT_PATTERN_DEFN);

    private Pattern datePattern;
    private String owner;
    private boolean answered;

    public CopyrightMatcher(String start, String stop, String owner) {
        super();
        if (start != null) {
            if (stop != null) {
                datePattern = Pattern.compile(String.format("\\b%s\\s?-\\s?%s\\b", start, stop));
            } else {
                datePattern = Pattern.compile(String.format("\\b%s\\b", start));
            }
        } else {
            datePattern = null;
        }
        this.owner = owner;
        this.answered = false;
    }

    @Override
    public boolean matches(String line) {
        return answered ? false : processLine(line);
    }

    private boolean processLine(String line) {
        String buffer = null;
        Matcher matcher = COPYRIGHT_PATTERN.matcher(line);
        if (matcher.matches()) {
            buffer = line.substring(matcher.end());
        }

        if (buffer != null) {
            // check for 2 options "date owner"
            // "owner date"
            if (owner == null) {
                matcher = datePattern.matcher(buffer);
                if (matcher.matches() && matcher.start() == 0) {
                    answered = true;
                    return true;
                }
            } else {
                if (datePattern != null) {
                    matcher = datePattern.matcher(buffer);
                    if (!matcher.matches()) {
                        return false;
                    }
                    if (matcher.start() == 0) {
                        buffer = buffer.substring(matcher.end());
                    }
                }
                buffer.trim();
                if (buffer.startsWith(owner)) {
                    answered = true;
                    return true;
                }
            }

        }
        return false;
    }

    @Override
    public void reset() {
        answered = false;
    }
}
