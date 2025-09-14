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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.rat.license.ILicense;
import org.apache.rat.utils.DefaultLog;
import org.apache.tika.mime.MediaType;

/**
 * Data about the document under test.
 */
public class MetaData {

    /** The list of matched licenses */
    private final SortedSet<ILicense> matchedLicenses;
    /** The list of License Family Categories that are approved */
    private Predicate<ILicense> approvalPredicate;
    /** The charset for this document */
    private Charset charset;
    /** The media type for this document */
    private MediaType mediaType;
    /** The document type for this document */
    private Document.Type documentType;
    /** The flag for directory types */
    private boolean isDirectory;

    /**
     * Create metadata without a content type.
     */
    public MetaData() {
        this.matchedLicenses = new TreeSet<>();
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
     * Returns {@code true} if {@link #setCharset} has been called.
     * @return {@code true} if {@link #setCharset} has been called.
     */
    public boolean hasCharset() {
        return charset != null;
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
     * Gets the predicate to filter approved licenses.
     * @return The predicate to validate licenses.
     */
    private Predicate<ILicense> getApprovalPredicate() {
        if (approvalPredicate == null) {
            DefaultLog.getInstance().error("Approval Predicate was not set.");
            throw new IllegalStateException("Approval Predicate was not set.");
        }
        return approvalPredicate;
    }

    /**
     * Sets the set of approved licenses.
     * @param approvalPredicate the predicate to validate licenses.
     */
    public void setApprovalPredicate(final Predicate<ILicense> approvalPredicate) {
        this.approvalPredicate = approvalPredicate;
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
        return getApprovalPredicate().test(license);
    }

    /**
     * Gets the stream of unapproved licenses that have been matched.
     * @return the stream of unapproved licenses that have been matched.
     */
    public Stream<ILicense> unapprovedLicenses() {
        return licenses().filter(lic -> !isApproved(lic));
    }

    /**
     * Sets the document type.
     * @param type the document type for the document being recorded.
     */
    public void setDocumentType(final Document.Type type) {
        this.documentType = type;
    }

    /**
     * Set the directory flag.
     * @param state the state to set the directory flag in.
     */
    public void setIsDirectory(final boolean state) {
        this.isDirectory = state;
    }

    /**
     * Return {@code true} if the directory flag was set.
     * @return the directory flag.
     */
    public boolean isDirectory() {
        return isDirectory;
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
        return String.format("MetaData[%s license, %s approved]", matchedLicenses.size(),
                approvalPredicate == null ? "unknown" : matchedLicenses.stream().filter(getApprovalPredicate()).count());
    }
}
