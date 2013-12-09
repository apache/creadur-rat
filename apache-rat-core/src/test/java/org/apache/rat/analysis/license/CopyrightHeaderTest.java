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
package org.apache.rat.analysis.license;

import static org.apache.rat.api.domain.RatLicenseFamily.APACHE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class CopyrightHeaderTest.
 */
public class CopyrightHeaderTest {

	/** The Constant MATCHING_HEADERS. */
	private static final String[] MATCHING_HEADERS = {
			"/*  Copyright 2012 FooBar.*/", "/*  copyright 2012 foobar.*/",
			"/*  Copyright 2012-2013 FooBar.*/" };

	/** The Constant NON_MATCHING_HEADERS. */
	private static final String[] NON_MATCHING_HEADERS = { "/*  Copyright*/",
			"/*  Copyright FooBar.*/", "/*  Copyright 2013*/",
			"/*  Copyright 123a*/", "/*  Copyright 123f oobar*/",
			"/*  Copyright 2013FooBar*/", "/*  Copyright 2012 2013 FooBar.*/" };

	/** The header. */
	private CopyrightHeader header;

	/** The subject. */
	private Document subject = new MockLocation("subject");

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		header = new CopyrightHeader(APACHE.licenseFamily(), "FooBar");
		subject = new MockLocation("subject");
	}

	/**
	 * Test match copyright header.
	 */
	@Test
	public void testMatchCopyrightHeader() {
		for (String line : MATCHING_HEADERS) {
			assertTrue("Copyright Header should be matched",
					header.match(subject, line));
			header.reset();
		}
	}

	/**
	 * Test no match copyright header.
	 */
	@Test
	public void testNoMatchCopyrightHeader() {
		for (String line : NON_MATCHING_HEADERS) {
			assertFalse("Copyright Header shouldn't be matched",
					header.match(subject, line));
			header.reset();
		}
	}
}
