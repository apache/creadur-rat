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
package org.apache.rat.testhelpers;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

public class TestingLicense implements ILicense {

    private final ILicenseFamily family;
    IHeaderMatcher matcher;
    private String derivedFrom;
    private String notes;

    public TestingLicense() {
        this("DfltTst", new TestingMatcher());
    }

    public TestingLicense(String id, IHeaderMatcher matcher) {
        this.family = ILicenseFamily.builder().setLicenseFamilyCategory(id)
                .setLicenseFamilyName("TestingLicense: " + id).build();
        this.matcher = matcher;
        this.derivedFrom = null;
        this.notes = null;
    }

    @Override
    public String toString() {
        return family.toString();
    }

    public ILicenseFamily getFamily() {
        return family;
    }

    public IHeaderMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(IHeaderMatcher matcher) {
        this.matcher = matcher;
    }

    public void setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    @Override
    public String getId() {
        return matcher.getId();
    }

    @Override
    public void reset() {
        matcher.reset();
    }

    @Override
    public State matches(String line) {
        return matcher.matches(line);
    }

    @Override
    public State finalizeState() {
        return matcher.finalizeState();
    }

    @Override
    public State currentState() {
        return matcher.currentState();
    }

    @Override
    public ILicenseFamily getLicenseFamily() {
        return family;
    }

    @Override
    public int compareTo(ILicense other) {
        return ILicense.getComparator().compare(this, other);
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public String derivedFrom() {
        return derivedFrom;
    }

}
