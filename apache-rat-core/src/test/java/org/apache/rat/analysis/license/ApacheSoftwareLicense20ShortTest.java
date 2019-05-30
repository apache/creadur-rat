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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test to see if short form license information will be recognized correctly.
 * 
 * @see  <a href="https://www.apache.org/legal/src-headers.html#is-a-short-form-of-the-source-header-available">https://www.apache.org/legal/src-headers.html#is-a-short-form-of-the-source-header-available</a>
 * 
 * @author Karl Heinz Marbaise
 *
 */
public class ApacheSoftwareLicense20ShortTest {

	private static final String SHORT_LICENSE = "Licensed to the Apache Software Foundation (ASF) "
			+ "under one or more contributor license agreements; and to You under the Apache License, Version 2.0.";

	@Test
	public void apacheShortLicenseShouldBeIdentified() {
		ApacheSoftwareLicense20 worker = new ApacheSoftwareLicense20();
		assertTrue(worker.matches(ApacheSoftwareLicense20.FIRST_LICENSE_LINE_SHORT));
	}

	@Test
	public void apacheShortLicenseShouldBeIdentifiedWithDifferentPreAndPostFixes() {
		ApacheSoftwareLicense20 worker = new ApacheSoftwareLicense20();
		assertTrue(worker.matches(SHORT_LICENSE));
		assertTrue(worker.matches("# " + SHORT_LICENSE));
		assertTrue(worker.matches("##" + SHORT_LICENSE));
		assertTrue(worker.matches("##" + SHORT_LICENSE + "##"));
		assertTrue(worker.matches("/* " + SHORT_LICENSE + "*/"));
		assertTrue(worker.matches("/* " + SHORT_LICENSE + "*/"));
	}

}
