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

import rat.analysis.IHeaderMatcher;
import rat.analysis.generation.GeneratedLicenseNotRequired;
import rat.analysis.generation.JavaDocLicenseNotRequired;
import rat.analysis.license.ApacheSoftwareLicense20;
import rat.analysis.license.DojoLicenseHeader;
import rat.analysis.license.OASISLicense;
import rat.analysis.license.TMF854LicenseHeader;
import rat.analysis.license.W3CDocLicense;
import rat.analysis.license.W3CLicense;
import rat.analysis.util.HeaderMatcherMultiplexer;



/**
 * Utility class that holds constants shared by the CLI tool and the Ant tasks.
 */
public class Defaults {
    /** no instances */
    private Defaults() {}

    /**
     * The standard liest of licenses to include in the reports.
     */
    public static final IHeaderMatcher[] DEFAULT_MATCHERS =
        new IHeaderMatcher[] {
            new ApacheSoftwareLicense20(),
            new W3CLicense(), 
            new W3CDocLicense(), 
            new OASISLicense(),
            new JavaDocLicenseNotRequired(), 
            new GeneratedLicenseNotRequired(),
            new DojoLicenseHeader(),
            new TMF854LicenseHeader()
    };
    
    public static final String PLAIN_STYLESHEET = "rat/plain-rat.xsl";
    
    public static final InputStream getPlainStyleSheet() {
        InputStream result = Defaults.class.getClassLoader().getResourceAsStream(Defaults.PLAIN_STYLESHEET);
        return result;
    }
    
    public static final InputStream getDefaultStyleSheet() {
        InputStream result = getPlainStyleSheet();
        return result;
    }
    
    public static final IHeaderMatcher createDefaultMatcher() {
        return new HeaderMatcherMultiplexer(Defaults.DEFAULT_MATCHERS);
    }
}
