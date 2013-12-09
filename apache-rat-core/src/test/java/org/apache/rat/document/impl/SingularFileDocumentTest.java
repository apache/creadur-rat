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
package org.apache.rat.document.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;

import org.apache.rat.api.Document;
import org.apache.rat.test.utils.Resources;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class SingularFileDocumentTest.
 */
public class SingularFileDocumentTest {

	/** The document. */
	private Document document;

	/** The file. */
	private File file;

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		file = Resources.getResourceFile("elements/Source.java");
		document = new MonolithicFileDocument(file);
	}

	/**
	 * Reader.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void reader() throws Exception {
		Reader reader = document.reader();
		assertNotNull("Reader should be returned", reader);
		assertEquals("First file line expected", "package elements;",
				new BufferedReader(reader).readLine());
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	@Test
	public void getName() {
		final String name = document.getName();
		assertNotNull("Name is set", name);
		assertEquals("Name is filename", DocumentImplUtils.toName(file), name);
	}
}
