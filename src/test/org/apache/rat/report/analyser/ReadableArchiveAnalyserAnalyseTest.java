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
import rat.document.impl.zip.ZipFileDocument;
import rat.report.claim.impl.xml.SimpleXmlClaimReporter;
import rat.report.xml.writer.impl.base.XmlWriter;

public class ReadableArchiveAnalyserAnalyseTest extends TestCase {

    StringWriter out;
    ReadableArchiveAnalyser analyser;
    
    protected void setUp() throws Exception {
        super.setUp();
        out = new StringWriter();
        XmlWriter writer = new XmlWriter(out);
        analyser = new ReadableArchiveAnalyser(new SimpleXmlClaimReporter(writer));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testVisitReadableArchive() throws Exception {
        String name = "src/test/elements/dummy.jar";
        ZipFileDocument document = new ZipFileDocument(new File(name));
        analyser.analyse(document);
        assertEquals("Readable attribute set",
                     "<resource name='"
                     + name.replace('/', File.separatorChar)
                     + "'><archive-type name='readable'/>", out.toString());
    }

    public void testVisitUnreadableArchive() throws Exception {
        String name = "src/test/artifacts/dummy.tar.gz";
        ZipFileDocument document = new ZipFileDocument((new File(name)));
        analyser.analyse(document);
        assertEquals("Readable attribute unset",
                     "<resource name='"
                     + name.replace('/', File.separatorChar)
                     + "'><archive-type name='unreadable'/>", out.toString());

    }


}
