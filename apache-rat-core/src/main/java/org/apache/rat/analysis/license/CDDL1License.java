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

import org.apache.commons.lang.ArrayUtils;
import org.apache.rat.api.MetaData;

/**
 * Base CDDL 1.0 license.
 */
public class CDDL1License extends SimplePatternBasedLicense {

	/** The Constant LICENSE_LINE. */
    public static final String LICENSE_LINE =
            "The contents of this file are subject to the terms of the Common Development[\\\\r\\\\n\\\\s]+"
            + "and Distribution License(\"CDDL\") (the \"License\"). You may not use this file[\\\\r\\\\n\\\\s]+"
            + "except in compliance with the License.";

	/** The Constant LICENSE_URL. */
    public static final String LICENSE_URL =
            ".*https://oss.oracle.com/licenses/CDDL.*";

	/**
	 * Instantiates a new cDD l1 license.
	 */
    public CDDL1License() {
        super(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_CDLL1,
                MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_CDDL1,
                "", new String[]{LICENSE_LINE, LICENSE_URL});
    }

	/**
	 * Gets the reg ex patterns.
	 * 
	 * @return the reg ex patterns
	 */
    private Pattern[] getRegExPatterns() {
        final Pattern[] result;
        final String[] pttrns = getPatterns();
        if (ArrayUtils.isEmpty(pttrns)) {
            result = new Pattern[0];
        } else {
            result = new Pattern[pttrns.length];
            for (int i = 0; i < pttrns.length; i++) {
                result[i] = Pattern.compile(pttrns[i]);
            }
        }

        return result;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.rat.analysis.license.SimplePatternBasedLicense#matches(java
	 * .lang.String)
	 */
    @Override
    protected boolean matches(final String pLine) {
        if (pLine != null) {
            final String[] pttrns = getPatterns();
            if (pttrns != null) {
                for (Pattern pttrn : getRegExPatterns()) {
                    if (pttrn.matcher(pLine).find()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
