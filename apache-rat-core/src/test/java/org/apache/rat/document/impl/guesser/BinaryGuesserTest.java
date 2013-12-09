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
package org.apache.rat.document.impl.guesser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.rat.api.Document;
import org.apache.rat.document.MockDocument;
import org.apache.rat.document.impl.FileDocument;
import org.junit.Test;

/**
 * The Class BinaryGuesserTest.
 */
public class BinaryGuesserTest {

	/**
	 * Test matches.
	 */
	@Test
	public void testBinaryGuesserMatches() {
		assertThatDocumentIsBinary("image.png");
		assertThatDocumentIsBinary("libicudata.so.34.");
	}

	/**
	 * Assert that document is binary.
	 * 
	 * @param name
	 *            the name
	 */
	private void assertThatDocumentIsBinary(final String name) {
		assertTrue("Value return must be True",
				new BinaryGuesser().matches(new MockDocument(name)));
	}

	/**
	 * Used to swallow a MalformedInputException and return false because the
	 * encoding of the stream was different from the platform's default
	 * encoding.
	 * 
	 * @throws Throwable
	 *             the throwable
	 * @see "RAT-81"
	 */
	@Test
	public void testBinaryWithMalformedInputRAT81() throws Throwable {
		Document doc = new FileDocument(new File(
				"src/test/resources/binaries/UTF16_with_signature.xml"));
		Reader reader = null;
		try {
			final char[] dummy = new char[100];
			reader = doc.reader();
			reader.read(dummy);
			// if we get here, the UTF-16 encoded file didn't throw
			// any exception, try the UTF-8 encoded one
			reader.close();
			doc = new FileDocument(new File(
					"src/test/resources/binaries/UTF8_with_signature.xml"));
			reader = doc.reader();
			reader.read(dummy);
		} catch (final IOException e) {
			assertTrue("Value return must be True",
					new BinaryGuesser().matches(doc));
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Real binary content.
	 */
	@Test
	public void testRealBinaryContent() {
		// This test is not accurate on all platforms
		if (System.getProperty("file.encoding").startsWith("ANSI")) {
			assertTrue("Value return must be True",
					new BinaryGuesser().matches(new FileDocument(new File(
					"src/test/resources/binaries/Image-png.not"))));
		}
	}

	/**
	 * Textual content.
	 */
	@Test
	public void testTextualContent() {
		assertFalse("Value return must be False",
				new BinaryGuesser().matches(new FileDocument(new File(
				"src/test/resources/elements/Text.txt"))));
	}

	/**
	 * Empty file.
	 */
	@Test
	public void testEmptyFile() {
		assertFalse("Value return must be False",
				new BinaryGuesser().matches(new FileDocument(new File(
				"src/test/resources/elements/sub/Empty.txt"))));
	}
}
