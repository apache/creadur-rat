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
package org.apache.rat.analysis;

import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.BaseObject;
import org.apache.rat.report.claim.BasePredicate;
import org.apache.rat.report.claim.HeaderTypeObject;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.IPredicate;
import org.apache.rat.report.claim.ISubject;
import org.apache.rat.report.claim.LicenseApprovalObject;
import org.apache.rat.report.claim.LicenseFamilyCode;
import org.apache.rat.report.claim.LicenseFamilyName;

public class Claims {
    public static final IPredicate TYPE_PREDICATE = new BasePredicate("type");
    public static final IPredicate ARCHIVE_TYPE_PREDICATE = new BasePredicate("archive-type");
    public static final IPredicate LICENSE_APPROVAL_PREDICATE = new BasePredicate("license-approval");
    public static final IPredicate LICENSE_FAMILY_PREDICATE = new BasePredicate("license-family");
    public static final IPredicate HEADER_SAMPLE_PREDICATE = new BasePredicate("header-sample");
    public static final IPredicate HEADER_TYPE_PREDICATE = new BasePredicate("header-type");
    
    public static void reportHeaderSampleClaim(final String sample, final ISubject subject, IClaimReporter reporter) throws RatReportFailedException {
        reporter.claim(subject, HEADER_SAMPLE_PREDICATE, new BaseObject(sample), true);
    }
    
    public static void reportGeneratedHeaderTypeClaim(final ISubject subject, IClaimReporter reporter) throws RatReportFailedException {
        reportHeaderTypeClaim(HeaderTypeObject.GENERATED, subject, reporter);
    }
    
    public static void reportHeaderTypeClaim(final HeaderTypeObject type, final ISubject subject, IClaimReporter reporter) throws RatReportFailedException {
        reporter.claim(subject, HEADER_TYPE_PREDICATE, type, false);
    }
    
    public static void reportGeneratedClaims(final ISubject subject, final String notes, final IClaimReporter reporter) throws RatReportFailedException {
        Claims.reportHeaderSampleClaim(notes, subject, reporter);
        Claims.reportGeneratedHeaderTypeClaim(subject, reporter);
    }
    
    public static void reportStandardClaims(final ISubject subject, final String notes, final HeaderTypeObject code, final LicenseFamilyName name, final IClaimReporter reporter) throws RatReportFailedException {
        Claims.reportHeaderTypeClaim(code, subject, reporter);
        Claims.reportHeaderSampleClaim(notes, subject, reporter);
        reporter.claim(subject, LICENSE_FAMILY_PREDICATE, name, false);
    }
    
    public static void reportLicenseApprovalClaim(final ISubject subject, final boolean isAcceptable, final IClaimReporter reporter) throws RatReportFailedException {
        final LicenseApprovalObject object = isAcceptable ? LicenseApprovalObject.TRUE : LicenseApprovalObject.FALSE;
        reporter.claim(subject, LICENSE_APPROVAL_PREDICATE, object, false);
    }

    public static final LicenseFamilyCode ASL_CODE = new LicenseFamilyCode("AL   ");
    public static final LicenseFamilyCode OASIS_CODE = new LicenseFamilyCode("OASIS");
    public static final LicenseFamilyCode W3CD_CODE = new LicenseFamilyCode("W3CD ");
    public static final LicenseFamilyCode W3C_CODE = new LicenseFamilyCode("W3C  ");
    public static final LicenseFamilyCode DOJO = new LicenseFamilyCode("DOJO ");
    public static final LicenseFamilyCode TMF854 = new LicenseFamilyCode("TMF  ");
}
