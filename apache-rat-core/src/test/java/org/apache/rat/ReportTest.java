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
package org.apache.rat;

import org.apache.rat.analysis.util.HeaderMatcherMultiplexer;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.walker.DirectoryWalker;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReportTest {
    private static final String NL = System.getProperty("line.separator");
    private static final String PARAGRAPH = "*****************************************************";
    private static final String FILE_PARAGRAPH = "=====================================================";

    private static final String HEADER =
            NL +
                    PARAGRAPH + NL +//
                    "Summary" + NL +//
                    "-------" + NL +//
                    "Generated at: ";

    private static String getElementsReports(String pElementsPath) {
        return
                NL + "Notes: 2" + NL +//
                        "Binaries: 2" + NL +//
                        "Archives: 1" + NL +//
                        "Standards: 7" + NL +//
                        "" + NL +//
                        "Apache Licensed: 4" + NL +//
                        "Generated Documents: 0" + NL +//
                        "" + NL +//
                        "JavaDocs are generated, thus a license header is optional." + NL +//
                        "Generated files do not require license headers." + NL +//
                        "" + NL +//
                        "2 Unknown Licenses" + NL +//
                        "" + NL +//
                        PARAGRAPH + NL +//
                        "" + NL +//
                        "Files with unapproved licenses:" + NL +//
                        "" + NL +//
                        "  " + pElementsPath + "/Source.java" + NL +//
                        "  " + pElementsPath + "/sub/Empty.txt" + NL +//
                        "" + NL +//
                        PARAGRAPH + NL +//
                        "" + NL +//
                        "Archives:" + NL +//
                        "" + NL +//
                        " + " + pElementsPath + "/dummy.jar" + NL +//
                        " " + NL +//
                        PARAGRAPH + NL +//
                        "  Files with Apache License headers will be marked AL" + NL +//
                        "  Binary files (which do not require any license headers) will be marked B" + NL +//
                        "  Compressed archives will be marked A" + NL +//
                        "  Notices, licenses etc. will be marked N" + NL +//
                        "  MIT   " + pElementsPath + "/ILoggerFactory.java" + NL +//
                        "  B     " + pElementsPath + "/Image.png" + NL +//
                        "  N     " + pElementsPath + "/LICENSE" + NL +//
                        "  N     " + pElementsPath + "/NOTICE" + NL +//
                        " !????? " + pElementsPath + "/Source.java" + NL +//
                        "  AL    " + pElementsPath + "/Text.txt" + NL +//
                        "  AL    " + pElementsPath + "/TextHttps.txt" + NL +//
                        "  AL    " + pElementsPath + "/Xml.xml" + NL +//
                        "  AL    " + pElementsPath + "/buildr.rb" + NL +//
                        "  A     " + pElementsPath + "/dummy.jar" + NL +//
                        "  B     " + pElementsPath + "/plain.json" + NL +//
                        " !????? " + pElementsPath + "/sub/Empty.txt" + NL +//
                        " " + NL +//
                        PARAGRAPH + NL +//
                        NL +//
                        " Printing headers for text files without a valid license header..." + NL +//
                        " " + NL +//
                        FILE_PARAGRAPH + NL +//
                        "== File: " + pElementsPath + "/Source.java" + NL +//
                        FILE_PARAGRAPH + NL + //
                        "package elements;" + NL +//
                        "" + NL +//
                        "/*" + NL +//
                        " * This file does intentionally *NOT* contain an AL license header," + NL +//
                        " * because it is used in the test suite." + NL +//
                        " */" + NL +//
                        "public class Source {" + NL +//
                        "" + NL +//
                        "}" + NL +//
                        "" + NL +//
                        FILE_PARAGRAPH + NL +//
                        "== File: " + pElementsPath + "/sub/Empty.txt" + NL +//
                        FILE_PARAGRAPH + NL +//
                        NL;
    }

    @Test
    public void plainReportWithArchivesAndUnapprovedLicenses() throws Exception {
        StringWriter out = new StringWriter();
        HeaderMatcherMultiplexer matcherMultiplexer = new HeaderMatcherMultiplexer(Defaults.DEFAULT_MATCHERS);
        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setApproveDefaultLicenses(true);
        configuration.setHeaderMatcher(matcherMultiplexer);
        Report.report(out, new DirectoryWalker(new File(elementsPath)),
                Defaults.getPlainStyleSheet(), configuration);

        String result = out.getBuffer().toString();
        assertTrue("'Generated at' is present in " + result,
                result.startsWith(HEADER));

        final int generatedAtLineEnd = result.indexOf(NL, HEADER.length());

        final String elementsReports = getElementsReports(elementsPath);
        assertEquals("Report created was: " + result,
                elementsReports,
                result.substring(generatedAtLineEnd + NL.length()));
    }

    @Test
    public void parseExclusionsForCLIUsage() throws IOException {
        final FilenameFilter filter = Report.parseExclusions(Arrays.asList("", " # foo/bar", "foo", "##", " ./foo/bar"));
        assertNotNull(filter);
    }
}
