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
package org.apache.rat.analysis;

import org.apache.rat.api.Document;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.impl.MonolithicFileDocument;
import org.apache.rat.report.claim.impl.xml.SimpleXmlClaimReporter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class AnalyserFactoryTest {

    StringWriter out;
    SimpleXmlClaimReporter reporter;
    IHeaderMatcher matcherStub;

    @Before
    public void setUp() throws Exception {
        out = new StringWriter();
        XmlWriter writer = new XmlWriter(out);
        reporter = new SimpleXmlClaimReporter(writer);
        matcherStub = new IHeaderMatcher() {

            public boolean match(Document subject, String line) throws RatHeaderAnalysisException {
                return false;
            }

            public void reset() {
            }            
        };
     }

    @Test
    public void standardTypeAnalyser() throws Exception {
        MonolithicFileDocument document = new MonolithicFileDocument(new File("src/test/resources/elements/Text.txt"));
        IDocumentAnalyser analyser = DefaultAnalyserFactory.createDefaultAnalyser(matcherStub);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("Open standard element", "<resource name='src/test/resources/elements/Text.txt'><header-sample>/*\n" +
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
                "</header-sample><header-type name='?????'/><license-family name='?????'/><type name='standard'/>", out.toString());
    }

    @Test
    public void noteTypeAnalyser() throws Exception {
        MonolithicFileDocument document = new MonolithicFileDocument(new File("src/test/elements/LICENSE"));
        IDocumentAnalyser analyser = DefaultAnalyserFactory.createDefaultAnalyser(matcherStub);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("Open note element", "<resource name='src/test/elements/LICENSE'><type name='notice'/>", out.toString());
    }

    @Test
    public void binaryTypeAnalyser() throws Exception {
        MonolithicFileDocument document = new MonolithicFileDocument(new File("src/test/elements/Image.png"));
        IDocumentAnalyser analyser = DefaultAnalyserFactory.createDefaultAnalyser(matcherStub);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("Open binary element", "<resource name='src/test/elements/Image.png'><type name='binary'/>", out.toString());
    }

    @Test
    public void archiveTypeAnalyser() throws Exception {
        MonolithicFileDocument document = new MonolithicFileDocument(new File("src/test/elements/Dummy.jar"));
        IDocumentAnalyser analyser = DefaultAnalyserFactory.createDefaultAnalyser(matcherStub);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("Open archive element", "<resource name='src/test/elements/Dummy.jar'><type name='archive'/>", out.toString());
    }
}
