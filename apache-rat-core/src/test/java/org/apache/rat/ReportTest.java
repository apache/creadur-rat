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
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReportTest {
    private static final String HEADER =
            "\n" + 
            "*****************************************************\n" + 
            "Summary\n" + 
            "-------\n" + 
            "Generated at: ";

    private static String getElementsReports(String pElementsPath) {
        return
            "Notes: 2\n" + 
            "Binaries: 1\n" + 
            "Archives: 1\n" + 
            "Standards: 6\n" + 
            "\n" + 
            "Apache Licensed: 3\n" + 
            "Generated Documents: 0\n" + 
            "\n" + 
            "JavaDocs are generated and so license header is optional\n" + 
            "Generated files do not required license headers\n" + 
            "\n" + 
            "2 Unknown Licenses\n" + 
            "\n" + 
            "*******************************\n" + 
            "\n" + 
            "Unapproved licenses:\n" + 
            "\n" +
            "  " + pElementsPath + "/Source.java\n" +
            "  " + pElementsPath + "/sub/Empty.txt\n" +
            "\n" +
            "*******************************\n" + 
            "\n" + 
            "Archives:\n" + 
            "\n" + 
            " + " + pElementsPath + "/dummy.jar\n" + 
            " \n" + 
            "*****************************************************\n" + 
            "  Files with Apache License headers will be marked AL\n" + 
            "  Binary files (which do not require AL headers) will be marked B\n" + 
            "  Compressed archives will be marked A\n" + 
            "  Notices, licenses etc will be marked N\n" + 
            "  MIT   " + pElementsPath + "/ILoggerFactory.java\n" + 
            "  B     " + pElementsPath + "/Image.png\n" + 
            "  N     " + pElementsPath + "/LICENSE\n" + 
            "  N     " + pElementsPath + "/NOTICE\n" + 
            " !????? " + pElementsPath + "/Source.java\n" + 
            "  AL    " + pElementsPath + "/Text.txt\n" + 
            "  AL    " + pElementsPath + "/Xml.xml\n" + 
            "  AL    " + pElementsPath + "/buildr.rb\n" + 
            "  A     " + pElementsPath + "/dummy.jar\n" + 
            " !????? " + pElementsPath + "/sub/Empty.txt\n" + 
            " \n" + 
            "*****************************************************\n" + 
            " Printing headers for files without AL header...\n" + 
            " \n" + 
            " \n" + 
            "=======================================================================\n" + 
            "==" + pElementsPath + "/Source.java\n" + 
            "=======================================================================\n" + 
            "package elements;\n" + 
            "\n" +
            "/*\n" +
            " * This file does intentionally *NOT* contain an AL license header,\n" +
            " * because it is used in the test suite.\n" +
            " */\n" +
            "public class Source {\n" + 
            "\n" + 
            "}\n" + 
            "\n" + 
            "=======================================================================\n" + 
            "==" + pElementsPath + "/sub/Empty.txt\n" + 
            "=======================================================================\n" + 
            "\n";
    }
    
    @Test
    public void plainReport() throws Exception {
        StringWriter out = new StringWriter();
        HeaderMatcherMultiplexer matcherMultiplexer = new HeaderMatcherMultiplexer(Defaults.DEFAULT_MATCHERS);
        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setHeaderMatcher(matcherMultiplexer);
        Report.report(out, new DirectoryWalker(new File(elementsPath)),
                Defaults.getPlainStyleSheet(), configuration);
        String result = out.getBuffer().toString();
        final String nl = System.getProperty("line.separator");
        assertTrue("'Generated at' is present in " + result,
                   result.startsWith(HEADER.replaceAll("\n", nl)));
        final int generatedAtLineEnd = result.indexOf(nl, HEADER.length());
        final String elementsReports = getElementsReports(elementsPath);
        assertEquals("Report created",
                     elementsReports.replaceAll("\n", nl),
                     result.substring(generatedAtLineEnd + nl.length()));
    }
}
