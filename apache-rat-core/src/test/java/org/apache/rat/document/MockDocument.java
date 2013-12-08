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
package org.apache.rat.document;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

/**
 * The Class MockDocument.
 */
public class MockDocument implements Document {

	/** The reader. */
	public Reader reader;

	/** The name. */
	public String name;

	/** The meta data. */
	private final MetaData metaData = new MetaData();

	/**
	 * Instantiates a new mock document.
	 */
	public MockDocument() {
		this(null, "name");
	}

	/**
	 * Instantiates a new mock document.
	 * 
	 * @param name
	 *            the name
	 */
	public MockDocument(String name) {
		this(null, name);
	}

	/**
	 * Instantiates a new mock document.
	 * 
	 * @param reader
	 *            the reader
	 * @param name
	 *            the name
	 */
	public MockDocument(Reader reader, String name) {
		super();
		this.reader = reader;
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.api.Document#reader()
	 */
	public Reader reader() throws IOException {
		return reader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.api.Document#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.api.Document#isComposite()
	 */
	public boolean isComposite() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.api.Document#getMetaData()
	 */
	public MetaData getMetaData() {
		return metaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.api.Document#inputStream()
	 */
	public InputStream inputStream() throws IOException {
		throw new UnsupportedOperationException();
	}
}
