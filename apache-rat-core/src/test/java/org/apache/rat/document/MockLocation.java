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
 *
 */
public class MockLocation implements Document {

	public String name;
	public String url;
	private final MetaData metaData = new MetaData();

	/**
	 * 
	 */
	public MockLocation() {
		this("name", "url");
	}

	/**
	 * 
	 * @param name
	 */
	public MockLocation(final String name) {
		this(name, "url");
	}

	/**
	 * 
	 * @param name
	 * @param url
	 */
	public MockLocation(final String name, final String url) {
		super();
		this.name = name;
		this.url = url;
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return
	 */
	public String getURL() {
		return url;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isComposite() {
		return false;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public Reader reader() throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @return
	 */
	public MetaData getMetaData() {
		return metaData;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public InputStream inputStream() throws IOException {
		throw new UnsupportedOperationException();
	}
}
