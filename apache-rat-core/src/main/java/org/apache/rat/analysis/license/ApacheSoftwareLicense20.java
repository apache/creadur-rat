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

import org.apache.rat.analysis.Claims;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.document.IResource;
import org.apache.rat.license.Apache20LicenseFamily;
import org.apache.rat.report.claim.IClaimReporter;

/**
 * Matches Apache Software License, Version 2.0
 *
 */
public final class ApacheSoftwareLicense20 extends BaseLicense implements
		IHeaderMatcher {

	public static final String FIRST_LICENSE_LINE = "Licensed under the Apache License, Version 2.0 (the \"License\")";
	public static final String LICENSE_REFERENCE_LINE = "http://www.apache.org/licenses/LICENSE-2.0";
	
	public ApacheSoftwareLicense20() {
		super(Claims.ASL_CODE, Apache20LicenseFamily.APACHE_SOFTWARE_LICENSE_NAME, "");
	}
	
	public boolean match(IResource subject, String line, IClaimReporter reporter) throws RatHeaderAnalysisException {
        final boolean result = matches(line);
		if (result) {
			reportOnLicense(subject, reporter);
		}
        return result;
	}

	boolean matches(String line) {
		return line.indexOf(FIRST_LICENSE_LINE, 0) >= 0 
		|| line.indexOf(LICENSE_REFERENCE_LINE, 0) >= 0;
	}
	
	public void reset() {	
	}
}
