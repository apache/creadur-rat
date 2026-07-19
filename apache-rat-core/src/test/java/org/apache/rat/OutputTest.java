/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   https://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat;

import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.config.results.ClaimValidator;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.claim.ClaimStatisticTest;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.FileUtils;
import org.apache.rat.utils.StandardXmlFactory;
import org.apache.rat.utils.StandardXmlFactoryTest;
import org.apache.rat.walker.DirectoryWalker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlunit.assertj3.XmlAssert;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutputTest {

    static Path tempPath;

    @BeforeAll
    static void setup() throws IOException {
        tempPath = Files.createTempDirectory("outputTest").toAbsolutePath();
    }

    @AfterAll
    static void teardown() {
        FileUtils.delete(tempPath.toFile());
    }

    @Test
    void documentReadingTest() throws IOException, SAXException, TransformerException {
        Path testPath = tempPath.resolve("documentReading");
        File testFile = testPath.toFile();
        FileUtils.mkDir(testFile);
        DocumentName workingDirectory = DocumentName.builder(testPath.toFile()).setBaseName(testFile).build();
        Document document;
        DocumentName documentFile = workingDirectory.resolve("document.xml");
        try (InputStream inputStream = OutputTest.class.getClassLoader().getResourceAsStream("XmlOutputExamples/elements.xml")) {
            document = StandardXmlFactory.documentBuilder().parse(inputStream);
            StandardXmlFactory.writeDocument(document, documentFile.asFile());
        }

        Reporter.Output.Builder builder = Reporter.Output.builder().document(documentFile.getName(), workingDirectory);

        Reporter.Output output = builder.build();
        XmlAssert.assertThat(output.getDocument()).and(document)
                .ignoreWhitespace()
                .areIdentical();
    }

    @Test
    void statisticReadingTest() throws IOException {
        Path testPath = tempPath.resolve("statisticReading");
        File testFile = testPath.toFile();
        FileUtils.mkDir(testFile);
        DocumentName workingDirectory = DocumentName.builder(testPath.toFile()).setBaseName(testFile).build();
        DocumentName documentFile = workingDirectory.resolve("statistic.xml");

        ClaimStatistic underTest = new ClaimStatistic();
        underTest.incLicenseCategoryCount("familyCategory", 1);
        underTest.incCounter(ClaimStatistic.Counter.APPROVED, 2);
        underTest.incCounter(org.apache.rat.api.Document.Type.IGNORED, 3);
        underTest.incLicenseNameCount("licenseName", 4);

        ClaimStatistic.SerDes serDes = underTest.serDes();
        StringWriter stringWriter = new StringWriter();
        serDes.serialize(stringWriter);
        try (FileOutputStream fos = new FileOutputStream(documentFile.asFile())) {
            fos.write(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        }

        Reporter.Output.Builder builder = Reporter.Output.builder().statistic(documentFile.getName(), workingDirectory);

        Reporter.Output output = builder.build();
        ClaimStatisticTest.assertSame(output.getStatistic(), underTest);
    }

    @Test
    void readingBadFileTest() {
        Path testPath = tempPath.resolve("readingBadFileTest");
        File testFile = testPath.toFile();
        FileUtils.mkDir(testFile);
        DocumentName workingDirectory = DocumentName.builder(testPath.toFile()).setBaseName(testFile).build();
        DocumentName documentFile = workingDirectory.resolve("missing.file");
        String name = documentFile.getName();
        Reporter.Output.Builder builder = Reporter.Output.builder();
        assertThatThrownBy(() -> builder.statistic(name, workingDirectory))
                .as("statistic read")
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Unable to read file: " + testPath.resolve("missing.file"));

        assertThatThrownBy(() -> builder.configuration(name, workingDirectory))
                .as("configuration read")
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Unable to read file: " + testPath.resolve("missing.file"));

        assertThatThrownBy(() -> builder.document(name, workingDirectory))
                .as("document read")
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Unable to read file: " + testPath.resolve("missing.file"));
    }

    @Test
    void configurationReadingTest() throws IOException {
        Path testPath = tempPath.resolve("configurationReading");
        File testFile = testPath.toFile();
        FileUtils.mkDir(testFile);
        DocumentName workingDirectory = DocumentName.builder(testPath.toFile()).setBaseName(testFile).build();
        DocumentName documentFile = workingDirectory.resolve("configuration.xml");

        ReportConfiguration underTest = new ReportConfiguration();
        underTest.setAddLicenseHeaders(AddLicenseHeaders.FORCED);
        underTest.listFamilies(LicenseSetFactory.LicenseFilter.APPROVED);
        underTest.listLicenses(LicenseSetFactory.LicenseFilter.ALL);
        underTest.setDryRun(true);
        underTest.setArchiveProcessing(ReportConfiguration.Processing.NOTIFICATION);
        underTest.setStandardProcessing(ReportConfiguration.Processing.ABSENCE);
        underTest.setStyleSheet(StyleSheets.MISSING_HEADERS.getStyleSheet());
        underTest.setOut(new File("/some/file/somewhere"));
        underTest.setCopyrightMessage("the copyright message");
        underTest.addSource(new File("/my/file"));
        underTest.addSource(new ReportConfigurationTest.TestingReportable());
        underTest.addExcludedPatterns(List.of("pattern/**", "pattern2/**"));
        underTest.addExcludedCollection(StandardCollection.BAZAAR);
        underTest.addExcludedCollection(StandardCollection.MISC);
        underTest.addExcludedFileProcessor(StandardCollection.HIDDEN_FILE);
        underTest.addExcludedMatcher(DocumentNameMatcher.MATCHES_ALL);
        underTest.addIncludedPatterns(List.of("**/pattern3", "**/pattern4"));
        underTest.addIncludedCollection(StandardCollection.ARCH);
        underTest.addIncludedCollection(StandardCollection.BITKEEPER);
        underTest.addIncludedMatcher(DocumentNameMatcher.MATCHES_NONE);

        ClaimValidator claimValidator = underTest.getClaimValidator();
        claimValidator.setMax(ClaimStatistic.Counter.APPROVED, 5);
        claimValidator.setMin(ClaimStatistic.Counter.APPROVED, 3);
        claimValidator.setMax(ClaimStatistic.Counter.ARCHIVES, 10);
        claimValidator.setMin(ClaimStatistic.Counter.BINARIES, 4);

        StringWriter stringWriter = new StringWriter();
        underTest.serDes().serialize(stringWriter);

        try (FileOutputStream fos = new FileOutputStream(documentFile.asFile())) {
            fos.write(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        }

        Reporter.Output.Builder builder = Reporter.Output.builder().configuration(documentFile.getName(), workingDirectory);

        Reporter.Output output = builder.build();
        ReportConfigurationTest.assertSame(output.getConfiguration(), underTest);
    }

    private ReportConfiguration initializeConfiguration() throws URISyntaxException {
        Defaults defaults = Defaults.builder().build();
        final File elementsFile = Resources.getExampleResource("exampleData");
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setFrom(defaults);
        DocumentName documentName = DocumentName.builder(elementsFile).build();
        configuration.addSource(new DirectoryWalker(new FileDocument(documentName, elementsFile,
                configuration.getDocumentExcluder(documentName))));
        return configuration;
    }

    @Test
    void listLicensesReportTest() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ReportConfiguration configuration = initializeConfiguration();
        configuration.setOut(new ReportConfiguration.IODescriptor<>("listLicensesReportTest", () -> out));
        configuration.setStyleSheet(StyleSheets.UNAPPROVED_LICENSES.getStyleSheet());
        Reporter.Output output = Reporter.Output.builder()
                .statistic(new ClaimStatistic())
                .document(null)
                .configuration(configuration)
                .build();
        output.listLicenses(LicenseSetFactory.LicenseFilter.NONE);

        out.flush();
        String document = out.toString();
        assertThat(document).contains("Licenses (NONE):");
        StringWriter writer = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(writer)) {
            output.listLicenses(printWriter, LicenseSetFactory.LicenseFilter.ALL);
        }
        assertThat(writer.toString()).contains("Licenses (ALL):");
    }

    @Test
    void formatTest() throws IOException, SAXException {
        Reporter.Output output = Reporter.Output.builder()
                .statistic(new ClaimStatistic())
                .document(StandardXmlFactoryTest.simpleDocument())
                .configuration(new ReportConfiguration())
                .build();

        IOSupplier<InputStream> badStyleSheet = () -> new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Expected exception.");
            }
        };
        assertThatThrownBy(() -> output.format(badStyleSheet, ReportConfiguration.SYSTEM_OUT.ioSupplier()))
                .as("badStyleSheet")
                .isInstanceOf(RatException.class)
                .hasMessageContaining("Expected exception")
                .hasCauseInstanceOf(TransformerException.class);

        IOSupplier<InputStream> missingStyleSheet = () -> {throw new IOException("Expected exception.");};


        assertThatThrownBy(() -> output.format(missingStyleSheet, ReportConfiguration.SYSTEM_OUT.ioSupplier()))
                .as("missingStyleSheet")
                .isInstanceOf(RatException.class)
                .hasMessageContaining("Expected exception")
                .hasCauseInstanceOf(IOException.class);

        IOSupplier<OutputStream> badOutput = () -> new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Expected exception.");
            }
        };
        assertThatThrownBy(() -> output.format(StyleSheets.XML.getStyleSheet().ioSupplier(), badOutput))
                .as("badOutput")
                .isInstanceOf(RatException.class)
                .hasMessageContaining("Expected exception")
                .hasCauseInstanceOf(TransformerException.class);

        IOSupplier<OutputStream> missingOutput = () ->  {throw new IOException("Expected exception.");};
        assertThatThrownBy(() -> output.format(StyleSheets.XML.getStyleSheet().ioSupplier(), missingOutput))
                .as("missingOutput")
                .isInstanceOf(RatException.class)
                .hasMessageContaining("Expected exception")
                .hasCauseInstanceOf(IOException.class);

    }
}
