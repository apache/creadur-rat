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

import junit.framework.TestCase;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.MockLicenseMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class MatcherMultiplexerTest.
 */
public class MatcherMultiplexerTest extends TestCase {

	/** The Constant LINE_ONE. */
	private static final String LINE_ONE = "Line One";

	/** The Constant LINE_TWO. */
	private static final String LINE_TWO = "Line Two";

	/** The matcher one. */
	private MockLicenseMatcher matcherOne;

	/** The matcher two. */
	private MockLicenseMatcher matcherTwo;

	/** The multiplexer. */
	private HeaderMatcherMultiplexer multiplexer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		matcherOne = new MockLicenseMatcher();
		matcherTwo = new MockLicenseMatcher();
		IHeaderMatcher[] matchers = { matcherOne, matcherTwo };
		multiplexer = new HeaderMatcherMultiplexer(matchers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test matcher line.
	 * 
	 */
	@Test
	public void testMatcherLineHeaderMatcherMultiplexer() {
		matcherOne.result = false;
		matcherTwo.result = false;
		final Document subject = new MockLocation("subject");
		multiplexer.match(subject, LINE_ONE);
		multiplexer.match(subject, LINE_TWO);
		assertEquals("One line", 2, matcherOne.lines.size());
	}

	/**
	 * Test matcher value header matcher multiplexer.
	 */
	@Test
	public void testMatcherValueHeaderMatcherMultiplexer() {
		matcherOne.result = false;
		matcherTwo.result = false;
		final Document subject = new MockLocation("subject");
		multiplexer.match(subject, LINE_ONE);
		multiplexer.match(subject, LINE_TWO);
		assertEquals("Same as line passed", LINE_TWO, matcherTwo.lines.get(1));
	}

	/**
	 * Test reset.
	 */
	@Test
	public void testResetHeaderMatcherMultiplexer() {
		multiplexer.reset();
		multiplexer.reset();
		assertEquals("Reset once", 2, matcherTwo.resets);
	}
}
