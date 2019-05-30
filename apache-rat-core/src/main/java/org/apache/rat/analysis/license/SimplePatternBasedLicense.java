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

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData.Datum;


/**
 * @since Rat 0.8
 */
public class SimplePatternBasedLicense extends BaseLicense implements IHeaderMatcher {
    private String[] patterns;

    public SimplePatternBasedLicense() {
    }

    protected SimplePatternBasedLicense(Datum pLicenseFamilyCategory, Datum pLicenseFamilyName,
            String pNotes, String[] pPatterns) {
        super(pLicenseFamilyCategory, pLicenseFamilyName, pNotes);
        setPatterns(pPatterns);
    }
    
    public String[] getPatterns() {
        return patterns;
    }

    public void setPatterns(String[] pPatterns) {
        patterns = pPatterns;
    }

    protected boolean matches(String pLine) {
        if (pLine != null) {
            final String[] pttrns = getPatterns();
            if (pttrns != null) {
                for (String pttrn : pttrns) {
                    if (pLine.contains(pttrn)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void reset() {
        // Nothing to do
    }

    public boolean match(Document pSubject, String pLine) throws RatHeaderAnalysisException {
        final boolean result = matches(pLine);
        if (result) {
            reportOnLicense(pSubject);
        }
        return result;
    }
}
