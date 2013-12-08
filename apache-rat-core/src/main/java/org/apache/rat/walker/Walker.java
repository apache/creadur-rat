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

package org.apache.rat.walker;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import org.apache.rat.report.IReportable;

/**
 * Abstract walker.
 */
public abstract class Walker implements IReportable {

	/** The file. */
	protected final File file;

	/** The name. */
	protected final String name;

	/** The filter. */
	protected final FilenameFilter filter;

	/**
	 * Regex filter.
	 * 
	 * @param pattern
	 *            the pattern
	 * @return the filename filter
	 */
	protected static FilenameFilter regexFilter(final Pattern pattern) {
		return new FilenameFilter() {
			public boolean accept(File dir, String name) {
				final boolean result;
				if (pattern == null) {
					result = true;
				} else {
					result = !pattern.matcher(name).matches();
				}
				return result;
			}
		};
	}

	/**
	 * Checks if is restricted.
	 * 
	 * @param file
	 *            the file
	 * @return true, if is restricted
	 */
	protected boolean isRestricted(File file) {
		String name = file.getName();
		boolean result = name.startsWith(".");
		return result;
	}

	/**
	 * Ignored.
	 * 
	 * @param file
	 *            the file
	 * @return true, if successful
	 */
	protected final boolean ignored(final File file) {
		boolean result = false;
		if (filter != null) {
			final String name = file.getName();
			final File dir = file.getParentFile();
			result = !filter.accept(dir, name);
		}
		return result;
	}

	/**
	 * Instantiates a new walker.
	 * 
	 * @param file
	 *            the file
	 * @param filter
	 *            the filter
	 */
	public Walker(File file, final FilenameFilter filter) {
		this(file.getPath(), file, filter);
	}

	/**
	 * Instantiates a new walker.
	 * 
	 * @param name
	 *            the name
	 * @param file
	 *            the file
	 * @param filter
	 *            the filter
	 */
	protected Walker(final String name, final File file,
			final FilenameFilter filter) {
		this.name = name;
		this.file = file;
		this.filter = filter;
	}

}
