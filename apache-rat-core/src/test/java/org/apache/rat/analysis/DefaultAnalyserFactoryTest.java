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

import java.io.File;
import java.io.StringWriter;

import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FileDocument;
import org.apache.rat.report.claim.SimpleXmlClaimReporter;
import org.apache.rat.report.xml.writer.XmlWriter;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TextUtils;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultAnalyserFactoryTest {

    private final DocumentName basedir;

    private StringWriter out;
    private SimpleXmlClaimReporter reporter;
    private IDocumentAnalyser analyser;

    DefaultAnalyserFactoryTest() {
        basedir = new DocumentName(new File(Files.currentFolder(), Resources.SRC_TEST_RESOURCES));
    }

    @BeforeEach
    public void setUp() {
        out = new StringWriter();
        reporter = new SimpleXmlClaimReporter(new XmlWriter(out));
        ReportConfiguration config = new ReportConfiguration();
        config.addLicense(UnknownLicense.INSTANCE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);
    }

    @Test
    public void standardTypeAnalyser() throws Exception {
        String[] expected = {
                "<resource name='/elements/Text.txt' type='STANDARD'>"
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

        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/Text.txt"), p -> true);
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        for (String exp : expected) {
            assertTrue(result.contains(exp), () -> exp);
        }
    }

    @Test
    public void noteTypeAnalyser() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/LICENSE"), p -> true);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='/elements/LICENSE' type='NOTICE'/>", out.toString());
    }

    @Test
    public void binaryTypeAnalyser() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/Image.png"), p -> true);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='/elements/Image.png' type='BINARY'/>", out.toString());
    }

    @Test
    public void archiveTypeAnalyserTest() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/dummy.jar"), p -> true);
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='/elements/dummy.jar' type='ARCHIVE'/>", out.toString());
    }

    @Test
    public void archivesAbsenceTest() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/dummy.jar"), p -> true);
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setArchiveProcessing(ReportConfiguration.Processing.ABSENCE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        TextUtils.assertContains("<resource name='/elements/dummy.jar' type='ARCHIVE'>", result);
        TextUtils.assertContains("<license id='?????' name='Unknown license' approval='false' family='?????'/>", result);
        TextUtils.assertContains("<license id='ASL' name='Applied Apache License Version 2.0' approval='false' family='AL   '/>", result);
    }

    @Test
    public void archivesPresenceTest() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/dummy.jar"), p -> true);
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setArchiveProcessing(ReportConfiguration.Processing.PRESENCE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        TextUtils.assertContains("<resource name='/elements/dummy.jar' type='ARCHIVE'>", result);
        TextUtils.assertNotContains("<license id='?????' name='Unknown license' approval='false' family='?????'/>", result);
        TextUtils.assertContains("<license id='ASL' name='Applied Apache License Version 2.0' approval='false' family='AL   '/>", result);
    }

    @Test
    public void archiveTypeAnalyser() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/dummy.jar"), p -> true);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='/elements/dummy.jar' type='ARCHIVE'/>", out.toString());
    }

    @Test
    public void RAT211_bmp_Test() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/jira/RAT211/side_left.bmp"), p -> true);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals("<resource name='/jira/RAT211/side_left.bmp' type='BINARY'/>", out.toString());
    }

    @Test
    public void RAT211_dia_Test() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/jira/RAT211/leader-election-message-arrives.dia"), p -> true);
        analyser.analyse(document);
        reporter.report(document);
        assertEquals(
                "<resource name='/jira/RAT211/leader-election-message-arrives.dia' type='ARCHIVE'/>",
                out.toString());
    }

    @Test
    public void RAT147_unix_Test() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/jira/RAT147/unix-newlines.txt.bin"), p -> true);
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        TextUtils.assertPatternInTarget(
                "<resource name='/jira/RAT147/unix-newlines.txt.bin' type='STANDARD'",
                result);
        TextUtils.assertPatternInTarget("sentence 1.$", result);
        TextUtils.assertPatternInTarget("^sentence 2.$", result);
        TextUtils.assertPatternInTarget("^sentence 3.$", result);
        TextUtils.assertPatternInTarget("^sentence 4.$", result);
    }

    @Test
    public void RAT147_windows_Test() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/jira/RAT147/windows-newlines.txt.bin"), p -> true);
        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        TextUtils.assertPatternInTarget(
                "<resource name='/jira/RAT147/windows-newlines.txt.bin' type='STANDARD'",
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
        config.setStandardProcessing(ReportConfiguration.Processing.NOTIFICATION);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);

        Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/Text.txt"), p -> true);
        analyser.analyse(document);
        assertFalse(document.getMetaData().detectedLicense());

        document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/sub/Empty.txt"), p -> true);
        analyser.analyse(document);
        assertFalse(document.getMetaData().detectedLicense());
    }

    @Test
    public void standardAbsenceTest() throws Exception {

        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setStandardProcessing(ReportConfiguration.Processing.ABSENCE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);

        Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/Text.txt"), p -> true);
        analyser.analyse(document);
        assertTrue(document.getMetaData().detectedLicense());

        document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/sub/Empty.txt"), p -> true);
        analyser.analyse(document);
        assertTrue(document.getMetaData().detectedLicense());
    }

    @Test
    public void standardPresenceTest() throws Exception {
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setStandardProcessing(ReportConfiguration.Processing.PRESENCE);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);

        Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/Text.txt"), p -> true);
        analyser.analyse(document);
        assertTrue(document.getMetaData().detectedLicense());

        document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/sub/Empty.txt"), p -> true);
        analyser.analyse(document);
        assertFalse(document.getMetaData().detectedLicense());
    }
}
