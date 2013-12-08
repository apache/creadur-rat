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

import java.util.Arrays;
import java.util.List;

import org.apache.rat.api.Document;

/**
 * The Class NoteGuesser.
 */
public class NoteGuesser {

	/** The Constant NOTE_FILE_NAMES. */
	private static final String[] NOTE_FILE_NAMES = { "NOTICE", "LICENSE",
			"LICENSE.TXT", "NOTICE.TXT", "INSTALL", "INSTALL.TXT", "README",
			"README.TXT", "NEWS", "NEWS.TXT", "AUTHOR", "AUTHOR.TXT",
			"AUTHORS", "AUTHORS.txt", "CHANGELOG", "CHANGELOG.TXT",
			"DISCLAIMER", "DISCLAIMER.TXT", "KEYS", "KEYS.TXT",
			"RELEASE-NOTES", "RELEASE-NOTES.TXT", "RELEASE_NOTES",
			"RELEASE_NOTES.TXT", "UPGRADE", "UPGRADE.TXT", "STATUS",
			"STATUS.TXT", "THIRD_PARTY_NOTICES", "THIRD_PARTY_NOTICES.TXT",
			"COPYRIGHT", "COPYRIGHT.TXT", "BUILDING", "BUILDING.TXT", "BUILD",
			"BUILT.TXT", };

	/** The Constant NOTE_FILE_EXTENSIONS. */
	private static final String[] NOTE_FILE_EXTENSIONS = { "LICENSE",
			"LICENSE.TXT", "NOTICE", "NOTICE.TXT", };

	/** The note file names. */
	private final String[] noteFileNames;

	/** The note file extensions. */
	private final String[] noteFileExtensions;

	/**
	 * Instantiates a new note guesser.
	 */
	public NoteGuesser() {
		this(NOTE_FILE_NAMES, NOTE_FILE_EXTENSIONS);
	}

	/**
	 * Instantiates a new note guesser.
	 * 
	 * @param noteFileNames
	 *            the note file names
	 * @param noteFileExtensions
	 *            the note file extensions
	 */
	public NoteGuesser(final String[] noteFileNames,
			final String... noteFileExtensions) {
		super();
		this.noteFileNames = noteFileNames.clone();
		this.noteFileExtensions = noteFileExtensions;
	}

	/**
	 * Is a file by that name a note file?.
	 * 
	 * @param name
	 *            the name
	 * @return true, if is note
	 */
	private final boolean isNote(final String name) {
		boolean result = false;
		if (name != null) {
			final List<String> list = Arrays.asList(this.noteFileNames);
			final String normalisedName = new GuessUtils().normalise(name);
			if (list.contains(name) || list.contains(normalisedName)) {
				result = true;
			} else {
				for (String element : this.noteFileExtensions) {
					if (normalisedName.endsWith("." + element)) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Matches.
	 * 
	 * @param document
	 *            the document
	 * @return true, if successful
	 */
	public final boolean matches(final Document document) {
		return isNote(document.getName());
	}
}
