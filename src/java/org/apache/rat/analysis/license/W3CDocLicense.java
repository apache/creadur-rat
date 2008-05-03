/*
 * Copyright 2006 Guillaume Nodet
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
package org.apache.rat.analysis.license;

import org.apache.rat.analysis.Claims;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.license.W3CDocumentLicenseFamily;
import org.apache.rat.report.claim.IClaimReporter;

public class W3CDocLicense extends BaseLicense implements IHeaderMatcher {

    private static final String NOTES 
        = "Note that W3CD does not allow modifications. See http://www.w3.org/Consortium/Legal/2002/copyright-documents-20021231.";
    private static final String COPYRIGHT_URL 
    = "http://www.w3.org/Consortium/Legal/2002/copyright-documents-20021231";
    
    public W3CDocLicense() {
        super(Claims.W3CD_CODE, W3CDocumentLicenseFamily.W3C_DOCUMENT_COPYRIGHT_NAME,  NOTES);
        
    }

    public boolean match(String subject, String line, IClaimReporter reporter) throws RatHeaderAnalysisException {
        boolean result = line != null && line.indexOf(COPYRIGHT_URL) != -1;
        if (result) {
            reportOnLicense(subject, reporter);
        }
        return result;
    }

    public void reset() {
        // Matcher is not stateful 
    }

}
