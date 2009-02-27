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
package org.apache.rat.policy;

import java.util.Arrays;

import org.apache.rat.analysis.Claims;
import org.apache.rat.license.Apache20LicenseFamily;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.ModifiedBSDLicenseFamily;
import org.apache.rat.license.OASISLicenseFamily;
import org.apache.rat.license.W3CDocumentLicenseFamily;
import org.apache.rat.license.W3CSoftwareLicenseFamily;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.IClaimReporter;

public class DefaultPolicy implements IClaimReporter {

    private static final String[] APPROVED_LICENSES = {
        Apache20LicenseFamily.APACHE_SOFTWARE_LICENSE_NAME, OASISLicenseFamily.OASIS_OPEN_LICENSE_NAME, 
        W3CSoftwareLicenseFamily.W3C_SOFTWARE_COPYRIGHT_NAME, W3CDocumentLicenseFamily.W3C_DOCUMENT_COPYRIGHT_NAME,
        ModifiedBSDLicenseFamily.MODIFIED_BSD_LICENSE_NAME
    };
    
    private static final String[] toNames(final ILicenseFamily[] approvedLicenses) {
        String[] results = null;
        if (approvedLicenses != null) {
            final int length = approvedLicenses.length;
            results = new String[length];
            for (int i=0;i<length;i++) {
                results[i] = approvedLicenses[i].getFamilyName();
            }
        }
        return results;
    }
    
    private final IClaimReporter reporter;
    private final String[] approvedLicenseNames;
    
    public DefaultPolicy(final IClaimReporter reporter) {
        this(reporter, APPROVED_LICENSES);
    }
    
    public DefaultPolicy(final IClaimReporter reporter, final ILicenseFamily[] approvedLicenses) {
        this(reporter, toNames(approvedLicenses));
    }
    
    public DefaultPolicy(final IClaimReporter reporter, final String[] approvedLicenseNames) {
        super();
        this.reporter = reporter;
        if (approvedLicenseNames == null) {
            this.approvedLicenseNames = APPROVED_LICENSES;
        } else {
            final int length = approvedLicenseNames.length;
            this.approvedLicenseNames = new String[length];
            System.arraycopy(approvedLicenseNames, 0, this.approvedLicenseNames, 0, length);
        }
        Arrays.sort(this.approvedLicenseNames);
    }

    public void claim(CharSequence subject, CharSequence predicate,
            CharSequence object, boolean isLiteral)
            throws RatReportFailedException {
        if (Claims.LICENSE_FAMILY_PREDICATE.equals(predicate)) {
            final boolean isApproved = Arrays.binarySearch(approvedLicenseNames, object) >= 0;
            Claims.reportLicenseApprovalClaim(subject, isApproved, reporter);
        }
    }

}
