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

import org.apache.rat.analysis.matchers.AbstractMatcherContainer;
import org.apache.rat.config.parameters.Description;
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
    private State lastState;

    /**
     * Constructs the LicenseCollection from the provided ILicense collection.
     * @param enclosed The collection of ILicenses to compose this License
     * implementation from. May not be null.
     */
    public LicenseCollection(Collection<ILicense> enclosed) {
        super(enclosed, null);
        this.enclosed = Collections.unmodifiableCollection(enclosed);
        this.matchingLicense = null;
        this.lastState = State.i;
    }

    @Override
    public String getId() {
        return "Default License Collection";
    }

    @Override
    public void reset() {
        enclosed.forEach(ILicense::reset);
        this.lastState = State.i;
        this.matchingLicense = null;
    }

    @Override
    public State matches(String line) {
        State dflt = State.f;
        for (ILicense license : enclosed) {
            switch (license.matches(line)) {
            case t:
                this.matchingLicense = license;
                lastState = State.t;
                return State.t;
            case i:
                dflt = State.i;
                break;
            default:
                // do nothing
                break;
            }
        }
        lastState = dflt;
        return dflt;
    }

    @Override
    public State currentState() {
        if (lastState == State.t) {
            return lastState;
        }
        for (ILicense license : enclosed) {
            switch (license.currentState()) {
            case t:
                this.matchingLicense = license;
                lastState = State.t;
                return lastState;
            case i:
                lastState = State.i;
                return lastState;
            case f:
                // do nothing;
                break;
            }
        }
        lastState = State.f;
        return lastState;
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
    public String getName() {
        return getLicenseFamily().getFamilyName();
    }

    @Override
    public Description getDescription() {
        if (matchingLicense != null) {
            return matchingLicense.getDescription();
        }
        return new Description(Type.License, "licenseCollection",
                "A collection of ILicenses that acts as a single License for purposes of Analysis.", false, null, null);
    }

    @Override
    public IHeaderMatcher getMatcher() {
        return matchingLicense;
    }
}
