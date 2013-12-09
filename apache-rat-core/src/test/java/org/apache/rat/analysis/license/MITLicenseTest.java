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

import java.util.HashMap;
import java.util.Map;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class MITLicenseTest.
 */
public class MITLicenseTest {

	/** The subject. */
	private Document subject;

	/**
	 * To ease testing provide a map with a given license version and the string
	 * to test for.
	 */
	private static Map<IHeaderMatcher, String> licenseStringMap;

	/**
	 * If you replace this with BeforeClass and make this method static the
	 * build fails at line 71.
	 */
	@Before
	public void initLicencesUnderTest() {
		licenseStringMap = new HashMap<IHeaderMatcher, String>();
		licenseStringMap.put(new MITLicense(), MITLicense.FIRST_LICENSE_LINE
				+ "\n" + MITLicense.MIDDLE_LICENSE_LINE + "\r\n * "
				+ MITLicense.AS_IS_LICENSE_LINE);
		this.subject = new MockLocation("subject");
	}

	/**
	 * Test negative match mit license.
	 */
	@Test
	public void testNegativeMatchMITLicense() {
		for (Map.Entry<IHeaderMatcher, String> licenceUnderTest : licenseStringMap
				.entrySet()) {
			assertFalse(
					"Error match MITLicense",
					licenceUnderTest.getKey().match(subject,
							"'Behold, Telemachus! (nor fear the sight,)"));
		}
	}

	/**
	 * Test positive match mit license.
	 */
	@Test
	public void testPositiveMatchMITLicense() {
		for (Map.Entry<IHeaderMatcher, String> licenceUnderTest : licenseStringMap
				.entrySet()) {
			assertTrue(
					"Not match MITLicense",
					licenceUnderTest.getKey().match(subject,
							"\t" + licenceUnderTest.getValue()));
		}
	}

	/**
	 * Test notes.
	 */
	@Test
	public void testNotes() {
		assertThat(
				new MITLicense().getNotes(),
				is("Note that MIT requires a NOTICE. All modifications require notes. See http://opensource.org/licenses/MIT."));
	}

	/**
	 * Test category.
	 */
	@Test
	public void testCategory() {
		assertThat(new MITLicense().getLicenseFamilyCategory(), is("MIT  "));
	}

	/**
	 * Test name.
	 */
	@Test
	public void testName() {
		assertThat(new MITLicense().getLicenseFamilyName(),
				is("The MIT License"));
	}

}
