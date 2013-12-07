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

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

/**
 * Looks for documents contain the OASIS copyright claim plus derivative work clause.
 * Perhaps need to match more.
 */
public class OASISLicense extends FullTextMatchingLicense {

	/** The Constant COPYRIGHT_PATTERN_DEFN. */
    private static final String COPYRIGHT_PATTERN_DEFN = ".*Copyright\\s.*OASIS Open.*";

	/** The Constant CLAUSE_DEFN. */
    private static final String CLAUSE_DEFN
    = "This document and translations of it may be copied and furnished to others and derivative works" +
            "that comment on or otherwise explain it or assist in its implementation may be prepared" +
            "copied published and distributed";
    
	/** The Constant COPYRIGHT_PATTERN. */
    private static final Pattern COPYRIGHT_PATTERN = Pattern.compile(COPYRIGHT_PATTERN_DEFN);

	/** The copyright match. */
    private boolean copyrightMatch;
    
	/**
	 * Instantiates a new oASIS license.
	 */
    public OASISLicense() {
        super(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_OASIS,
              MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_OASIS_OPEN_LICENSE,
              "No modifications allowed",
              CLAUSE_DEFN);
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.rat.analysis.license.FullTextMatchingLicense#match(org.apache
	 * .rat.api.Document, java.lang.String)
	 */
    @Override
    public boolean match(final Document subject, final String line) {
        boolean result = false;
        if (copyrightMatch) {
            result = super.match(subject, line);
        } else {
            copyrightMatch = COPYRIGHT_PATTERN.matcher(line).matches();
        }
        return result;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.analysis.license.FullTextMatchingLicense#reset()
	 */
    @Override
    public void reset() {
        copyrightMatch = false;
        super.reset();
    }
}
