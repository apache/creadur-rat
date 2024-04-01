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

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.ConfigComponent;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;

/**
 * A simple implementation of ILicense.
 */
@ConfigComponent(type = Component.Type.License)
public class SimpleLicense implements ILicense {

    @ConfigComponent(type = Component.Type.BuilderParam, desc = "The family this license belongs to.", parameterType = ILicenseFamily.class)
    private ILicenseFamily family;

    @ConfigComponent(type = Component.Type.Unlabeled, desc = "The matcher for this license.")
    private IHeaderMatcher matcher;
    @ConfigComponent(type = Component.Type.Parameter, desc = "The notes about this license.")
    private String notes;
    @ConfigComponent(type = Component.Type.Parameter, desc = "The name of this license.")
    private String name;
    @ConfigComponent(type = Component.Type.Parameter, desc = "The ID for this license.")
    private String id;

    SimpleLicense(ILicenseFamily family, IHeaderMatcher matcher, String notes, String name, String id) {
        Objects.requireNonNull(matcher, "Matcher must not be null");
        Objects.requireNonNull(family, "Family must not be null");
        this.family = family;
        this.matcher = matcher;
        this.notes = notes;
        this.name = StringUtils.defaultIfBlank(name, family.getFamilyName());
        this.id = StringUtils.defaultIfBlank(id, family.getFamilyCategory().trim());
    }

    @Override
    public String toString() {
        return String.format("%s:%s", getId(), getName());
    }

    public ILicenseFamily getFamily() {
        return family;
    }

    public void setFamily(ILicenseFamily family) {
        this.family = family;
    }

    @Override
    public IHeaderMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(IHeaderMatcher matcher) {
        this.matcher = matcher;
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
    public int compareTo(ILicense other) {
        return ILicense.getComparator().compare(this, other);
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Description getDescription() {
        return DescriptionBuilder.build(this);
    }
}
