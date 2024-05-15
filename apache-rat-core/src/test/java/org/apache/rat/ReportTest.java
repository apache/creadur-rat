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
package org.apache.rat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ReportTest {
    @TempDir
    static File tempDirectory;

    @AfterEach
    @EnabledOnOs(OS.WINDOWS)
    void cleanUp() {
        System.gc();
    }

    private ReportConfiguration createConfig(String... args) throws IOException, ParseException {
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), args);
        return Report.createConfiguration(DefaultLog.getInstance(), "target/test-classes/elements", cl);
    }

    @Test
    public void parseExclusionsForCLIUsage() {
        final FilenameFilter filter = Report
                .parseExclusions(DefaultLog.getInstance(), Arrays.asList("", " # foo/bar", "foo", "##", " ./foo/bar"));
        assertNotNull(filter);
    }

    @Test
    public void testDefaultConfiguration() throws ParseException, IOException {
        String[] empty = {};
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), empty);
        ReportConfiguration config = Report.createConfiguration(DefaultLog.getInstance(), "", cl);
        ReportConfigurationTest.validateDefault(config);
    }

    @Test
    public void testOutputOption() throws Exception {
        File output = new File(tempDirectory, "test");
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[]{"-o", output.getCanonicalPath()});
        ReportConfiguration config = Report.createConfiguration(DefaultLog.getInstance(), "target/test-classes/elements", cl);
        new Reporter(config).output();
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("2 Unknown Licenses"));
        assertTrue(content.contains("target/test-classes/elements/Source.java"));
        assertTrue(content.contains("target/test-classes/elements/sub/Empty.txt"));
    }

    @Test
    public void helpTest() {
        Options opts = Report.buildOptions();
        StringWriter out = new StringWriter();
        Report.printUsage(new PrintWriter(out), opts);

        String result = out.toString();
        System.out.println(result);

        TextUtils.assertContains("-a ", result);
        TextUtils.assertContains("-A,--addLicense ", result);
        TextUtils.assertContains("--archive <ProcessingType> ", result);
        TextUtils.assertContains("-c,--copyright <arg> ", result);
        TextUtils.assertContains("-d,--dir <DirOrArchive> ", result);
        TextUtils.assertContains("--dry-run ", result);
        TextUtils.assertContains("-e,--exclude <Expression> ", result);
        TextUtils.assertContains("-E,--exclude-file <FileOrURI> ", result);
        TextUtils.assertContains("-f,--force ", result);
        TextUtils.assertContains("-h,--help ", result);
        TextUtils.assertContains("--licenses <FileOrURI> ", result);
        TextUtils.assertContains("--list-families <LicenseFilter> ", result);
        TextUtils.assertContains("--list-licenses <LicenseFilter> ", result);
        TextUtils.assertContains("--log-level <LogLevel> ", result);
        TextUtils.assertContains("--no-default-licenses ", result);
        TextUtils.assertContains("-o,--out <arg> ", result);
        TextUtils.assertContains("-s,--stylesheet <StyleSheet> ", result);
        TextUtils.assertContains("--scan-hidden-directories ", result);
        TextUtils.assertContains("-x,--xml ", result);
    }

    private static String shortOpt(Option opt) {
        return "-" + opt.getOpt();
    }

    private static String longOpt(Option opt) {
        return "--" + opt.getLongOpt();
    }

    @Test
    public void testXMLOutput() throws Exception {
        ReportConfiguration config = createConfig();
        assertTrue(config.isStyleReport());

        config = createConfig(shortOpt(Report.XML));
        assertFalse(config.isStyleReport());

        config = createConfig(longOpt(Report.XML));
        assertFalse(config.isStyleReport());
    }

    @Test
    public void LicensesOptionTest() throws Exception {
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[]{"-licenses", "target/test-classes/report/LicenseOne.xml"});
        ReportConfiguration config = Report.createConfiguration(DefaultLog.getInstance(),"", cl);
        SortedSet<ILicense> set = config.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
        assertTrue(LicenseSetFactory.search("LiOne", "LiOne", set).isPresent());
        assertFalse(LicenseSetFactory.search("LiOne", "LiTwo", set).isPresent(),"LiOne/LiTwo");
        assertFalse(LicenseSetFactory.search("LiTwo", "LiTwo", set).isPresent(), "LiTwo");
    }

    @Test
    public void LicensesOptionNoDefaultsTest() throws Exception {
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[] {"--no-default", "--licenses", "target/test-classes/report/LicenseOne.xml", "--licenses", "target/test-classes/report/LicenseTwo.xml"});
        ReportConfiguration config = Report.createConfiguration(DefaultLog.getInstance(), "", cl);
        SortedSet<ILicense> set = config.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
        assertEquals(2, set.size());
        assertTrue(LicenseSetFactory.search("LiOne", "LiOne", set).isPresent(), "LiOne");
        assertFalse(LicenseSetFactory.search("LiOne", "LiTwo", set).isPresent(), "LiOne/LiTwo");
        assertTrue(LicenseSetFactory.search("LiTwo", "LiTwo", set).isPresent(), "LiTwo");
    }

    @ParameterizedTest
    @MethodSource("optionsProvider")
    public void testOptionsUpdateConfig(String[] args, Predicate<ReportConfiguration> test) throws Exception {
        ReportConfiguration config = Report.parseCommands(args, (o)-> {}, true);
        assertNotNull(config, "Did not create ReportConfiguration");
        assertTrue(test.test(config));
    }

    static Stream<Arguments> optionsProvider() {

        List<Arguments> lst = new ArrayList<>();
        String[] args;
        Predicate<ReportConfiguration> test;

//        Report.ARCHIVE
        for (ReportConfiguration.Processing processing : ReportConfiguration.Processing.values()) {
            args = new String[]{longOpt(Report.ARCHIVE), processing.name().toLowerCase()};
            test = c -> c.getArchiveProcessing() == processing;
            lst.add(Arguments.of(args, test));
        }
//        Report.COPYRIGHT
        args = new String[]{longOpt(Report.COPYRIGHT), "My copyright statement"};
        test = c -> c.getCopyrightMessage() == null;
        lst.add(Arguments.of(args, test));
        args = new String[]{longOpt(Report.COPYRIGHT), "My copyright statement",
                "--" + Report.ADD.getOptions().stream().filter(o -> !o.isDeprecated()).findAny().get().getLongOpt()};
        test = c -> "My copyright statement".equals(c.getCopyrightMessage());
        lst.add(Arguments.of(args, test));

//        Report.DIR;

//        Report.DRY_RUN;
        args = new String[]{"--" + Report.DRY_RUN.getLongOpt()};
        test = ReportConfiguration::isDryRun;
        lst.add(Arguments.of(args, test));

//        Report.EXCLUDE_CLI;
        args = new String[]{"--" + Report.EXCLUDE_CLI.getLongOpt(), "*.foo","[A-Z]\\.bar", "justbaz"};
        test = c -> {
            FilenameFilter f = c.getFilesToIgnore();

            assertNotNull(f);
            assertFalse(f.accept(tempDirectory, "some.foo" ), "some.foo");
            assertFalse(f.accept(tempDirectory, "B.bar"), "B.bar");
            assertFalse(f.accept(tempDirectory, "justbaz" ), "justbaz");
            assertTrue(f.accept(tempDirectory, "notbaz"), "notbaz");
            return true;
        };
        lst.add(Arguments.of(args, test));

//        Report.EXCLUDE_FILE_CLI;
        File outputFile = new File(tempDirectory, "exclude.txt");
        try (FileWriter fw = new FileWriter(outputFile)) {
            fw.write("*.foo");
            fw.write(System.lineSeparator());
            fw.write("[A-Z]\\.bar");
            fw.write(System.lineSeparator());
            fw.write("justbaz");
            fw.write(System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        args = new String[]{"--" + Report.EXCLUDE_FILE_CLI.getLongOpt(), outputFile.getPath()};
        // same test as above
        lst.add(Arguments.of(args, test));

//        Report.FORCE
        args = new String[]{longOpt(Report.FORCE)};
        test = ReportConfiguration::isAddingLicensesForced;
        lst.add(Arguments.of(args, test.negate()));

        args = new String[]{longOpt(Report.FORCE),
                "--" + Report.ADD.getOptions().stream().filter(o -> !o.isDeprecated()).findAny().get().getLongOpt()};
        lst.add(Arguments.of(args, test));

//        Report.LICENSES -- tested in standalone test: testLicenseOption()

//        Report.LIST_LICENSES;
        for (LicenseSetFactory.LicenseFilter f : LicenseSetFactory.LicenseFilter.values()) {
            args = new String[]{"--" + Report.LIST_LICENSES.getLongOpt(), f.name().toLowerCase()};
            test = c -> c.listLicenses() == f;
            lst.add(Arguments.of(args, test));
        }

//        Report.LIST_FAMILIES
        for (LicenseSetFactory.LicenseFilter f : LicenseSetFactory.LicenseFilter.values()) {
            args = new String[]{"--" + Report.LIST_FAMILIES.getLongOpt(), f.name().toLowerCase()};
            test = c -> c.listFamilies() == f;
            lst.add(Arguments.of(args, test));
        }

//        Report LOG_LEVEL
        DefaultLog log = (DefaultLog) DefaultLog.getInstance();
        for (Log.Level l : Log.Level.values()) {
            args = new String[]{"--" + Report.LOG_LEVEL.getLongOpt(), l.name().toLowerCase()};
            test = c -> log.getLevel() == l;
            lst.add(Arguments.of(args, test));
        }
//        Report.NO_DEFAULTS
        args = new String[]{"--" + Report.NO_DEFAULTS.getLongOpt()};
        test = c -> {
            assertEquals(0, c.getLicenses(LicenseSetFactory.LicenseFilter.ALL).size());
            return true;
        };
        lst.add(Arguments.of(args, test));

//        Report.OUT;
        File outFile = new File( tempDirectory, "outexample");
        args = new String[] {"--"+Report.OUT.getLongOpt(), outFile.getAbsolutePath()};
        test = c -> {
            try (OutputStream os = c.getOutput().get()) {
                os.write("Hello world".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream(outFile)))) {
                return "Hello world".equals(reader.readLine());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        lst.add(Arguments.of(args, test));

//        Report.SCAN_HIDDEN_DIRECTORIES
        args = new String[]{"--" + Report.SCAN_HIDDEN_DIRECTORIES.getLongOpt()};
        test = c -> c.getDirectoriesToIgnore() == FalseFileFilter.FALSE;
        lst.add(Arguments.of(args, test));

//        Report.STYLESHEET_CLI
        test = c -> c.getStyleSheet() != null;
        for (String sheet : new String[]{"plain-rat", "missing-headers", "unapproved-licenses"}) {
            args = new String[]{"--" + Report.STYLESHEET_CLI.getLongOpt(), sheet};
            lst.add(Arguments.of(args, test));
        }
        URL url = ReportTest.class.getResource("MatcherContainerResource.txt");
        args = new String[]{"--" + Report.STYLESHEET_CLI.getLongOpt(), url.getFile()};
        lst.add(Arguments.of(args, test));

//        Report.XML;
        args = new String[]{longOpt(Report.XML)};
        test = ReportConfiguration::isStyleReport;
        lst.add(Arguments.of(args, test.negate()));
        return lst.stream();
    }

    @Test
    public void testDeprecatedUseLogged() throws IOException {
        TestingLog log = new TestingLog();
        try {
            DefaultLog.setInstance(log);
            String[] args = {longOpt(Report.DIR), "foo", "-a"};
            ReportConfiguration config = Report.parseCommands(args, (o) -> {
            }, true);

        } finally {
            DefaultLog.setInstance(null);
        }
        log.assertContains("WARN: Option [-d, --dir] used.  Deprecated for removal since 0.17.0: Use '--'");
        log.assertContains("WARN: Option [-a] used.  Deprecated for removal since 0.17.0: Use '-A or --addLicense'");
    }

    @Test
    public void testLicenseOption() throws IOException {
        File cfgFile = new File( tempDirectory, "test.xml");
        try (IXmlWriter writer = new XmlWriter(new FileWriter(cfgFile))) {
            writer.openElement("rat-config").openElement("families").openElement("family")
                    .attribute("id","TEST").attribute("name", "Test license family")
                    .closeElement().closeElement() // closed families
                    .openElement("licenses").openElement("license")
                    .attribute("family", "TEST")
                    .attribute("id", "TEST-1").attribute("name", "Test license")
                    .openElement("text").content("Hello world").closeElement()
                    .closeDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String[] args = new String[]{longOpt(Report.LICENSES), new File( tempDirectory, "test.xml").getPath(), longOpt(Report.NO_DEFAULTS)};
        ReportConfiguration config = Report.parseCommands(args, (o)-> {}, true);
        assertNotNull(config, "Did not create ReportConfiguraiton");
        assertEquals(1, config.getLicenses(LicenseSetFactory.LicenseFilter.ALL).size());
    }
}
