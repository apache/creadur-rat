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

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.SimpleLicenseFamily;

/*
 * Todo move to bulder pattern
 */

public abstract class BaseLicense implements IHeaderMatcher {
    private ILicenseFamily family;
    private String notes;
    private String idPrefix;

    public BaseLicense(ILicenseFamily family, String notes) {
        this(family, notes, null);
    }
    
    public BaseLicense(ILicenseFamily family, String notes, String idPrefix) {
        this.family = family;
        this.notes = notes;
        this.idPrefix = idPrefix;
    }
    
    @Override
    public String toString() {
        return getId();
    }
    
    public String getId() {
        return String.format( "%s:%s:%s", idPrefix==null?"":idPrefix, family.getFamilyCategory(), this.getClass().getSimpleName());
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public ILicenseFamily getLicenseFamily() {
        return family;
    }

    public void setLicenseFamilyCategory(String category) {
        this.family = new SimpleLicenseFamily(category, family == null? null : family.getFamilyName());
    }
    
    public void setLicenseFamilyName(String name) {
        this.family = new SimpleLicenseFamily(family == null? null : family.getFamilyCategory(), name);
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
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_HEADER_CATEGORY, family.getFamilyCategory()));
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY, family.getFamilyCategory()));
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, family.getFamilyName()));
    }

    /**
     * Removes everything except letter or digit from text.
     * @param text The text to remove extra chars from.
     * @return the pruned text.
     */
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

    @Override
    public void reportFamily(Consumer<ILicenseFamily> consumer) {
        consumer.accept(family);
    }

    @Override
    public void extractMatcher(Consumer<IHeaderMatcher> consumer, Predicate<ILicenseFamily> comparator) {
        if (comparator.test(family)) {
            consumer.accept(this);
        }
    }
}
