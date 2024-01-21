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

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.inspector.AbstractInspector;
import org.apache.rat.inspector.Inspector;
import org.apache.rat.inspector.Inspector.Type;

/**
 * A simple implementation of ILicense.
 */
class SimpleLicense implements ILicense {

    private ILicenseFamily family;
    private IHeaderMatcher matcher;
    private String derivedFrom;
    private String notes;
    private String name;
    private String id;

    SimpleLicense(ILicenseFamily family, IHeaderMatcher matcher, String derivedFrom, String notes, String name, String id) {
        Objects.requireNonNull(matcher, "Matcher must not be null");
        Objects.requireNonNull(family, "Family must not be null");  
        this.family = family;
        this.matcher = matcher;
        this.derivedFrom = derivedFrom;
        this.notes = notes;
        this.name = StringUtils.defaultIfBlank(name, family.getFamilyName());
        this.id = StringUtils.defaultIfBlank(id, family.getFamilyCategory().trim());
    }

    @Override
    public String toString() {
        return String.format( "%s:%s", getId(), getName());
    }

    public ILicenseFamily getFamily() {
        return family;
    }

    public void setFamily(ILicenseFamily family) {
        this.family = family;
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
        return id;
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
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Inspector getInspector() {
        return AbstractInspector.license(this, this.matcher.getInspector());
    }
}
