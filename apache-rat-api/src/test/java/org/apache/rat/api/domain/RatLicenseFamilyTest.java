/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.api.domain;

import static org.apache.rat.api.domain.RatLicenseFamily.APACHE;
import static org.apache.rat.api.domain.RatLicenseFamily.CDDL1;
import static org.apache.rat.api.domain.RatLicenseFamily.DOJO;
import static org.apache.rat.api.domain.RatLicenseFamily.GPL1;
import static org.apache.rat.api.domain.RatLicenseFamily.GPL2;
import static org.apache.rat.api.domain.RatLicenseFamily.GPL3;
import static org.apache.rat.api.domain.RatLicenseFamily.MIT;
import static org.apache.rat.api.domain.RatLicenseFamily.OASIS;
import static org.apache.rat.api.domain.RatLicenseFamily.TMF854;
import static org.apache.rat.api.domain.RatLicenseFamily.W3C;
import static org.apache.rat.api.domain.RatLicenseFamily.W3C_DOCUMENTATION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class RatLicenseFamilyTest {

    @Test
    public void testW3CLicenseFamilyCategory() {
        assertThat(W3C.getCategory(), is("W3C  "));
    }

    @Test
    public void testW3CLicenseFamilyName() {
        assertThat(W3C.getName(), is("W3C Software Copyright"));
    }

    @Test
    public void testW3CLicenseFamilyNotes() {
        assertThat(
                W3C.getNotes(),
                is("Note that W3C requires a NOTICE. All modifications require notes. See http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231."));
    }

    @Test
    public void testW3CDocLicenseFamilyCategory() {
        assertThat(W3C_DOCUMENTATION.getCategory(), is("W3CD "));
    }

    @Test
    public void testW3CDocLicenseFamilyName() {
        assertThat(W3C_DOCUMENTATION.getName(), is("W3C Document Copyright"));
    }

    @Test
    public void testW3CDocLicenseFamilyNotes() {
        assertThat(
                W3C_DOCUMENTATION.getNotes(),
                is("Note that W3CD does not allow modifications. See http://www.w3.org/Consortium/Legal/2002/copyright-documents-20021231."));
    }

	@Test
	public void testAPACHELicenseFamilyCategory() {
		assertThat(APACHE.getCategory(), is("AL   "));
	}

	@Test
	public void testAPACHELicenseFamilyName() {
		assertThat(APACHE.getName(), is("Apache License Version 2.0"));
	}

	@Test
	public void testAPACHELicenseFamilyNotes() {
		assertThat(
				APACHE.getNotes(),
				is("Note that APACHE requires a NOTICE. All modifications require notes. See http://www.apache.org/licenses/LICENSE-2.0."));
	}

	@Test
	public void testGPL1LicenseFamilyCategory() {
		assertThat(GPL1.getCategory(), is("GPL1 "));
	}

	@Test
	public void testGPL1LicenseFamilyName() {
		assertThat(GPL1.getName(), is("GNU General Public License, version 1"));
	}

	@Test
	public void testGPL1LicenseFamilyNotes() {
		assertThat(
				GPL1.getNotes(),
				is("Note that GPL1 requires a NOTICE. All modifications require notes. See http://www.gnu.org/licenses/gpl-1.0.html."));
	}

	@Test
	public void testGPL2LicenseFamilyCategory() {
		assertThat(GPL2.getCategory(), is("GPL2 "));
	}

	@Test
	public void testGPL2LicenseFamilyName() {
		assertThat(GPL2.getName(), is("GNU General Public License, version 2"));
	}

	@Test
	public void testGPL2LicenseFamilyNotes() {
		assertThat(
				GPL2.getNotes(),
				is("Note that GPL2 requires a NOTICE. All modifications require notes. See http://www.gnu.org/licenses/gpl-2.0.html."));
	}

	@Test
	public void testGPL3LicenseFamilyCategory() {
		assertThat(GPL3.getCategory(), is("GPL3 "));
	}

	@Test
	public void testGPL3LicenseFamilyName() {
		assertThat(GPL3.getName(), is("GNU General Public License, version 3"));
	}

	@Test
	public void testGPL3LicenseFamilyNotes() {
		assertThat(
				GPL3.getNotes(),
				is("Note that GPL3 requires a NOTICE. All modifications require notes. See http://www.gnu.org/licenses/gpl-3.0.html."));
	}

	@Test
	public void testMITLicenseFamilyCategory() {
		assertThat(MIT.getCategory(), is("MIT  "));
	}

	@Test
	public void testMITLicenseFamilyName() {
		assertThat(MIT.getName(), is("The MIT License"));
	}

	@Test
	public void testMITLicenseFamilyNotes() {
		assertThat(
				MIT.getNotes(),
				is("Note that MIT requires a NOTICE. All modifications require notes. See http://opensource.org/licenses/MIT."));
	}

	@Test
	public void testCDDL1LicenseFamilyCategory() {
		assertThat(CDDL1.getCategory(), is("CDDL1"));
	}

	@Test
	public void testCDDL1LicenseFamilyName() {
		assertThat(CDDL1.getName(),
				is("COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0"));
	}

	@Test
	public void testCDDL1LicenseFamilyNotes() {
		assertThat(
				CDDL1.getNotes(),
				is("Note that CDDL1 requires a NOTICE. All modifications require notes. See https://oss.oracle.com/licenses/CDDL."));
	}

	@Test
	public void testOASISLicenseFamilyCategory() {
		assertThat(OASIS.getCategory(), is("OASIS"));
	}

	@Test
	public void testOASISLicenseFamilyName() {
		assertThat(OASIS.getName(), is("OASIS Open License"));
	}

	@Test
	public void testOASISLicenseFamilyNotes() {
		assertThat(
				OASIS.getNotes(),
				is("Note that OASIS requires a NOTICE. All modifications require notes. See https://www.oasis-open.org/policies-guidelines/ipr."));
	}
	
	@Test
	public void testTMF854LicenseFamilyCategory() {
		assertThat(TMF854.getCategory(), is("TMF  "));
	}

	@Test
	public void testTMF854LicenseFamilyName() {
		assertThat(TMF854.getName(), is("Modified BSD License"));
	}

	@Test
	public void testTMF854LicenseFamilyNotes() {
		assertThat(
				TMF854.getNotes(),
				is("Note that TMF854 requires a NOTICE. All modifications require notes. See http://opensource.org/licenses/BSD-3-Clause."));
	}
	
	@Test
	public void testDOJOLicenseFamilyCategory() {
		assertThat(DOJO.getCategory(), is("DOJO "));
	}

	@Test
	public void testDOJOLicenseFamilyName() {
		assertThat(DOJO.getName(), is("Modified BSD License"));
	}

	@Test
	public void testDOJOLicenseFamilyNotes() {
		assertThat(
				DOJO.getNotes(),
				is("Note that DOJO requires a NOTICE. All modifications require notes. See http://dojotoolkit.org/community/licensing.shtml."));
	}
}
