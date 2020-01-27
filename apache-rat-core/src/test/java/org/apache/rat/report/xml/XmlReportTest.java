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
package org.apache.rat.report.xml;

import org.apache.rat.analysis.DefaultAnalyserFactory;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.report.AbstractReport;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.impl.xml.SimpleXmlClaimReporter;
import org.apache.rat.report.claim.util.ClaimReporterMultiplexer;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.walker.DirectoryWalker;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

public class XmlReportTest {

    private static final Pattern IGNORE = Pattern.compile(".svn");
    private StringWriter out;
    private IXmlWriter writer;
    private RatReport report;
    
    @Before
    public void setUp() throws Exception {
        out = new StringWriter();
        writer = new XmlWriter(out);
        writer.startDocument();
        final SimpleXmlClaimReporter reporter = new SimpleXmlClaimReporter(writer);
        final IHeaderMatcher matcher = new IHeaderMatcher() {

            public boolean match(Document subject, String line) throws RatHeaderAnalysisException {
                return false;
            }

            public void reset() {
            }            
        };
        IDocumentAnalyser analyser = DefaultAnalyserFactory.createDefaultAnalyser(matcher);
        final List<AbstractReport> reporters = new ArrayList<>();
        reporters.add(reporter);
        report = new ClaimReporterMultiplexer(analyser, reporters); 
    }

    private void report(DirectoryWalker directory) throws Exception {
        directory.run(report);
    }
    
    @Test
    public void baseReport() throws Exception {
        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        DirectoryWalker directory = new DirectoryWalker(new File(elementsPath), IGNORE);
        report.startReport();
        report(directory);
        report.endReport();
        writer.closeDocument();
        final String output = out.toString();
        assertTrue("Preamble and document element are OK",
                   output.startsWith("<?xml version='1.0'?>" +
                "<rat-report timestamp="));
        assertTrue(output+"Part after timestamp attribute is OK",
                   output.endsWith(">" +
                    "<resource name='" + elementsPath + "/Image.png'><type name='binary'/></resource>" +
                    "<resource name='" + elementsPath + "/LICENSE'><type name='notice'/></resource>" +
                    "<resource name='" + elementsPath + "/NOTICE'><type name='notice'/></resource>" +
                    "<resource name='" + elementsPath + "/Source.java'><header-sample>package elements;\n" +
"\n" +
"/*\n" +
" * This file does intentionally *NOT* contain an AL license header,\n" +
" * because it is used in the test suite.\n" +
" */\n" +
"public class Source {\n" +
"\n" +
"}\n" + "</header-sample><header-type name='?????'/><license-family name='?????'/><type name='standard'/></resource>" +
                    "<resource name='" + elementsPath + "/Text.txt'><header-sample>/*\n" +
" * Licensed to the Apache Software Foundation (ASF) under one\n" +
" * or more contributor license agreements.  See the NOTICE file\n" +
" * distributed with this work for additional information\n" +
" * regarding copyright ownership.  The ASF licenses this file\n" +
" * to you under the Apache License, Version 2.0 (the \"License\");\n" +
" * you may not use this file except in compliance with the License.\n" +
" * You may obtain a copy of the License at\n" +
" *\n" +
" *    http://www.apache.org/licenses/LICENSE-2.0\n" +
" *\n" +
" * Unless required by applicable law or agreed to in writing,\n" +
" * software distributed under the License is distributed on an\n" +
" * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
" * KIND, either express or implied.  See the License for the\n" +
" * specific language governing permissions and limitations\n" +
" * under the License.    \n" +
" */\n" +
"\n" +
"            \n" +
"</header-sample><header-type name='?????'/><license-family name='?????'/><type name='standard'/></resource>" +
"<resource name='" + elementsPath + "/TextHttps.txt'><header-sample>/*\n" +
" * Licensed to the Apache Software Foundation (ASF) under one\n" +
" * or more contributor license agreements.  See the NOTICE file\n" +
" * distributed with this work for additional information\n" +
" * regarding copyright ownership.  The ASF licenses this file\n" +
" * to you under the Apache License, Version 2.0 (the \"License\");\n" +
" * you may not use this file except in compliance with the License.\n" +
" * You may obtain a copy of the License at\n" +
" *\n" +
" *    https://www.apache.org/licenses/LICENSE-2.0.txt\n" +
" *\n" +
" * Unless required by applicable law or agreed to in writing,\n" +
" * software distributed under the License is distributed on an\n" +
" * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
" * KIND, either express or implied.  See the License for the\n" +
" * specific language governing permissions and limitations\n" +
" * under the License.    \n" +
" */\n" +
"\n" +
"            \n" +
"</header-sample><header-type name='?????'/><license-family name='?????'/><type name='standard'/></resource>" +
                    "<resource name='" + elementsPath + "/Xml.xml'><header-sample>&lt;?xml version='1.0'?&gt;\n" +
"&lt;!--\n" +
" Licensed to the Apache Software Foundation (ASF) under one   *\n" +
" or more contributor license agreements.  See the NOTICE file *\n" +
" distributed with this work for additional information        *\n" +
" regarding copyright ownership.  The ASF licenses this file   *\n" +
" to you under the Apache License, Version 2.0 (the            *\n" +
" \"License\"); you may not use this file except in compliance   *\n" +
" with the License.  You may obtain a copy of the License at   *\n" +
"                                                              *\n" +
"   http://www.apache.org/licenses/LICENSE-2.0                 *\n" +
"                                                              *\n" +
" Unless required by applicable law or agreed to in writing,   *\n" +
" software distributed under the License is distributed on an  *\n" +
" \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *\n" +
" KIND, either express or implied.  See the License for the    *\n" +
" specific language governing permissions and limitations      *\n" +
" under the License.                                           *\n" +
"--&gt;\n" +
"&lt;document/&gt;\n" +
"</header-sample><header-type name='?????'/><license-family name='?????'/><type name='standard'/></resource>" +
                    "<resource name='" + elementsPath + "/buildr.rb'><header-sample># Licensed to the Apache Software Foundation (ASF) under one or more\n" +
"# contributor license agreements.  See the NOTICE file distributed with this\n" +
"# work for additional information regarding copyright ownership.  The ASF\n" +
"# licenses this file to you under the Apache License, Version 2.0 (the\n" +
"# \"License\"); you may not use this file except in compliance with the License.\n" +
"# You may obtain a copy of the License at\n" +
"#\n" +
"#    http://www.apache.org/licenses/LICENSE-2.0\n" +
"#\n" +
"# Unless required by applicable law or agreed to in writing, software\n" +
"# distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT\n" +
"# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the\n" +
"# License for the specific language governing permissions and limitations under\n" +
"# the License.\n" +
"\n" +
"unless defined?(Buildr::VERSION)\n" +
"  require 'buildr/version'\n" +
"end\n" +
"\n" +
"require 'buildr/core'\n" +
"require 'buildr/packaging'\n" +
"require 'buildr/java'\n" +
"require 'buildr/ide'\n" +
"require 'buildr/shell'\n" +
"require 'buildr/run'\n" +
"\n" +
"# Methods defined in Buildr are both instance methods (e.g. when included in Project)\n" +
"# and class methods when invoked like Buildr.artifacts().\n" +
"module Buildr ; extend self ; end\n" +
"\n" +
"# The Buildfile object (self) has access to all the Buildr methods and constants.\n" +
"class &lt;&lt; self ; include Buildr ; end\n" +
"\n" +
"# All modules defined under Buildr::* can be referenced without Buildr:: prefix\n" +
"# unless a conflict exists (e.g.  Buildr::RSpec vs ::RSpec)\n" +
"class Object #:nodoc:\n" +
"  Buildr.constants.each do |name|\n" +
"    const = Buildr.const_get(name)\n" +
"    if const.is_a?(Module)\n" +
"      const_set name, const unless const_defined?(name)\n" +
"    end\n" +
"  end\n" +
"end\n" +
"\n" +
                "</header-sample><header-type name='?????'/><license-family name='?????'/><type name='standard'/></resource>" +
                           "<resource name='" + elementsPath + "/dummy.jar'><type name='archive'/></resource>" +
                           "<resource name='" + elementsPath + "/plain.json'><type name='binary'/></resource>" +
                    "<resource name='" + elementsPath + "/sub/Empty.txt'><header-sample>\n</header-sample><header-type name='?????'/><license-family name='?????'/><type name='standard'/></resource>" +
                    "</rat-report>"));
        assertTrue("Is well formed", XmlUtils.isWellFormedXml(output));
    }

}
