/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package org.apache.rat.license;

import org.apache.rat.ImplementationException;
import org.apache.rat.testhelpers.TestingMatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ILicenseBuilderTest {
    @Test
    public void testNoFamily() {
        assertThrows(ImplementationException.class, () ->
            ILicense.builder()
                .setId("licId")
                .setMatcher(new TestingMatcher())
                .setName("No Family")
                .setNote("The note")
                .build(), "License 'family'  must be specified");


        assertThrows(ImplementationException.class, () ->
                ILicense.builder()
                .setId("licId")
                .setMatcher(new TestingMatcher())
                .setName("empty Family")
                .setNote("The note")
                .setFamily("")
                .build(), "License 'family'  must be specified");

        assertThrows(ImplementationException.class, () ->
                ILicense.builder()
                .setId("licId")
                .setMatcher(new TestingMatcher())
                .setName("Null family")
                .setNote("The note")
                .setFamily(null)
                .build(), "License 'family'  must be specified");
    }
}
