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

public class AppliedApacheSoftwareLicenseTest extends AbstractLicenseTest {

    private static String ID = "ASL";
    private static String NAME = "Applied Apache License Version 2.0";
    private static String[][] targets = { { "simple", "/*\n" + " *  Copyright 2012-2013 FooBar.\n" + " *\n"
            + " *  Licensed under the Apache License, Version 2.0 (the \"License\");\n"
            + " *  you may not use this file except in compliance with the License.\n" + " *\n"
            + " *  You may obtain a copy of the License at\n" + " *       http://www.apache.org/licenses/LICENSE-2.0\n"
            + " *\n" + " *  Unless required by applicable law or agreed to in writing, software\n"
            + " *  distributed under the License is distributed on an \"AS IS\" BASIS,\n"
            + " *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
            + " *  See the License for the specific language governing permissions and\n"
            + " *  limitations under the License.\n" + " */\n" } };

    public static Stream<Arguments> parameterProvider() {
        return Stream.of(Arguments.of(ID, ApacheSoftwareLicenseTest.id, ApacheSoftwareLicenseTest.name, null, targets));
    }

    /*
      
    
    @Test(timeout = 2000) // may need to be adjusted if many more files are added
    public void goodFiles() throws Exception {
        DirectoryScanner.testFilesInDir("appliedAL20/good", license, true);
    }
    
    @Test(timeout = 2000) // may need to be adjusted if many more files are added
    public void baddFiles() throws Exception {
        DirectoryScanner.testFilesInDir("appliedAL20/bad", license, false);
    }
    */

}
