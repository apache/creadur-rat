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
package rat;

import java.io.File;
import java.io.StringWriter;

import junit.framework.TestCase;
import rat.analysis.util.HeaderMatcherMultiplexer;

public class ReportTest extends TestCase {

    private static final String ELEMENTS_REPORTS = 
            "\n" + 
            "*****************************************************\n" + 
            "Summary\n" + 
            "-------\n" + 
            "Notes: 2\n" + 
            "Binaries: 1\n" + 
            "Archives: 1\n" + 
            "Standards: 4\n" + 
            "\n" + 
            "Apache Licensed: 2\n" + 
            "Generated Documents: 0\n" + 
            "\n" + 
            "JavaDocs are generated and so license header is optional\n" + 
            "Generated files do not required license headers\n" + 
            "\n" + 
            "2 Unknown Licenses\n" + 
            "\n" + 
            "*******************************\n" + 
            "\n" + 
            "Archives (+ indicates readable, $ unreadable): \n" + 
            "\n" + 
            " + src/test/elements/dummy.jar\n" + 
            " \n" + 
            "*****************************************************\n" + 
            "  Files with AL headers will be marked L\n" + 
            "  Binary files (which do not require AL headers) will be marked B\n" + 
            "  Compressed archives will be marked A\n" + 
            "  Notices, licenses etc will be marked N\n" + 
            "  B     src/test/elements/Image.png\n" + 
            "  N     src/test/elements/LICENSE\n" + 
            "  N     src/test/elements/NOTICE\n" + 
            " !????? src/test/elements/Source.java\n" + 
            "  AL    src/test/elements/Text.txt\n" + 
            "  AL    src/test/elements/Xml.xml\n" + 
            "  A     src/test/elements/dummy.jar\n" + 
            " !????? src/test/elements/sub/Empty.txt\n" + 
            " \n" + 
            " *****************************************************\n" + 
            " Printing headers for files without AL header...\n" + 
            " \n" + 
            " \n" + 
            " =======================================================================\n" + 
            " ==src/test/elements/Source.java\n" + 
            " =======================================================================\n" + 
            " package elements;\n" + 
            "\n" + 
            "public class Source {\n" + 
            "\n" + 
            "}\n" + 
            "\n" + 
            " =======================================================================\n" + 
            " ==src/test/elements/sub/Empty.txt\n" + 
            " =======================================================================\n" + 
            " ";
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPlainReport() throws Exception {
        StringWriter out = new StringWriter();
        HeaderMatcherMultiplexer matcherMultiplexer = new HeaderMatcherMultiplexer(Defaults.DEFAULT_MATCHERS);
        Report.report(out, new DirectoryWalker(new File("src/test/elements")),
                Defaults.getPlainStyleSheet(), matcherMultiplexer, null);
        String result = out.getBuffer().toString();
        assertEquals("Report created",
                     ELEMENTS_REPORTS.replaceAll("\n",
                                                 System.getProperty("line.separator")),
                     result);
    }
}
