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
import org.apache.rat.api.MetaData;
import org.apache.rat.api.MetaData.Datum;


public class SimplePatternBasedLicense implements IHeaderMatcher {
    private String licenseFamilyCategory;
    private String licenseFamilyName;
    private String notes;
    private String[] patterns;

    public SimplePatternBasedLicense() {
    }

    protected SimplePatternBasedLicense(Datum pLicenseFamilyCategory, Datum pLicenseFamilyName,
            String pNotes, String[] pPatterns) {
        if (!MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY.equals(pLicenseFamilyCategory.getName())) {
            throw new IllegalStateException("Expected " + MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY
                    + ", got " + pLicenseFamilyCategory.getName());
        }
        setLicenseFamilyCategory(pLicenseFamilyCategory.getValue());
        if (!MetaData.RAT_URL_LICENSE_FAMILY_NAME.equals(pLicenseFamilyName.getName())) {
            throw new IllegalStateException("Expected " + MetaData.RAT_URL_LICENSE_FAMILY_NAME
                    + ", got " + pLicenseFamilyName.getName());
        }
        setLicenseFamilyName(pLicenseFamilyName.getValue());
        setNotes(pNotes);
        setPatterns(pPatterns);
    }
    
    public String[] getPatterns() {
        return patterns;
    }

    public void setPatterns(String[] pPatterns) {
        patterns = pPatterns;
    }

    public String getLicenseFamilyCategory() {
        return licenseFamilyCategory;
    }

    public void setLicenseFamilyCategory(String pDocumentCategory) {
        licenseFamilyCategory = pDocumentCategory;
    }

    public String getLicenseFamilyName() {
        return licenseFamilyName;
    }

    public void setLicenseFamilyName(String pLicenseFamilyCategory) {
        licenseFamilyName = pLicenseFamilyCategory;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String pNotes) {
        notes = pNotes;
    }

    protected void reportOnLicense(Document subject) throws RatHeaderAnalysisException {
        final MetaData metaData = subject.getMetaData();
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_HEADER_SAMPLE, notes));
        final String licFamilyCategory = getLicenseFamilyCategory();
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_HEADER_CATEGORY, licFamilyCategory));
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY, licFamilyCategory));
    }

    protected boolean matches(String pLine) {
        if (pLine != null) {
            final String[] pttrns = getPatterns();
            if (pttrns != null) {
                for (int i = 0;  i < pttrns.length;  i++) {
                    if (pLine.indexOf(pttrns [i], 0) >= 0) {
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
