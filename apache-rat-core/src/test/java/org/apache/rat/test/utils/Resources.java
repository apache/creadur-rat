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
package org.apache.rat.test.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.rat.document.impl.DocumentImplUtils;

/**
 * Utility class, which provides static methods for creating test cases.
 */
public final class Resources {
	/**
	 * Private constructor, to prevent accidental instantiation.
	 */
	private Resources() {
		// Does nothing
	}

	/**
	 * Locates a resource file in the class path.
	 * 
	 * @param pResource
	 * @return
	 * @throws FileNotFoundException
	 */
	public static File getResourceFile(final String pResource)
			throws FileNotFoundException {
		final File file = new File("src/test/resources", pResource);
		if (!file.isFile()) {
			throw new FileNotFoundException("Unable to locate resource file: "
					+ pResource);
		}
		return file;
	}

	/**
	 * Locates a set of resource files in the class path.
	 * 
	 * @param pResource
	 * @return
	 * @throws FileNotFoundException
	 */
	public static File[] getResourceFiles(final String pResource)
			throws FileNotFoundException {
		final File file = new File("src/test/resources", pResource);
		if (!file.isDirectory()) {
			throw new FileNotFoundException(
					"Unable to locate resource directory: " + file);
		}
		return file.listFiles(new FileFilter() {
			public boolean accept(final File pathname) {
				return pathname.isFile();
			}
		});
	}

	/**
	 * Locates a resource file in the class path and returns an
	 * {@link InputStream}.
	 * 
	 * @param pResource
	 * @return
	 * @throws IOException
	 */
	public static InputStream getResourceStream(final String pResource)
			throws IOException {
		return new FileInputStream(getResourceFile(pResource));
	}

	/**
	 * Locates a resource file in the class path and returns a {@link Reader}.
	 * 
	 * @param pResource
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static Reader getResourceReader(final String pResource)
			throws UnsupportedEncodingException, IOException {
		return new InputStreamReader(getResourceStream(pResource), "UTF-8");
	}

	/**
	 * Locates a resource file in the class path and returns a
	 * {@link BufferedReader}.
	 * 
	 * @param pResource
	 * @return
	 * @throws IOException
	 */
	public static BufferedReader getBufferedResourceReader(
			final String pResource) throws IOException {
		return new BufferedReader(getResourceReader(pResource));
	}

	/**
	 * Locates a resource file in the class path and returns a
	 * {@link BufferedReader}.
	 * 
	 * @param file
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	public static BufferedReader getBufferedReader(final File file)
			throws UnsupportedEncodingException, FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(
				file), "UTF-8"));
	}

	/**
	 * Locates the name of a directory, which contains the given resource file.
	 * 
	 * @param pResource
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String getResourceDirectory(final String pResource)
			throws FileNotFoundException {
		final File resource = getResourceFile(pResource);
		final File dir = resource.getParentFile();
		return DocumentImplUtils.toName(dir);
	}
}
