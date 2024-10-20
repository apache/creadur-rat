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
package org.apache.rat.analysis.license;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

public class IllumosLicenseTest extends AbstractLicenseTest {
    private static final String id = "ILLUMOS";
    private static final String note = "Modified CDDL1 license";
    private static final String[][] targets = { { "illumos",
            "The contents of this file are subject to the terms of the "
                    + "Common Development and Distribution License (the \"License\") "
                    + "You may not use this file except in compliance with the License. " } };

    public static Stream<Arguments> parameterProvider() {
        return Stream.of(Arguments.of(id, CDDL1LicenseTest.id, CDDL1LicenseTest.name, note, targets));
    }
}
