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

import org.apache.commons.io.IOUtils;
import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;
import org.apache.rat.test.utils.Resources;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AppliedApacheSoftwareLicense20Test {

    private static final String HEADER
            = "/*\n"
            + " *  Copyright 2012-2013 FooBar.\n"
            + " *\n"
            + " *  Licensed under the Apache License, Version 2.0 (the \"License\");\n"
            + " *  you may not use this file except in compliance with the License.\n"
            + " *\n"
            + " *  You may obtain a copy of the License at\n"
            + " *       http://www.apache.org/licenses/LICENSE-2.0\n"
            + " *\n"
            + " *  Unless required by applicable law or agreed to in writing, software\n"
            + " *  distributed under the License is distributed on an \"AS IS\" BASIS,\n"
            + " *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
            + " *  See the License for the specific language governing permissions and\n"
            + " *  limitations under the License.\n"
            + " */\n";

    private AppliedApacheSoftwareLicense20 license;

    private MockClaimReporter reporter;

    @Before
    public void setUp() throws Exception {
        license = new AppliedApacheSoftwareLicense20("FooBar");
        reporter = new MockClaimReporter();
    }

    @Test
    public void match() throws Exception {
        BufferedReader in = new BufferedReader(new StringReader(HEADER));
        String line = in.readLine();
        boolean result = false;
        final Document subject = new MockLocation("subject");
        while (line != null) {
            result = license.match(subject, line);
            line = in.readLine();
        }
        assertTrue("Applied AL2.0 license should be matched", result);
        license.reset();
        result = license.match(subject, "New line");
        assertFalse("After reset, content should build up again", result);
    }

    @Test
    public void noMatch() throws Exception {
        BufferedReader in = null;
        try {
            in = Resources.getBufferedResourceReader("elements/Source.java");
            String line = in.readLine();
            boolean result = false;
            final Document subject = new MockLocation("subject");
            while (line != null) {
                result = license.match(subject, line);
                line = in.readLine();
            }
            assertFalse("Applied AL2.0 license should not be matched", result);
            license.reset();
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Test(timeout = 2000) // may need to be adjusted if many more files are added
    public void goodFiles() throws Exception {
        DirectoryScanner.testFilesInDir("appliedAL20/good", license, true);
    }

    @Test(timeout = 2000) // may need to be adjusted if many more files are added
    public void baddFiles() throws Exception {
        DirectoryScanner.testFilesInDir("appliedAL20/bad", license, false);
    }

}
