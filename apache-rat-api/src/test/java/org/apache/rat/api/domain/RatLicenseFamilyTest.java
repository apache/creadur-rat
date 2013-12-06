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

import static org.apache.rat.api.domain.RatLicenseFamily.W3C;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RatLicenseFamilyTest {

    @Test
    public void testW3CLicenseFamilyCategory() {
        assertThat(W3C.getCategory(), is("W3C  "));
    }

    @Test
    public void testW3CLicenseFamilyName() {
        assertThat(W3C.getName(), is("W3C Software Copyright"));
    }

    @Test
    public void testW3CLicenseFamilyNotes() {
        assertThat(W3C.getNotes(), is(""));
    }
}
