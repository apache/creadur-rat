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
package org.apache.rat.tools;

import org.apache.commons.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class CasedString {
    private String string;
    private StringCase stringCase;

    static Function<String[],String> camelJoiner = a -> {
        StringBuilder sb = new StringBuilder(a[0]);

        for (int i=1;i<a.length;i++) {
            sb.append(WordUtils.capitalize(a[i]));
        }
        return sb.toString();
    };

    public enum StringCase {Camel(Character::isUpperCase, true,  camelJoiner),
        Snake(c -> c =='_', false, a -> String.join("_", a)),
        Kebab(c -> c == '-', false ,a -> String.join("-", a));

        private final Predicate<Character> splitter;
        private final boolean preserveSplit;
        private final Function<String[],String> joiner;

        StringCase(final Predicate<Character> splitter, final boolean preserveSplit, final Function<String[],String> joiner) {
            this.splitter = splitter;
            this.preserveSplit = preserveSplit;
            this.joiner = joiner;
        }
    }

    public CasedString(StringCase stringCase, String string) {
        this.string = string;
        this.stringCase = stringCase;
    }

    private String[] split() {
        List<String> lst = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (char c : string.toCharArray())
        {
            if (stringCase.splitter.test(c)) {
                if (sb.length() > 0) {
                    lst.add(sb.toString());
                    sb.setLength(0);
                }
                if (stringCase.preserveSplit) {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            lst.add(sb.toString());
        }
        return lst.toArray(new String[lst.size()]);
    }

    public String toCase(StringCase stringCase) {
        if (stringCase == this.stringCase) {
            return string;
        }
        return stringCase.joiner.apply(split());
    }

    @Override
    public String toString() {
        return string;
    }
}
