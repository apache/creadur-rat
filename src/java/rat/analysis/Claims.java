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
package rat.analysis;

import rat.report.RatReportFailedException;
import rat.report.claim.IClaimReporter;

public class Claims {

    public static final String LICENSE_APPROVAL_PREDICATE = "license-approval";
    public static final String LICENSE_FAMILY_PREDICATE = "license-family";
    public static final String HEADER_SAMPLE_PREDICATE = "header-sample";
    public static final String HEADER_TYPE_PREDICATE = "header-type";
    
    public static void reportHeaderSampleClaim(final String sample, final String subject, IClaimReporter reporter) throws RatReportFailedException {
        reporter.claim(subject, HEADER_SAMPLE_PREDICATE, sample, true);
    }
    
    public static void reportGeneratedHeaderTypeClaim(final String subject, IClaimReporter reporter) throws RatReportFailedException {
        reportHeaderTypeClaim("GEN  ", subject, reporter);
    }
    
    public static void reportHeaderTypeClaim(final String type, final String subject, IClaimReporter reporter) throws RatReportFailedException {
        reporter.claim(subject, HEADER_TYPE_PREDICATE, type, false);
    }
    
    public static void reportGeneratedClaims(final String subject, final String notes, final IClaimReporter reporter) throws RatReportFailedException {
        Claims.reportHeaderSampleClaim(notes, subject, reporter);
        Claims.reportGeneratedHeaderTypeClaim(subject, reporter);
    }
    
    public static void reportStandardClaims(final String subject, final String notes, final String code, final String name, final IClaimReporter reporter) throws RatReportFailedException {
        Claims.reportHeaderTypeClaim(code, subject, reporter);
        Claims.reportHeaderSampleClaim(notes, subject, reporter);

        reporter.claim(subject, LICENSE_FAMILY_PREDICATE, name, false);
    }
    
    public static void reportLicenseApprovalClaim(final CharSequence subject, final boolean isAcceptable, final IClaimReporter reporter) throws RatReportFailedException {
        // TODO: replace with more finely grained system
        final String approvalValue = Boolean.toString(isAcceptable);
        // TODO: not very readable; 
        // TODO: replace when license approval factored into separate phase
        // TODO: probably name='ASF'
        reporter.claim(subject, LICENSE_APPROVAL_PREDICATE, approvalValue, false);
    }

    public static final String ASL_CODE = "AL   ";
    public static final String OASIS_CODE = "OASIS";
    public static final String W3CD_CODE = "W3CD ";
    public static final String W3C_CODE = "W3C  ";
    public static final String DOJO = "DOJO ";
    public static final String TMF854 = "TMF  ";
}
