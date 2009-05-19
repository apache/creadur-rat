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
import org.apache.rat.document.IDocument;
import org.apache.rat.license.ModifiedBSDLicenseFamily;
import org.apache.rat.report.claim.IClaimReporter;


public class TMF854LicenseHeader extends BaseLicense implements IHeaderMatcher {
    
    private static final String COPYRIGHT_HEADER 
    = "TMF854 Version 1.0 - Copyright TeleManagement Forum";
    
    //  TMF854 Version 1.0 - Copyright TeleManagement Forum 

    public TMF854LicenseHeader() {
        super(Claims.TMF854, ModifiedBSDLicenseFamily.MODIFIED_BSD_LICENSE_NAME, "BSD");
    }


    public void reset() {

    }

    public boolean match(IDocument subject, String line, IClaimReporter reporter) throws RatHeaderAnalysisException {
        final boolean result = matches(line);
        if (result) {
            reportOnLicense(subject, reporter);
        }
        return result;
    }

    boolean matches(String line) {
        boolean result = (line != null && line.indexOf(COPYRIGHT_HEADER) != -1);
        return result;
    }
    
}
