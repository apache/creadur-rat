/*
 * Copyright 2006 Robert Burrell Donkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package rat.analysis.license;

import rat.analysis.Claims;
import rat.analysis.RatHeaderAnalysisException;
import rat.report.RatReportFailedException;
import rat.report.claim.IClaimReporter;

public class BaseLicense {
	private final String code;
	private final String name;
	private final String notes;
	
	public BaseLicense(final String code, final String name, final String notes)
	{
		this.code = code;
		this.name = name;
		this.notes = notes;
	}
    
    public final void reportOnLicense(String subject, IClaimReporter reporter) throws RatHeaderAnalysisException {
        final String name = getName();
        final String code = getCode();
        final String notes = getNotes();
        try {
            Claims.reportStandardClaims(subject, notes, code, name, reporter);

        } catch (RatReportFailedException e) {
            // Cannot recover
            throw new RatHeaderAnalysisException("Cannot report on license information", e);
        }
    }

    public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getNotes() {
		return notes;
	}
}
