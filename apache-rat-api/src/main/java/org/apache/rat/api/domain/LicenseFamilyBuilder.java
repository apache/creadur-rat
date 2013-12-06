/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.api.domain;

public final class LicenseFamilyBuilder {

    public static LicenseFamilyBuilder aLicenseFamily() {

        return new LicenseFamilyBuilder();
    }

    private String notes;
    private String category;
    private String name;

    private LicenseFamilyBuilder() {
    }

    public LicenseFamilyBuilder withNotes(final String notes) {
        this.notes = notes;
        return this;
    }

    public LicenseFamily build() {
        return new LicenseFamily(this.notes, this.category, this.name);
    }

    public LicenseFamilyBuilder withCategory(final String category) {
        this.category = category;
        return this;
    }

    public LicenseFamilyBuilder withName(final String name) {
        this.name = name;
        return this;
    }
}
