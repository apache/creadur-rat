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
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
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

    /**
     * Creates a cased string by parsing the string argument for the specific case.
     * @param stringCase the case of the string being parsed.
     * @param string the string to parse.
     */
    public CasedString(StringCase stringCase, String string) {
        this.parts = string == null ? CasedString.StringCase.NULL_SEGMENT : stringCase.getSegments(string.trim());
        this.stringCase = stringCase;
    }

    /**
     * Creates a cased string of the specified case and segments
     * @param stringCase the case of the string.
     * @param segments the segments of the string.
     */
    public CasedString(StringCase stringCase, String[] segments) {
        this.parts = segments;
        this.stringCase = stringCase;
    }

    /**
     * Converts this cased string into another format.
     * @param stringCase the desired format.
     * @return the new CasedString.
     */
    public CasedString as(StringCase stringCase) {
        return stringCase.name.equals(this.stringCase.name) ? this : new CasedString(stringCase, (String[])Arrays.copyOf(this.parts, this.parts.length));
    }

    /**
     * Gets the segments of this cased string.
     * @return the segments of this cased string.
     */
    public String[] getSegments() {
        return this.parts;
    }

    /**
     * Generates a string from this cased string but with the desired case.
     * @param stringCase the desired case.
     * @return this cased string in the desired case.
     */
    public String toCase(StringCase stringCase) {
        return this.parts == CasedString.StringCase.NULL_SEGMENT ? null : (String)stringCase.assemble(this.getSegments());
    }

    @Override
    public String toString() {
        return this.toCase(this.stringCase);
    }

    /**
     * The definition of a String case.
     */
    public static class StringCase {
        /** The camel case.  Example: "HelloWorld"*/
        public static final StringCase CAMEL;
        /** The pascal case.  Example: "helloWorld" */
        public static final StringCase PASCAL;
        /** The Snake case. Example: "hello_world" */
        public static final StringCase SNAKE;
        /** The Kebab case. Example: "hello-world" */
        public static final StringCase KEBAB;
        /** The phrase case. Example: "hello world" */
        public static final StringCase PHRASE;
        /** The dot case.  Example: "hello.world" */
        public static final StringCase DOT;
        /** The slash case. Example: "hello/world" */
        public static final StringCase SLASH;
        /** A marker for the parsing of a NULL string. */
        private static final String[] NULL_SEGMENT;
        /** An empty segment marker. */
        private static final String[] EMPTY_SEGMENT;
        /** The name of this case */
        private final String name;
        /** The predicate that determines if a character is a spliter character.  A splitter character
         * is the character that signals the start of a new segment.
         */
        private final Predicate<Character> splitter;
        /**
         * If {@code true} the spliter character is preserved as part of the subsequent section otherwise,
         * the spliter character is discarded.
         */
        private final boolean preserveSplit;
        /** The function that converts segments into the String representation */
        private final Function<String[], String> joiner;
        /** A function to provide post-processing on the joined string */
        private final Function<String, String> postProcess;

        /**
         * Constructs a StringCase
         * @param name the name of the case.
         * @param splitter the splitter to determine when to split a string.
         * @param preserveSplit the preserveSplit flag.
         * @param joiner the joiner to assemble the String from the segments.
         */
        public StringCase(String name, Predicate<Character> splitter, boolean preserveSplit, Function<String[], String> joiner) {
            this(name, splitter, preserveSplit, joiner, Function.identity());
        }

        /**
         * Constructs a String case for the common cases where the delimiter is not preserved in the segments.
         * @param name the name of the case.
         * @param delimiter the delimter between segments.
         */
        public StringCase(String name, char delimiter) {
            this(name, (c) -> c == delimiter, false, simpleJoiner(delimiter));
        }

        /**
         * Constructs a StingCase.
         * @param name the name of the string case.
         * @param splitter the splitter to detect segments.
         * @param preserveSplit the flag to preserve the splitter character.
         * @param joiner the joiner to assemble a String from segments.
         * @param postProcess the post-process applied to the string from the joiner.
         */
        public StringCase(String name, Predicate<Character> splitter, boolean preserveSplit, Function<String[], String> joiner, Function<String, String> postProcess) {
            this.name = name;
            this.splitter = splitter;
            this.preserveSplit = preserveSplit;
            this.joiner = joiner;
            this.postProcess = postProcess;
        }

        /**
         * A simple joiner that assembles a String from a collection of segments.
         * Correctly handles the case where there are zero length segments.
         * @param delimiter the delimiter to use between the segments.
         * @return the assembled string.
         */
        public static Function<String[], String> simpleJoiner(char delimiter) {
            return (s) -> String.join(String.valueOf(delimiter), (CharSequence[])Arrays.stream(s).filter(Objects::nonNull).toArray(String[]::new));
        }

        @Override
        public String toString() {
            return this.name;
        }

        /**
         * Assembles segments into a String.
         * @param segments the segments to assemble.
         * @return the complete String.
         */
        public String assemble(String[] segments) {
            return (String)this.joiner.apply(segments);
        }

        /**
         * Parses a String into segments.
         * @param string the string to parse
         * @return the segments from the string.
         */
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

                return (String[])lst.stream().map(this.postProcess).filter(Objects::nonNull).toArray(String[]::new);
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
