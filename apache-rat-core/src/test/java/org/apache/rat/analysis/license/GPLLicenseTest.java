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

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests GPL license occurrences within comments and other characters. Works for
 * GPL1 to GPL3.
 */
@RunWith(Parameterized.class)
public class GPLLicenseTest extends AbstractLicenseTest {

    private static Object[] GPL1 = { "GPL1", "GNU General Public License, version 1", new String[][] {
            { "fulltext", "This program is free software; you can redistribute it and/or modify\n "
                    + "it under the terms of the GNU General Public License as published by\n "
                    + "the Free Software Foundation; either version 1, or (at your option)\n " + "any later version." },
            { "spdx-tab", "SPDX-License-Identifier:\tGPL-1.0-only" },
            { "spdx-space", "SPDX-License-Identifier: GPL-1.0-only" }, } };

    private static Object[] GPL2 = { "GPL2", "GNU General Public License, version 2",
            new String[][] {
                    { "fulltext",
                            "This program is free software; you can redistribute it and/or\n"
                                    + "modify it under the terms of the GNU General Public License\n"
                                    + "as published by the Free Software Foundation; either version 2\n"
                                    + "of the License, or (at your option) any later version." },
                    { "spdx-tab", "SPDX-License-Identifier:\tGPL-2.0-only" },
                    { "spdx-space", "SPDX-License-Identifier: GPL-2.0-only" }, } };

    private static Object[] GPL3 = { "GPL3", "GNU General Public License, version 3",
            new String[][] {
                    { "fulltext",
                            "This program is free software: you can redistribute it and/or modify\n"
                                    + "    it under the terms of the GNU General Public License as published by\n"
                                    + "    the Free Software Foundation, either version 3 of the License, or\n"
                                    + "    (at your option) any later version." },
                    { "spdx-tab", "SPDX-License-Identifier:\tGPL-3.0-only" },
                    { "spdx-space", "SPDX-License-Identifier: GPL-3.0-only" }, } };

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(GPL1, GPL2, GPL3);
    }

    public GPLLicenseTest(String cat, String name, String[][] targets) {
        super(cat, name, null, targets);
    }
}
