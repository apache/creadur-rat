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
package org.apache.rat.header;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

/**
 * The Class HeaderMatcherTest.
 */
public class HeaderMatcherTest {

	/** The matcher. */
	private HeaderMatcher matcher;

	/** The filter. */
	private SimpleCharFilter filter;

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		filter = new SimpleCharFilter();
		matcher = new HeaderMatcher(filter, 20);
	}

	/**
	 * Simple matches.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSimpleMatches() throws IOException {
		Pattern hatPattern = Pattern.compile("(.*)hat(.*)");
		StringReader reader = new StringReader("The mad hatter");
		matcher.read(reader);
		assertTrue("Expresion must be true", matcher.matches(hatPattern));
	}

	/**
	 * Test non simple matches.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testNonSimpleMatches() throws IOException {
		Pattern headPattern = Pattern.compile("head....");
		StringReader reader = new StringReader("The mad hatter");
		matcher.read(reader);
		assertFalse("Expresion must be false", matcher.matches(headPattern));
	}

	/**
	 * Filtered matches.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testFilteredMatches() throws IOException {
		Pattern capPattern = Pattern.compile("cap(.*)");
		StringReader reader = new StringReader("capped");
		matcher.read(reader);
		assertTrue("Expresion must be true", matcher.matches(capPattern));
	}

	/**
	 * Test non filtered matches.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testNonFilteredMatches() throws IOException {
		Pattern capPattern = Pattern.compile("cap(.*)");
		filter.filterOut = true;
		StringReader reader = new StringReader("capped");
		matcher.read(reader);
		assertFalse("Expresion must be false", matcher.matches(capPattern));
	}

	/**
	 * No lines.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testNoLines() throws IOException {
		StringReader reader = new StringReader("None");
		matcher.read(reader);
		assertEquals("No lines read", 0, matcher.lines());
	}

	/**
	 * Lines.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testLines() throws IOException {
		StringReader reader = new StringReader("One\nTwo\nThree\n");
		matcher.read(reader);
		assertEquals("Three lines read", 3, matcher.lines());
	}

	/**
	 * Too many lines.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testTooManyLines() throws IOException {
		StringReader reader = new StringReader(
				"WhateverWhateverWhateverWhateverWhateverWhateverWhateverWhatever");
		matcher.read(reader);
		assertEquals("Too many lines read", -1, matcher.lines());
	}
}
