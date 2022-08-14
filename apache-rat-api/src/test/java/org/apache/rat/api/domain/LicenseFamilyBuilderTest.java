/*
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class LicenseFamilyBuilderTest {

    @Test
    public void testBuilderBuilds() {
        final LicenseFamily family =
                LicenseFamilyBuilder.aLicenseFamily().build();
        assertNotNull("Builder should build", family);
    }

    @Test
    public void testWithNotes() {
        final String someNotes = "Some notes";
        final LicenseFamily family =
                LicenseFamilyBuilder.aLicenseFamily().withNotes(someNotes)
                        .build();
        assertThat(family.getNotes(), is(someNotes));
    }

    @Test
    public void testWithCategory() {
        final String someCategory = "Some license category";
        final LicenseFamily family =
                LicenseFamilyBuilder.aLicenseFamily()
                        .withCategory(someCategory).build();
        assertThat(family.getCategory(), is(someCategory));
    }

    @Test
    public void testWithName() {
        final String someName = "A name for a license";
        final LicenseFamily family =
                LicenseFamilyBuilder.aLicenseFamily().withName(someName)
                        .build();
        assertThat(family.getName(), is(someName));

    }
}
