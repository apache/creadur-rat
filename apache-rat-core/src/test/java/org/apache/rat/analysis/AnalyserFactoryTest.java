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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;

import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.impl.MonolithicFileDocument;
import org.apache.rat.license.ILicense;
import org.apache.rat.report.claim.impl.xml.SimpleXmlClaimReporter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AnalyserFactoryTest {

    private static ILicense MATCHES_NOTHING_MATCHER = UnknownLicense.INSTANCE;

    private StringWriter out;
    private SimpleXmlClaimReporter reporter;
    private IDocumentAnalyser analyser;

    @BeforeEach
    public void setUp() throws Exception {
        out = new StringWriter();
        reporter = new SimpleXmlClaimReporter(new XmlWriter(out));
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(DefaultLog.INSTANCE,
                Arrays.asList(MATCHES_NOTHING_MATCHER));
    }

    @Test
    public void standardTypeAnalyser() throws Exception {
        String[] expected = {
                "<resource name='src/test/resources/elements/Text.txt' type='STANDARD'>"
                        + "<license id='?????' name='Unknown license' approval='false' family='?????'/>"
                        + "<sample><![CDATA[ /*", //
                " * Licensed to the Apache Software Foundation (ASF) under one", //
                " * or more contributor license agreements.  See the NOTICE file", //
                " * distributed with this work for additional information", //
                " * regarding copyright ownership.  The ASF licenses this file", //
                " * to you under the Apache License, Version 2.0 (the \"License\");", //
                " * you may not use this file except in compliance with the License.", //
                " * You may obtain a copy of the License at", //
                " *    http://www.apache.org/licenses/LICENSE-2.0", //
                " * Unless required by applicable law or agreed to in writing,", //
                " * software distributed under the License is distributed on an", //
                " * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY", //
                " * KIND, either express or implied.  See the License for the", //
                " * specific language governing permissions and limitations", //
                " * under the License.", //
                " ]]></sample></resource>" };
        
        final MonolithicFileDocument document = new MonolithicFileDocument(
                Resources.getResourceFile("/elements/Text.txt"));
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        for (String exp : expected ) { 
            assertTrue(result.contains(exp), () -> exp);
        }
    }

    @Test
    public void noteTypeAnalyser() throws Exception {
        final MonolithicFileDocument document = new MonolithicFileDocument(
                Resources.getResourceFile("/elements/LICENSE"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='src/test/resources/elements/LICENSE' type='NOTICE'/>", out.toString(),
                "Open note element");
    }

    @Test
    public void binaryTypeAnalyser() throws Exception {
        final MonolithicFileDocument document = new MonolithicFileDocument(
                Resources.getResourceFile("/elements/Image.png"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='src/test/resources/elements/Image.png' type='BINARY'/>", out.toString(),
                "Open binary element");
    }

    @Test
    public void archiveTypeAnalyser() throws Exception {
        final MonolithicFileDocument document = new MonolithicFileDocument(
                Resources.getResourceFile("/elements/dummy.jar"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='src/test/resources/elements/dummy.jar' type='ARCHIVE'/>", out.toString(),
                "Open archive element");
    }

    @Test
    public void archiveTypeAnalyserIntelliJ() throws Exception {
        final MonolithicFileDocument document = new MonolithicFileDocument(
                Resources.getResourceFile("/elements/dummy.jar"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='src/test/resources/elements/dummy.jar' type='ARCHIVE'/>", out.toString(),
                "Open archive element");
    }
}
