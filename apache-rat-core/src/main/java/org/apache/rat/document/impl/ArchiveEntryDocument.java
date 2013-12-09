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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

/**
 * The Class ArchiveEntryDocument.
 */
public class ArchiveEntryDocument implements Document {

	/** The contents. */
	final private byte[] contents;

	/** The name. */
	private final String name;

	/** The meta data. */
	private final MetaData metaData = new MetaData();

	/**
	 * Instantiates a new archive entry document.
	 * 
	 * @param file
	 *            the file
	 * @param contents
	 *            the contents
	 */
	public ArchiveEntryDocument(final File file, final byte... contents) {
		super();
		name = DocumentImplUtils.toName(file);
		this.contents = contents.clone();
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
	 * @see org.apache.rat.api.Document#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.api.Document#inputStream()
	 */
	public InputStream inputStream() throws IOException {
		return new ByteArrayInputStream(contents);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.api.Document#isComposite()
	 */
	public boolean isComposite() {
		return new DocumentImplUtils().isZipStream(new ByteArrayInputStream(
				contents));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.api.Document#reader()
	 */
	public Reader reader() throws IOException {
		return new InputStreamReader(new ByteArrayInputStream(contents));
	}

	/**
	 * Representations suitable for logging.
	 * 
	 * @return a <code>String</code> representation of this object.
	 */
	@Override
	public String toString() {
		return "TarEntryDocument ( " + "name = " + this.name + " "
				+ "metaData = " + this.metaData + " " + " )";
	}
}
