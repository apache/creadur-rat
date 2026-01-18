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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.DocumentAnalyser;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FileDocument;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.report.claim.SimpleXmlClaimReporter;
import org.apache.rat.report.xml.writer.XmlWriter;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.utils.FileUtils;
import org.apache.rat.testhelpers.TestingDocument;
import org.apache.rat.testhelpers.TestingDocumentAnalyser;
import org.apache.rat.testhelpers.TextUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AnalyserFactoryTest {

    @TempDir
    private static Path tempDir;

    private final DocumentName basedir;

    private StringWriter out;
    private SimpleXmlClaimReporter reporter;
    private DocumentAnalyser analyser;

    AnalyserFactoryTest() {
        basedir = DocumentName.builder(tempDir.toFile()).build();
    }

    @BeforeEach
    public void setUp() {
        out = new StringWriter();
        reporter = new SimpleXmlClaimReporter(new XmlWriter(out));
        ReportConfiguration config = new ReportConfiguration();
        config.addLicense(UnknownLicense.INSTANCE);
        analyser = AnalyserFactory.createConfiguredAnalyser(config);
    }

    private File copyResource(final String resourceName) throws IOException {
        final DocumentName outputName = DocumentName.builder(basedir).setName(resourceName).build();
        final File outputFile = outputName.asFile();
        FileUtils.mkDir(outputFile.getParentFile());
        try (InputStream input = AnalyserFactoryTest.class.getResourceAsStream(resourceName);
             OutputStream output = new FileOutputStream(outputFile)) {
            assertThat(input).isNotNull();
            IOUtils.copy(input, output);
        }
        return outputFile;
    }

    @Test
    public void standardTypeAnalyser() throws Exception {
        final File outputFile = copyResource("/exampleData/Text.txt");
        final Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);

        analyser.analyse(document);
        assertThat(document.getMetaData().getDocumentType()).isEqualTo(Document.Type.STANDARD);
        assertThat(document.getMetaData().getMediaType().toString()).isEqualTo("text/plain");
        assertThat(document.getMetaData().licenses().count()).isEqualTo(1);
        document.getMetaData().licenses().forEach(lic -> assertThat(lic).isEqualTo(UnknownLicense.INSTANCE));
    }

    @Test
    public void noteTypeAnalyser() throws Exception {
        final File outputFile = copyResource("/exampleData/LICENSE");
        final Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);

        analyser.analyse(document);
        assertThat(document.getMetaData().getDocumentType()).isEqualTo(Document.Type.NOTICE);
        assertThat(document.getMetaData().getMediaType().toString()).isEqualTo("text/plain");
    }

    @Test
    public void binaryTypeAnalyser() throws Exception {
        final File outputFile = copyResource("/exampleData/Image.png");
        final Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);

        analyser.analyse(document);
        assertThat(document.getMetaData().getDocumentType()).isEqualTo(Document.Type.BINARY);
        assertThat(document.getMetaData().getMediaType().toString()).isEqualTo("image/png");
    }

    @Test
    public void archiveTypeAnalyserTest() throws Exception {
        final File outputFile = copyResource("/exampleData/dummy.jar");
        final Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);

        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        analyser = AnalyserFactory.createConfiguredAnalyser(config);
        analyser.analyse(document);
        assertThat(document.getMetaData().getDocumentType()).isEqualTo(Document.Type.ARCHIVE);
        assertThat(document.getMetaData().getMediaType().toString()).isEqualTo("application/java-archive");
    }

    private static Stream<Arguments> archiveProcessingTestData() {
        List<Arguments> lst = new ArrayList<>();
        lst.add(Arguments.of(ReportConfiguration.Processing.NOTIFICATION, 0));
        lst.add(Arguments.of(ReportConfiguration.Processing.PRESENCE, 1));
        lst.add(Arguments.of(ReportConfiguration.Processing.ABSENCE, 2));
        return lst.stream();
    }

    @ParameterizedTest
    @MethodSource("archiveProcessingTestData")
    public void archiveProcessingTest(ReportConfiguration.Processing archiveProcessing, int expectedLicenseCount) throws Exception {
        final File outputFile = copyResource("/exampleData/dummy.jar");
        final Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);

        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setArchiveProcessing(archiveProcessing);
        document.getMetaData().setApprovalPredicate(config.getLicenseSetFactory().getApprovedLicensePredicate());
        analyser = AnalyserFactory.createConfiguredAnalyser(config);
        analyser.analyse(document);
        assertThat(document.getMetaData().getDocumentType()).isEqualTo(Document.Type.ARCHIVE);
        assertThat(document.getMetaData().getMediaType().toString()).isEqualTo("application/java-archive");
        assertThat(document.getMetaData().licenses().count()).as(archiveProcessing.desc() + " count").isEqualTo(expectedLicenseCount);
    }

    @Test
    public void missingFileTest() throws URISyntaxException {
        final Document document = new FileDocument(basedir,
                new File(Resources.getExampleResource("exampleData"), "not_a_real_file"), DocumentNameMatcher.MATCHES_ALL);
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        analyser = AnalyserFactory.createConfiguredAnalyser(config);
        assertThatThrownBy(() -> analyser.analyse(document)).isInstanceOf(RatDocumentAnalysisException.class);
    }

    @Test
    public void archiveTypeAnalyser() throws Exception {
        final File outputFile = copyResource("/exampleData/dummy.jar");
        final Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);

        analyser.analyse(document);
        assertThat(document.getMetaData().getDocumentType()).isEqualTo(Document.Type.ARCHIVE);
        assertThat(document.getMetaData().getMediaType().toString()).isEqualTo("application/java-archive");
    }

    @Test
    public void RAT211_bmp_Test() throws Exception {
        final File outputFile = copyResource("/jira/RAT211/side_left.bmp");
        final Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);

        analyser.analyse(document);
        assertThat(document.getMetaData().getDocumentType()).isEqualTo(Document.Type.BINARY);
        assertThat(document.getMetaData().getMediaType().toString()).isEqualTo("image/bmp");
    }

    @Test
    public void RAT211_dia_Test() throws Exception {
        final File outputFile = copyResource("/jira/RAT211/leader-election-message-arrives.dia");
        final Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);

        analyser.analyse(document);
        assertThat(document.getMetaData().getDocumentType()).isEqualTo(Document.Type.ARCHIVE);
        assertThat(document.getMetaData().getMediaType().toString()).isEqualTo("application/gzip");
    }

    @Test
    public void RAT147_unix_Test() throws Exception {
        final File outputFile = copyResource("/jira/RAT147/unix-newlines.txt.bin");
        final Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);

        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        TextUtils.assertPatternInTarget(
                "<resource name='/jira/RAT147/unix-newlines.txt.bin' type='STANDARD'",
                result);
    }

    @Test
    public void RAT147_windows_Test() throws Exception {
        final File outputFile = copyResource("/jira/RAT147/windows-newlines.txt.bin");
        final Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);

        analyser.analyse(document);
        reporter.report(document);
        String result = out.toString();
        TextUtils.assertPatternInTarget(
                "<resource name='/jira/RAT147/windows-newlines.txt.bin' type='STANDARD'",
                result);
    }

    @Test
    public void standardNotificationTest() throws Exception {
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setStandardProcessing(ReportConfiguration.Processing.NOTIFICATION);
        analyser = AnalyserFactory.createConfiguredAnalyser(config);

        File outputFile = copyResource("/exampleData/Text.txt");
        Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertThat(document.getMetaData().detectedLicense()).isFalse();

        outputFile = copyResource("/exampleData/sub/Empty.txt");
        document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertThat(document.getMetaData().detectedLicense()).isFalse();
    }

    @Test
    public void standardAbsenceTest() throws Exception {
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setStandardProcessing(ReportConfiguration.Processing.ABSENCE);
        analyser = AnalyserFactory.createConfiguredAnalyser(config);

        File outputFile = copyResource("/exampleData/Text.txt");
        Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertThat(document.getMetaData().detectedLicense()).isTrue();

        outputFile = copyResource("/exampleData/sub/Empty.txt");
        document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertThat(document.getMetaData().detectedLicense()).isTrue();
    }

    @Test
    public void standardPresenceTest() throws Exception {
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);
        config.setStandardProcessing(ReportConfiguration.Processing.PRESENCE);
        analyser = AnalyserFactory.createConfiguredAnalyser(config);

        File outputFile = copyResource("/exampleData/Text.txt");
        Document document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertThat(document.getMetaData().detectedLicense()).isTrue();

        outputFile = copyResource("/exampleData/sub/Empty.txt");
        document = new FileDocument(basedir, outputFile, DocumentNameMatcher.MATCHES_ALL);
        analyser.analyse(document);
        assertThat(document.getMetaData().detectedLicense()).isFalse();
    }

    @Test
    public void testMultiplexer() throws Exception {
        TestingDocumentAnalyser[] analysers = {
                new TestingDocumentAnalyser(),
                new TestingDocumentAnalyser(),
                new TestingDocumentAnalyser()
        };
        DocumentAnalyser multiplexer = AnalyserFactory.createMultiplexer(analysers);
        TestingDocument document = new TestingDocument();

        multiplexer.analyse(document);
        for (int i = 0; i < analysers.length; i++) {
            assertThat(analysers[i].matches.size()).as("Matcher " + i).isEqualTo(1);
            assertThat(analysers[i].matches.get(0)).as("Matcher " + i).isEqualTo(document);
        }
    }
}
