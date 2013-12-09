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
 * The Class ApacheSoftwareLicense20Test.
 */
public class ApacheSoftwareLicense20Test {
	
	/** The worker. */
	private ApacheSoftwareLicense20 worker;

	/**
	 * Sets the up.
	 */
    @Before
    public void setUp() {
    	worker = new ApacheSoftwareLicense20();
    }

	/**
	 * Test matches apache license.
	 */
    @Test
    public void testMatchesApacheLicense() {
		assertTrue("Matches the apache license",
				worker.matches(ApacheSoftwareLicense20.FIRST_LICENSE_LINE));
    }
    
	/**
	 * Test matches non apache license.
	 */
    @Test
    public void testMatchesNonApacheLicense() {
		assertFalse("Not Matches the reference apache license",
				worker.matches("'Behold, Telemachus! (nor fear the sight,)"));
    }
    
	/**
	 * Test match apache license line.
	 */
    @Test
    public void testMatchApacheLicenseLine() {
        final Document subject = new MockLocation("subject");
		assertTrue("Match the apache license", worker.match(subject,
				ApacheSoftwareLicense20.FIRST_LICENSE_LINE));
	}
    
	/**
	 * Test match non apache license.
	 */
	@Test
	public void testMatchNonApacheLicense() {
		final Document subject = new MockLocation("subject");
		if (worker.getNotes() != null) {
			assertFalse("Not Match the reference apache license", worker.match(
					subject, "'Behold, Telemachus! (nor fear the sight,)"));
		}
	}

	/**
	 * Test notes.
	 */
	@Test
	public void testNotes() {
		assertThat(
				this.worker.getNotes(),
				is("Note that APACHE requires a NOTICE. All modifications require notes. See http://www.apache.org/licenses/LICENSE-2.0."));
	}

	/**
	 * Test category.
	 */
	@Test
	public void testCategory() {
		assertThat(this.worker.getLicenseFamilyCategory(), is("AL   "));
	}

	/**
	 * Test name.
	 */
	@Test
	public void testName() {
		assertThat(this.worker.getLicenseFamilyName(),
				is("Apache License Version 2.0"));
	}

}
