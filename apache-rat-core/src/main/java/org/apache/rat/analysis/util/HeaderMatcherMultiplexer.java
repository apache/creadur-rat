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
package org.apache.rat.analysis.util;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.Document;

/**
 * Delegates to an ordered set of matchers.
 * 
 */
public final class HeaderMatcherMultiplexer implements IHeaderMatcher {

	/** The matchers. */
	private final IHeaderMatcher[] matchers;

	/**
	 * Instantiates a new header matcher multiplexer.
	 * 
	 * @param matchers
	 *            the matchers
	 */
	public HeaderMatcherMultiplexer(final IHeaderMatcher[] matchers) {
		this.matchers = matchers.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.rat.analysis.IHeaderMatcher#match(org.apache.rat.api.Document,
	 * java.lang.String)
	 */
	public boolean match(final Document subject, final String line) {
		boolean result = false;
		for (IHeaderMatcher matcher : matchers) {
			result = matcher.match(subject, line);
			if (result) {
				break;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.analysis.IHeaderMatcher#reset()
	 */
	public void reset() {
		for (IHeaderMatcher matcher : matchers) {
			matcher.reset();
		}
	}

}
