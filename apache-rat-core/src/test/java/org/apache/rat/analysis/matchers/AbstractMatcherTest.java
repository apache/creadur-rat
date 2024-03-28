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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;

public class AbstractMatcherTest {

    private IHeaders dummyHeader = makeHeaders(null, null);

    protected void assertValues(IHeaderMatcher target, boolean... values) {
        for (int i = 0; i < values.length; i++) {
            final int idx = i;
            assertEquals(values[i], target.matches(dummyHeader), () -> String.format("Position %s", idx));
        }
    }

    public static IHeaders makeHeaders(String raw, String pruned) {
        return new IHeaders() {

            @Override
            public String raw() {
                if (raw == null) {
                    throw new UnsupportedOperationException("Should not be called");
                }
                return raw;
            }

            @Override
            public String pruned() {
                if (pruned == null) {
                    throw new UnsupportedOperationException("Should not be called");
                }
                return FullTextMatcher.prune(pruned).toLowerCase(Locale.ENGLISH);
            }

            @Override
            public String toString() {
                return "AbstractMatcherTest";
            }
        };
    }

}
