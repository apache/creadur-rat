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
package org.apache.rat.analysis.generation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.apache.rat.test.utils.Resources;
import org.junit.Before;
import org.junit.Test;


/**
 * The Class JavaDocLicenseNotRequiredTest.
 */
public class JavaDocLicenseNotRequiredTest {

	private static IHeaderMatcher license;

	@Before
	public void setUp() {
		license = new JavaDocLicenseNotRequired();
	}

	@Test
	public void testMatchIndexDocLicense() throws IOException {
		final boolean result = readAndMatch("index.html");
		assertTrue("Is a javadoc", result);
	}

	@Test
	public void testMatchClassDocLicense() throws IOException {
		final boolean result = readAndMatch("ArchiveElement.html");
		assertTrue("Is a javadoc", result);
	}

	@Test
	public void testMatchNonJavaDocLicense() throws IOException {
		final boolean result = readAndMatch("notjavadoc.html");
		assertFalse("Not javadocs and so should return null", result);
	}

	/**
	 * Read and match.
	 * 
	 * @param name
	 *            the name
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private boolean readAndMatch(final String name) throws IOException {
		final File file = Resources.getResourceFile("javadocs/" + name);
		boolean result = false;
		final BufferedReader bufferedReader = new BufferedReader(
				new FileReader(file));
		String line = String.valueOf(bufferedReader.readLine());
		final Document subject = new MockLocation("subject");
		while (bufferedReader.readLine() != null && !result) {
			result = ((JavaDocLicenseNotRequired) license).match(subject, line);
			line = bufferedReader.readLine();
		}
		bufferedReader.close();
		return result;
	}
}
