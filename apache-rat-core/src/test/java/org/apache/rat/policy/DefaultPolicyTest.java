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
package org.apache.rat.policy;

import static org.apache.rat.api.domain.RatLicenseFamily.APACHE;
import static org.apache.rat.api.domain.RatLicenseFamily.MIT;
import static org.apache.rat.api.domain.RatLicenseFamily.OASIS;
import static org.apache.rat.api.domain.RatLicenseFamily.W3C;
import static org.apache.rat.api.domain.RatLicenseFamily.W3C_DOCUMENTATION;
import static org.junit.Assert.assertEquals;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.document.MockLocation;
import org.apache.rat.license.Apache20LicenseFamily;
import org.apache.rat.license.CDDL1LicenseFamily;
import org.apache.rat.license.GPL1LicenseFamily;
import org.apache.rat.license.GPL2LicenseFamily;
import org.apache.rat.license.GPL3LicenseFamily;
import org.apache.rat.license.MITLicenseFamily;
import org.apache.rat.license.ModifiedBSDLicenseFamily;
import org.apache.rat.license.OASISLicenseFamily;
import org.apache.rat.license.ILicenseFamily;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class DefaultPolicyTest.
 */
public class DefaultPolicyTest {

	/** The policy. */
	private DefaultPolicy policy;
	
	/** The subject. */
	private Document subject;

	/**
	 * Sets the up.
	 *
	 */
	@Before
	public void setUp() {
		policy = new DefaultPolicy();
		subject = new MockLocation("subject");
	}

	/**
	 * Test al family.
	 *
	 */
	@Test
	public void testALFamily() {
		subject.getMetaData().set(
				new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, APACHE
						.getName()));
		policy.analyse(subject);
		assertApproval(true);
	}

	/**
	 * Assert approval.
	 *
	 * @param pApproved the approved
	 */
	private void assertApproval(final boolean pApproved) {
		assertEquals("Metadata value are equals.", pApproved,
				MetaData.RAT_APPROVED_LICENSE_VALUE_TRUE
						.equals(subject.getMetaData().value(
								MetaData.RAT_URL_APPROVED_LICENSE)));
	}

	/**
	 * Test oasis family.
	 *
	 */
	@Test
	public void testOASISFamily() {
		subject.getMetaData().set(
				new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, OASIS
						.getName()));
		policy.analyse(subject);
		assertApproval(true);
	}

	/**
	 * Test w3 c family.
	 *
	 */
	@Test
	public void testW3CFamily() {
		subject.getMetaData().set(
				new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, W3C
						.getName()));
		policy.analyse(subject);
		assertApproval(true);
	}

	/**
	 * Test w3 c doc family.
	 *
	 */
	@Test
	public void testW3CDocFamily() {
		subject.getMetaData().set(
				new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME,
						W3C_DOCUMENTATION.getName()));
		policy.analyse(subject);
		assertApproval(true);
	}

	/**
	 * Test mit family.
	 *
	 */
	@Test
	public void testMITFamily() {
		subject.getMetaData().set(
				new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, MIT
						.getName()));
		policy.analyse(subject);
		assertApproval(true);
	}

	/**
	 * Test unknown family.
	 *
	 */
	@Test
	public void testUnknownFamily() {
		subject.getMetaData().set(
				MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_UNKNOWN);
		policy.analyse(subject);
		assertApproval(false);
	}

	/**
	 * Test constructor i license family.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testConstructorILicenseFamily() throws Exception {
		ILicenseFamily[] approvedLicenses = new ILicenseFamily[8];
		approvedLicenses[0] = new Apache20LicenseFamily();
		approvedLicenses[1] = new CDDL1LicenseFamily();
		approvedLicenses[2] = new GPL1LicenseFamily();
		approvedLicenses[3] = new GPL2LicenseFamily();
		approvedLicenses[4] = new GPL3LicenseFamily();
		approvedLicenses[5] = new MITLicenseFamily();
		approvedLicenses[6] = new ModifiedBSDLicenseFamily();
		approvedLicenses[7] = new OASISLicenseFamily();
		policy = new DefaultPolicy(approvedLicenses);
		Assert.assertNotNull(policy);
	}
}
