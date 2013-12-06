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
 * Tests GPL license occurrences within comments and other characters. Works for
 * GPL1 to GPL3.
 */
public class GPL123LicenseTest {
	private Document subject;

	/**
	 * To ease testing provide a map with a given license version and the string
	 * to test for.
	 */
	private static Map<IHeaderMatcher, String> licenseStringMap;

	/**
	 * If you replace this with BeforeClass and make this method static the
	 * build fails at line 67.
	 */
	@Before
	public void initLicencesUnderTest() {
		licenseStringMap = new HashMap<IHeaderMatcher, String>();
		licenseStringMap.put(new GPL1License(), GPL1License.FIRST_LICENSE_LINE);
		licenseStringMap.put(new GPL2License(), GPL2License.FIRST_LICENSE_LINE);
		licenseStringMap.put(new GPL3License(), GPL3License.FIRST_LICENSE_LINE);
		this.subject = new MockLocation("subject");
	}

	@Test
	public void testNegativeMatchGPL123License() {
		for (Map.Entry<IHeaderMatcher, String> licenceUnderTest : licenseStringMap
				.entrySet()) {
			assertFalse(
					"Error Match GPL123 license",
					licenceUnderTest.getKey().match(subject,
							"'Behold, Telemachus! (nor fear the sight,)"));
		}
	}

	@Test
	public void testPositiveMatchGPL123License() {
		for (Map.Entry<IHeaderMatcher, String> licenceUnderTest : licenseStringMap
				.entrySet()) {
			assertTrue("Not Match GPL123 license", licenceUnderTest.getKey()
					.match(subject, "\t" + licenceUnderTest.getValue()));
		}
	}
	
	@Test
	public void testGPL1Notes() {
		assertThat(
				new GPL1License().getNotes(),
				is("Note that GPL1 requires a NOTICE. All modifications require notes. See http://www.gnu.org/licenses/gpl-1.0.html."));
	}

	@Test
	public void testGPL1Category() {
		assertThat(new GPL1License().getLicenseFamilyCategory(), is("GPL1 "));
	}

	@Test
	public void testGPL1Name() {
		assertThat(new GPL1License().getLicenseFamilyName(),
				is("GNU General Public License, version 1"));
	}

	@Test
	public void testGPL2Notes() {
		assertThat(
				new GPL2License().getNotes(),
				is("Note that GPL2 requires a NOTICE. All modifications require notes. See http://www.gnu.org/licenses/gpl-2.0.html."));
	}

	@Test
	public void testGPL2Category() {
		assertThat(new GPL2License().getLicenseFamilyCategory(), is("GPL2 "));
	}

	@Test
	public void testGPL2Name() {
		assertThat(new GPL2License().getLicenseFamilyName(),
				is("GNU General Public License, version 2"));
	}

	@Test
	public void testGPL3Notes() {
		assertThat(
				new GPL3License().getNotes(),
				is("Note that GPL3 requires a NOTICE. All modifications require notes. See http://www.gnu.org/licenses/gpl-3.0.html."));
	}

	@Test
	public void testGPL3Category() {
		assertThat(new GPL3License().getLicenseFamilyCategory(), is("GPL3 "));
	}

	@Test
	public void testGPL3Name() {
		assertThat(new GPL3License().getLicenseFamilyName(),
				is("GNU General Public License, version 3"));
	}

}
