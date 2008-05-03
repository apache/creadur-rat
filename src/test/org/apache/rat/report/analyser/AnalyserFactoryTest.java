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
package org.apache.rat.report.analyser;

import java.io.File;
import java.io.StringWriter;

import junit.framework.TestCase;
import rat.document.IDocumentAnalyser;
import rat.document.impl.MonolithicFileDocument;
import rat.report.claim.impl.xml.SimpleXmlClaimReporter;
import rat.report.xml.writer.impl.base.XmlWriter;

public class AnalyserFactoryTest extends TestCase {

    StringWriter out;
    SimpleXmlClaimReporter reporter;
    
    protected void setUp() throws Exception {
        super.setUp();
        out = new StringWriter();
        XmlWriter writer = new XmlWriter(out);
        reporter = new SimpleXmlClaimReporter(writer);
     }

    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testStandardTypeAnalyser() throws Exception {
        MonolithicFileDocument document = new MonolithicFileDocument(new File("src/test/elements/Text.txt"));
        IDocumentAnalyser analyser = DefaultAnalyserFactory.createStandardTypeAnalyser(reporter);
        analyser.analyse(document);
        assertEquals("Open standard element", "<resource name='src/test/elements/Text.txt'><type name='standard'/>", out.toString());
    }

    public void testNoteTypeAnalyser() throws Exception {
        MonolithicFileDocument document = new MonolithicFileDocument(new File("src/test/elements/LICENSE"));
        IDocumentAnalyser analyser = DefaultAnalyserFactory.createNoticeTypeAnalyser(reporter);
        analyser.analyse(document);
        assertEquals("Open note element", "<resource name='src/test/elements/LICENSE'><type name='notice'/>", out.toString());
    }

    public void testBinaryTypeAnalyser() throws Exception {
        MonolithicFileDocument document = new MonolithicFileDocument(new File("src/test/elements/Image.png"));
        IDocumentAnalyser analyser = DefaultAnalyserFactory.createBinaryTypeAnalyser(reporter);
        analyser.analyse(document);
        assertEquals("Open note element", "<resource name='src/test/elements/Image.png'><type name='binary'/>", out.toString());
    }

    public void testArchiveTypeAnalyser() throws Exception {
        MonolithicFileDocument document = new MonolithicFileDocument(new File("src/test/elements/Dummy.jar"));
        IDocumentAnalyser analyser = DefaultAnalyserFactory.createArchiveTypeAnalyser(reporter);
        analyser.analyse(document);
        assertEquals("Open note element", "<resource name='src/test/elements/Dummy.jar'><type name='archive'/>", out.toString());
    }
}
