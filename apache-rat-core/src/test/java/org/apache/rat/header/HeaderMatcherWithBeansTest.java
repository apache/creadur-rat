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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

/**
 * The Class HeaderMatcherWithBeansTest.
 */
public class HeaderMatcherWithBeansTest {


	/** The matcher. */
	private HeaderMatcher matcher;

	/** The beans. */
	private HeaderBean[] beans;

	/**
	 * Sets the up.
	 * 
	 */
	@Before
	public void setUp() {
		Pattern hatPattern = Pattern.compile("(.*)hat(.*)");
		HeaderBean[] beans = { new HeaderBean(), new HeaderBean(),
				new HeaderBean(hatPattern, true) };
		this.beans = beans;
		SimpleCharFilter filter = new SimpleCharFilter();
		matcher = new HeaderMatcher(filter, 20, beans);
	}

	/**
	 * Nulls.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testNulls() throws IOException {
		beans[0].setMatch(false);
		beans[1].setMatch(true);
		beans[2].setMatch(false);
		StringReader reader = new StringReader("Whatever");
		matcher.read(reader);
		assertFalse("State preserved", beans[0].isMatch());
	}

	/**
	 * Matches.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testMatches() throws IOException {
		beans[0].setHeaderPattern(Pattern.compile("What(.*)"));
		beans[1].setHeaderPattern(Pattern.compile("(.*)ever"));
		beans[2].setHeaderPattern(Pattern.compile("What"));
		StringReader reader = new StringReader("Whatever");
		matcher.read(reader);
		assertTrue("Match header pattern", beans[1].isMatch());
	}
}
