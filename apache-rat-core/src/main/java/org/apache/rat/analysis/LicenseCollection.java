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
package org.apache.rat.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.rat.analysis.matchers.AbstractMatcherContainer;
import org.apache.rat.config.parameters.DescriptionImpl;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

/**
 * A collection of ILicenses that acts as a single License for purposes of
 * Analysis. <p> This class process each license in turn on each
 * {@code matches(String)} call. When a match is found the ILicenseFamily for
 * the matching license is captured and used as the family for this license. If
 * no matching license has been found the default {@code dummy} license category
 * is used.
 */
class LicenseCollection extends AbstractMatcherContainer implements ILicense {

    private static final ILicenseFamily DEFAULT = ILicenseFamily.builder().setLicenseFamilyCategory("Dummy")
            .setLicenseFamilyName("HeaderMatcherCollection default license family").build();
    private final Collection<ILicense> enclosed;
    private ILicense matchingLicense;

    /**
     * Constructs the LicenseCollection from the provided ILicense collection.
     * @param enclosed The collection of ILicenses to compose this License
     * implementation from. May not be null.
     */
    public LicenseCollection(Collection<ILicense> enclosed) {
        super(enclosed);
        this.enclosed = Collections.unmodifiableCollection(enclosed);
        this.matchingLicense = null;
    }

    @Override
    public String getId() {
        return "Default License Collection";
    }

    @Override
    public void reset() {
        enclosed.forEach(ILicense::reset);
        this.matchingLicense = null;
    }

    @Override
    public boolean matches(IHeaders headers) {
        for (ILicense license : enclosed) {
            if (license.matches(headers)) {
                matchingLicense = license;
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(ILicense arg0) {
        return getLicenseFamily().compareTo(arg0.getLicenseFamily());
    }

    @Override
    public ILicenseFamily getLicenseFamily() {
        return matchingLicense == null ? DEFAULT : matchingLicense.getLicenseFamily();
    }

    @Override
    public String getNotes() {
        return matchingLicense == null ? null : matchingLicense.getNotes();
    }

    @Override
    public String derivedFrom() {
        return matchingLicense == null ? null : matchingLicense.derivedFrom();
    }

    @Override
    public String getName() {
        return getLicenseFamily().getFamilyName();
    }

    @Override
    public Description getDescription() {
        if (matchingLicense != null) {
            return matchingLicense.getDescription();
        }
        return new DescriptionImpl(Type.License, "licenseCollection",
                "A collection of ILicenses that acts as a single License for purposes of Analysis.", null) {
            @Override
            public Collection<Description> getChildren() {
                return enclosed.stream().map(ILicense::getDescription).collect(Collectors.toList());
            }
        };
    }
}
