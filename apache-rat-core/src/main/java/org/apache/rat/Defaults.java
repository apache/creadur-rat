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
import org.apache.rat.api.MetaData;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Utility class that holds constants shared by the CLI tool and the Ant tasks.
 */
public class Defaults {
    /**
     * no instances
     */
    private Defaults() {
    }

    /**
     * The standard list of licenses to include in the reports.
     * Needs to match configuration in {@link org.apache.rat.policy.DefaultPolicy}.
     */
    public static final List<IHeaderMatcher> DEFAULT_MATCHERS = Collections.unmodifiableList(
            Arrays.asList(new ApacheSoftwareLicense20(),
                    new GPL1License(),
                    new GPL2License(),
                    new GPL3License(),
                    new MITLicense(),
                    new W3CLicense(),
                    new W3CDocLicense(),
                    new OASISLicense(),
                    new JavaDocLicenseNotRequired(), // does not have a MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_... entry
                    new GeneratedLicenseNotRequired(), // does not have a MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_... entry
                    new DojoLicenseHeader(),
                    new TMF854LicenseHeader(),
                    new CDDL1License()));

    // all classes in license package implementing ILicenseFamily
    public static final List<String> DEFAULT_LICENSE_FAMILIES = Collections.unmodifiableList(
            Arrays.asList(
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_ACADEMIC_FREE_LICENSE_VERSION_2_1, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_APACHE_LICENSE_VERSION_2_0, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_CDDL1, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_1, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_2, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_3, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_MIT, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_MODIFIED_BSD_LICENSE, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_OASIS_OPEN_LICENSE, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_DOCUMENT_COPYRIGHT, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_SOFTWARE_COPYRIGHT
                    //
            ));

    public static final String PLAIN_STYLESHEET = "org/apache/rat/plain-rat.xsl";
    public static final String UNAPPROVED_LICENSES_STYLESHEET = "org/apache/rat/unapproved-licenses.xsl";

    public static InputStream getPlainStyleSheet() {
        return Defaults.class.getClassLoader().getResourceAsStream(Defaults.PLAIN_STYLESHEET);
    }

    public static InputStream getUnapprovedLicensesStyleSheet() {
        return Defaults.class.getClassLoader().getResourceAsStream(Defaults.UNAPPROVED_LICENSES_STYLESHEET);
    }

    public static InputStream getDefaultStyleSheet() {
        return getPlainStyleSheet();
    }

    public static IHeaderMatcher createDefaultMatcher() {
        return new HeaderMatcherMultiplexer(Defaults.DEFAULT_MATCHERS);
    }
}
