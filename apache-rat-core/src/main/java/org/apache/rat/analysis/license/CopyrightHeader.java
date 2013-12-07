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

import java.util.regex.Pattern;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData.Datum;
import org.apache.rat.api.domain.LicenseFamily;

/**
 * Matches a typical Copyright header line only based on a regex pattern which
 * allows for one (starting) year or year range, and a configurable copyright
 * owner.
 * 
 * <p>
 * The matching is done case insensitive
 * </p>
 * 
 * <ul>
 * Example supported Copyright header lines, using copyright owner "FooBar"
 * <li>* Copyright 2010 FooBar. *</li>
 * <li>* Copyright 2010-2012 FooBar. *</li>
 * <li>*copyright 2012 foobar*</li>
 * </ul>
 * 
 * <p>
 * Note also that the copyright owner is appended to the regex pattern, so can
 * support additional regex but also requires escaping where needed,<br/>
 * e.g. use "FooBar \(www\.foobar\.com\)" for matching "FooBar (www.foobar.com)"
 * </p>
 * 
 * @since Rat 0.9
 */
public class CopyrightHeader extends BaseLicense implements IHeaderMatcher {

	/** The Constant COPYRIGHT_PREFIX_PATTERN_DEFN. */
	public static final String COPYRIGHT_PREFIX_PATTERN_DEFN = ".*Copyright [0-9]{4}(\\-[0-9]{4})? ";

	/** The copyright pattern. */
	private Pattern copyrightPattern;
	
	/** The copyright owner. */
	private String copyrightOwner;
	
	/** The copyright match. */
	private boolean copyrightMatch;
	
	/**
	 * Constructs a license indicated by the given patterns.
	 * 
	 * @param pLicenseFamily
	 *            not null
	 * @param copyrightOwner
	 *            not null
	 */
	protected CopyrightHeader(final LicenseFamily pLicenseFamily,
			final String copyrightOwner) {
		super(pLicenseFamily);
		setCopyrightOwner(copyrightOwner);
	}

	// Called by ctor, so must not be overridden
	/**
	 * Sets the copyright owner.
	 *
	 * @param copyrightOwner the new copyright owner
	 */
	public final void setCopyrightOwner(final String copyrightOwner) {
		this.copyrightOwner = copyrightOwner;
		this.copyrightPattern = Pattern.compile(COPYRIGHT_PREFIX_PATTERN_DEFN
				+ copyrightOwner + ".*", Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Gets the copy right owner.
	 *
	 * @return the copy right owner
	 */
	public String getCopyRightOwner() {
		return copyrightOwner;
	}

	/**
	 * Checks for copyright pattern.
	 *
	 * @return true, if successful
	 */
	public boolean hasCopyrightPattern() {
		return copyrightPattern != null;
	}

	/**
	 * Checks if is copyright match.
	 *
	 * @return true, if is copyright match
	 */
	protected boolean isCopyrightMatch() {
		return copyrightMatch;
	}

	/**
	 * Match copyright.
	 *
	 * @param s the s
	 * @return true, if successful
	 */
	protected boolean matchCopyright(final String text) {
		if (!copyrightMatch) {
			copyrightMatch = copyrightPattern.matcher(text).matches();
		}
		return copyrightMatch;
	}

	/* (non-Javadoc)
	 * @see org.apache.rat.analysis.IHeaderMatcher#match(org.apache.rat.api.Document, java.lang.String)
	 */
	public boolean match(final Document subject, final String text) {
		if (!copyrightMatch && matchCopyright(text)) {
			reportOnLicense(subject);
		}
		return copyrightMatch;
	}

	/* (non-Javadoc)
	 * @see org.apache.rat.analysis.IHeaderMatcher#reset()
	 */
	public void reset() {
		copyrightMatch = false;
	}
}
