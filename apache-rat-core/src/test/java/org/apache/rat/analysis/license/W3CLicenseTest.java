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

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;


public class W3CLicenseTest extends AbstractLicenseTest {

    private static String W3C_note = "Note that W3C requires a NOTICE.\n" + "All modifications require notes.\n"
            + "See http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231.";
    private static Arguments W3C = Arguments.of( "W3C", "W3C", "W3C Software Copyright", W3C_note,
            new String[][] { { "fulltext", "http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231" },
                    { "spdx-tab", "SPDX-License-Identifier:\tW3C" },
                    { "spdx-space", "SPDX-License-Identifier: W3C" }, } );

    private static String W3CD_note = "Note that W3CD does not allow modifications.\n"
            + "See http://www.w3.org/Consortium/Legal/2002/copyright-documents-20021231.";
    private static Arguments W3CD = Arguments.of("W3CD", "W3CD","W3C Document Copyright", W3CD_note, new String[][] {
            { "fulltext", "http://www.w3.org/Consortium/Legal/2002/copyright-documents-20021231" }, } );

    public static Stream<Arguments> parameterProvider() {
        return Stream.of(W3C, W3CD);
    }
    
}
