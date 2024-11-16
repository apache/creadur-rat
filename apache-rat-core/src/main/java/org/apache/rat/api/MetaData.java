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

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.tika.mime.MediaType;

/**
 * Data about the document under test.
 */
public class MetaData {

    /** The list of matched licenses */
    private final SortedSet<ILicense> matchedLicenses;
    /** The list of License Family Categories that are approved */
    private final Set<String> approvedLicenses;
    /** The charset for this document */
    private Charset charset;
    /** The media type for this document */
    private MediaType mediaType;
    /** The document type for this document */
    private Document.Type documentType;
    /** The sample of the header for this document */
    private String sampleHeader;

    /**
     * Create metadata without a content type.
     */
    public MetaData() {
        this.matchedLicenses = new TreeSet<>();
        this.approvedLicenses = new HashSet<>();
    }

    /**
     * Gets the charset for the document. If the charset was not set will return the system default charset.
     * @return the charset for the document
     */
    public Charset getCharset() {
        return charset == null ? Charset.defaultCharset() : charset;
    }

    /**
     * Sets the charset for the document. If set to {@code null} the system default charset will be used.
     * @param charset the charset to use.
     */
    public void setCharset(final Charset charset) {
        this.charset = charset;
    }

    /**
     * Gets the defined media type.
     * @return the media type.
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * Sets the defined media type.
     * @param mediaType the media type.
     */
    public void setMediaType(final MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Determines if a matching license has been detected.
     * @return true if there is a matching license.
     */
    public boolean detectedLicense() {
        return !matchedLicenses.isEmpty();
    }

    /**
     * Sets the set of approved licenses.
     * @param approvedLicenseFamilies the set of approved license families.
     */
    public void setApprovedLicenses(final Set<ILicenseFamily> approvedLicenseFamilies) {
        licenses().filter(lic -> approvedLicenseFamilies.contains(lic.getLicenseFamily()))
                .forEach(lic -> approvedLicenses.add(lic.getId()));
    }

    /**
     * Gets the stream of licenses that have been matched.
     * @return the stream of licenses that have been matched.
     */
    public Stream<ILicense> licenses() {
        return matchedLicenses.stream();
    }

    /**
     * Gets the stream of approved licenses that have been matched.
     * @return the stream of approved licenses that have been matched.
     */
    public Stream<ILicense> approvedLicenses() {
        return licenses().filter(this::isApproved);
    }

    /**
     * Determine if the license is an approved license.
     * @param license the license to check.
     * @return {@code true} if the license is in the list of approved licenses, {@code false} otherwise.
     */
    public boolean isApproved(final ILicense license) {
        return approvedLicenses.contains(license.getId());
    }

    /**
     * Gets the stream of unapproved licenses that have been matched.
     * @return the stream of unapproved licenses that have been matched.
     */
    public Stream<ILicense> unapprovedLicenses() {
        return licenses().filter(lic -> !isApproved(lic));
    }

    /**
     * Sets the sample header. This is the header that was collected during processing.
     * @param sampleHeader the sample header to use.
     */
    public void setSampleHeader(final String sampleHeader) {
        this.sampleHeader = sampleHeader;
    }

    /**
     * Gets the sample header.
     * @return the sample header.
     */
    public String getSampleHeader() {
        return sampleHeader;
    }

    /**
     * Sets the document type.
     * @param type the document type for the document being recorded.
     */
    public void setDocumentType(final Document.Type type) {
        this.documentType = type;
    }

    /**
     * Gets the document type.
     * @return the document type of the document that was recorded.
     */
    public Document.Type getDocumentType() {
        return this.documentType;
    }

    /**
     * Add the license information to the metadata.
     * @param license the license to add metadata for.
     */
    public void reportOnLicense(final ILicense license) {
        this.matchedLicenses.add(license);
    }

    /**
     * Remove matched licenses based on a predicate. Will remove licenses for which the predicate
     * returns true.
     * @param filter the predicate to use.
     */
    public void removeLicenses(final Predicate<ILicense> filter) {
        this.matchedLicenses.removeIf(filter);
    }

    @Override
    public String toString() {
        return String.format("MetaData[%s license, %s approved]", matchedLicenses.size(), approvedLicenses.size());
    }
}
