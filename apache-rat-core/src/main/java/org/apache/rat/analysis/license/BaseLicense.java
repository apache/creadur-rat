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

import java.util.Arrays;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.AbstractHeaderMatcher;
import org.apache.rat.analysis.matchers.AbstractMatcherContainer;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

public abstract class BaseLicense extends AbstractHeaderMatcher implements ILicense {
    private ILicenseFamily family;
    private String notes;
    private String derivedFrom;
    private IHeaderMatcher matcher;

    public BaseLicense(ILicenseFamily family, String notes, IHeaderMatcher matcher) {
        this(null, family, notes, matcher);
    }

    public BaseLicense(String idPrefix, ILicenseFamily family, String notes, IHeaderMatcher matcher) {
        this(null, family, notes, matcher, null);
    }

    public BaseLicense(String idPrefix, ILicenseFamily family, String notes, IHeaderMatcher matcher,
            String derivedFrom) {
        super(String.format("%s:%s:%s/%s", idPrefix == null ? "" : idPrefix, family.getFamilyCategory(),
                matcher.getClass().getSimpleName(), matcher.getId()));
        this.family = family;
        this.notes = notes;
        this.derivedFrom = derivedFrom;
        this.matcher = matcher;
    }

    @Override
    public ILicenseFamily getLicenseFamily() {
        return family;
    }

    @Override
    public int compareTo(ILicense arg0) {
        return ILicense.getComparator().compare(this, arg0);
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public State matches(String line) {
        return matcher.matches(line);
    }
    
    @Override
    public State currentState() {
        return matcher.currentState();
    }
    
    @Override
    public State finalizeState() {
        return matcher.finalizeState();
    }
    
    @Override
    public void reset() {
        matcher.reset();
    }

    @Override
    public String derivedFrom() {
        return derivedFrom;
    }

    /**
     * Removes everything except letter or digit from text.
     * 
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
}
