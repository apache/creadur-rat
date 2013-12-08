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


	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		capacity = 50;
		SimpleCharFilter filter = new SimpleCharFilter();
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
	public void testNoFiltering() throws IOException {
		String input = "Whatever";
		StringReader reader = new StringReader(input);
		CharSequence result = factory.filter(reader);
		String output = result.toString();
		assertEquals("No filtering so input equals output.", input, output);
	}

	/**
	 * Filtering.
	 * 
	 * @throws IOException
	 * 
	 */
	@Test
	public void testFiltering() throws IOException {
		String input = "Whatever";
		StringReader reader = new StringReader(input);
		CharSequence result = factory.filter(reader);
		String output = result.toString();
		assertEquals("No filtering so input equals output.", input, output);
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
	public void testOverCapacity() throws IOException {
		String input = "WhateverWhateverWhateverWhateverWhateverWhateverWhateverWhateverWhateverWhatever";
		StringReader reader = new StringReader(input);
		CharSequence result = factory.filter(reader);
		String output = new StringBuffer().append(result).toString();
		assertEquals("No filtering so input equals output.",
				input.substring(0, capacity), output);
	}
}
