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
package org.apache.rat;

import java.io.InputStream;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.generation.GeneratedLicenseNotRequired;
import org.apache.rat.analysis.generation.JavaDocLicenseNotRequired;
import org.apache.rat.analysis.license.ApacheSoftwareLicense20;
import org.apache.rat.analysis.license.CDDL1License;
import org.apache.rat.analysis.license.DojoLicenseHeader;
import org.apache.rat.analysis.license.GPL1License;
import org.apache.rat.analysis.license.GPL2License;
import org.apache.rat.analysis.license.GPL3License;
import org.apache.rat.analysis.license.MITLicense;
import org.apache.rat.analysis.license.OASISLicense;
import org.apache.rat.analysis.license.TMF854LicenseHeader;
import org.apache.rat.analysis.license.W3CDocLicense;
import org.apache.rat.analysis.license.W3CLicense;
import org.apache.rat.analysis.util.HeaderMatcherMultiplexer;

/**
 * Utility class that holds constants shared by the CLI tool and the Ant tasks.
 */
public class Defaults {

	/**
	 * The standard list of licenses to include in the reports.
	 */
	public static final IHeaderMatcher[] DEFAULT_MATCHERS = new IHeaderMatcher[] {
			new ApacheSoftwareLicense20(), new GPL1License(),
			new GPL2License(), new GPL3License(), new MITLicense(),
			new W3CLicense(), new W3CDocLicense(), new OASISLicense(),
			new JavaDocLicenseNotRequired(), new GeneratedLicenseNotRequired(),
			new DojoLicenseHeader(), new TMF854LicenseHeader(),
			new CDDL1License(), };

	/** The Constant PLAIN_STYLESHEET. */
	public static final String PLAIN_STYLESHEET = "org/apache/rat/plain-rat.xsl";

	/**
	 * no instances.
	 */
	public Defaults() {
		super();
	}

	/**
	 * Gets the plain style sheet.
	 * 
	 * @return the plain style sheet
	 */
	public static InputStream getPlainStyleSheet() {
		InputStream result = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(Defaults.PLAIN_STYLESHEET);
		return result;
	}

	/**
	 * Gets the default style sheet.
	 * 
	 * @return the default style sheet
	 */
	public static InputStream getDefaultStyleSheet() {
		return getPlainStyleSheet();
	}

	/**
	 * Creates the default matcher.
	 * 
	 * @return the i header matcher
	 */
	public static IHeaderMatcher createDefaultMatcher() {
		return new HeaderMatcherMultiplexer(Defaults.DEFAULT_MATCHERS);
	}
}
