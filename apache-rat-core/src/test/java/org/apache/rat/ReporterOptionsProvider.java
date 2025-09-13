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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.config.results.ClaimValidator;
import org.apache.rat.configuration.builders.SpdxBuilder;
import org.apache.rat.help.Help;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.test.AbstractOptionsProvider;
import org.apache.rat.test.utils.OptionFormatter;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.FileUtils;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.apache.rat.commandline.Arg.HELP_LICENSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * A class to provide the Options and tests to the testOptionsUpdateConfig.
 */
class ReporterOptionsProvider extends AbstractOptionsProvider implements ArgumentsProvider {
    static File sourceDir;

    /**
     * A flag to determine if help was called
     */
    final AtomicBoolean helpCalled = new AtomicBoolean(false);

    public ReporterOptionsProvider() {
        super(ReporterOptionsTest.testPath.toFile());
        processTestFunctionAnnotations();
        testMap.put("addLicense", this::addLicenseTest);
        testMap.remove("add-license");
        testMap.put("dir", () -> DefaultLog.getInstance().info("--dir has no valid test"));
        super.validate(Collections.emptyList());
    }

    /**
     * Generate a ReportConfiguration from a set of arguments.
     * Forces the {@code helpCalled} flag to be reset.
     *
     * @param args the arguments.
     * @return A ReportConfiguration
     * @throws IOException on critical error.
     */
    @Override
    protected final ReportConfiguration generateConfig(List<Pair<Option, String[]>> args) throws IOException {
        return generateConfig(args, false);
    }

    protected final ReportConfiguration generateConfig(List<Pair<Option, String[]>> args, boolean helpExpected) throws IOException {
        if (sourceDir == null) {
            throw new IOException("sourceDir not set");
        }
        helpCalled.set(false);
        ReportConfiguration config = OptionCollection.parseCommands(sourceDir, extractArgs(args), o -> helpCalled.set(true), true);
        assertThat(helpCalled.get()).as("Help was called").isEqualTo(helpExpected);
        if (config != null && !config.hasSource()) {
            config.addSource(OptionCollection.getReportable(sourceDir, config));
        }
        return config;
    }

    private void configureSourceDir(Option option) {
        sourceDir = new File(baseDir, OptionFormatter.getName(option));
        FileUtils.mkDir(sourceDir);
    }

    private void validateNoArgSetup() throws IOException, RatException {
        // verify that without args the report is ok.
        TestingLog log = new TestingLog();
        DefaultLog.setInstance(log);
        try {
            ReportConfiguration config = generateConfig(Collections.emptyList());
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            ClaimValidator validator = config.getClaimValidator();
            assertThat(validator.listIssues(claimStatistic)).isEmpty();
        } finally {
            DefaultLog.setInstance(null);
        }

    }

    @OptionCollectionTest.TestFunction
    protected void addLicenseTest() {
        editLicenseTest(Arg.EDIT_ADD.find("addLicense"));
    }

    @OptionCollectionTest.TestFunction
    protected void editLicenseTest() {
        editLicenseTest(Arg.EDIT_ADD.find("edit-license"));
    }

    private void editLicenseTest(final Option option) {
        try {
            configureSourceDir(option);
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, null));
            File testFile = writeFile("NoLicense.java", "class NoLicense {}");
            File resultFile = new File(sourceDir, "NoLicense.java.new");
            FileUtils.delete(resultFile);

            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic).isNotNull();
            String contents = String.join("\n", IOUtils.readLines(new FileReader(testFile)));
            assertThat(contents).isEqualTo("class NoLicense {}");
            assertThat(resultFile).exists();
            contents = String.join("\n", IOUtils.readLines(new FileReader(resultFile)));
            assertThat(contents).isEqualTo("/*\n" +
                    " * Licensed to the Apache Software Foundation (ASF) under one\n" +
                    " * or more contributor license agreements.  See the NOTICE file\n" +
                    " * distributed with this work for additional information\n" +
                    " * regarding copyright ownership.  The ASF licenses this file\n" +
                    " * to you under the Apache License, Version 2.0 (the\n" +
                    " * \"License\"); you may not use this file except in compliance\n" +
                    " * with the License.  You may obtain a copy of the License at\n" +
                    " * \n" +
                    " *   http://www.apache.org/licenses/LICENSE-2.0\n" +
                    " * \n" +
                    " * Unless required by applicable law or agreed to in writing,\n" +
                    " * software distributed under the License is distributed on an\n" +
                    " * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
                    " * KIND, either express or implied.  See the License for the\n" +
                    " * specific language governing permissions and limitations\n" +
                    " * under the License.\n" +
                    " */\n\n" +
                    "class NoLicense {}");
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    protected File writeFile(String name) {
        return FileUtils.writeFile(sourceDir, name, Collections.singletonList(name));
    }

    protected File writeFile(String name, String content) {
        return FileUtils.writeFile(sourceDir, name, Collections.singletonList(content));
    }

    protected File writeFile(String name, Iterable<String> content) {
        return FileUtils.writeFile(sourceDir, name, content);
    }

    @OptionCollectionTest.TestFunction
    private void execLicensesDeniedTest(final Option option, final String[] args) {
        try {
            configureSourceDir(option);
            File illumosFile = writeFile("illumousFile.java", "The contents of this file are " +
                    "subject to the terms of the Common Development and Distribution License (the \"License\") You " +
                    "may not use this file except in compliance with the License.");

            validateNoArgSetup();

            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            ClaimValidator validator = config.getClaimValidator();
            assertThat(validator.listIssues(claimStatistic)).containsExactly("UNAPPROVED");
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void licensesDeniedTest() {
        execLicensesDeniedTest(Arg.LICENSES_DENIED.find("licenses-denied"), new String[]{"ILLUMOS"});
    }

    @OptionCollectionTest.TestFunction
    protected void licensesDeniedFileTest() {
        File outputFile = FileUtils.writeFile(baseDir, "licensesDenied.txt", Collections.singletonList("ILLUMOS"));
        execLicensesDeniedTest(Arg.LICENSES_DENIED_FILE.find("licenses-denied-file"),
                new String[]{outputFile.getAbsolutePath()});
    }

    private void noDefaultsTest(final Option option) {
        try {
            configureSourceDir(option);
            File testFile = writeFile("Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Apache-2.0\n",
                    "*/\n\n", "class Test {}\n"));

            validateNoArgSetup();

            ReportConfiguration config = generateConfig(ImmutablePair.of(option, null));
            Reporter reporter = new Reporter(config);
            try {
                reporter.execute();
                fail("Should have thrown exception");
            } catch (RatException e) {
                ClaimStatistic claimStatistic = reporter.getClaimsStatistic();
                ClaimValidator validator = config.getClaimValidator();
                assertThat(validator.listIssues(claimStatistic)).containsExactlyInAnyOrder("DOCUMENT_TYPES", "LICENSE_CATEGORIES", "LICENSE_NAMES", "STANDARDS");
            }
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void noDefaultLicensesTest() {
        noDefaultsTest(Arg.CONFIGURATION_NO_DEFAULTS.find("no-default-licenses"));
    }

    @OptionCollectionTest.TestFunction
    protected void configurationNoDefaultsTest() {
        noDefaultsTest(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"));
    }

    @OptionCollectionTest.TestFunction
    protected void counterMaxTest() {
        Option option = Arg.COUNTER_MAX.option();
        String[] arg = {null};
        try {
            configureSourceDir(option);
            File testFile = writeFile("Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Unapproved\n",
                    "*/\n\n", "class Test {}\n"));

            ReportConfiguration config = generateConfig(Collections.emptyList());
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            ClaimValidator validator = config.getClaimValidator();
            assertThat(validator.listIssues(claimStatistic)).containsExactly("UNAPPROVED");

            arg[0] = "Unapproved:1";
            config = generateConfig(ImmutablePair.of(option, arg));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            validator = config.getClaimValidator();
            assertThat(validator.listIssues(claimStatistic)).isEmpty();
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void counterMinTest() {
        Option option = Arg.COUNTER_MIN.option();
        String[] arg = {null};

        try {
            configureSourceDir(option);
            File testFile = writeFile("Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Unapproved\n",
                    "*/\n\n", "class Test {}\n"));

            ReportConfiguration config = generateConfig(Collections.emptyList());
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            ClaimValidator validator = config.getClaimValidator();
            assertThat(validator.listIssues(claimStatistic)).containsExactly("UNAPPROVED");

            arg[0] = "Unapproved:1";
            config = generateConfig(ImmutablePair.of(option, arg));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            validator = config.getClaimValidator();
            assertThat(validator.listIssues(claimStatistic)).isEmpty();
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    // exclude tests
    private void execExcludeTest(final Option option, final String[] args) {
        String[] notExcluded = {"notbaz", "well._afile"};
        String[] excluded = {"some.foo", "B.bar", "justbaz"};
        try {
            configureSourceDir(option);
            writeFile("notbaz");
            writeFile("well._afile");
            writeFile("some.foo");
            writeFile("B.bar");
            writeFile("justbaz");

            ReportConfiguration config = generateConfig(Collections.emptyList());
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(5);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);

            // filter out source
            config = generateConfig(ImmutablePair.of(option, args));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(3);
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    private void excludeFileTest(final Option option) {
        configureSourceDir(option);
        File outputFile = FileUtils.writeFile(baseDir, "exclude.txt", Arrays.asList(EXCLUDE_ARGS));
        execExcludeTest(option, new String[]{outputFile.getAbsolutePath()});
    }

    @OptionCollectionTest.TestFunction
    protected void excludeFileTest() {
        excludeFileTest(Arg.EXCLUDE_FILE.find("exclude-file"));
    }

    @OptionCollectionTest.TestFunction
    protected void inputExcludeFileTest() {
        excludeFileTest(Arg.EXCLUDE_FILE.find("input-exclude-file"));
    }

    @OptionCollectionTest.TestFunction
    protected void excludeTest() {
        execExcludeTest(Arg.EXCLUDE.find("exclude"), EXCLUDE_ARGS);
    }

    @OptionCollectionTest.TestFunction
    protected void inputExcludeTest() {
        execExcludeTest(Arg.EXCLUDE.find("input-exclude"), EXCLUDE_ARGS);
    }

    @OptionCollectionTest.TestFunction
    protected void inputExcludeSizeTest() {
        Option option = Arg.EXCLUDE_SIZE.option();
        String[] args = {"5"};

        try {
            configureSourceDir(option);
            writeFile("Hi.txt", "Hi");
            writeFile("Hello.txt", "Hello");
            writeFile("HelloWorld.txt", "HelloWorld");

            ReportConfiguration config = generateConfig(Collections.emptyList());
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);

            // filter out source
            config = generateConfig(ImmutablePair.of(option, args));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void inputExcludeStdTest() {
        Option option = Arg.EXCLUDE_STD.find("input-exclude-std");
        String[] args = {StandardCollection.MAVEN.name()};
        // these files are excluded by default "afile~", ".#afile", "%afile%", "._afile"
        // these files are not excluded by default "afile~more", "what.#afile", "%afile%withMore", "well._afile", "build.log"
        // build.log is excluded by MAVEN.
        try {
            configureSourceDir(option);
            writeFile("afile~");
            writeFile(".#afile");
            writeFile("%afile%");
            writeFile("._afile");
            writeFile("afile~more");
            writeFile("what.#afile");
            writeFile("%afile%withMore");
            writeFile("well._afile");
            writeFile("build.log");

            ReportConfiguration config = generateConfig(Collections.emptyList());
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(5);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(4);

            config = generateConfig(ImmutablePair.of(option, args));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(4);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(5);
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void inputExcludeParsedScmTest() {
        Option option = Arg.EXCLUDE_PARSE_SCM.find("input-exclude-parsed-scm");
        String[] args = {"GIT"};
        String[] lines = {
                "# somethings",
                "!thingone", "thing*", System.lineSeparator(),
                "# some fish",
                "**/fish", "*_fish",
                "# some colorful directories",
                "red/", "blue/*/"};

        try {
            configureSourceDir(option);

            writeFile(".gitignore", Arrays.asList(lines));
            writeFile("thingone");
            writeFile("thingtwo");

            File dir = new File(sourceDir, "dir");
            FileUtils.mkDir(dir);
            FileUtils.writeFile(dir, "fish_two");
            FileUtils.writeFile(dir, "fish");

            dir = new File(sourceDir, "red");
            FileUtils.mkDir(dir);
            FileUtils.writeFile(dir, "fish");

            dir = new File(sourceDir, "blue/fish");
            FileUtils.mkDir(dir);
            FileUtils.writeFile(dir, "dory");

            dir = new File(sourceDir, "some");
            FileUtils.mkDir(dir);
            FileUtils.writeFile(dir, "fish");
            FileUtils.writeFile(dir, "things");
            FileUtils.writeFile(dir, "thingone");

            dir = new File(sourceDir, "another");
            FileUtils.mkDir(dir);
            FileUtils.writeFile(dir, "red_fish");

            ReportConfiguration config = generateConfig(Collections.emptyList());
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(11);
            // .gitignore is ignored by default as it is hidden but not counted
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);

            config = generateConfig(ImmutablePair.of(option, args));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(8);
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    // include tests
    private void execIncludeTest(final Option option, final String[] args) {
        Option excludeOption = Arg.EXCLUDE.option();
        String[] notExcluded = {"B.bar", "justbaz", "notbaz"};
        String[] excluded = {"some.foo"};
        try {
            configureSourceDir(option);
            writeFile("notbaz");
            writeFile("some.foo");
            writeFile("B.bar");
            writeFile("justbaz");

            ReportConfiguration config = generateConfig(Collections.emptyList());
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(4);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);

            // verify exclude removes most files.
            config = generateConfig(ImmutablePair.of(excludeOption, EXCLUDE_ARGS));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
            // .gitignore is ignored by default as it is hidden but not counted
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(3);

            // verify include pust them back
            config = generateConfig(ImmutablePair.of(option, args), ImmutablePair.of(excludeOption, EXCLUDE_ARGS));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
            // .gitignore is ignored by default as it is hidden but not counted
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    private void includeFileTest(final Option option) {
        File outputFile = FileUtils.writeFile(baseDir, "include.txt", Arrays.asList(INCLUDE_ARGS));
        execIncludeTest(option, new String[]{outputFile.getAbsolutePath()});
    }

    @OptionCollectionTest.TestFunction
    protected void inputIncludeFileTest() {
        includeFileTest(Arg.INCLUDE_FILE.find("input-include-file"));
    }

    @OptionCollectionTest.TestFunction
    protected void includesFileTest() {
        includeFileTest(Arg.INCLUDE_FILE.find("includes-file"));
    }

    @OptionCollectionTest.TestFunction
    protected void includeTest() {
        execIncludeTest(Arg.INCLUDE.find("include"), INCLUDE_ARGS);
    }

    @OptionCollectionTest.TestFunction
    protected void inputIncludeTest() {
        execIncludeTest(Arg.INCLUDE.find("input-include"), INCLUDE_ARGS);
    }

    @OptionCollectionTest.TestFunction
    protected void inputIncludeStdTest() {
        Option option = Arg.INCLUDE_STD.find("input-include-std");
        String[] args = {StandardCollection.MISC.name()};
        try {
            configureSourceDir(option);

            writeFile("afile~more");
            writeFile("afile~");
            writeFile(".#afile");
            writeFile("%afile%");
            writeFile("._afile");
            writeFile("what.#afile");
            writeFile("%afile%withMore");
            writeFile("well._afile");

            ImmutablePair<Option, String[]> excludes = ImmutablePair.of(Arg.EXCLUDE.find("input-exclude"),
                    new String[]{"*~more", "*~"});

            ReportConfiguration config = generateConfig(Collections.singletonList(excludes));
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(5);

            config = generateConfig(excludes, ImmutablePair.of(option, args));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(7);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void inputSourceTest() {
        Option option = Arg.SOURCE.find("input-source");
        try {
            configureSourceDir(option);

            writeFile("codefile");
            File inputFile = writeFile("intput.txt", "codefile");
            writeFile("notcodFile");

            ReportConfiguration config = generateConfig();
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);

            config = generateConfig(ImmutablePair.of(option, new String[]{inputFile.getAbsolutePath()}));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    private ReportConfiguration addCatzLicense(ReportConfiguration config) {
        String catz = ILicenseFamily.makeCategory("catz");
        config.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory(catz).setLicenseFamilyName("catz").build());
        config.addLicense(ILicense.builder().setFamily(catz)
                .setMatcher(new SpdxBuilder().setName("catz")));
        return config;
    }

    private void execLicenseFamiliesApprovedTest(final Option option, final String[] args) {
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, args);
        try {
            configureSourceDir(option);
            // write the catz licensed text file
            writeFile("catz.txt", "SPDX-License-Identifier: catz");

            ReportConfiguration config = addCatzLicense(generateConfig());
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();

            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(0);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);

            config = addCatzLicense(generateConfig(arg1));
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void licenseFamiliesApprovedFileTest() {
        Option option = Arg.FAMILIES_APPROVED_FILE.find("license-families-approved-file");
        File outputFile = FileUtils.writeFile(baseDir, "familiesApproved.txt", Collections.singletonList("catz"));
        execLicenseFamiliesApprovedTest(option, new String[]{outputFile.getAbsolutePath()});
    }

    @OptionCollectionTest.TestFunction
    protected void licenseFamiliesApprovedTest() {
        execLicenseFamiliesApprovedTest(Arg.FAMILIES_APPROVED.find("license-families-approved"),
                new String[]{"catz"});
    }

    private void execLicenseFamiliesDeniedTest(final Option option, final String[] args) {
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, args);
        String bsd = ILicenseFamily.makeCategory("BSD-3");
        try {
            configureSourceDir(option);

            // write the catz licensed text file
            writeFile("bsd.txt", "SPDX-License-Identifier: BSD-3-Clause");

            ReportConfiguration config = generateConfig();
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);

            config = generateConfig(arg1);
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(0);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void licenseFamiliesDeniedFileTest() {
        File outputFile = FileUtils.writeFile(baseDir, "familiesDenied.txt", Collections.singletonList("BSD-3"));
        execLicenseFamiliesDeniedTest(Arg.FAMILIES_DENIED_FILE.find("license-families-denied-file"),
                new String[]{outputFile.getAbsolutePath()});
    }

    @OptionCollectionTest.TestFunction
    protected void licenseFamiliesDeniedTest() {
        execLicenseFamiliesDeniedTest(Arg.FAMILIES_DENIED.find("license-families-denied"),
                new String[]{"BSD-3"});
    }


    private void configTest(final Option option) {
        try {
            configureSourceDir(option);
            String[] args = {
                    Resources.getResourceFile("OptionTools/One.xml").getAbsolutePath(),
                    Resources.getResourceFile("OptionTools/Two.xml").getAbsolutePath()};

            Pair<Option, String[]> arg1 = ImmutablePair.of(option, args);

            writeFile("bsd.txt", "SPDX-License-Identifier: BSD-3-Clause");
            writeFile("one.txt", "one is the lonelest number");

            ReportConfiguration config = generateConfig();
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);

            config = generateConfig(arg1);
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(2);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);

            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"), null);

            config = generateConfig(arg1, arg2);
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);

        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void licensesTest() {
        configTest(Arg.CONFIGURATION.find("licenses"));
    }

    @OptionCollectionTest.TestFunction
    protected void configTest() {
        configTest(Arg.CONFIGURATION.find("config"));
    }

    @OptionCollectionTest.TestFunction
    protected void execLicensesApprovedTest(final Option option, String[] args) {
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, args);

        try {
            configureSourceDir(option);

            writeFile("gpl.txt", "SPDX-License-Identifier: GPL-1.0-only");
            writeFile("apl.txt", "SPDX-License-Identifier: Apache-2.0");

            ReportConfiguration config = generateConfig();
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);

            config = generateConfig(arg1);
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(2);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);


        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void licensesApprovedFileTest() {
        File outputFile = FileUtils.writeFile(baseDir, "licensesApproved.txt", Collections.singletonList("GPL1"));
        execLicensesApprovedTest(Arg.LICENSES_APPROVED_FILE.find("licenses-approved-file"),
                new String[]{outputFile.getAbsolutePath()});
    }

    @OptionCollectionTest.TestFunction
    protected void licensesApprovedTest() {
        execLicensesApprovedTest(Arg.LICENSES_APPROVED.find("licenses-approved"),
                new String[]{"GPL1"});
    }

    @OptionCollectionTest.TestFunction
    protected void scanHiddenDirectoriesTest() {
        Option option = Arg.INCLUDE_STD.find("scan-hidden-directories");
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, null);

        try {
            configureSourceDir(option);

            writeFile("apl.txt", "SPDX-License-Identifier: Apache-2.0");

            File hiddenDir = new File(sourceDir, ".hiddendir");
            FileUtils.mkDir(hiddenDir);
            FileUtils.writeFile(hiddenDir, "gpl.txt", Collections.singletonList("SPDX-License-Identifier: GPL-1.0-only"));

            ReportConfiguration config = generateConfig();
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);

            config = generateConfig(arg1);
            reporter = new Reporter(config);
            claimStatistic = reporter.execute();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    private void outTest(final Option option) {
        try {
            configureSourceDir(option);
            writeFile("apl.txt", "SPDX-License-Identifier: Apache-2.0");
            File outFile = new File(sourceDir, "outexample");
            FileUtils.delete(outFile);
            String[] args = new String[]{outFile.getAbsolutePath()};

            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.output();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);

            String actualText = TextUtils.readFile(outFile);
            TextUtils.assertContainsExactly(1, "Apache License Version 2.0: 1 ", actualText);
            TextUtils.assertContainsExactly(1, "STANDARD: 1 ", actualText);

        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void outTest() {
        outTest(Arg.OUTPUT_FILE.find("out"));
    }

    @OptionCollectionTest.TestFunction
    protected void outputFileTest() {
        outTest(Arg.OUTPUT_FILE.find("output-file"));
    }

    private void styleSheetTest(final Option option) {
        PrintStream origin = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(baos)) {
            System.setOut(out);
            configureSourceDir(option);
            // create a dummy stylesheet so that we have a local file for users of the testing jar.
            File file = writeFile("stylesheet", "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                    "    <xsl:template match=\"@*|node()\">\n" +
                    "        Hello world\n" +
                    "    </xsl:template>\n" +
                    "</xsl:stylesheet>");

            String[] args = {null};
            for (StyleSheets sheet : StyleSheets.values()) {
                args[0] = sheet.arg();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                Reporter reporter = new Reporter(config);
                ClaimStatistic claimStatistic = reporter.output();
                assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(0);
                assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);

                String actualText = baos.toString(StandardCharsets.UTF_8.name());
                switch (sheet) {
                    case MISSING_HEADERS:
                        TextUtils.assertContainsExactly(1, "Files with missing headers:" + System.lineSeparator() +
                                "  /stylesheet", actualText);
                        break;
                    case PLAIN:
                        TextUtils.assertContainsExactly(1, "Unknown license: 1 ", actualText);
                        TextUtils.assertContainsExactly(1, "?????: 1 ", actualText);
                        break;
                    case XML:
                        TextUtils.assertContainsExactly(1, "<resource encoding=\"ISO-8859-1\" mediaType=\"text/plain\" name=\"/stylesheet\" type=\"STANDARD\">", actualText);
                        break;
                    case UNAPPROVED_LICENSES:
                        TextUtils.assertContainsExactly(1, "Files with unapproved licenses:" + System.lineSeparator() + "  /stylesheet", actualText);
                        break;
                    default:
                        fail("No test for stylesheet " + sheet);
                        break;
                }
                baos.reset();
            }
            args[0] = file.getAbsolutePath();
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.output();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(0);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);

            String actualText = baos.toString(StandardCharsets.UTF_8.name());
            TextUtils.assertContainsExactly(1, "Hello world", actualText);

        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        } finally {
            System.setOut(origin);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void stylesheetTest() {
        styleSheetTest(Arg.OUTPUT_STYLE.find("stylesheet"));
    }

    @OptionCollectionTest.TestFunction
    protected void outputStyleTest() {
        styleSheetTest(Arg.OUTPUT_STYLE.find("output-style"));
    }

    @OptionCollectionTest.TestFunction
    protected void xmlTest() {
        PrintStream origin = System.out;
        Option option = Arg.OUTPUT_STYLE.find("xml");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(baos)) {
            System.setOut(out);
            configureSourceDir(option);
            // create a dummy stylesheet so that we match the stylesheet tests.
            File file = writeFile("stylesheet", "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                    "    <xsl:template match=\"@*|node()\">\n" +
                    "        Hello world\n" +
                    "    </xsl:template>\n" +
                    "</xsl:stylesheet>");

            ReportConfiguration config = generateConfig(ImmutablePair.of(option, null));
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.output();
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(0);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);

            String actualText = baos.toString(StandardCharsets.UTF_8.name());
            TextUtils.assertContainsExactly(1, "<resource encoding=\"ISO-8859-1\" mediaType=\"text/plain\" name=\"/stylesheet\" type=\"STANDARD\">", actualText);

            try (InputStream expected = StyleSheets.getStyleSheet("xml").get();
                 InputStream actual = config.getStyleSheet().get()) {
                assertThat(IOUtils.contentEquals(expected, actual)).as("'xml' does not match").isTrue();
            }
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        } finally {
            System.setOut(origin);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void logLevelTest() {
        PrintStream origin = System.out;
        Option option = Arg.LOG_LEVEL.find("log-level");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Log.Level oldLevel = DefaultLog.getInstance().getLevel();
        try (PrintStream out = new PrintStream(baos)) {
            System.setOut(out);
            configureSourceDir(option);

            ReportConfiguration config = generateConfig();
            Reporter reporter = new Reporter(config);
            reporter.output();
            TextUtils.assertNotContains("DEBUG", baos.toString(StandardCharsets.UTF_8.name()));

            config = generateConfig(ImmutablePair.of(option, new String[]{"debug"}));
            reporter = new Reporter(config);
            reporter.output();
            TextUtils.assertContains("DEBUG", baos.toString(StandardCharsets.UTF_8.name()));
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        } finally {
            System.setOut(origin);
            DefaultLog.getInstance().setLevel(oldLevel);
        }
    }

    private void listLicenses(final Option option) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        String[] args = {null};

        try {
            configureSourceDir(option);
            File outFile = new File(sourceDir, "out.xml");
            FileUtils.delete(outFile);
            ImmutablePair<Option, String[]> outputFile = ImmutablePair.of(Arg.OUTPUT_FILE.option(), new String[]{outFile.getAbsolutePath()});
            ImmutablePair<Option, String[]> stylesheet = ImmutablePair.of(Arg.OUTPUT_STYLE.option(), new String[]{StyleSheets.XML.arg()});
            for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
                args[0] = filter.name();
                ReportConfiguration config = generateConfig(outputFile, stylesheet, ImmutablePair.of(option, args));
                Reporter reporter = new Reporter(config);
                reporter.output();
                Document document = XmlUtils.toDom(new FileInputStream(outFile));
                switch (filter) {
                    case ALL:
                        XmlUtils.assertIsPresent(filter.name(), document, xPath, "/rat-report/rat-config/licenses/license[@id='AL']");
                        XmlUtils.assertIsPresent(filter.name(), document, xPath, "/rat-report/rat-config/licenses/license[@id='GPL1']");
                        break;
                    case APPROVED:
                        XmlUtils.assertIsPresent(filter.name(), document, xPath, "/rat-report/rat-config/licenses/license[@id='AL']");
                        XmlUtils.assertIsNotPresent(filter.name(), document, xPath, "/rat-report/rat-config/licenses/license[@id='GPL1']");
                        break;
                    case NONE:
                        XmlUtils.assertIsNotPresent(filter.name(), document, xPath, "/rat-report/rat-config/licenses/license[@id='AL']");
                        XmlUtils.assertIsNotPresent(filter.name(), document, xPath, "/rat-report/rat-config/licenses/license[@id='GPL1']");
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected filter: " + filter);
                }
            }
        } catch (IOException | RatException | SAXException | ParserConfigurationException |
                 XPathExpressionException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void listLicensesTest() {
        listLicenses(Arg.OUTPUT_LICENSES.find("list-licenses"));
    }

    @OptionCollectionTest.TestFunction
    protected void outputLicensesTest() {
        listLicenses(Arg.OUTPUT_LICENSES.find("output-licenses"));
    }

    private void listFamilies(final Option option) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        String[] args = {null};

        try {
            configureSourceDir(option);
            File outFile = new File(sourceDir, "out.xml");
            FileUtils.delete(outFile);
            ImmutablePair<Option, String[]> outputFile = ImmutablePair.of(Arg.OUTPUT_FILE.option(), new String[]{outFile.getAbsolutePath()});
            ImmutablePair<Option, String[]> stylesheet = ImmutablePair.of(Arg.OUTPUT_STYLE.option(), new String[]{StyleSheets.XML.arg()});
            for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
                args[0] = filter.name();
                ReportConfiguration config = generateConfig(outputFile, stylesheet, ImmutablePair.of(option, args));
                Reporter reporter = new Reporter(config);
                reporter.output();
                Document document = XmlUtils.toDom(Files.newInputStream(outFile.toPath()));
                switch (filter) {
                    case ALL:
                        XmlUtils.assertIsPresent(filter.name(), document, xPath, "/rat-report/rat-config/families/family[@id='AL']");
                        XmlUtils.assertIsPresent(filter.name(), document, xPath, "/rat-report/rat-config/families/family[@id='GPL']");
                        break;
                    case APPROVED:
                        XmlUtils.assertIsPresent(filter.name(), document, xPath, "/rat-report/rat-config/families/family[@id='AL']");
                        XmlUtils.assertIsNotPresent(filter.name(), document, xPath, "/rat-report/rat-config/families/family[@id='GPL']");
                        break;
                    case NONE:
                        XmlUtils.assertIsNotPresent(filter.name(), document, xPath, "/rat-report/rat-config/families/family[@id='AL']");
                        XmlUtils.assertIsNotPresent(filter.name(), document, xPath, "/rat-report/rat-config/families/family[@id='GPL']");
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected filter: " + filter);
                }
            }
        } catch (IOException | RatException | SAXException | ParserConfigurationException |
                 XPathExpressionException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void listFamiliesTest() {
        listFamilies(Arg.OUTPUT_FAMILIES.find("list-families"));
    }

    @OptionCollectionTest.TestFunction
    protected void outputFamiliesTest() {
        listFamilies(Arg.OUTPUT_FAMILIES.find("output-families"));
    }

    private void archiveTest(final Option option) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        String[] args = {null};

        try {
            configureSourceDir(option);
            File outFile = new File(sourceDir, "out.xml");
            FileUtils.delete(outFile);
            ImmutablePair<Option, String[]> outputFile = ImmutablePair.of(Arg.OUTPUT_FILE.option(), new String[]{outFile.getAbsolutePath()});
            ImmutablePair<Option, String[]> stylesheet = ImmutablePair.of(Arg.OUTPUT_STYLE.option(), new String[]{StyleSheets.XML.arg()});
            File archive = Resources.getResourceFile("tikaFiles/archive/dummy.jar");
            File localArchive = new File(sourceDir, "dummy.jar");
            try (InputStream in = Files.newInputStream(archive.toPath());
                 OutputStream out = Files.newOutputStream(localArchive.toPath())) {
                IOUtils.copy(in, out);
            }

            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                args[0] = proc.name();
                ReportConfiguration config = generateConfig(outputFile, stylesheet, ImmutablePair.of(option, args));
                Reporter reporter = new Reporter(config);
                reporter.output();

                Document document = XmlUtils.toDom(Files.newInputStream(outFile.toPath()));
                XmlUtils.assertIsPresent(proc.name(), document, xPath, "/rat-report/resource[@name='/dummy.jar']");
                switch (proc) {
                    case ABSENCE:
                        XmlUtils.assertIsPresent(proc.name(), document, xPath, "/rat-report/resource[@name='/dummy.jar']/license[@family='AL   ']");
                        XmlUtils.assertIsPresent(proc.name(), document, xPath, "/rat-report/resource[@name='/dummy.jar']/license[@family='?????']");
                        break;
                    case PRESENCE:
                        XmlUtils.assertIsPresent(proc.name(), document, xPath, "/rat-report/resource[@name='/dummy.jar']/license[@family='AL   ']");
                        XmlUtils.assertIsNotPresent(proc.name(), document, xPath, "/rat-report/resource[@name='/dummy.jar']/license[@family='?????']");
                        break;
                    case NOTIFICATION:
                        XmlUtils.assertIsNotPresent(proc.name(), document, xPath, "/rat-report/resource[@name='/dummy.jar']/license[@family='AL   ']");
                        XmlUtils.assertIsNotPresent(proc.name(), document, xPath, "/rat-report/resource[@name='/dummy.jar']/license[@family='?????']");
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected processing " + proc);
                }
            }
        } catch (IOException | RatException | SAXException | ParserConfigurationException |
                 XPathExpressionException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void outputArchiveTest() {
        archiveTest(Arg.OUTPUT_ARCHIVE.find("output-archive"));
    }

    private void standardTest(final Option option) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        String[] args = {null};

        try {
            configureSourceDir(option);
            File outFile = new File(sourceDir, "out.xml");
            ImmutablePair<Option, String[]> outputFile = ImmutablePair.of(Arg.OUTPUT_FILE.option(), new String[]{outFile.getAbsolutePath()});
            ImmutablePair<Option, String[]> stylesheet = ImmutablePair.of(Arg.OUTPUT_STYLE.option(), new String[]{StyleSheets.XML.arg()});

            writeFile("Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Apache-2.0\n",
                    "*/\n\n", "class Test {}\n"));
            writeFile("Missing.java", Arrays.asList("/* no license */\n\n", "class Test {}\n"));

            String testDoc = "/rat-report/resource[@name='/Test.java']";
            String missingDoc = "/rat-report/resource[@name='/Missing.java']";

            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                args[0] = proc.name();
                ReportConfiguration config = generateConfig(outputFile, stylesheet, ImmutablePair.of(option, args));
                Reporter reporter = new Reporter(config);
                reporter.output();

                Document document = XmlUtils.toDom(Files.newInputStream(outFile.toPath()));
                XmlUtils.assertIsPresent(proc.name(), document, xPath, testDoc);
                XmlUtils.assertIsPresent(proc.name(), document, xPath, missingDoc);

                switch (proc) {
                    case ABSENCE:
                        XmlUtils.assertIsPresent(proc.name(), document, xPath, testDoc + "/license[@family='AL   ']");
                        XmlUtils.assertIsPresent(proc.name(), document, xPath, missingDoc + "/license[@family='?????']");
                        break;
                    case PRESENCE:
                        XmlUtils.assertIsPresent(proc.name(), document, xPath, testDoc + "/license[@family='AL   ']");
                        XmlUtils.assertIsNotPresent(proc.name(), document, xPath, missingDoc + "/license[@family='?????']");
                        break;
                    case NOTIFICATION:
                        XmlUtils.assertIsNotPresent(proc.name(), document, xPath, testDoc + "/license[@family='AL   ']");
                        XmlUtils.assertIsNotPresent(proc.name(), document, xPath, missingDoc + "/license[@family='?????']");
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected processing " + proc);
                }
            }
        } catch (IOException | RatException | SAXException | ParserConfigurationException |
                 XPathExpressionException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void outputStandardTest() {
        standardTest(Arg.OUTPUT_STANDARD.find("output-standard"));
    }

    private void editCopyrightTest(final Option option, final Option extraOption) {
        final String myCopyright = "MyCopyright";
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, new String[]{myCopyright});
        final boolean forced = Arg.EDIT_OVERWRITE.option().equals(extraOption);
        final boolean dryRun = Arg.DRY_RUN.option().equals(extraOption);
        Pair<Option, String[]> extraArg = null;
        if (forced || dryRun) {
            extraArg = ImmutablePair.of(extraOption, null);
        }

        try {
            configureSourceDir(option);
            File javaFile = writeFile("Missing.java", Arrays.asList("/* no license */\n\n", "class Test {}\n"));
            File newJavaFile = new File(sourceDir, "Missing.java.new");
            FileUtils.delete(newJavaFile);

            ReportConfiguration config = extraArg != null ? generateConfig(arg1, extraArg) : generateConfig(arg1);
            Reporter reporter = new Reporter(config);
            reporter.execute();

            String actualText = TextUtils.readFile(javaFile);
            TextUtils.assertNotContains(myCopyright, actualText);

            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null);
            config = extraArg != null ? generateConfig(arg1, arg2, extraArg) : generateConfig(arg1, arg2);
            reporter = new Reporter(config);
            reporter.execute();

            actualText = TextUtils.readFile(javaFile);
            if (forced) {
                TextUtils.assertContains(myCopyright, actualText);
                assertThat(newJavaFile).doesNotExist();
            } else if (dryRun) {
                TextUtils.assertNotContains(myCopyright, actualText);
                assertThat(newJavaFile).doesNotExist();
            } else {
                TextUtils.assertNotContains(myCopyright, actualText);
                assertThat(newJavaFile).exists();
            }
        } catch (IOException | RatException e) {
            fail(e.getMessage(), e);
        }
    }

    @OptionCollectionTest.TestFunction
    protected void copyrightTest() {
        editCopyrightTest(Arg.EDIT_COPYRIGHT.find("copyright"), null);
    }

    @OptionCollectionTest.TestFunction
    protected void editCopyrightTest() {
        editCopyrightTest(Arg.EDIT_COPYRIGHT.find("edit-copyright"), null);
    }

    @OptionCollectionTest.TestFunction
    protected void forceTest() {
        editCopyrightTest(Arg.EDIT_COPYRIGHT.find("edit-copyright"), Arg.EDIT_OVERWRITE.find("force"));
    }

    @OptionCollectionTest.TestFunction
    protected void editOverwriteTest() {
        editCopyrightTest(Arg.EDIT_COPYRIGHT.find("edit-copyright"), Arg.EDIT_OVERWRITE.find("edit-overwrite"));
    }

    @OptionCollectionTest.TestFunction
    @Override
    public void helpTest() {
        PrintStream origin = System.out;
        Options options = OptionCollection.buildOptions();
        Pair<Option, String[]> arg1 = ImmutablePair.of(OptionCollection.HELP, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String actualText = null;
        try (PrintStream out = new PrintStream(baos)) {
            System.setOut(out);
            configureSourceDir(OptionCollection.HELP);

            ReportConfiguration config = generateConfig(Arrays.asList(arg1), true);
            assertThat(helpCalled.get()).as("Help was not called").isTrue();
            new Help(System.out).printUsage(options);
            actualText = baos.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            fail(e.getMessage(), e);
        } finally {
            System.setOut(origin);
        }

        // verify all the options
        assertThat(actualText).contains("====== Available Options ======");
        for (Option option : options.getOptions()) {
            StringBuilder regex = new StringBuilder();
            if (option.getOpt() != null) {
                regex.append("-").append(option.getOpt());
                if (option.hasLongOpt()) {
                    regex.append(",");
                }
            }
            if (option.hasLongOpt()) {
                regex.append("--").append(option.getLongOpt());
            }
            if (option.hasArg()) {
                String name = option.getArgName() == null ? "arg" : option.getArgName();
                regex.append(".+\\<").append(name).append("\\>");
            }
            if (option.isDeprecated()) {
                regex.append(".+\\[Deprecated ");
            }
            assertThat(Pattern.compile(regex.toString()).matcher(actualText).find()).as("missing '" + regex + "'").isTrue();
        }

        assertThat(actualText).contains("====== Argument Types ======");
        assertThat(actualText).contains("====== Standard Collections ======");
        for (StandardCollection collection : StandardCollection.values()) {
            assertThat(actualText).contains("<" + collection.name() + ">");
        }
    }

    @OptionCollectionTest.TestFunction
    protected void helpLicensesTest() {
        PrintStream origin = System.out;
        Option option = HELP_LICENSES.option();
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String actualText = null;
        try (PrintStream out = new PrintStream(baos)) {
            System.setOut(out);
            configureSourceDir(option);
            ReportConfiguration config = generateConfig(arg1);
            actualText = baos.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            fail(e.getMessage(), e);
        } finally {
            System.setOut(origin);
        }

        assertThat(actualText).isNotNull();
        TextUtils.assertContains("====== Licenses ======", actualText);
        TextUtils.assertContains("====== Defined Matchers ======", actualText);
        TextUtils.assertContains("====== Defined Families ======", actualText);
    }

    @OptionCollectionTest.TestFunction
    protected void dryRunTest() {
        editCopyrightTest(Arg.EDIT_COPYRIGHT.find("edit-copyright"), Arg.DRY_RUN.find("dry-run"));
    }
}
