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
import org.apache.rat.api.domain.LicenseFamily;

/**
 * The Class BaseLicense.
 */
public class BaseLicense {

    /** The license family category. */
    private String licenseFamilyCategory;

    /** The license family name. */
    private String licenseFamilyName;

    /** The notes. */
    private String notes;

    /**
     * Constructs empty base license. Useful when creating an instance via
     * reflection.
     */
    public BaseLicense() {
        this.licenseFamilyCategory = null;
        this.licenseFamilyName = null;
        this.notes = null;
    }

    /**
     * Instantiates a new base license.
     * 
     * @param licenseFamilyCategory
     *            the license family category
     * @param licenseFamilyName
     *            the license family name
     * @param notes
     *            the notes
     */
    public BaseLicense(final MetaData.Datum licenseFamilyCategory,
            final MetaData.Datum licenseFamilyName, final String notes) {
        if (!MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY
                .equals(licenseFamilyCategory.getName())) {
            throw new IllegalStateException("Expected "
                    + MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY + ", got "
                    + licenseFamilyCategory.getName());
        }
        setLicenseFamilyCategory(licenseFamilyCategory.getValue());
        if (!MetaData.RAT_URL_LICENSE_FAMILY_NAME.equals(licenseFamilyName
                .getName())) {
            throw new IllegalStateException("Expected "
                    + MetaData.RAT_URL_LICENSE_FAMILY_NAME + ", got "
                    + licenseFamilyName.getName());
        }
        setLicenseFamilyName(licenseFamilyName.getValue());
        setNotes(notes);
    }

    /**
     * Constructs a license based on the given family.
     * 
     * @param licenseFamily
     *            not null
     */
    public BaseLicense(final LicenseFamily licenseFamily) {
        this.notes = licenseFamily.getNotes();
        this.licenseFamilyCategory = licenseFamily.getCategory();
        this.licenseFamilyName = licenseFamily.getName();
    }

    /**
     * Gets the license family category.
     * 
     * @return the license family category
     */
    public String getLicenseFamilyCategory() {
        return this.licenseFamilyCategory;
    }

    /**
     * Sets the license family category.
     * 
     * @param pDocumentCategory
     *            the new license family category
     */
    public void setLicenseFamilyCategory(final String pDocumentCategory) {
        this.licenseFamilyCategory = pDocumentCategory;
    }

    /**
     * Gets the license family name.
     * 
     * @return the license family name
     */
    public String getLicenseFamilyName() {
        return this.licenseFamilyName;
    }

    /**
     * Sets the license family name.
     * 
     * @param pLicenseFamilyCategory
     *            the new license family name
     */
    public void setLicenseFamilyName(final String pLicenseFamilyCategory) {
        this.licenseFamilyName = pLicenseFamilyCategory;
    }

    /**
     * Gets the notes.
     * 
     * @return the notes
     */
    public String getNotes() {
        return this.notes;
    }

    /**
     * Sets the notes.
     * 
     * @param pNotes
     *            the new notes
     */
    public void setNotes(final String pNotes) {
        this.notes = pNotes;
    }

    /**
     * Report on license.
     * 
     * @param subject
     *            the subject
     */
    public final void reportOnLicense(final Document subject) {
        final MetaData metaData = subject.getMetaData();
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_HEADER_SAMPLE,
                this.notes));
        final String licFamilyCategory = getLicenseFamilyCategory();
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_HEADER_CATEGORY,
                licFamilyCategory));
        metaData.set(new MetaData.Datum(
                MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY, licFamilyCategory));
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME,
                getLicenseFamilyName()));
    }

    /**
     * Prune.
     * 
     * @param text
     *            the text
     * @return the string
     */
    protected static final String prune(final String text) {
        final int length = text.length();
        final StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char charIndex = text.charAt(i);
            if (Character.isLetterOrDigit(charIndex)) {
                buffer.append(charIndex);
            }
        }
        return buffer.toString();
    }

}
