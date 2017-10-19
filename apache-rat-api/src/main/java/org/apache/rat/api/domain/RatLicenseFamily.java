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

import static org.apache.rat.api.domain.LicenseFamilyBuilder.aLicenseFamily;

/**
 * Enumerates standard license families known to Rat.
 */
public enum RatLicenseFamily {

	APACHE(
			"Apache License Version 2.0",
			"AL   ",
			"Note that APACHE requires a NOTICE. All modifications require notes. See http://www.apache.org/licenses/LICENSE-2.0."),
	GPL1(
			"GNU General Public License, version 1",
			"GPL1 ",
			"Note that GPL1 requires a NOTICE. All modifications require notes. See http://www.gnu.org/licenses/gpl-1.0.html."),
	GPL2(
			"GNU General Public License, version 2",
			"GPL2 ",
			"Note that GPL2 requires a NOTICE. All modifications require notes. See http://www.gnu.org/licenses/gpl-2.0.html."),
	GPL3(
			"GNU General Public License, version 3",
			"GPL3 ",
			"Note that GPL3 requires a NOTICE. All modifications require notes. See http://www.gnu.org/licenses/gpl-3.0.html."),
	MIT(
			"The MIT License",
			"MIT  ",
			"Note that MIT requires a NOTICE. All modifications require notes. See http://opensource.org/licenses/MIT."),
	CDDL1(
			"COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0",
			"CDDL1",
			"Note that CDDL1 requires a NOTICE. All modifications require notes. See https://oss.oracle.com/licenses/CDDL."),
	OASIS(
			"OASIS Open License",
			"OASIS",
			"Note that OASIS requires a NOTICE. All modifications require notes. See https://www.oasis-open.org/policies-guidelines/ipr."),
	TMF854(
			"Modified BSD License",
			"TMF  ",
			"Note that TMF854 requires a NOTICE. All modifications require notes. See http://opensource.org/licenses/BSD-3-Clause."),
	 DOJO(
			"Modified BSD License",
			"DOJO ",
			"Note that DOJO requires a NOTICE. All modifications require notes. See http://dojotoolkit.org/community/licensing.shtml."),
    W3C(
            "W3C Software Copyright",
            "W3C  ",
            "Note that W3C requires a NOTICE. All modifications require notes. See http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231."),

    W3C_DOCUMENTATION(
            "W3C Document Copyright",
            "W3CD ",
            "Note that W3CD does not allow modifications. See http://www.w3.org/Consortium/Legal/2002/copyright-documents-20021231.");

    /** @see LicenseFamily#getName() */
    private final String name;
    /** @see LicenseFamily#getCategory() */
    private final String category;
    /** @see LicenseFamily#getNotes() */
    private final String notes;
    /** Constructed from other data */
    private final LicenseFamily licenseFamily;

    /**
     * Constructs an instance.
     * 
     * @param name
     *            not null
     * @param category
     *            not null
     * @param notes
     *            not null
     */
	RatLicenseFamily(final String name, final String category,
					 final String notes) {
        this.name = name;
        this.category = category;
        this.notes = notes;
        this.licenseFamily = aLicenseFamily().withCategory(getCategory())
                .withName(getName()).withNotes(getNotes()).build();
    }

    /**
     * @see LicenseFamily#getName()
     * 
     * @return not null
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see LicenseFamily#getCategory()
     * 
     * @return possibly null
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * @see LicenseFamily#getNotes()
     * 
     * @return possibly null
     */
    public String getNotes() {
        return this.notes;
    }

    /**
     * Gets a {@link LicenseFamily} representing this data.
     * 
     * @return not null
     */
    public LicenseFamily licenseFamily() {
        return this.licenseFamily;
    }
}
