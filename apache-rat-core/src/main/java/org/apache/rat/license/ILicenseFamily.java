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

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;

/**
 * The definition of the license family.
 */
public interface ILicenseFamily extends Comparable<ILicenseFamily> {

    /** The license family for unknown licenses */
    ILicenseFamily UNKNOWN = new Builder().setLicenseFamilyName("Unknown license").setLicenseFamilyCategory("?????").build();

    /**
     * Gets the family name.
     * @return the license family name.
     */
    String getFamilyName();

    /**
     * Gets the family category.
     * @return the license family category.
     */
    String getFamilyCategory();

    /**
     * Gets the Builder for license families.
     * @return A builder for an ILicenseFamily.
     */
    static ILicenseFamily.Builder builder() {
        return new Builder();
    }

    /**
     * Convert a potential category string into a category string of exactly 5
     * characters either by truncating the string or appending spaces as necessary.
     *
     * @param cat the string to convert.
     * @return a string of exactly 5 characters.
     */
    static String makeCategory(String cat) {
        return cat == null ? "     " : cat.concat("     ").substring(0, Builder.CATEGORY_LENGTH);
    }

    @Override
    default int compareTo(ILicenseFamily other) {
        return getFamilyCategory().compareTo(other.getFamilyCategory());
    }

    /**
     * The definition of an ILicenseFamily builder.
     */
    class Builder {
        /** The maximum length of the category */
        private static final int CATEGORY_LENGTH = 5;
        /** The category for the family */
        private String licenseFamilyCategory;
        /** The name of the family */
        private String licenseFamilyName;

        /**
         * Sets the license family category. Will trim or extend the string with spaces
         * to ensure that it is exactly 5 characters.
         *
         * @param licenseFamilyCategory the category string
         * @return this builder for chaining.
         */
        public Builder setLicenseFamilyCategory(final String licenseFamilyCategory) {
            this.licenseFamilyCategory = licenseFamilyCategory;
            return this;
        }

        /**
         * Sets the license family name.
         *
         * @param licenseFamilyName the name string
         * @return this builder for chaining.
         */
        public Builder setLicenseFamilyName(final String licenseFamilyName) {
            this.licenseFamilyName = licenseFamilyName;
            return this;
        }

        /**
         * Gets the category that this builder is building.
         *
         * @return the category that this builder is building.
         */
        public String getCategory() {
            return licenseFamilyCategory;
        }

        /**
         * Builds the defined license family.
         * @return a new ILicenseFamily instance.
         */
        public ILicenseFamily build() {
            if (StringUtils.isBlank(licenseFamilyCategory)) {
                throw new ConfigurationException("LicenseFamily Category must be specified");
            }
            if (StringUtils.isBlank(licenseFamilyName)) {
                throw new ConfigurationException("LicenseFamily Name must be specified");
            }
            return new ILicenseFamily() {
                private final String cat = ILicenseFamily.makeCategory(licenseFamilyCategory);
                private final String name = licenseFamilyName;
                @Override
                public String toString() {
                    return String.format("%s %s", getFamilyCategory(), getFamilyName());
                }

                @Override
                public String getFamilyName() {
                    return name;
                }

                @Override
                public String getFamilyCategory() {
                    return cat;
                }
            };
        }
    }
}
