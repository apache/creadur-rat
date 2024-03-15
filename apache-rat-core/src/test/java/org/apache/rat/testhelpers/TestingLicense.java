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

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

/**
 * A class to quickly build testing licenses.
 */
public class TestingLicense implements ILicense {

    private final ILicenseFamily family;
    private IHeaderMatcher matcher;
    private String derivedFrom;
    private String notes;
    private String name;
    private String id;

    /**
     * Creates a testing license named "DfltTst" with category of "DfltT" and a default 
     * TestingMatcher.
     * @see TestingMatcher
     */
    public TestingLicense() {
        this("DfltTst", new TestingMatcher());
    }
    
    /**
     * Creates a testing license with the specified id and a default TestingMatcher
     * @param id The ID to use.
     * @see TestingMatcher
     */
    public TestingLicense(String id) {
        this(id, new TestingMatcher());
    }

    /**
     * Creates a testing license wit the specified id and matcher.
     * @param id the ID to use
     * @param matcher the matcher to execute.
     */
    public TestingLicense(String id, IHeaderMatcher matcher) {
        this(matcher, ILicenseFamily.builder().setLicenseFamilyCategory(id)
                .setLicenseFamilyName("TestingLicense: " + id).build());
    }
    
    /**
     * Creates a testing license with the specified matcher and family.
     * @param matcher the matcher to use.
     * @param family the family for this license.
     */
    public TestingLicense(IHeaderMatcher matcher, ILicenseFamily family) {
        this.family = family;
        this.matcher = matcher;
        this.derivedFrom = null;
        this.notes = null;
    }
    
    /**
     * Create a testing license for the specified family using a default TestingMatcher
     * @param family the family for the license.
     * @see TestingMatcher
     */
    public TestingLicense(ILicenseFamily family) {
        this(new TestingMatcher(), family );
    }

    @Override
    public String toString() {
        return family.toString();
    }

    /**
     * Gets the family from  the license
     * @return the license family.
     */
    public ILicenseFamily getFamily() {
        return family;
    }

    /**
     * Gets the matcher from the license
     * @return the matcher.
     */
    public IHeaderMatcher getMatcher() {
        return matcher;
    }

    /**
     * Sets the derived from value for this license.
     * @param derivedFrom the license this license is derived from.
     */
    public void setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }
    
    /**
     * Sets the name from value for this license.
     * @param name the name of this license.
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(id, family.getFamilyCategory().trim());
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
    public String getName() {
        return StringUtils.defaultIfBlank(name, family.getFamilyName());
    }
    
    @Override
    public String derivedFrom() {
        return derivedFrom;
    }
 
    @Override
    public Description getDescription() {
        return new ILicense.ILicenseDescription(this, matcher);
    }

}
