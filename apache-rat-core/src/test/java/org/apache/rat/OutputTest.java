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

import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.config.results.ClaimValidator;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.claim.ClaimStatisticTest;
import org.apache.rat.utils.FileUtils;
import org.apache.rat.utils.StandardXmlFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlunit.assertj3.XmlAssert;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class OutputTest {

    static Path tempPath;

    @BeforeAll
    static void setup() throws IOException {
        tempPath = Files.createTempDirectory("outputTest").toAbsolutePath();
    }

    @AfterAll
    static void teardown() throws IOException {
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
        try (InputStream inputStream = OutputTest.class.getClassLoader().getResourceAsStream("XmlOutputExamples/elements.xml");) {
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
    void stasticReadingTest() throws IOException {
        Path testPath = tempPath.resolve("statisticReading");
        File testFile = testPath.toFile();
        FileUtils.mkDir(testFile);
        DocumentName workingDirectory = DocumentName.builder(testPath.toFile()).setBaseName(testFile).build();
        DocumentName documentFile = workingDirectory.resolve("statistic.xml");

        ClaimStatistic underTest = new ClaimStatistic();
        underTest.incLicenseCategoryCount("familyCagegory", 1);
        underTest.incCounter(ClaimStatistic.Counter.APPROVED, 2);
        underTest.incCounter(org.apache.rat.api.Document.Type.IGNORED, 3);
        underTest.incLicenseNameCount("licenseName", 4);

        ClaimStatistic.Serde serde = underTest.serde();
        StringWriter stringWriter = new StringWriter();
        serde.serialize(stringWriter);
        try (FileOutputStream fos = new FileOutputStream(documentFile.asFile())) {
            fos.write(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        }

        Reporter.Output.Builder builder = Reporter.Output.builder().statistic(documentFile.getName(), workingDirectory);

        Reporter.Output output = builder.build();
        ClaimStatisticTest.assertSame(output.getStatistic(), underTest);
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
        underTest.serde().serialize(stringWriter);

        try (FileOutputStream fos = new FileOutputStream(documentFile.asFile())) {
            fos.write(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        }

        Reporter.Output.Builder builder = Reporter.Output.builder().configuration(documentFile.getName(), workingDirectory);

        Reporter.Output output = builder.build();
        ReportConfigurationTest.assertSame(output.getConfiguration(), underTest);
    }
}
