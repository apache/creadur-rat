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

import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.document.IResource;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.LicenseFamilyCode;
import org.apache.rat.report.claim.LicenseFamilyName;
import org.apache.rat.report.claim.impl.LicenseFamilyClaim;

public class BaseLicense {
	private final LicenseFamilyCode code;
	private final LicenseFamilyName name;
	private final String notes;
	
	public BaseLicense(final LicenseFamilyCode code, final LicenseFamilyName name, final String notes)
	{
		this.code = code;
		this.name = name;
		this.notes = notes;
	}
    
    public final void reportOnLicense(IResource subject, IClaimReporter reporter) throws RatHeaderAnalysisException {
        final LicenseFamilyName name = getName();
        final LicenseFamilyCode code = getCode();
        final String notes = getNotes();
        try {
            reporter.claim(new LicenseFamilyClaim(subject, name, code, notes));
        } catch (RatReportFailedException e) {
            // Cannot recover
            throw new RatHeaderAnalysisException("Cannot report on license information", e);
        }
    }

    public LicenseFamilyCode getCode() {
		return code;
	}

	public LicenseFamilyName getName() {
		return name;
	}

	public String getNotes() {
		return notes;
	}
}
