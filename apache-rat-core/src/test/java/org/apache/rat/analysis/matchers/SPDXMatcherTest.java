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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SPDXMatcherTest {
    final SPDXMatcherFactory factory = SPDXMatcherFactory.newInstance();
    final IHeaderMatcher target1 = factory.create("hello");
    final IHeaderMatcher target2 = factory.create("world");
    final IHeaderMatcher target3 = factory.create("goodbye");

    @BeforeEach
    void setup() {
        target1.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            " hello OR world", " (hello AND world)", "\t hello\tOR\tworld\t",
            "\u00a0hello\u2003OR\u00a0world", "hello OR world */"
    })
    void matchesAllExpressionIdentifiers(String expression) {
        IHeaders headers = AbstractMatcherTest.makeHeaders(SPDXMatcherFactory.LICENSE_IDENTIFIER + expression, null);
        assertTrue(target1.matches(headers));
        assertTrue(target2.matches(headers));
        assertFalse(target3.matches(headers));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello AND", "hello garbage", "(hello", "hello WITH", "hello++"})
    void malformedExpressionDoesNotMatchItsFirstIdentifier(String expression) {
        IHeaders headers = AbstractMatcherTest.makeHeaders(SPDXMatcherFactory.LICENSE_IDENTIFIER + " " + expression, null);
        assertFalse(target1.matches(headers));
    }

    @Test
    void exceptionRequiresAnExplicitCombinedMatcher() {
        IHeaderMatcher combined = factory.create("hello\tWITH  exception");
        IHeaders headers = AbstractMatcherTest.makeHeaders("SPDX-License-Identifier: hello WITH exception", null);
        assertFalse(target1.matches(headers));
        assertTrue(combined.matches(headers));
    }

    @Test
    void resetsBetweenDocuments() {
        assertTrue(target1.matches(AbstractMatcherTest.makeHeaders("SPDX-License-Identifier: hello", null)));
        target1.reset();
        IHeaders headers = AbstractMatcherTest.makeHeaders("SPDX-License-Identifier: world", null);
        assertFalse(target1.matches(headers));
        assertTrue(target2.matches(headers));
    }

    @Test
    void testMatch() {
        String sb = SPDXMatcherFactory.LICENSE_IDENTIFIER + " world" + System.lineSeparator() +
                SPDXMatcherFactory.LICENSE_IDENTIFIER + " hello" + System.lineSeparator();

        IHeaders headers =  AbstractMatcherTest.makeHeaders(sb,null);

        assertTrue(target1.matches(headers));
        assertTrue(target2.matches(headers));
        assertFalse(target3.matches(headers));
        target1.reset();
    }
}
