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

import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

class LicenseCollection implements ILicense {
    private static final ILicenseFamily DEFAULT = ILicenseFamily.builder().setLicenseFamilyCategory("Dummy")
            .setLicenseFamilyName("HeaderMatcherCollection default license family").build();
    private Collection<ILicense> enclosed;
    private ILicense matchingLicense;

    public LicenseCollection(Collection<ILicense> enclosed) {
        this.matchingLicense = null;
        this.enclosed = enclosed;
    }

    @Override
    public String getId() {
        return "Default License Collection";
    }

    @Override
    public void reset() {
        enclosed.stream().forEach(ILicense::reset);
    }

    @Override
    public boolean matches(String line) {
        for (ILicense license : enclosed) {
            if (license.matches(line)) {
                this.matchingLicense = license;
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(ILicense arg0) {
        return getLicenseFamily().compareTo(arg0.getLicenseFamily());
    }

    public ILicenseFamily getLicenseFamily() {
        return matchingLicense == null ? DEFAULT : matchingLicense.getLicenseFamily();
    }

    public String getNotes() {
        return matchingLicense == null ? null : matchingLicense.getNotes();
    }

    public String derivedFrom() {
        return matchingLicense == null ? null : matchingLicense.derivedFrom();
    }
}
