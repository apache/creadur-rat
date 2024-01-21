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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.util.Arrays;

import org.apache.rat.analysis.IHeaderMatcher.State;
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

    private static ILicense MATCHES_NOTHING_MATCHER = mock(ILicense.class);

    static {
            when(MATCHES_NOTHING_MATCHER.matches(any())).thenReturn(State.f);
            when(MATCHES_NOTHING_MATCHER.currentState()).thenReturn(State.f);
            when(MATCHES_NOTHING_MATCHER.finalizeState()).thenReturn(State.f);
    }

    private StringWriter out;
    private SimpleXmlClaimReporter reporter;
    private IDocumentAnalyser analyser;

    @BeforeEach
    public void setUp() throws Exception {
        out = new StringWriter();
        final XmlWriter writer = new XmlWriter(out);
        reporter = new SimpleXmlClaimReporter(writer);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(DefaultLog.INSTANCE, Arrays.asList(MATCHES_NOTHING_MATCHER));
    }

    @Test
    public void standardTypeAnalyser() throws Exception {
        final MonolithicFileDocument document = new MonolithicFileDocument(
                Resources.getResourceFile("/elements/Text.txt"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals( //
                "<resource name='src/test/resources/elements/Text.txt'><header-sample>/*\n"
                        + " * Licensed to the Apache Software Foundation (ASF) under one\n"
                        + " * or more contributor license agreements.  See the NOTICE file\n"
                        + " * distributed with this work for additional information\n"
                        + " * regarding copyright ownership.  The ASF licenses this file\n"
                        + " * to you under the Apache License, Version 2.0 (the \"License\");\n"
                        + " * you may not use this file except in compliance with the License.\n"
                        + " * You may obtain a copy of the License at\n" + " *\n"
                        + " *    http://www.apache.org/licenses/LICENSE-2.0\n" + " *\n"
                        + " * Unless required by applicable law or agreed to in writing,\n"
                        + " * software distributed under the License is distributed on an\n"
                        + " * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n"
                        + " * KIND, either express or implied.  See the License for the\n"
                        + " * specific language governing permissions and limitations\n" + " * under the License.    \n"
                        + " */\n" + "\n" + "            \n"
                        + "</header-sample><header-type name='?????'/><license-family name='Unknown license'/><type name='standard'/>",
                out.toString(), "Open standard element");
    }

    @Test
    public void noteTypeAnalyser() throws Exception {
        final MonolithicFileDocument document = new MonolithicFileDocument(
                Resources.getResourceFile("/elements/LICENSE"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='src/test/resources/elements/LICENSE'><type name='notice'/>",
                out.toString(), "Open note element");
    }

    @Test
    public void binaryTypeAnalyser() throws Exception {
        final MonolithicFileDocument document = new MonolithicFileDocument(
                Resources.getResourceFile("/elements/Image.png"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals(
                "<resource name='src/test/resources/elements/Image.png'><type name='binary'/>", 
                out.toString(), "Open binary element");
    }

    @Test
    public void archiveTypeAnalyser() throws Exception {
        final MonolithicFileDocument document = new MonolithicFileDocument(
                Resources.getResourceFile("/elements/dummy.jar"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals(
                "<resource name='src/test/resources/elements/dummy.jar'><type name='archive'/>", out.toString(), "Open archive element");
    }

    @Test
    public void archiveTypeAnalyserIntelliJ() throws Exception {
        final MonolithicFileDocument document = new MonolithicFileDocument(
                Resources.getResourceFile("/elements/dummy.jar"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals(
                "<resource name='src/test/resources/elements/dummy.jar'><type name='archive'/>", out.toString(), "Open archive element");
    }
}
