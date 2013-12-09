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

package org.apache.rat;

import org.apache.rat.license.ILicenseFamily;
import org.junit.Assert;
import org.junit.Test;


/**
 * The Class ReportConfigurationTest.
 */
public class ReportConfigurationTest {



	/**
	 * Test set approved license names.
	 */
    @Test
	public void testSetApprovedLicenseNames() {
		ReportConfiguration reportConfiguration = new ReportConfiguration();
		ILicenseFamily[] approvedLicenseNames = new ILicenseFamily[1];
		reportConfiguration.setApprovedLicenseNames(approvedLicenseNames);
	}

	/**
	 * Test get copyright message.
	 */
	@Test
	public void testGetCopyrightMessage() {
		ReportConfiguration reportConfiguration = new ReportConfiguration();
		reportConfiguration.setCopyrightMessage("Message");
		Assert.assertNotNull(reportConfiguration.getCopyrightMessage());
	}

	/**
	 * Test is adding licenses forced.
	 */
	@Test
	public void testIsAddingLicensesForced() {
		ReportConfiguration reportConfiguration = new ReportConfiguration();
		reportConfiguration.setAddingLicensesForced(true);
		Assert.assertTrue(reportConfiguration.isAddingLicensesForced());
	}

	/**
	 * Test set adding licenses.
	 */
	@Test
	public void testSetAddingLicenses() {
		ReportConfiguration reportConfiguration = new ReportConfiguration();
		reportConfiguration.setAddingLicenses(true);
	}

}
