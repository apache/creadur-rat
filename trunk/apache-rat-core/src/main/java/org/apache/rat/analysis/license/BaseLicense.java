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

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

public class BaseLicense {
    private String licenseFamilyCategory;
    private String licenseFamilyName;
    private String notes;

    public BaseLicense() {
    }

    public BaseLicense(final MetaData.Datum licenseFamilyCategory, final MetaData.Datum licenseFamilyName, final String notes)
    {
        if (!MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY.equals(licenseFamilyCategory.getName())) {
            throw new IllegalStateException("Expected " + MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY
                    + ", got " + licenseFamilyCategory.getName());
        }
        setLicenseFamilyCategory(licenseFamilyCategory.getValue());
        if (!MetaData.RAT_URL_LICENSE_FAMILY_NAME.equals(licenseFamilyName.getName())) {
            throw new IllegalStateException("Expected " + MetaData.RAT_URL_LICENSE_FAMILY_NAME
                    + ", got " + licenseFamilyName.getName());
        }
        setLicenseFamilyName(licenseFamilyName.getValue());
        setNotes(notes);
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

    public final void reportOnLicense(Document subject) {
        final MetaData metaData = subject.getMetaData();
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_HEADER_SAMPLE, notes));
        final String licFamilyCategory = getLicenseFamilyCategory();
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_HEADER_CATEGORY, licFamilyCategory));
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY, licFamilyCategory));
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, getLicenseFamilyName()));
    }

    protected static final String prune(String text) {
        final int length = text.length();
        final StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char at = text.charAt(i);
            if (Character.isLetterOrDigit(at)) {
                buffer.append(at);
            }
        }
        return buffer.toString();
    }

}
