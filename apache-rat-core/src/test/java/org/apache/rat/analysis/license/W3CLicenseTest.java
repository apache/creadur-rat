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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class W3CLicenseTest.
 */
public class W3CLicenseTest {

	/** The Constant COPYRIGHT_URL. */
    public static final String COPYRIGHT_URL =
            "http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231";

	/** The Constant COPYRIGHT_URL_COMMENTED. */
    public static final String COPYRIGHT_URL_COMMENTED =
            "# http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231 #";

	/** The Constant COPYRIGHT_URL_XML. */
    public static final String COPYRIGHT_URL_XML =
            "<!-- http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231 -->";

	/** The license. */
    W3CLicense license;

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
    @Before
    public void setUp() throws Exception {
        this.license = new W3CLicense();
    }

	/**
	 * Match.
	 * 
	 * @throws Exception
	 *             the exception
	 */
    @Test
    public void match() throws Exception {
        final Document subject = new MockLocation("subject");
        assertTrue("Expected matcher to return license",
                this.license.match(subject, COPYRIGHT_URL));
        assertTrue("Expected matcher to return license",
                this.license.match(subject, COPYRIGHT_URL_COMMENTED));
        assertTrue("Expected matcher to return license",
                this.license.match(subject, COPYRIGHT_URL_XML));
        assertFalse("Return null if the license isn't matched",
                this.license.match(subject, "Bogus"));
    }

	/**
	 * Test notes.
	 */
    @Test
    public void testNotes() {
        assertThat(
                this.license.getNotes(),
                is("Note that W3C requires a NOTICE. All modifications require notes. See http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231."));
    }

	/**
	 * Test category.
	 */
    @Test
    public void testCategory() {
        assertThat(this.license.getLicenseFamilyCategory(), is("W3C  "));
    }

	/**
	 * Test name.
	 */
    @Test
    public void testName() {
        assertThat(this.license.getLicenseFamilyName(),
                is("W3C Software Copyright"));
    }
}
