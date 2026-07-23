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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.apache.rat.api.Document.Type;
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.document.FileDocument;
import org.apache.rat.document.DocumentName;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.claim.ClaimStatisticTest;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.BaseOptionCollection;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.testhelpers.data.ReportTestDataProvider;
import org.apache.rat.testhelpers.data.TestData;
import org.apache.rat.testhelpers.data.ValidatorData;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.rat.utils.StandardXmlFactory;
import org.apache.rat.walker.DirectoryWalker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Tests the output of the Reporter.
 */
public class ReporterTest {
    /**
     * temporary file.  Not using tempDir because it does not
     * always work.
     */
    private static Path tempPath;

    /**
     * The directory for the test.
     */
    private Path testPath;

    /**
     * Directory for the test data.
     */
    final String basedir;

    private final OptionCollectionParser collectionParser;

    ReporterTest() throws URISyntaxException {
        basedir = Resources.getExampleResource("exampleData").getPath();
        collectionParser = new OptionCollectionParser(BaseOptionCollection.builder().build());
    }

    @BeforeAll
    static void setUp() throws IOException {
        tempPath = Files.createTempDirectory("ReporterTest");
    }

    @AfterAll
    static void cleanup() throws IOException {
        FileUtils.deleteDirectory(tempPath.toFile());
    }

    @BeforeEach
    void setUpTest(TestInfo testInfo) {
        Optional<Method> testMethod = testInfo.getTestMethod();
        this.testPath = tempPath.resolve(testMethod.map(Method::getName).orElseGet(() -> UUID.randomUUID().toString()));
        File file = testPath.toFile();
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new RuntimeException("Unable to create temp directory: " + file.getName());
            }
        } else {
            if (!file.isDirectory()) {
                throw new RuntimeException(file.getName() + " is NOT a directory.");
            }
        }
    }

    @Test
    void testExecute() throws RatException {
        File output = testPath.resolve("output.xml").toFile();
        ArgumentContext ctxt = collectionParser.parseCommands(new File("."), new String[]{"--output-style", "xml", "--output-file", output.getPath(), basedir});
        ClaimStatistic statistic = new Reporter(ctxt.getConfiguration()).execute().getStatistic();

        assertThat(statistic.getCounter(Type.ARCHIVE)).isEqualTo(1);
        assertThat(statistic.getCounter(Type.BINARY)).isEqualTo(2);
        assertThat(statistic.getCounter(Type.IGNORED)).isEqualTo(2);
        assertThat(statistic.getCounter(Type.NOTICE)).isEqualTo(2);
        assertThat(statistic.getCounter(Type.STANDARD)).isEqualTo(8);
        assertThat(statistic.getCounter(Type.UNKNOWN)).isEqualTo(0);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(8);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.ARCHIVES)).isEqualTo(1);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.BINARIES)).isEqualTo(2);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.DOCUMENT_TYPES)).isEqualTo(5);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(2);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.LICENSE_CATEGORIES)).isEqualTo(4);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.LICENSE_NAMES)).isEqualTo(5);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.NOTICES)).isEqualTo(2);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(8);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(2);
        assertThat(statistic.getCounter(ClaimStatistic.Counter.UNKNOWN)).isEqualTo(2);

        List<Type> typeList = statistic.getDocumentTypes();
        assertThat(typeList).isEqualTo(Arrays.asList(Type.ARCHIVE, Type.BINARY, Type.IGNORED, Type.NOTICE, Type.STANDARD));

        TreeMap<String, Integer> expected = new TreeMap<>();
        expected.put("Unknown license", 2);
        expected.put("Apache License 2.0", 5);
        expected.put("The MIT License", 1);
        expected.put("BSD 3 clause", 1);
        expected.put("The Telemanagement Forum License", 1);
        TreeMap<String, Integer> actual = new TreeMap<>();

        for (String licenseName : statistic.getLicenseNames()) {
            actual.put(licenseName, statistic.getLicenseNameCount(licenseName));
        }
        assertThat(actual).isEqualTo(expected);

        expected.clear();
        expected.put("?????", 2);
        expected.put("AL   ", 5);
        expected.put("BSD-3", 2);
        expected.put("MIT  ", 1);
        actual.clear();
        for (String licenseCategory : statistic.getLicenseFamilyCategories()) {
            actual.put(licenseCategory, statistic.getLicenseCategoryCount(licenseCategory));
        }
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testExecuteNoSource() throws RatException, TransformerException {
        File output = testPath.resolve("testExecuteNoSource").toFile();
        ArgumentContext context = collectionParser.parseCommands(new File("."), new String[]{"--output-style", "xml", "--output-file", output.getPath()});
        ReportConfiguration config = OptionCollection.createConfiguration(context);
        Reporter.Output result = new Reporter(config).execute();
        assertThat(StandardXmlFactory.serializeDocument(result.getDocument())).isEmpty();
        ClaimStatisticTest.assertSame(result.getStatistic(), new ClaimStatistic());
    }

    @Test
    void testOutputOption() throws Exception {
        File output = testPath.resolve("testOutputOption.txt").toFile();
        ArgumentContext ctxt = collectionParser.parseCommands(new File("."), new String[]{"--output-file", output.getCanonicalPath(), basedir});
        new Reporter(ctxt.getConfiguration()).execute().format(ctxt.getConfiguration());
        assertThat(output.exists()).isTrue();
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        assertThat(content).containsPattern(Pattern.compile("^! Unapproved:\\s*2 ", Pattern.MULTILINE))
                .contains("/Source.java")
                .contains("/sub/Empty.txt");
    }

    @Test
    void testGetOutputMethod() throws Exception {
        File output = testPath.resolve("testGetOutputMethod.txt").toFile();
        ArgumentContext context = collectionParser.parseCommands(new File("."), new String[]{"-o", output.getCanonicalPath(), basedir});
        Reporter reporter = new Reporter(context.getConfiguration());
        Reporter.Output expected = reporter.execute();
        assertThat(reporter.getOutput()).isEqualTo(expected);
    }

    @Test
    void testDefaultOutput() throws Exception {
        File output = testPath.resolve("captured.txt").toFile();

        PrintStream origin = System.out;
        try (PrintStream out = new PrintStream(output)) {
            System.setOut(out);
            ArgumentContext ctxt = collectionParser.parseCommands(new File("."), new String[]{basedir});
            new Reporter(ctxt.getConfiguration()).execute().format(ctxt.getConfiguration());
        } finally {
            System.setOut(origin);
        }
        assertThat(output).exists();
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        verifyStandardContent(content);
    }
    
    private static Map<String, String> mapOf(String... parts) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < parts.length; i+=2) {
            map.put(parts[i], parts[i+1]);
        }
        return map;
    }

    @Test
    void testXMLOutput() throws Exception {
        Map<String, Map<String, String>> expected = new HashMap<>();
        expected.put("/.hiddenDirectory", mapOf("isDirectory", "true", "mediaType", "application/octet-stream",
                "type", "IGNORED"));
        expected.put("/ILoggerFactory.java", mapOf("encoding", "ISO-8859-1", "mediaType", "text/x-java-source",
                "type", "STANDARD"));
        expected.put("/Image.png", mapOf("mediaType", "image/png", "type", "BINARY"));
        expected.put("/LICENSE", mapOf("encoding", "ISO-8859-1", "mediaType", "text/plain", "type", "NOTICE"));
        expected.put("/NOTICE", mapOf("encoding", "ISO-8859-1", "mediaType", "text/plain", "type", "NOTICE"));
        expected.put("/Source.java", mapOf("encoding", "ISO-8859-1", "mediaType", "text/x-java-source",
                "type", "STANDARD"));
        expected.put("/Text.txt", mapOf("encoding", "ISO-8859-1", "mediaType", "text/plain",
                "type", "STANDARD"));
        expected.put("/TextHttps.txt", mapOf("encoding", "ISO-8859-1", "mediaType", "text/plain",
                "type", "STANDARD"));
        expected.put("/Xml.xml", mapOf("encoding", "ISO-8859-1", "mediaType", "application/xml",
                "type", "STANDARD"));
        expected.put("/buildr.rb", mapOf("encoding", "ISO-8859-1", "mediaType", "text/x-ruby",
                "type", "STANDARD"));
        expected.put("/dummy.jar", mapOf("mediaType", "application/java-archive",
                "type", "ARCHIVE"));
        expected.put("/generated.txt", mapOf("encoding", "ISO-8859-1", "mediaType", "text/plain",
                "type", "IGNORED"));
        expected.put("/plain.json", mapOf("mediaType", "application/json",
                "type", "BINARY"));
        expected.put("/sub/Empty.txt", mapOf("encoding", "UTF-8", "mediaType", "text/plain",
                "type", "STANDARD"));
        expected.put("/tri.txt", mapOf("encoding", "ISO-8859-1", "mediaType", "text/plain",
                "type", "STANDARD"));

        File output = testPath.resolve(".rat/testXMLOutput").toFile();
        org.apache.rat.utils.FileUtils.mkDir(output.getParentFile());
        ArgumentContext ctxt = collectionParser.parseCommands(testPath.toFile(), new String[]{"--output-style", "xml", "--output-file", output.getPath(), basedir});
        new Reporter(ctxt.getConfiguration()).execute().format(ctxt.getConfiguration());

        assertThat(output).exists();
        Document doc = XmlUtils.toDom(java.nio.file.Files.newInputStream(output.toPath()));
        XPath xPath = XPathFactory.newInstance().newXPath();

        for (Map.Entry<String, Map<String, String>> entry : expected.entrySet()) {
            XmlUtils.assertAttributes(doc, xPath, String.format("/rat-report/resource[@name='%s']", entry.getKey()), entry.getValue());
        }

        NodeList nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@approval='false']");
        assertThat(nodeList.getLength()).isEqualTo(2);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@id='AL2.0']");
        assertThat(nodeList.getLength()).isEqualTo(5);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@id='MIT']");
        assertThat(nodeList.getLength()).isEqualTo(1);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@id='BSD-3']");
        assertThat(nodeList.getLength()).isEqualTo(1);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@id='TMF']");
        assertThat(nodeList.getLength()).isEqualTo(1);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@id='?????']");
        assertThat(nodeList.getLength()).isEqualTo(2);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='STANDARD']");
        assertThat(nodeList.getLength()).isEqualTo(8);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='ARCHIVE']");
        assertThat(nodeList.getLength()).isEqualTo(1);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='BINARY']");
        assertThat(nodeList.getLength()).isEqualTo(2);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='IGNORED']");
        assertThat(nodeList.getLength()).isEqualTo(2);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='UNKNOWN']");
        assertThat(nodeList.getLength()).isEqualTo(0);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='NOTICE']");
        assertThat(nodeList.getLength()).isEqualTo(2);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='IGNORED']/license");
        assertThat(nodeList.getLength()).isEqualTo(0);

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@isDirectory='true']");
        assertThat(nodeList.getLength()).isEqualTo(1);

        // check licenses
        Map<String, String> apacheLicense = mapOf("approval", "true", "family", "AL   ", "id", "AL2.0", "name", "Apache License 2.0");
        Map<String, String> unknownLicense = mapOf("approval", "false", "family", "?????",
                "id", "?????", "name", "Unknown license");
        expected = new HashMap<>();
        expected.put("/.hiddenDirectory", Collections.emptyMap());
        expected.put("/ILoggerFactory.java", mapOf("approval", "true", "family", "MIT  ",
                "id", "MIT", "name", "The MIT License"));
        expected.put("/Image/png", Collections.emptyMap());
        expected.put("/LICENSE", Collections.emptyMap());
        expected.put("/NOTICE", Collections.emptyMap());
        expected.put("/Source.java", unknownLicense);
        expected.put("/Text.txt", apacheLicense);
        expected.put("/TextHttps.txt", apacheLicense);
        expected.put("/Xml.xml", apacheLicense);
        expected.put("/buildr.rb", apacheLicense);
        expected.put("/dummy.jar", Collections.emptyMap());
        expected.put("/generated.txt", Collections.emptyMap());
        expected.put("/plain.json", Collections.emptyMap());
        expected.put("/sub/Empty.txt", unknownLicense);
        expected.put("/tri.txt", mapOf("approval", "true", "family", "BSD-3", "id", "TMF",
                "name", "The Telemanagement Forum License"));

        for (Map.Entry<String, Map<String, String>> entry : expected.entrySet()) {
            Map<String, String> attrs = entry.getValue();
            if (attrs.isEmpty()) {
                String xpath = String.format("/rat-report/resource[@name='%s']/license",entry.getKey());
                nodeList = XmlUtils.getNodeList(doc, xPath, xpath);
                assertThat(nodeList.getLength()).as(xpath).isEqualTo(0);
            } else {
                XmlUtils.assertAttributes(doc, xPath, String.format("/rat-report/resource[@name='%s']/license[@id='%s']",
                        entry.getKey(), attrs.get("id")), attrs);
            }
        }
    }

    /**
     * Finds a node via xpath on the document. And then checks family, approval and
     * type of elements of the node.
     *
     * @param doc the document to check
     * @param xpath the XPath instance to use.
     * @param resource the xpath statement to locate the node.
     * @param licenseInfo the license info for the node. (can be null)
     * @param type the type of resource located.
     * @throws Exception on XPath error.
     */
    private static void checkNode(final Document doc, final XPath xpath, final String resource, final LicenseInfo licenseInfo,
                                  final String type) throws Exception {
        XmlUtils.getNode(doc, xpath, String.format("/rat-report/resource[@name='%s'][@type='%s']", resource, type));
        if (licenseInfo != null) {
            XmlUtils.getNode(doc, xpath,
                    String.format("/rat-report/resource[@name='%s'][@type='%s']/license[@id='%s'][@family='%s']",
                            resource, type, licenseInfo.id, licenseInfo.family));
            XmlUtils.getNode(doc, xpath,
                    String.format("/rat-report/resource[@name='%s'][@type='%s']/license[@id='%s'][@approval='%s']",
                            resource, type, licenseInfo.id, licenseInfo.approval));
            if (licenseInfo.hasNotes) {
                XmlUtils.getNode(doc, xpath,
                        String.format("/rat-report/resource[@name='%s'][@type='%s']/license[@id='%s']/notes", resource,
                                type, licenseInfo.id));
            }
        }
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

    private void verifyStandardContent(final String document) {
        assertThat(document)
                .containsPattern(Pattern.compile("^  Notices:\\s*2 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^  Binaries:\\s*2 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^  Archives:\\s*1 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^  Standards:\\s*8 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^  Ignored:\\s*2 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^! Unapproved:\\s*2 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^  Unknown:\\s*2 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^Apache License 2.0: 5 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^BSD 3 clause: 1 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^The MIT License: 1 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^The Telemanagement Forum License: 1 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^Unknown license: 2 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^\\Q?????\\E: 2 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^AL   : 5 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^BSD-3: 2 ", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("^MIT  : 1 ", Pattern.MULTILINE))
                .containsPattern(
                        Pattern.compile("^Files with unapproved licenses\\s+\\*+\\s+" //
                                + "\\Q/Source.java\\E\\s+" //
                                + "\\Q/sub/Empty.txt\\E\\s", Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(true, Type.ARCHIVE, "/dummy.jar"), Pattern.MULTILINE))
                .containsPattern(
                        Pattern.compile(ReporterTestUtils.documentOut(true, Type.STANDARD, "/ILoggerFactory.java")
                                + ReporterTestUtils.licenseOut("MIT", "The MIT License"), Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(true, Type.BINARY, "/Image.png"), Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(true, Type.NOTICE, "/LICENSE"), Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(true, Type.NOTICE, "/NOTICE"), Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(false, Type.STANDARD, "/Source.java")
                        + ReporterTestUtils.UNKNOWN_LICENSE, Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(true, Type.STANDARD, "/Text.txt")
                        + ReporterTestUtils.APACHE_LICENSE, Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(true, Type.STANDARD, "/Xml.xml")
                        + ReporterTestUtils.APACHE_LICENSE, Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(true, Type.STANDARD, "/buildr.rb")
                        + ReporterTestUtils.APACHE_LICENSE, Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(true, Type.STANDARD, "/TextHttps.txt")
                        + ReporterTestUtils.APACHE_LICENSE, Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(true, Type.STANDARD, "/tri.txt")
                        + ReporterTestUtils.APACHE_LICENSE + ReporterTestUtils.licenseOut("BSD-3", "BSD 3 clause")
                        + ReporterTestUtils.licenseOut("BSD-3", "TMF", "The Telemanagement Forum License"), Pattern.MULTILINE))
                .containsPattern(Pattern.compile(ReporterTestUtils.documentOut(false, Type.STANDARD, "/sub/Empty.txt")
                        + ReporterTestUtils.UNKNOWN_LICENSE, Pattern.MULTILINE));
    }

    private Validator initValidator() throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(Reporter.class.getResourceAsStream("/org/apache/rat/rat-report.xsd"));
        Schema schema = factory.newSchema(schemaFile);
        return schema.newValidator();
    }

    @Test
    void xmlReportTest() throws Exception {
        ReportConfiguration configuration = initializeConfiguration();
        Document doc = new Reporter(configuration).execute().getDocument();

        XPath xPath = XPathFactory.newInstance().newXPath();

        XmlUtils.getNode(doc, xPath, "/rat-report[@timestamp]");

        LicenseInfo apacheLic = new LicenseInfo("AL2.0", "AL", true, false);
        checkNode(doc, xPath, "/ILoggerFactory.java", new LicenseInfo("MIT", true, false),
                "STANDARD");
        checkNode(doc, xPath, "/Image.png", null, "BINARY");
        checkNode(doc, xPath, "/LICENSE", null, "NOTICE");
        checkNode(doc, xPath, "/NOTICE", null, "NOTICE");
        checkNode(doc, xPath, "/Source.java", new LicenseInfo("?????", false, false),
                "STANDARD");
        checkNode(doc, xPath, "/Text.txt", apacheLic, "STANDARD");
        checkNode(doc, xPath, "/TextHttps.txt", apacheLic, "STANDARD");
        checkNode(doc, xPath, "/Xml.xml", apacheLic, "STANDARD");
        checkNode(doc, xPath, "/buildr.rb", apacheLic, "STANDARD");
        checkNode(doc, xPath, "/dummy.jar", null, "ARCHIVE");
        checkNode(doc, xPath, "/sub/Empty.txt", new LicenseInfo("?????", false, false),
                "STANDARD");
        checkNode(doc, xPath, "/tri.txt", apacheLic, "STANDARD");
        checkNode(doc, xPath, "/tri.txt", new LicenseInfo("BSD-3", true, false), "STANDARD");
        checkNode(doc, xPath, "/tri.txt", new LicenseInfo("TMF", "BSD-3", true, false),
                "STANDARD");
        checkNode(doc, xPath, "/generated.txt", null, "IGNORED");
        NodeList nodeList = (NodeList) xPath.compile("/rat-report/resource").evaluate(doc, XPathConstants.NODESET);
        assertThat(nodeList.getLength()).isEqualTo(15);
        Validator validator = initValidator();
        try {
            validator.validate(new DOMSource(doc));
        } catch (SAXException e) {
            fail("Missing properties?", e);
        }
    }

    @Test
    void plainReportTest() throws Exception {
        final String NL = System.lineSeparator();
        final String SEPARATOR = "*****************************************************";
        final String HEADER = SEPARATOR + NL + //
                "Summary" + NL + //
                SEPARATOR + NL + //
                "Generated at: ";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ReportConfiguration configuration = initializeConfiguration();
        configuration.setOut(new ReportConfiguration.IODescriptor<>("plainReportTest", () -> out));
        new Reporter(configuration).execute().format(configuration);

        String document = out.toString();

        assertThat(document).doesNotContainPattern("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        assertThat(document).as(() -> "'Generated at' is not present in \n" + document).startsWith(HEADER);

        verifyStandardContent(document);
    }

    @Test
    void unapprovedLicensesReportTest() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ReportConfiguration configuration = initializeConfiguration();
        configuration.setOut(new ReportConfiguration.IODescriptor<>("unapprovedLicensesReportTest", () -> out));
        configuration.setStyleSheet(this.getClass().getResource("/org/apache/rat/unapproved-licenses.xsl"));
        new Reporter(configuration).execute().format(configuration);

        String document = out.toString();

        assertThat(document).containsOnlyOnce("Generated at: ")
                .containsPattern(Pattern.compile("\\Q/Source.java\\E$", Pattern.MULTILINE))
                .containsPattern(Pattern.compile("\\Q/sub/Empty.txt\\E", Pattern.MULTILINE));
    }

    @Test
    void counterMaxTest() throws Exception {
        ReportConfiguration config = initializeConfiguration();
        Reporter.Output output = new Reporter(config).execute();
        assertThat(config.getClaimValidator().hasErrors()).isTrue();
        assertThat(config.getClaimValidator().isValid(ClaimStatistic.Counter.UNAPPROVED, output.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)))
                .isFalse();

        config = initializeConfiguration();
        config.getClaimValidator().setMax(ClaimStatistic.Counter.UNAPPROVED, 2);
        output = new Reporter(config).execute();
        assertThat(config.getClaimValidator().hasErrors()).isFalse();
        assertThat(config.getClaimValidator().isValid(ClaimStatistic.Counter.UNAPPROVED, output.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)))
                .isTrue();
    }

    static Stream<Arguments> getTestData() {
        BaseOptionCollection.Builder builder = BaseOptionCollection.builder()
                        .unsupported(Arg.OUTPUT_FILE);
        return new ReportTestDataProvider().getOptionTests(builder.build()).stream().map(testData ->
                Arguments.of(testData.getTestName(), testData));
    }

    @ParameterizedTest( name = "{index} {0}")
    @MethodSource("getTestData")
    void testReportData(String name, TestData test) throws Exception {
        Path invokePath = testPath.resolve(test.getTestName());
        org.apache.rat.utils.FileUtils.mkDir(invokePath.toFile());

        test.setupFiles(invokePath);
        ArgumentContext ctxt = collectionParser.parseCommands(invokePath.toFile(),
                test.getCommandLine(invokePath.toString()));
        if (test.expectingException()) {
            assertThatThrownBy(() -> new Reporter(ctxt.getConfiguration()).execute()).as("Expected throws from " + name)
                    .hasMessageContaining(test.getExpectedException().getMessage());
            ValidatorData data = new ValidatorData(Reporter.Output.builder().configuration(ctxt.getConfiguration()).build(),
                    invokePath.toString());
            test.getValidator().accept(data);
        } else {
            Reporter.Output output = ctxt.getConfiguration() != null ? new Reporter(ctxt.getConfiguration()).execute() :
                    Reporter.Output.builder().build();
            ValidatorData data = new ValidatorData(output, invokePath.toString());
            data.getOutput().format(data.getConfiguration());
            test.getValidator().accept(data);
        }
    }

    private record LicenseInfo(String id, String family, boolean approval, boolean hasNotes) {
        LicenseInfo(String id, boolean approval, boolean hasNotes) {
            this(id, id, approval, hasNotes);
        }

        private LicenseInfo(String id, String family, boolean approval, boolean hasNotes) {
            this.id = id;
            this.family = ILicenseFamily.makeCategory(family);
            this.approval = approval;
            this.hasNotes = hasNotes;
        }
    }
}
