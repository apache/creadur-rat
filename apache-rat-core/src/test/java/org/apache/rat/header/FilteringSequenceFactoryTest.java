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
package org.apache.rat.header;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

/**
 * The Class FilteringSequenceFactoryTest.
 */
public class FilteringSequenceFactoryTest {

	/** The capacity. */
	private int capacity;

	/** The factory. */
	private FilteringSequenceFactory factory;

	/** The filter. */
	private SimpleCharFilter filter;

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		capacity = 50;
		filter = new SimpleCharFilter();
		factory = new FilteringSequenceFactory(capacity, filter);
	}

	/**
	 * No filtering.
	 * 
	 * @throws IOException
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void noFiltering() throws IOException {
		String input = "Whatever";
		StringReader reader = new StringReader(input);
		CharSequence result = factory.filter(reader);
		assertNotNull("Not Null Var", result);
		String output = result.toString();
		assertEquals("No filtering so input equals output.", input, output);
		reader = new StringReader(input);
		result = factory.filter(reader);
		assertNotNull("Not Null Var", result);
		output = result.toString();
		assertEquals(
				"No filtering so input equals output. Independent of previous input",
				input, output);
	}

	/**
	 * Filtering.
	 * 
	 * @throws IOException
	 * 
	 */
	@Test
	public void filtering() throws IOException {
		String input = "Whatever";
		StringReader reader = new StringReader(input);
		CharSequence result = factory.filter(reader);
		assertNotNull("Not null Var", result);
		String output = result.toString();
		assertEquals("No filtering so input equals output.", input, output);
		filter.filterOut = true;
		reader = new StringReader(input);
		result = factory.filter(reader);
		assertNotNull("Not Null Var", result);
		assertEquals(
				"All filtered output is empty. Independent of previous input",
				0, result.length());
	}

	/**
	 * Over capacity.
	 * 
	 * @throws IOException
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void overCapacity() throws IOException {
		String input = "WhateverWhateverWhateverWhateverWhateverWhateverWhateverWhateverWhateverWhatever";
		StringReader reader = new StringReader(input);
		CharSequence result = factory.filter(reader);
		assertNotNull("Nor Null Var", result);
		String output = new StringBuffer().append(result).toString();
		assertEquals("No filtering so input equals output.",
				input.substring(0, capacity), output);
	}
}
