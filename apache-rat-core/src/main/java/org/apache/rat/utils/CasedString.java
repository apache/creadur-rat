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
package org.apache.rat.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.text.WordUtils;

/**
 * Handles converting from one string case to another (e.g. camel case to snake case).
 * @since 0.17
 */
public class CasedString {
    private final String[] parts;
    private final StringCase stringCase;
    private static final Function<String[], String> PASCAL_JOINER = (strings) -> {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(strings).map((s) -> s == null ? "" : s).forEach((token) -> sb.append(WordUtils.capitalize(token.toLowerCase(Locale.ROOT))));
        return sb.toString();
    };

    public CasedString(StringCase stringCase, String string) {
        this.parts = string == null ? CasedString.StringCase.NULL_SEGMENT : stringCase.getSegments(string.trim());
        this.stringCase = stringCase;
    }

    public CasedString(StringCase stringCase, String[] parts) {
        this.parts = parts;
        this.stringCase = stringCase;
    }

    public CasedString as(StringCase stringCase) {
        return stringCase.name.equals(this.stringCase.name) ? this : new CasedString(stringCase, (String[])Arrays.copyOf(this.parts, this.parts.length));
    }

    public String[] getSegments() {
        return this.parts;
    }

    public String toCase(StringCase stringCase) {
        return this.parts == CasedString.StringCase.NULL_SEGMENT ? null : (String)stringCase.joiner.apply(this.getSegments());
    }

    public String toString() {
        return this.toCase(this.stringCase);
    }

    public static class StringCase {
        public static final StringCase CAMEL;
        public static final StringCase PASCAL;
        public static final StringCase SNAKE;
        public static final StringCase KEBAB;
        public static final StringCase PHRASE;
        public static final StringCase DOT;
        public static final StringCase SLASH;
        private static final String[] NULL_SEGMENT;
        private static final String[] EMPTY_SEGMENT;
        private final String name;
        private final Predicate<Character> splitter;
        private final boolean preserveSplit;
        private final Function<String[], String> joiner;
        private final Function<String, String> postProcess;

        public StringCase(String name, Predicate<Character> splitter, boolean preserveSplit, Function<String[], String> joiner) {
            this(name, splitter, preserveSplit, joiner, Function.identity());
        }

        public StringCase(String name, char delimiter) {
            this(name, (c) -> c == delimiter, false, simpleJoiner(delimiter));
        }

        public static Function<String[], String> simpleJoiner(char delimiter) {
            return (s) -> String.join(String.valueOf(delimiter), (CharSequence[])Arrays.stream(s).filter(Objects::nonNull).toArray((x$0) -> new String[x$0]));
        }

        public StringCase(String name, Predicate<Character> splitter, boolean preserveSplit, Function<String[], String> joiner, Function<String, String> postProcess) {
            this.name = name;
            this.splitter = splitter;
            this.preserveSplit = preserveSplit;
            this.joiner = joiner;
            this.postProcess = postProcess;
        }

        public String toString() {
            return this.name;
        }

        public String assemble(String[] segments) {
            return (String)this.joiner.apply(segments);
        }

        public String[] getSegments(String string) {
            if (string == null) {
                return NULL_SEGMENT;
            } else if (string.isEmpty()) {
                return EMPTY_SEGMENT;
            } else {
                List<String> lst = new ArrayList();
                StringBuilder sb = new StringBuilder();

                for(char c : string.toCharArray()) {
                    if (this.splitter.test(c)) {
                        lst.add(sb.toString());
                        sb.setLength(0);
                        if (this.preserveSplit) {
                            sb.append(c);
                        }
                    } else {
                        sb.append(c);
                    }
                }

                if (!sb.isEmpty()) {
                    lst.add(sb.toString());
                }

                return (String[])lst.stream().map(this.postProcess).filter(Objects::nonNull).toArray((x$0) -> new String[x$0]);
            }
        }

        static {
            CAMEL = new StringCase("CAMEL", Character::isUpperCase, true, CasedString.PASCAL_JOINER.andThen(WordUtils::uncapitalize), (x) -> (String)StringUtils.defaultIfEmpty(x, (CharSequence)null));
            PASCAL = new StringCase("PASCAL", Character::isUpperCase, true, CasedString.PASCAL_JOINER, (x) -> (String)StringUtils.defaultIfEmpty(x, (CharSequence)null));
            SNAKE = new StringCase("SNAKE", '_');
            KEBAB = new StringCase("KEBAB", '-');
            PHRASE = new StringCase("PHRASE", Character::isWhitespace, false, simpleJoiner(' '));
            DOT = new StringCase("DOT", '.');
            SLASH = new StringCase("SLASH", '/');
            NULL_SEGMENT = new String[0];
            EMPTY_SEGMENT = new String[]{""};
        }
    }
}
