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
import org.apache.rat.license.ILicenseFamily.Builder;

/**
 * An instance of the ILicenseFamily Builder.
 */
public class ILicenseFamilyBuilder implements Builder {

    private String licenseFamilyCategory;
    private String licenseFamilyName;

    @Override
    public Builder setLicenseFamilyCategory(String licenseFamilyCategory) {
        this.licenseFamilyCategory = licenseFamilyCategory;
        return this;
    }

    @Override
    public String getCategory() {
        return licenseFamilyCategory;
    }

    @Override
    public Builder setLicenseFamilyName(String licenseFamilyName) {
        this.licenseFamilyName = licenseFamilyName;
        return this;
    }

    @Override
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
            public final String getFamilyName() {
                return name;
            }

            @Override
            public String getFamilyCategory() {
                return cat;
            }
        };
    }
}