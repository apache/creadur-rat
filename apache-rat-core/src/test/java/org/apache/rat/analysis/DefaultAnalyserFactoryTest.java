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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.impl.FileDocument;
import org.apache.rat.report.claim.impl.xml.SimpleXmlClaimReporter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultAnalyserFactoryTest {

    private StringWriter out;
    private SimpleXmlClaimReporter reporter;
    private IDocumentAnalyser analyser;

    @BeforeEach
    public void setUp() throws Exception {
        out = new StringWriter();
        reporter = new SimpleXmlClaimReporter(new XmlWriter(out));
        ReportConfiguration config = new ReportConfiguration();
        config.addLicense(UnknownLicense.INSTANCE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);
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

        final Document document = new FileDocument(
                Resources.getResourceFile("/elements/Text.txt"));
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        for (String exp : expected) {
            assertTrue(result.contains(exp), () -> exp);
        }
    }

    @Test
    public void noteTypeAnalyser() throws Exception {
        final Document document = new FileDocument(
                Resources.getResourceFile("/elements/LICENSE"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='src/test/resources/elements/LICENSE' type='NOTICE'/>", out.toString());
    }

    @Test
    public void binaryTypeAnalyser() throws Exception {
        final Document document = new FileDocument(
                Resources.getResourceFile("/elements/Image.png"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='src/test/resources/elements/Image.png' type='BINARY'/>", out.toString());
    }

    @Test
    public void archiveTypeAnalyserTest() throws Exception {
        final Document document = new FileDocument(
                Resources.getResourceFile("/elements/dummy.jar"));
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setFilesToIgnore(FalseFileFilter.FALSE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='src/test/resources/elements/dummy.jar' type='ARCHIVE'/>", out.toString());
    }

    @Test
    public void archivesAbsenceTest() throws Exception {
        final Document document = new FileDocument(
                Resources.getResourceFile("/elements/dummy.jar"));
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setFilesToIgnore(FalseFileFilter.FALSE);
        config.setArchiveProcessing(ReportConfiguration.Processing.ABSENCE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        TextUtils.assertContains("<resource name='src/test/resources/elements/dummy.jar' type='ARCHIVE'>", out.toString());
        TextUtils.assertContains("<license id='?????' name='Unknown license' approval='false' family='?????'/>", out.toString());
        TextUtils.assertContains("<license id='ASL' name='Applied Apache License Version 2.0' approval='false' family='AL   '/>", out.toString());
    }

    @Test
    public void archivesPresenceTest() throws Exception {
        final Document document = new FileDocument(
                Resources.getResourceFile("/elements/dummy.jar"));
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setFilesToIgnore(FalseFileFilter.FALSE);
        config.setArchiveProcessing(ReportConfiguration.Processing.PRESENCE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        TextUtils.assertContains("<resource name='src/test/resources/elements/dummy.jar' type='ARCHIVE'>", out.toString());
        TextUtils.assertNotContains("<license id='?????' name='Unknown license' approval='false' family='?????'/>", out.toString());
        TextUtils.assertContains("<license id='ASL' name='Applied Apache License Version 2.0' approval='false' family='AL   '/>", out.toString());
    }

    @Test
    public void archiveTypeAnalyser() throws Exception {
        final Document document = new FileDocument(
                Resources.getResourceFile("/elements/dummy.jar"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='src/test/resources/elements/dummy.jar' type='ARCHIVE'/>", out.toString());
    }

    @Test
    public void RAT211_bmp_Test() throws Exception {
        final Document document = new FileDocument(
                Resources.getResourceFile("/jira/RAT211/side_left.bmp"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='src/test/resources/jira/RAT211/side_left.bmp' type='BINARY'/>", out.toString());
    }

    @Test
    public void RAT211_dia_Test() throws Exception {
        final Document document = new FileDocument(
                Resources.getResourceFile("/jira/RAT211/leader-election-message-arrives.dia"));
        analyser.analyse(document);
        reporter.report(document);
        assertEquals(
                "<resource name='src/test/resources/jira/RAT211/leader-election-message-arrives.dia' type='ARCHIVE'/>",
                out.toString());
    }

    @Test
    public void RAT147_unix_Test() throws Exception {
        final Document document = new FileDocument(
                Resources.getResourceFile("/jira/RAT147/unix-newlines.txt.bin"));
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        TextUtils.assertPatternInTarget(
                "<resource name='src/test/resources/jira/RAT147/unix-newlines.txt.bin' type='STANDARD'",
                result);
        TextUtils.assertPatternInTarget("sentence 1.$", result);
        TextUtils.assertPatternInTarget("^sentence 2.$", result);
        TextUtils.assertPatternInTarget("^sentence 3.$", result);
        TextUtils.assertPatternInTarget("^sentence 4.$", result);
    }

    @Test
    public void RAT147_windows_Test() throws Exception {
        final Document document = new FileDocument(
                Resources.getResourceFile("/jira/RAT147/windows-newlines.txt.bin"));
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        TextUtils.assertPatternInTarget(
                "<resource name='src/test/resources/jira/RAT147/windows-newlines.txt.bin' type='STANDARD'",
                result);
        TextUtils.assertPatternInTarget("sentence 1.$", result);
        TextUtils.assertPatternInTarget("^sentence 2.$", result);
        TextUtils.assertPatternInTarget("^sentence 3.$", result);
        TextUtils.assertPatternInTarget("^sentence 4.$", result);
    }

    @Test
    public void standardNotificationTest() throws Exception {

        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setFilesToIgnore(FalseFileFilter.FALSE);
        config.setStandardProcessing(ReportConfiguration.Processing.NOTIFICATION);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);

        Document document = new FileDocument(
                Resources.getResourceFile("/elements/Text.txt"));
        analyser.analyse(document);
        assertFalse(document.getMetaData().detectedLicense());

        document = new FileDocument(
                Resources.getResourceFile("/elements/sub/Empty.txt"));
        analyser.analyse(document);
        assertFalse(document.getMetaData().detectedLicense());
    }

    @Test
    public void standardAbsenceTest() throws Exception {

        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setFilesToIgnore(FalseFileFilter.FALSE);
        config.setStandardProcessing(ReportConfiguration.Processing.ABSENCE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);

        Document document = new FileDocument(
                Resources.getResourceFile("/elements/Text.txt"));
        analyser.analyse(document);
        assertTrue(document.getMetaData().detectedLicense());

        document = new FileDocument(
                Resources.getResourceFile("/elements/sub/Empty.txt"));
        analyser.analyse(document);
        assertTrue(document.getMetaData().detectedLicense());
    }

    @Test
    public void standardPresenceTest() throws Exception {
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setFilesToIgnore(FalseFileFilter.FALSE);
        config.setStandardProcessing(ReportConfiguration.Processing.PRESENCE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);

        Document document = new FileDocument(
                Resources.getResourceFile("/elements/Text.txt"));
        analyser.analyse(document);
        assertTrue(document.getMetaData().detectedLicense());

        document = new FileDocument(
                Resources.getResourceFile("/elements/sub/Empty.txt"));
        analyser.analyse(document);
        assertFalse(document.getMetaData().detectedLicense());
    }

}
