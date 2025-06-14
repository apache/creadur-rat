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
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

/**
 * A class to quickly build testing licenses.
 */
public class TestingLicense implements ILicense {

    private final ILicenseFamily family;
    private final IHeaderMatcher matcher;
    private final String note;
    private String name;
    private final String id;

    /**
     * Creates a testing license with the specified id and a default TestingMatcher
     * @param id The ID to use.
     * @see TestingMatcher
     */
    public TestingLicense(String id) {
        this(id, id, new TestingMatcher());
    }

    /**
     * Creates a testing license with the specified id and a default TestingMatcher
     * @param family the Family id
     * @param id The ID to use.
     * @see TestingMatcher
     */
    public TestingLicense(String family, String id) {
        this(family, id, new TestingMatcher());
    }

    /**
     * Creates a testing license wit the specified id and matcher.
     * @param id the ID to use
     * @param matcher the matcher to execute.
     */
    public TestingLicense(String family, String id, IHeaderMatcher matcher) {
        this(id, matcher, ILicenseFamily.builder().setLicenseFamilyCategory(family)
                .setLicenseFamilyName("TestingLicense: " + family).build());
    }

    /**
     * Creates a testing license with the specified matcher and family.
     * @param id the license id
     * @param matcher the matcher to use.
     * @param family the family for this license.
     */
    public TestingLicense(String id, IHeaderMatcher matcher, ILicenseFamily family) {
        this.family = family;
        this.matcher = matcher;
        this.note = null;
        this.id = id;
        this.name = id;
    }

    /**
     * Gets the family from the license
     * @return the license family.
     */
    public ILicenseFamily getFamily() {
        return family;
    }

    /**
     * Gets the matcher from the license
     * @return the matcher.
     */
    @Override
    public IHeaderMatcher getMatcher() {
        return matcher;
    }

    /**
     * Sets the name from value for this license.
     * @param name the name of this license.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void reset() {
        matcher.reset();
    }

    @Override
    public boolean matches(IHeaders line) {
        return matcher.matches(line);
    }

    @Override
    public ILicenseFamily getLicenseFamily() {
        return family;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public String getName() {
        return StringUtils.defaultIfBlank(name, family.getFamilyName());
    }

    @Override
    public boolean equals(Object o) {
        return ILicense.equals(this, o);
    }

    @Override
    public int hashCode() {
        return ILicense.hash(this);
    }

    @Override
    public String toString() {
        return String.format("%s[id='%s', family='%s']", name, id, family);
    }
}
