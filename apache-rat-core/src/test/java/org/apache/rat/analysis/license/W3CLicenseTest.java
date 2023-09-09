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

import org.apache.rat.api.Document;

import org.apache.rat.document.MockLocation;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class W3CLicenseTest extends AbstractMatcherTest {

    private static String W3C_note = "Note that W3C requires a NOTICE. All modifications require notes. See http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231.";
    private static Object[] W3C = { "W3C", "W3C Software Copyright",
            new String[][] {
                    { "fulltext", W3C_note, "http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231" },
                    { "spdx-tab", W3C_note, "SPDX-License-Identifier:\tW3C" },
                    { "spdx-space", W3C_note, "SPDX-License-Identifier: W3C" }, } };

    private static String W3CD_note = "Note that W3CD does not allow modifications. See http://www.w3.org/Consortium/Legal/2002/copyright-documents-20021231.";
    private static Object[] W3CD = { "W3CD", "W3C Document Copyright", new String[][] {
            { "fulltext", W3CD_note, "http://www.w3.org/Consortium/Legal/2002/copyright-documents-20021231" }, } };

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(W3C, W3CD);
    }

    public W3CLicenseTest(String cat, String name, String[][] targets) {
        super(cat, name, targets);
    }

}
