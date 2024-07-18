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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.ImplementationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.ConfigComponent;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;

/**
 * A simple implementation of ILicense.
 */
@ConfigComponent(type = ComponentType.LICENSE)
public class SimpleLicense implements ILicense {
    /** The license family for this license */
    @ConfigComponent(type = ComponentType.BUILD_PARAMETER, desc = "The defined license families.", name = "licenseFamilies")
    private ILicenseFamily family;
    /** The matcher for this license */
    @ConfigComponent(type = ComponentType.PARAMETER, desc = "The matcher for this license.", required = true)
    private IHeaderMatcher matcher;
    /** The notes for this license */
    @ConfigComponent(type = ComponentType.PARAMETER, desc = "The notes about this license.")
    private String note;
    /** The name of this license */
    @ConfigComponent(type = ComponentType.PARAMETER, desc = "The name of this license.")
    private String name;
    /** The ID for this license.  Must be unique */
    @ConfigComponent(type = ComponentType.PARAMETER, desc = "The ID for this license.")
    private String id;

    SimpleLicense(final ILicenseFamily family, final IHeaderMatcher matcher, final String notes, final String name, final String id) {
        Objects.requireNonNull(matcher, "Matcher must not be null");
        Objects.requireNonNull(family, "Family must not be null");
        this.family = family;
        this.matcher = matcher;
        this.note = notes;
        this.name = StringUtils.defaultIfBlank(name, family.getFamilyName());
        this.id = StringUtils.defaultIfBlank(id, family.getFamilyCategory().trim());
    }

    @Override
    public String toString() {
        return String.format("%s:%s", getId(), getName());
    }

    @ConfigComponent(type = ComponentType.PARAMETER, desc = "The license family category for this license.", required = true)
    public String getFamily() {
        return family.getFamilyCategory();
    }

    @Override
    public IHeaderMatcher getMatcher() {
        return matcher;
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
    public boolean matches(final IHeaders line) {
        return matcher.matches(line);
    }

    @Override
    public ILicenseFamily getLicenseFamily() {
        return family;
    }

    @Override
    public boolean equals(final Object o) {
        return ILicense.equals(this, o);
    }

    @Override
    public int hashCode() {
        return ILicense.hash(this);
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Description getDescription() {
        return DescriptionBuilder.build(this);
    }

    public static class Builder implements ILicense.Builder {
        /** The set of known license families */
        private SortedSet<ILicenseFamily> licenseFamilies;
        /** The matcher builder in use for this license */
        private IHeaderMatcher.Builder matcher;
        /** The notes for the license */
        private List<String> notes = new ArrayList<>();
        /** The name of this license */
        private String name;
        /** The ID of this license */
        private String id;
        /** The family category of this license */
        private String familyCategory;

        /**
         * Sets the matcher from a builder.
         * @param matcher the builder for the matcher for the license.
         * @return this builder for chaining.
         */
        @Override
        public Builder setMatcher(final IHeaderMatcher.Builder matcher) {
            this.matcher = matcher;
            return this;
        }

        /**
         * Sets the matcher.
         * @param matcher the matcher for the license.
         * @return this builder for chaining.
         */
        @Override
        public Builder setMatcher(final IHeaderMatcher matcher) {
            this.matcher = () -> matcher;
            return this;
        }

        /**
         * Sets the notes for the license. If called multiple times the notes are
         * concatenated to create a single note.
         * @param note the note for the license.
         * @return this builder.
         */
        @Override
        public Builder setNote(final String note) {
            if (StringUtils.isNotBlank(note)) {
                this.notes.add(note);
            }
            return this;
        }

        /**
         * Sets the ID of the license. If the ID is not set then the ID of the license
         * family is used.
         * @param id the ID for the license
         * @return this builder for chaining.
         */
        @Override
        public Builder setId(final String id) {
            this.id = id;
            return this;
        }

        /**
         * Set the family category for this license. The category must be unique across
         * all licenses and must be 5 characters. If more than 5 characters are provided
         * then only the first 5 are taken. If fewer than 5 characters are provided the
         * category is padded with spaces.
         * @param licenseFamilyCategory the family category for the license.
         * @return this builder for chaining.
         */
        @Override
        public Builder setFamily(final String licenseFamilyCategory) {
            this.familyCategory = licenseFamilyCategory;
            return this;
        }

        /**
         * Sets the name of the license. If the name is not set then the name of the
         * license family is used.
         * @param name the name for the license
         * @return this builder for chaining.
         */
        @Override
        public Builder setName(final String name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder setLicenseFamilies(final SortedSet<ILicenseFamily> licenseFamilies) {
            this.licenseFamilies = licenseFamilies;
            return this;
        }

        @Override
        public SimpleLicense build() {
            if (matcher == null) {
                throw new ConfigurationException("'matcher' must not be null");
            }
            if (licenseFamilies == null) {
                throw new ImplementationException("'licenseFamilies' must not be null");
            }
            if (StringUtils.isBlank(familyCategory)) {
                throw new ImplementationException("License 'family' must be specified");
            }

            String familyCat = ILicenseFamily.makeCategory(familyCategory);
            Optional<ILicenseFamily> family = licenseFamilies.stream().filter(f ->  f.getFamilyCategory().equals(familyCat)).findFirst();
            if (!family.isPresent()) {
                throw new ConfigurationException(String.format("License family '%s' not found.", familyCategory));
            }

            return new SimpleLicense(family.get(), matcher.build(), String.join(System.lineSeparator(), notes), name, id);
        }
    }
}
