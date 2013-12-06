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

import static org.apache.rat.api.domain.RatLicenseFamily.APACHE;

import org.apache.rat.api.Document;

/**
 * Matches an applied AL 2.0 License header, including a <em>required</em>
 * initial copyright header line, conforming the <a
 * href="http://apache.org/licenses/LICENSE-2.0.html#apply">template</a> from
 * the AL 2.0 license itself.
 * 
 * @since Rat 0.9
 */
public class AppliedApacheSoftwareLicense20 extends CopyrightHeader {

	/** The Constant ASL20_LICENSE_DEFN. */
	public static final String ASL20_LICENSE_DEFN = "Licensed under the Apache License, Version 2.0 (the \"License\");\n"
			+ "you may not use this file except in compliance with the License.\n"
			+ "You may obtain a copy of the License at\n"
			+ "http://www.apache.org/licenses/LICENSE-2.0\n"
			+ "Unless required by applicable law or agreed to in writing, software\n"
			+ "distributed under the License is distributed on an \"AS IS\" BASIS,\n"
			+ "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
			+ "See the License for the specific language governing permissions and\n"
			+ "limitations under the License.\n";

	/** The text matcher. */
	private final FullTextMatchingLicense textMatcher;

	/**
	 * Instantiates a new applied apache software license20.
	 */
	public AppliedApacheSoftwareLicense20() {
		super(APACHE.licenseFamily(), "");
		textMatcher = new FullTextMatchingLicense(APACHE.licenseFamily(),
				ASL20_LICENSE_DEFN);
	}

	/**
	 * Instantiates a new applied apache software license20.
	 * 
	 * @param copyrightOwner
	 *            the copyright owner
	 */
	public AppliedApacheSoftwareLicense20(final String copyrightOwner) {
		this();
		setCopyrightOwner(copyrightOwner);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.rat.analysis.license.CopyrightHeader#match(org.apache.rat.
	 * api.Document, java.lang.String)
	 */
	@Override
	public boolean match(final Document subject, final String text) {
		boolean result = false;
		if (isCopyrightMatch()) {
			// will report the match if it has occurred
			result = textMatcher.match(subject, text);
		} else {
			matchCopyright(text);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.analysis.license.CopyrightHeader#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		textMatcher.reset();
	}
}
