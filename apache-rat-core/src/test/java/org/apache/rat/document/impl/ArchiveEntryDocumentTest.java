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
package org.apache.rat.document.impl;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.rat.test.utils.Resources;
import org.junit.Assert;
import org.junit.Test;


/**
 * The Class ArchiveEntryDocumentTest.
 */
public class ArchiveEntryDocumentTest {


	/**
	 * Test archive entry document constructor.
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
    @Test
	public void testArchiveEntryDocumentConstructor()
			throws FileNotFoundException {
		File file = Resources.getResourceFile("elements/Source.java");
		byte[] arrayBytes = new byte[1];
		assertNotNull(new ArchiveEntryDocument(file, arrayBytes));
    }

	/**
	 * Test get meta data.
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	@Test
	public void testGetMetaData() throws FileNotFoundException {
		File file = Resources.getResourceFile("elements/Source.java");
		byte[] arrayBytes = new byte[1];
		ArchiveEntryDocument archiveEntryDocument = new ArchiveEntryDocument(
				file, arrayBytes);
		assertNotNull(archiveEntryDocument.getMetaData());
	}

	/**
	 * Test get name.
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	@Test
	public void testGetName() throws FileNotFoundException {
		File file = Resources.getResourceFile("elements/Source.java");
		byte[] arrayBytes = new byte[1];
		ArchiveEntryDocument archiveEntryDocument = new ArchiveEntryDocument(
				file, arrayBytes);
		Assert.assertEquals("src/test/resources/elements/Source.java",
				archiveEntryDocument.getName());
	}

	/**
	 * Test input stream.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testInputStream() throws IOException {
		File file = Resources.getResourceFile("elements/Source.java");
		byte[] arrayBytes = new byte[1];
		ArchiveEntryDocument archiveEntryDocument = new ArchiveEntryDocument(
				file, arrayBytes);
		assertNotNull(archiveEntryDocument.inputStream());
	}

	/**
	 * Test is composite.
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	@Test
	public void testIsComposite() throws FileNotFoundException {
		File file = Resources.getResourceFile("elements/Source.java");
		byte[] arrayBytes = new byte[1];
		ArchiveEntryDocument archiveEntryDocument = new ArchiveEntryDocument(
				file, arrayBytes);
		Assert.assertTrue(archiveEntryDocument.isComposite());
	}

	/**
	 * Test reader.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testReader() throws IOException {
		File file = Resources.getResourceFile("elements/Source.java");
		byte[] arrayBytes = new byte[1];
		ArchiveEntryDocument archiveEntryDocument = new ArchiveEntryDocument(
				file, arrayBytes);
		assertNotNull(archiveEntryDocument.reader());
	}

	/**
	 * Test to string.
	 * 
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	@Test
	public void testToString() throws FileNotFoundException {
		File file = Resources.getResourceFile("elements/Source.java");
		byte[] arrayBytes = new byte[1];
		ArchiveEntryDocument archiveEntryDocument = new ArchiveEntryDocument(
				file, arrayBytes);
		assertNotNull(archiveEntryDocument.toString());
		Assert.assertEquals("TarEntryDocument ( " + "name = "
				+ archiveEntryDocument.getName() + " "
 + "metaData = "
				+ archiveEntryDocument.getMetaData() + " " + " )",
				archiveEntryDocument.toString());
	}
}
