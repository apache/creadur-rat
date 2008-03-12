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
package rat.report.xml;

import java.io.File;
import java.io.StringWriter;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import rat.DirectoryWalker;
import rat.analysis.MockLicenseMatcher;
import rat.report.RatReport;
import rat.report.xml.writer.IXmlWriter;
import rat.report.xml.writer.impl.base.XmlWriter;

public class XmlReportFactoryTest extends TestCase {

    private static final Pattern IGNORE_EMPTY = Pattern.compile(".svn|Empty.txt");
    
    StringWriter out;
    IXmlWriter writer;
    
    protected void setUp() throws Exception {
        super.setUp();
        out = new StringWriter();
        writer = new XmlWriter(out);
        writer.startDocument();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    

    private void report(DirectoryWalker directory, RatReport report) throws Exception {
        directory.run(report);
    }
    
    public void testStandardReport() throws Exception {
        final MockLicenseMatcher mockLicenseMatcher = new MockLicenseMatcher();
        DirectoryWalker directory = new DirectoryWalker(new File("src/test/elements"), IGNORE_EMPTY);
        RatReport report = XmlReportFactory.createStandardReport(writer, mockLicenseMatcher);
        report.startReport();
        report(directory, report);
        report.endReport();
        writer.closeDocument();
        final String output = out.toString();
        assertEquals(
                "<?xml version='1.0'?>" +
                "<rat-report>" +
                "<resource name='src/test/elements/Image.png'><type name='binary'/></resource>" +
                "<resource name='src/test/elements/LICENSE'><type name='notice'/></resource>" +
                "<resource name='src/test/elements/NOTICE'><type name='notice'/></resource>" +
                "<resource name='src/test/elements/Source.java'><type name='standard'/>" +
                "</resource>" +
                "<resource name='src/test/elements/Text.txt'><type name='standard'/>" +
                "</resource>" +
                "<resource name='src/test/elements/Xml.xml'><type name='standard'/>" +
                "</resource>" +
                "<resource name='src/test/elements/dummy.jar'><type name='archive'/><archive-type name='readable'/></resource>" +
                "</rat-report>", output);
        assertTrue("Is well formed", XmlUtils.isWellFormedXml(output));
    }
}
