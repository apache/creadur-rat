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
package org.apache.rat.analysis.util;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.MockLicenseMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class MatcherMultiplexerTest {

    private static final String LINE_ONE = "Line One";
    private static final String LINE_TWO = "Line Two";

    private MockClaimReporter reporter;
    private MockLicenseMatcher matcherOne;
    private MockLicenseMatcher matcherTwo;

    private HeaderMatcherMultiplexer multiplexer;

    @Before
    public void setUp() {
        matcherOne = new MockLicenseMatcher();
        matcherTwo = new MockLicenseMatcher();
        multiplexer = new HeaderMatcherMultiplexer(Arrays.<IHeaderMatcher>asList(matcherOne, matcherTwo));
        reporter = new MockClaimReporter();
    }

    @Test
    public void testMatcherLine() throws Exception {
        matcherOne.result = false;
        matcherTwo.result = false;
        final Document subject = new MockLocation("subject");
        multiplexer.match(subject, LINE_ONE);
        assertEquals("One line", 1, matcherOne.lines.size());
        assertEquals("Same as line passed", LINE_ONE, matcherOne.lines.get(0));
        assertEquals("One line", 1, matcherTwo.lines.size());
        assertEquals("Same as line passed", LINE_ONE, matcherTwo.lines.get(0));
        multiplexer.match(subject, LINE_TWO);
        assertEquals("One line", 2, matcherOne.lines.size());
        assertEquals("Same as line passed", LINE_TWO, matcherOne.lines.get(1));
        assertEquals("One line", 2, matcherTwo.lines.size());
        assertEquals("Same as line passed", LINE_TWO, matcherTwo.lines.get(1));
    }

    @Test
    public void testReset() {
        multiplexer.reset();
        assertEquals("Reset once", 1, matcherOne.resets);
        assertEquals("Reset once", 1, matcherTwo.resets);
        multiplexer.reset();
        assertEquals("Reset twice", 2, matcherOne.resets);
        assertEquals("Reset twice", 2, matcherTwo.resets);
    }
}
