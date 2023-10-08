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
package org.apache.rat.license;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicense.Builder;

/**
 * An implementation of the ILicense Builder.
 */
class ILicenseBuilder implements Builder {

    private IHeaderMatcher.Builder matcher;

    private String notes;

    private String derivedFrom;

    private final ILicenseFamily.Builder licenseFamily = ILicenseFamily.builder();

    @Override
    public Builder setMatcher(IHeaderMatcher.Builder matcher) {
        this.matcher = matcher;
        return this;
    }
    
    @Override
    public Builder setMatcher(IHeaderMatcher matcher) {
        this.matcher = ()->matcher;
        return this;
    }

    @Override
    public Builder setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    @Override
    public Builder setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
        return this;
    }

    @Override
    public Builder setLicenseFamilyCategory(String licenseFamilyCategory) {
        this.licenseFamily.setLicenseFamilyCategory(licenseFamilyCategory);
        return this;
    }

    @Override
    public Builder setLicenseFamilyName(String licenseFamilyName) {
        this.licenseFamily.setLicenseFamilyName(licenseFamilyName);
        return this;
    }

    @Override
    public ILicense build() {
        return new SimpleLicense(licenseFamily.build(), matcher.build(), derivedFrom, notes);
    }
}