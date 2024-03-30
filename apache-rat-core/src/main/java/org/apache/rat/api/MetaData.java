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
package org.apache.rat.api;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

/**
 * Data about the document under test..
 */
public class MetaData {

    /** The list of matched licenses */
    private final SortedSet<ILicense> matchedLicenses;
    /** The list of License Family Categories that are approved */
    private final Set<String> approvedLicenses;

    private Document.Type documentType;
    private String sampleHeader;

    /**
     * Create metadata without a content type.
     */
    public MetaData() {
        this.matchedLicenses = new TreeSet<>();
        this.approvedLicenses = new HashSet<>();
    }

    public boolean detectedLicense() {
        return !matchedLicenses.isEmpty();
    }

    public void setApprovedLicenses(Set<ILicenseFamily> approvedLicenseFamilies) {
        licenses().filter(lic -> approvedLicenseFamilies.contains(lic.getLicenseFamily()))
                .forEach(lic -> approvedLicenses.add(lic.getId()));
    }

    public Stream<ILicense> licenses() {
        return matchedLicenses.stream();
    }

    public Stream<ILicense> approvedLicenses() {
        return licenses().filter(this::isApproved);
    }

    public boolean isApproved(ILicense license) {
        return approvedLicenses.contains(license.getId());
    }

    public Stream<ILicense> unapprovedLicenses() {
        return licenses().filter(lic -> !isApproved(lic));
    }

    public void setSampleHeader(String sampleHeader) {
        this.sampleHeader = sampleHeader;
    }

    public String getSampleHeader() {
        return sampleHeader;
    }

    public void setDocumentType(Document.Type type) {
        this.documentType = type;
    }

    public Document.Type getDocumentType() {
        return this.documentType;
    }

    /**
     * Add the license information to the metadata.
     * @param license the license to add metadata for.
     */
    public void reportOnLicense(ILicense license) {
        this.matchedLicenses.add(license);
    }
    
    @Override
    public String toString() {
        return String.format( "MetaData[%s license, %s approved]", matchedLicenses.size(), approvedLicenses.size());
    }
}
