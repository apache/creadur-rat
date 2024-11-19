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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.document.DocumentNameMatcher;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DefaultAnalyserFactoryTest {

    private final DocumentName basedir;

    private StringWriter out;
    private SimpleXmlClaimReporter reporter;
    private IDocumentAnalyser analyser;

    DefaultAnalyserFactoryTest() {
        basedir = DocumentName.builder(new File(Files.currentFolder(), Resources.SRC_TEST_RESOURCES)).build();
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
                " * under the License."
        };

        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/Text.txt"), DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertEquals(Document.Type.STANDARD, document.getMetaData().getDocumentType());
        assertEquals("text/plain", document.getMetaData().getMediaType().toString());
        assertEquals(1, document.getMetaData().licenses().count());
        document.getMetaData().licenses().forEach(lic -> assertEquals(UnknownLicense.INSTANCE, lic));
        String result = document.getMetaData().getSampleHeader();
        for (String exp : expected) {
            assertTrue(result.contains(exp), exp);
        }
    }

    @Test
    public void noteTypeAnalyser() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/LICENSE"), DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertEquals(Document.Type.NOTICE, document.getMetaData().getDocumentType());
        assertEquals("text/plain", document.getMetaData().getMediaType().toString());
    }

    @Test
    public void binaryTypeAnalyser() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/Image.png"), DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertEquals(Document.Type.BINARY, document.getMetaData().getDocumentType());
        assertEquals("image/png", document.getMetaData().getMediaType().toString());
    }

    @Test
    public void archiveTypeAnalyserTest() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/dummy.jar"), DocumentNameMatcher.MATCHES_ALL);
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);
        analyser.analyse(document);
        assertEquals(Document.Type.ARCHIVE, document.getMetaData().getDocumentType());
        assertEquals("application/java-archive", document.getMetaData().getMediaType().toString());
    }

    private static Stream<Arguments> archivesAbsenceTestData() {
        List<Arguments> lst = new ArrayList<>();
        lst.add(Arguments.of(ReportConfiguration.Processing.NOTIFICATION, 0));
        lst.add(Arguments.of(ReportConfiguration.Processing.PRESENCE, 2));
        lst.add(Arguments.of(ReportConfiguration.Processing.ABSENCE, 3));
        return lst.stream();
    }

    @ParameterizedTest
    @MethodSource("archivesAbsenceTestData")
    public void archivesAbsenceTest(ReportConfiguration.Processing archiveProcessing, int expectedLicenseCount) throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/dummy.jar"), DocumentNameMatcher.MATCHES_ALL);
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setArchiveProcessing(archiveProcessing);
        analyser = DefaultAnalyserFactory.createDefaultAnalyser(config);
        analyser.analyse(document);
        assertEquals(Document.Type.ARCHIVE, document.getMetaData().getDocumentType());
        assertEquals("application/java-archive", document.getMetaData().getMediaType().toString());
        assertEquals(expectedLicenseCount, document.getMetaData().licenses().count());
    }

    @Test
    public void archiveTypeAnalyser() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/dummy.jar"), DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertEquals(Document.Type.ARCHIVE, document.getMetaData().getDocumentType());
        assertEquals("application/java-archive", document.getMetaData().getMediaType().toString());
    }

    @Test
    public void RAT211_bmp_Test() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/jira/RAT211/side_left.bmp"), DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertEquals(Document.Type.BINARY, document.getMetaData().getDocumentType());
        assertEquals("image/bmp", document.getMetaData().getMediaType().toString());
    }

    @Test
    public void RAT211_dia_Test() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/jira/RAT211/leader-election-message-arrives.dia"), DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertEquals(Document.Type.ARCHIVE, document.getMetaData().getDocumentType());
        assertEquals("application/gzip", document.getMetaData().getMediaType().toString());
    }

    @Test
    public void RAT147_unix_Test() throws Exception {
        final Document document = new FileDocument(basedir,
                Resources.getResourceFile("/jira/RAT147/unix-newlines.txt.bin"), DocumentNameMatcher.MATCHES_ALL);
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
                Resources.getResourceFile("/jira/RAT147/windows-newlines.txt.bin"), DocumentNameMatcher.MATCHES_ALL);
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
                Resources.getResourceFile("/elements/Text.txt"), DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertFalse(document.getMetaData().detectedLicense());

        document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/sub/Empty.txt"), DocumentNameMatcher.MATCHES_ALL);
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
                Resources.getResourceFile("/elements/Text.txt"), DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertTrue(document.getMetaData().detectedLicense());

        document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/sub/Empty.txt"), DocumentNameMatcher.MATCHES_ALL);
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
                Resources.getResourceFile("/elements/Text.txt"), DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertTrue(document.getMetaData().detectedLicense());

        document = new FileDocument(basedir,
                Resources.getResourceFile("/elements/sub/Empty.txt"), DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertFalse(document.getMetaData().detectedLicense());
    }
}
