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

package org.apache.rat.report.claim;

import org.junit.Assert;
import org.junit.Test;


/**
 * The Class ClaimStatisticTest.
 */
public class ClaimStatisticTest {


	/**
	 * Test get num approved.
	 * 
	 */
	@Test
	public void testGetNumApproved() {
		ClaimStatistic claimStatistic = new ClaimStatistic();
		Assert.assertNotNull(claimStatistic.getNumApproved());
	}

	/**
	 * Test get num un approved.
	 */
	@Test
	public void testGetNumUnApproved() {
		ClaimStatistic claimStatistic = new ClaimStatistic();
		Assert.assertNotNull(claimStatistic.getNumUnApproved());
	}

	/**
	 * Test get num generated.
	 */
	@Test
	public void testGetNumGenerated() {
		ClaimStatistic claimStatistic = new ClaimStatistic();
		Assert.assertNotNull(claimStatistic.getNumGenerated());
	}

	/**
	 * Test get num unknown.
	 */
	@Test
	public void testGetNumUnknown() {
		ClaimStatistic claimStatistic = new ClaimStatistic();
		Assert.assertNotNull(claimStatistic.getNumUnknown());
	}

	/**
	 * Test get license file code map.
	 */
	@Test
	public void testGetLicenseFileCodeMap() {
		ClaimStatistic claimStatistic = new ClaimStatistic();
		Assert.assertNull(claimStatistic.getLicenseFileCodeMap());
	}

	/**
	 * Test get license file name map.
	 */
	@Test
	public void testGetLicenseFileNameMap() {
		ClaimStatistic claimStatistic = new ClaimStatistic();
		Assert.assertNull(claimStatistic.getLicenseFileNameMap());
	}

}
