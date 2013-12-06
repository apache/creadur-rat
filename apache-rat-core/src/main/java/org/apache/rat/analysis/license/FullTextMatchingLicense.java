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
package org.apache.rat.analysis.license;

import java.util.Locale;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData.Datum;

/**
 * Accumulates all letters and numbers contained inside the header and compares
 * it to the full text of a given license (after reducing it to letters and
 * numbers as well).
 * 
 * <p>
 * The text comparison is case insensitive but assumes only characters in the
 * US-ASCII charset are being matched.
 * </p>
 * 
 * @since Rat 0.9
 */
public class FullTextMatchingLicense extends BaseLicense implements
		IHeaderMatcher {

	/** The Constant DEFAULT_INITIAL_LINE_LENGTH. */
	// Number of match characters assumed to be present on first line
	private static final int DEFAULT_INITIAL_LINE_LENGTH = 20;
	
	private static final int ZERO = 0;

	/** The full text. */
	private String fullText;

	/** The first line. */
	private String firstLine;

	/** The seen first line. */
	private boolean seenFirstLine;

	/** The buffer. */
	private final StringBuilder buffer = new StringBuilder();

	/**
	 * Instantiates a new full text matching license.
	 *
	 * @param licenseFamilyCategory the license family category
	 * @param licenseFamilyName the license family name
	 * @param notes the notes
	 * @param fullText the full text
	 */
	protected FullTextMatchingLicense(final Datum licenseFamilyCategory,
			final Datum licenseFamilyName, final String notes, final String fullText) {
		super(licenseFamilyCategory, licenseFamilyName, notes);
		setFullText(fullText);
	}

	/**
	 * Sets the full text.
	 *
	 * @param text the new full text
	 */
	public final void setFullText(final String text) {
		int offset = text.indexOf('\n');
		if (offset == -1) {
			offset = Math.min(DEFAULT_INITIAL_LINE_LENGTH, text.length());
		}
		firstLine = prune(text.substring(0, offset))
				.toLowerCase(Locale.ENGLISH);
		fullText = prune(text).toLowerCase(Locale.ENGLISH);
		init();
	}

	/**
	 * Checks for full text.
	 *
	 * @return true, if successful
	 */
	public final boolean hasFullText() {
		return fullText != null;
	}

	/* (non-Javadoc)
	 * @see org.apache.rat.analysis.IHeaderMatcher#match(org.apache.rat.api.Document, java.lang.String)
	 */
	public boolean match(final Document subject, final String line) {
		boolean result = false;
		boolean follow = true;
		final String inputToMatch = prune(line).toLowerCase(Locale.ENGLISH);
		if (seenFirstLine) {
			// Accumulate more input
			buffer.append(inputToMatch);
		} else {
			int offset = inputToMatch.indexOf(firstLine);
			if (offset >= ZERO) {
				// we have a match, save the text starting with the match
				buffer.append(inputToMatch.substring(offset));
				seenFirstLine = true;
				// Drop out to check whether full text is matched
			} else {
				// we assume that the first line must appear in a single line no
				// more to do here
				result = false; 
				follow = false;
			}
		}
		if (buffer.length() >= fullText.length() && follow) {
			// we have enough data to match
			if (buffer.toString().contains(fullText)) {
				reportOnLicense(subject);
				// we found a match
				result = true;
			} else {
				// buffer contains first line but does not contain full text
				// It's possible that the buffer contains the first line again
				int offset = buffer.substring(1).toString().indexOf(firstLine);
				if (offset >= ZERO) {
					// first line found again
					buffer.delete(0, offset); // reset buffer to the new start
				} else {
					// buffer does not even contain first line, so cannot
					// be used to match full text
					init();
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.apache.rat.analysis.IHeaderMatcher#reset()
	 */
	public void reset() {
		init();
	}

	/**
	 * This is called indirectly from a ctor so must be final or private
	 */
	private void init() {
		buffer.setLength(0);
		seenFirstLine = false;
	}
}