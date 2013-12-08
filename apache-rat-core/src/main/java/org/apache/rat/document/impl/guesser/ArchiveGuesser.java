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

import java.util.Locale;

import org.apache.rat.api.Document;

/**
 * The Class ArchiveGuesser.
 */
public class ArchiveGuesser {

	/** The Constant ARCHIVE_EXTENSIONS. */
	private static final String[] ARCHIVE_EXTENSIONS = { "jar", "gz", "zip",
			"tar", "bz", "bz2", "rar", "war", };

	/** The archive extensions. */
	private final String[] archiveExtensions;

	/**
	 * Instantiates a new archive guesser.
	 */
	public ArchiveGuesser() {
		this(ARCHIVE_EXTENSIONS);
	}

	/**
	 * Instantiates a new archive guesser.
	 * 
	 * @param archiveExtensions
	 *            the archive extensions
	 */
	public ArchiveGuesser(final String[] archiveExtensions) {
		super();
		this.archiveExtensions = archiveExtensions;
	}

	/**
	 * Matches.
	 * 
	 * @param subject
	 *            the subject
	 * @return true, if successful
	 */
	public boolean matches(final Document subject) {
		return isArchive(subject.getName());
	}

	/**
	 * Is a file by that name an archive?.
	 * 
	 * @param name
	 *            the name
	 * @return true, if is archive
	 */
	public boolean isArchive(final String name) {
		if (name == null) {
			return false;
		}
		final String nameToLower = name.toLowerCase(Locale.US);
		for (final String element : this.archiveExtensions) {
			if (nameToLower.endsWith("." + element)) {
				return true;
			}
		}
		return false;
	}
}
