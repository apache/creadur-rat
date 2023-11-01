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

/**
 * The definition of the license family.
 */
public interface ILicenseFamily extends Comparable<ILicenseFamily> {
    /**
     * @return the license family name.
     */
    String getFamilyName();

    /**
     * @return the license family category.
     */
    String getFamilyCategory();

    /**
     * @return A builder for an ILicenseFamily.
     */
    static ILicenseFamily.Builder builder() {
        return new ILicenseFamilyBuilder();
    }

    /**
     * Convert a potential category string into a category string of exactly 5 characters either buy truncating
     * the string or appending spaces as necessary.
     * @param cat the string to convert.
     * @return a string of exactly 5 characters.
     */
    static String makeCategory(String cat) {
        return cat == null ? "     " : cat.concat("     ").substring(0, 5);
    }

    @Override
    default int compareTo(ILicenseFamily other) {
        return getFamilyCategory().compareTo(other.getFamilyCategory());
    }

    /**
     * The definition of an ILicenseFamily builder.
     */
    interface Builder {
        /**
         * Sets the license family category.  Will trim or extends the string with spaces to ensure that it is
         * exactly 5 characters.
         * @param licenseFamilyCategory the category string
         * @return this builder for chaining.
         */
        Builder setLicenseFamilyCategory(String licenseFamilyCategory);

        /**
         * Sets the license family name. 
         * @param licenseFamilyName the name string
         * @return this builder for chaining.
         */
        Builder setLicenseFamilyName(String licenseFamilyName);
        
        /**
         * Gets the category that this builder is building.
         * @return the category that this builder is building.
         */
        String getCategory();

        /**
         * @return a new ILicenseFamily instance.
         */
        ILicenseFamily build();
    }
}
