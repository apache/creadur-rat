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

public class OASISLicenseTest extends AbstractLicenseTest {

    private static String id = "OASIS";
    private static String name = "OASIS Open License";
    private static String notes = "No modifications allowed";
    private static String[][] targets = { { "fulltext",
            "This document and translations of it may be copied and furnished to "
                    + "others and derivative works that comment on or otherwise explain it or assist "
                    + "in its implementation may be prepared copied published and distributed"
                    + "\\nCopyright OASIS Open, 1999" }, };

    public static Stream<Arguments> parameterProvider() {
        return Stream.of(Arguments.of(id, id, name, notes, targets));
    }
    /*
    @Test(timeout = 2000) // may need to be adjusted if many more files are added
    public void goodFiles() throws Exception {
        DirectoryScanner.testFilesInDir("oasis/good", license, true);
    }
    
    @Test(timeout = 2000) // may need to be adjusted if many more files are added
    public void baddFiles() throws Exception {
        DirectoryScanner.testFilesInDir("oasis/bad", license, false);
    }
    */
}
