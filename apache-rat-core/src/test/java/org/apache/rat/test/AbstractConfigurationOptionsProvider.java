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
package org.apache.rat.test;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollectionTest;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReporterTest;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.DocumentName;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.writer.XmlWriter;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log.Level;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.SortedSet;

import static org.apache.rat.commandline.Arg.HELP_LICENSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * A list of methods that an OptionsProvider in a test case must support.
 * Use of this interface ensures consistent testing across the UIs. Each method
 * tests an Option from OptionCollection that must be implemented in the UI.
 * Each method in this interface tests an Option in {@link org.apache.rat.OptionCollection}.
 */
public abstract class AbstractConfigurationOptionsProvider extends AbstractOptionsProvider {

    /**
     * Copy the runtime data to the "target" directory.
     * @param baseDir the base directory to copy to.
     * @param targetDir the directory relative to the base directory to copy to.
     */
    public static void preserveData(File baseDir, String targetDir) {
        final Path recordPath = FileSystems.getDefault().getPath("target", targetDir);
        org.apache.rat.utils.FileUtils.mkDir(recordPath.toFile());
        try {
            FileUtils.copyDirectory(baseDir, recordPath.toFile());
        } catch (IOException e) {
            System.err.format("Unable to copy data from %s to %s%n", baseDir, recordPath);
        }
    }

    /**
     * Copies the test data to the specified directory.
     * @param baseDir the directory to copy the {@code /src/test/resources} to.
     * @return the {@code baseDir} argument.
     */
    public static File setup(final File baseDir) {
        try {
            final File sourceDir = Resources.getResourceDirectory("OptionTools");
            FileUtils.copyDirectory(sourceDir, new File(baseDir,"/src/test/resources/OptionTools"));
        } catch (IOException e) {
            DefaultLog.getInstance().error("Can not copy 'OptionTools' to " + baseDir, e);
        }
        return baseDir;
    }

    protected AbstractConfigurationOptionsProvider(final Collection<String> unsupportedArgs, final File baseDir) {
        super(setup(baseDir));
        addTest(OptionCollectionTest.OptionTest.namedTest("addLicense", this::addLicenseTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("config", this::configTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("configuration-no-defaults", this::configurationNoDefaultsTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("copyright", this::copyrightTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("counter-min", this::counterMinTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("counter-max", this::counterMaxTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("dir", () -> DefaultLog.getInstance().info("--dir has no valid test")));
        addTest(OptionCollectionTest.OptionTest.namedTest("dry-run", this::dryRunTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("edit-copyright", this::editCopyrightTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("edit-license", this::editLicenseTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("edit-overwrite", this::editOverwriteTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("exclude", this::excludeTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("exclude-file", this::excludeFileTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("force", this::forceTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("help-licenses", this::helpLicenses));
        addTest(OptionCollectionTest.OptionTest.namedTest("include", this::includeTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("includes-file", this::includesFileTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("input-exclude", this::inputExcludeTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("input-exclude-file", this::inputExcludeFileTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("input-exclude-parsed-scm", this::inputExcludeParsedScmTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("input-exclude-std", this::inputExcludeStdTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("input-exclude-size", this::inputExcludeSizeTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("input-include", this::inputIncludeTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("input-include-file", this::inputIncludeFileTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("input-include-std", this::inputIncludeStdTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("input-source", this::inputSourceTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("license-families-approved", this::licenseFamiliesApprovedTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("license-families-approved-file", this::licenseFamiliesApprovedFileTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("license-families-denied", this::licenseFamiliesDeniedTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("license-families-denied-file", this::licenseFamiliesDeniedFileTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("licenses", this::licensesTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("licenses-approved", this::licensesApprovedTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("licenses-approved-file", this::licensesApprovedFileTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("licenses-denied", this::licensesDeniedTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("licenses-denied-file", this::licensesDeniedFileTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("list-families", this::listFamiliesTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("list-licenses", this::listLicensesTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("log-level", this::logLevelTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("no-default-licenses", this::noDefaultsTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("out", this::outTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("output-archive", this::outputArchiveTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("output-families", this::outputFamiliesTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("output-file", this::outputFileTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("output-licenses", this::outputLicensesTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("output-standard", this::outputStandardTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("output-style", this::outputStyleTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("scan-hidden-directories", this::scanHiddenDirectoriesTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("stylesheet", this::styleSheetTest));
        addTest(OptionCollectionTest.OptionTest.namedTest("xml", this::xmlTest));
        super.validate(unsupportedArgs);
    }

    // exclude tests
    private void execExcludeTest(final Option option, final String[] args) {
        String[] notExcluded = {"notbaz", "well._afile"};
        String[] excluded = {"some.foo", "B.bar", "justbaz"};

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            DocumentNameMatcher excluder = config.getDocumentExcluder(baseName());
            for (String fname : notExcluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
            }
            for (String fname : excluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
            }
        });
    }

    private void excludeFileTest(final Option option) {
        File outputFile = writeFile("exclude.txt", Arrays.asList(EXCLUDE_ARGS));
        execExcludeTest(option, new String[]{outputFile.getAbsolutePath()});
    }

    protected void excludeFileTest() {
        excludeFileTest(Arg.EXCLUDE_FILE.find("exclude-file"));
    }

    protected void inputExcludeFileTest() {
        excludeFileTest(Arg.EXCLUDE_FILE.find("input-exclude-file"));
    }

    protected void excludeTest() {
        execExcludeTest(Arg.EXCLUDE.find("exclude"), EXCLUDE_ARGS);
    }

    protected void inputExcludeTest() {
        execExcludeTest(Arg.EXCLUDE.find("input-exclude"), EXCLUDE_ARGS);
    }

    protected void inputExcludeStdTest() {
        Option option = Arg.EXCLUDE_STD.find("input-exclude-std");
        String[] args = {StandardCollection.MISC.name()};
        String[] excluded = {"afile~", ".#afile", "%afile%", "._afile"};
        String[] notExcluded = {"afile~more", "what.#afile", "%afile%withMore", "well._afile"};

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            DocumentNameMatcher excluder = config.getDocumentExcluder(baseName());
            for (String fname : excluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
            }
            for (String fname : notExcluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
            }
        });
    }

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
        String[] notExcluded = {"thingone", "dir/fish_two", "some/thingone", "blue/fish/dory" };
        String[] excluded = {"thingtwo", "some/things", "dir/fish", "red/fish", "blue/fish", "some/fish", "another/red_fish"};

        writeFile(".gitignore", Arrays.asList(lines));
        File dir = new File(baseDir, "red");
        org.apache.rat.utils.FileUtils.mkDir(dir);
        dir = new File(baseDir, "blue");
        dir = new File(dir, "fish");
        org.apache.rat.utils.FileUtils.mkDir(dir);

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            DocumentNameMatcher excluder = config.getDocumentExcluder(baseName());
            for (String fname : excluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
            }
            for (String fname : notExcluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
            }
        });
    }

    private void inputExcludeSizeTest() {
        Option option = Arg.EXCLUDE_SIZE.option();
        String[] args = {"5"};
        writeFile("Hi.txt", Collections.singletonList("Hi"));
        writeFile("Hello.txt", Collections.singletonList("Hello"));
        writeFile("HelloWorld.txt", Collections.singletonList("HelloWorld"));

        String[] notExcluded = {"Hello.txt", "HelloWorld.txt"};
        String[] excluded = {"Hi.txt"};

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            DocumentNameMatcher excluder = config.getDocumentExcluder(baseName());
            for (String fname : excluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
            }
            for (String fname : notExcluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
            }
        });
    }

    // include tests
    private void execIncludeTest(final Option option, final String[] args) {
        Option excludeOption = Arg.EXCLUDE.option();
        String[] notExcluded = {"B.bar", "justbaz", "notbaz"};
        String[] excluded = {"some.foo"};

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args),
                    ImmutablePair.of(excludeOption, EXCLUDE_ARGS));
            DocumentNameMatcher excluder = config.getDocumentExcluder(baseName());
            for (String fname : excluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
            }
            for (String fname : notExcluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
            }
        });
    }

    private void includeFileTest(final Option option) {
        File outputFile = writeFile("include.txt", Arrays.asList(INCLUDE_ARGS));
        execIncludeTest(option, new String[]{outputFile.getAbsolutePath()});
    }

    protected void inputIncludeFileTest() {
        includeFileTest(Arg.INCLUDE_FILE.find("input-include-file"));
    }

    protected void includesFileTest() {
        includeFileTest(Arg.INCLUDE_FILE.find("includes-file"));
    }

    protected void includeTest() {
        execIncludeTest(Arg.INCLUDE.find("include"), INCLUDE_ARGS);
    }

    protected void inputIncludeTest() {
        execIncludeTest(Arg.INCLUDE.find("input-include"), INCLUDE_ARGS);
    }

    protected void inputIncludeStdTest() {
        ImmutablePair<Option, String[]> excludes = ImmutablePair.of(Arg.EXCLUDE.find("input-exclude"),
                new String[]{"*~more", "*~"});
        Option option = Arg.INCLUDE_STD.find("input-include-std");
        String[] args = {StandardCollection.MISC.name()};
        String[] excluded = {"afile~more"};
        String[] notExcluded = {"afile~", ".#afile", "%afile%", "._afile", "what.#afile", "%afile%withMore", "well._afile"};

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(excludes, ImmutablePair.of(option, args));
            DocumentNameMatcher excluder = config.getDocumentExcluder(baseName());
            for (String fname : excluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
            }
            for (String fname : notExcluded) {
                DocumentName docName = mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
            }
        });
    }

    protected void inputSourceTest() {
        Option option = Arg.SOURCE.find("input-source");
        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, new String[]{baseDir.getAbsolutePath()}));
            assertThat(config.hasSource()).isTrue();
        });
    }

    // LICENSE tests
    protected void execLicensesApprovedTest(final Option option, String[] args) {
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, args);

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(arg1);
            SortedSet<String> result = config.getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).contains("one", "two");
        });

        Pair<Option, String[]> arg2 = ImmutablePair.of(
                Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"),
                null
        );

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(arg1, arg2);
            SortedSet<String> result = config.getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).containsExactly("one", "two");
        });
    }

    protected void helpLicenses() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream origin = System.out;
        try (PrintStream out = new PrintStream(output)) {
            System.setOut(out);
            generateConfig(ImmutablePair.of(HELP_LICENSES.option(), null));
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            System.setOut(origin);
        }
        String txt = output.toString();
        TextUtils.assertContains("====== Licenses ======", txt);
        TextUtils.assertContains("====== Defined Matchers ======", txt);
        TextUtils.assertContains("====== Defined Families ======", txt);
    }

    protected void licensesApprovedFileTest() {
        File outputFile = writeFile("licensesApproved.txt", Arrays.asList("one", "two"));
        execLicensesApprovedTest(Arg.LICENSES_APPROVED_FILE.find("licenses-approved-file"),
                new String[]{outputFile.getAbsolutePath()});
    }

    protected void licensesApprovedTest() {
        execLicensesApprovedTest(Arg.LICENSES_APPROVED.find("licenses-approved"),
                new String[]{"one, two"});
    }

    private void execLicensesDeniedTest(final Option option, final String[] args) {
        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            assertThat(config.getLicenseIds(LicenseSetFactory.LicenseFilter.ALL)).contains("ILLUMOS");
            SortedSet<String> result = config.getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).doesNotContain("ILLUMOS");
        });
    }

    protected void licensesDeniedTest() {
        execLicensesDeniedTest(Arg.LICENSES_DENIED.find("licenses-denied"), new String[]{"ILLUMOS"});
    }

    protected void licensesDeniedFileTest() {
        File outputFile = writeFile("licensesDenied.txt", Collections.singletonList("ILLUMOS"));
        execLicensesDeniedTest(Arg.LICENSES_DENIED_FILE.find("licenses-denied-file"),
                new String[]{outputFile.getAbsolutePath()});
    }

    private void execLicenseFamiliesApprovedTest(final Option option, final String[] args) {
        String catz = ILicenseFamily.makeCategory("catz");
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, args);

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(arg1);
            SortedSet<String> result = config.getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).contains(catz);

            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"), null);
            config = generateConfig(arg1, arg2);
            result = config.getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).containsExactly(catz);
        });
    }

    protected void licenseFamiliesApprovedFileTest() {
        File outputFile = writeFile("familiesApproved.txt", Collections.singletonList("catz"));
        execLicenseFamiliesApprovedTest(Arg.FAMILIES_APPROVED_FILE.find("license-families-approved-file"),
                new String[]{outputFile.getAbsolutePath()});
    }

    protected void licenseFamiliesApprovedTest() {
        execLicenseFamiliesApprovedTest(Arg.FAMILIES_APPROVED.find("license-families-approved"),
                new String[]{"catz"});
    }

    private void execLicenseFamiliesDeniedTest(final Option option, final String[] args) {
        String gpl = ILicenseFamily.makeCategory("GPL");

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            assertThat(config.getLicenseCategories(LicenseSetFactory.LicenseFilter.ALL)).contains(gpl);
            SortedSet<String> result = config.getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).doesNotContain(gpl);
        });
    }

    protected void licenseFamiliesDeniedFileTest() {
        File outputFile = writeFile("familiesDenied.txt", Collections.singletonList("GPL"));
        execLicenseFamiliesDeniedTest(Arg.FAMILIES_DENIED_FILE.find("license-families-denied-file"),
                new String[]{outputFile.getAbsolutePath()});
    }

    protected void licenseFamiliesDeniedTest() {
        execLicenseFamiliesDeniedTest(Arg.FAMILIES_DENIED.find("license-families-denied"),
                new String[]{"GPL"});
    }

    protected void counterMaxTest() {
        Option option = Arg.COUNTER_MAX.option();
        String[] args = {null, null};

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.nullPair());
            assertThat(config.getClaimValidator().getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);
            args[0] = "Unapproved:-1";
            args[1] = "ignored:1";
            config = generateConfig(ImmutablePair.of(option, args));
            assertThat(config.getClaimValidator().getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(Integer.MAX_VALUE);
            assertThat(config.getClaimValidator().getMax(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
            args[1] = "unapproved:5";
            args[0] = "ignored:0";
            config = generateConfig(ImmutablePair.of(option, args));
            assertThat(config.getClaimValidator().getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
            assertThat(config.getClaimValidator().getMax(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
        });
    }

    protected void counterMinTest() {
        Option option = Arg.COUNTER_MIN.option();
        String[] args = {null, null};

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.nullPair());
            assertThat(config.getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);
            args[0] = "Unapproved:1";
            args[1] = "ignored:1";
            config = generateConfig(ImmutablePair.of(option, args));
            assertThat(config.getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
            assertThat(config.getClaimValidator().getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
            args[1] = "unapproved:5";
            args[0] = "ignored:0";
            config = generateConfig(ImmutablePair.of(option, args));
            assertThat(config.getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
            assertThat(config.getClaimValidator().getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
        });
    }

    private String writeConfigXML(String name) {
        String licenseText = """
                Licensed to the Apache Software Foundation (ASF) under one or more
                       contributor license agreements.  See the NOTICE file distributed with
                       this work for additional information regarding copyright ownership.
                               The ASF licenses this file to You under the Apache License, Version 2.0
                       (the "License"); you may not use this file except in compliance with
                       the License.  You may obtain a copy of the License at
                
                       http://www.apache.org/licenses/LICENSE-2.0
                
                       Unless required by applicable law or agreed to in writing, software
                       distributed under the License is distributed on an "AS IS" BASIS,
                               WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                               See the License for the specific language governing permissions and
                       limitations under the License.""";
        String capName = WordUtils.capitalize(name);
        String upperName = name.toUpperCase(Locale.ROOT);

        Path xmlPath = this.baseDir.toPath().resolve(".rat/" + capName + ".xml");
        File f = xmlPath.toFile();
        try {
            FileUtils.forceMkdir(f.getParentFile());
            try (FileWriter writer = new FileWriter(f);
                 XmlWriter xmlWriter = new XmlWriter(writer)) {

                xmlWriter.startDocument()
                        .comment(licenseText)
                        .startElement("rat-config")
                        .startElement("families")
                        .startElement("family")
                        .attribute("id", upperName)
                        .attribute("name", "from " + capName + ".xml")
                        .closeElement("families")
                        .startElement("licenses")
                        .startElement("license")
                        .attribute("family", upperName)
                        .startElement("text")
                        .content(name)
                        .closeElement("rat-config");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return xmlPath.toString();
    }

    private void configTest(final Option option) {
        String[] args = {writeConfigXML("one"), writeConfigXML("two")};
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, args);

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(arg1);
            SortedSet<ILicense> set = config.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
            assertThat(set).hasSizeGreaterThan(2);
            assertThat(LicenseSetFactory.search("ONE", "ONE", set)).isPresent();
            assertThat(LicenseSetFactory.search("TWO", "TWO", set)).isPresent();

            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"), null);

            config = generateConfig(arg1, arg2);
            set = config.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
            assertThat(set).hasSize(2);
            assertThat(LicenseSetFactory.search("ONE", "ONE", set)).isPresent();
            assertThat(LicenseSetFactory.search("TWO", "TWO", set)).isPresent();
        });
    }

    protected void licensesTest() {
        configTest(Arg.CONFIGURATION.find("licenses"));
    }

    protected void configTest() {
        configTest(Arg.CONFIGURATION.find("config"));
    }

    private void noDefaultsTest(final Option arg) {
        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(arg, null));
            assertThat(config.getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isEmpty();
            config = generateConfig(ImmutablePair.nullPair());
            assertThat(config.getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isNotEmpty();
        });
    }

    protected void noDefaultsTest() {
        noDefaultsTest(Arg.CONFIGURATION_NO_DEFAULTS.find("no-default-licenses"));
    }

    protected void configurationNoDefaultsTest() {
        noDefaultsTest(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"));
    }

    protected void dryRunTest() {
        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(Arg.DRY_RUN.find("dry-run"), null));
            assertThat(config.isDryRun()).isTrue();
            config = generateConfig(ImmutablePair.nullPair());
            assertThat(config.isDryRun()).isFalse();
        });
    }

    private void editCopyrightTest(final Option option) {
        assertDoesNotThrow(() -> {
            Pair<Option, String[]> arg1 = ImmutablePair.of(option, new String[]{"MyCopyright"});
            ReportConfiguration config;
            try {
                config = generateConfig(arg1);
                assertThat(config.getCopyrightMessage()).as("Copyright without --edit-license should not work").isNull();
            } catch (IOException e) {
                if (e.getCause() != null) {
                    fail(e.getMessage() + ": " + e.getCause().getMessage());
                }
                throw e;
            }

            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null);
            try {
                config = generateConfig(arg1, arg2);
                assertThat(config.getCopyrightMessage()).isEqualTo("MyCopyright");
            } catch (IOException e) {
                if (e.getCause() != null) {
                    fail(e.getMessage() + ": " + e.getCause().getMessage());
                }
                throw e;
            }
        });
    }

    protected void copyrightTest() {
        editCopyrightTest(Arg.EDIT_COPYRIGHT.find("copyright"));
    }

    protected void editCopyrightTest() {
        editCopyrightTest(Arg.EDIT_COPYRIGHT.find("edit-copyright"));
    }

    private void editLicenseTest(final Option option) {
        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, null));
            assertThat(config.isAddingLicenses()).isTrue();
            config = generateConfig(ImmutablePair.nullPair());
            assertThat(config.isAddingLicenses()).isFalse();
        });
    }

    protected void addLicenseTest() {
        editLicenseTest(Arg.EDIT_ADD.find("addLicense"));
    }

    protected void editLicenseTest() {
        editLicenseTest(Arg.EDIT_ADD.find("edit-license"));
    }

    private void overwriteTest(final Option option) {
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, null);

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(arg1);
            assertThat(config.isAddingLicensesForced()).isFalse();
            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null);

            config = generateConfig(arg1, arg2);
            assertThat(config.isAddingLicensesForced()).isTrue();
        });
    }

    protected void forceTest() {
        overwriteTest(Arg.EDIT_OVERWRITE.find("force"));
    }

    protected void editOverwriteTest() {
        overwriteTest(Arg.EDIT_OVERWRITE.find("edit-overwrite"));
    }

    protected void logLevelTest() {
        Option option = Arg.LOG_LEVEL.find("log-level");
        String[] args = {null};
        Level logLevel = DefaultLog.getInstance().getLevel();
        try {
            for (Level level : Level.values()) {
                try {
                    args[0] = level.name();
                    generateConfig(ImmutablePair.of(option, args));
                    assertThat(DefaultLog.getInstance().getLevel()).isEqualTo(level);
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        } finally {
            DefaultLog.getInstance().setLevel(logLevel);
        }
    }

    private void archiveTest(final Option option) {
        String[] args = {null};

        assertDoesNotThrow(() -> {
            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                args[0] = proc.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertThat(config.getArchiveProcessing()).isEqualTo(proc);
            }
        });
    }

    protected void outputArchiveTest() {
        archiveTest(Arg.OUTPUT_ARCHIVE.find("output-archive"));
    }

    private void listFamilies(final Option option) {
        String[] args = {null};

        assertDoesNotThrow(() -> {
            for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
                args[0] = filter.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertThat(config.listFamilies()).isEqualTo(filter);
            }
        });
    }

    protected void listFamiliesTest() {
        listFamilies(Arg.OUTPUT_FAMILIES.find("list-families"));
    }

    protected void outputFamiliesTest() {
        listFamilies(Arg.OUTPUT_FAMILIES.find("output-families"));
    }

    private void outTest(final Option option) {
        File outFile = new File(baseDir, "outexample-" + option.getLongOpt());
        String[] args = new String[]{outFile.getAbsolutePath()};

        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            try (OutputStream os = config.getOutput().get()) {
                os.write("Hello world".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(outFile.toPath())))) {
                assertThat(reader.readLine()).isEqualTo("Hello world");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected void outTest() {
        outTest(Arg.OUTPUT_FILE.find("out"));
    }

    protected void outputFileTest() {
        outTest(Arg.OUTPUT_FILE.find("output-file"));
    }

    private void listLicenses(final Option option) {
        String[] args = {null};

        assertDoesNotThrow(() -> {
            for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
                args[0] = filter.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertThat(config.listLicenses()).isEqualTo(filter);
            }
        });
    }

    protected void listLicensesTest() {
        listLicenses(Arg.OUTPUT_LICENSES.find("list-licenses"));
    }

    protected void outputLicensesTest() {
        listLicenses(Arg.OUTPUT_LICENSES.find("output-licenses"));
    }

    private void standardTest(final Option option) {
        String[] args = {null};
        assertDoesNotThrow(() -> {
            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                args[0] = proc.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertThat(config.getStandardProcessing()).isEqualTo(proc);
            }
        });
    }

    protected void outputStandardTest() {
        standardTest(Arg.OUTPUT_STANDARD.find("output-standard"));
    }

    private void styleSheetTest(final Option option) {
        // copy the dummy stylesheet so that we have a local file for users of the testing jar.
        File file = new File(baseDir, "stylesheet-" + option.getLongOpt());
        DocumentName workingDirectory = DocumentName.builder(file.getParentFile()).build();
        try (
                InputStream in = ReporterTest.class.getResourceAsStream("MatcherContainerResource.txt");
                OutputStream out = Files.newOutputStream(file.toPath())) {
            if (in == null) {
                fail("Could not copy MatcherContainerResource.txt: resource not found");
            } else {
                IOUtils.copy(in, out);
            }
        } catch (IOException e) {
            fail("Could not copy MatcherContainerResource.txt: " + e.getMessage());
        }

        // run the test
        String[] args = {null};
        assertDoesNotThrow(() -> {
            for (String sheet : new String[]{"plain-rat", "missing-headers", "unapproved-licenses", "stylesheet-" + option.getLongOpt()}) {
                args[0] = sheet;
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                try (InputStream expected = StyleSheets.getStyleSheet(sheet, workingDirectory).ioSupplier().get();
                     InputStream actual = config.getStyleSheet().get()) {
                    String expectedStr =  IOUtils.toString(expected, StandardCharsets.UTF_8);
                    String actualStr =  IOUtils.toString(actual, StandardCharsets.UTF_8);
                    assertThat(actualStr).as(() -> String.format("'%s' is not correct: %s != %s",
                            config.getStyleSheetDescriptor().name(),
                            actualStr, expectedStr)).isEqualTo(expectedStr);
                }
            }
        });
    }

    protected void styleSheetTest() {
        styleSheetTest(Arg.OUTPUT_STYLE.find("stylesheet"));
    }

    protected void outputStyleTest() {
        styleSheetTest(Arg.OUTPUT_STYLE.find("output-style"));
    }

    protected void scanHiddenDirectoriesTest() {
        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(Arg.INCLUDE_STD.find("scan-hidden-directories"), null));
            DocumentNameMatcher excluder = config.getDocumentExcluder(baseName());
            assertThat(excluder.matches(mkDocName(".file"))).as(".file").isTrue();
        });
    }

    protected void xmlTest() {
        assertDoesNotThrow(() -> {
            ReportConfiguration config = generateConfig(ImmutablePair.of(Arg.OUTPUT_STYLE.find("xml"), null));
            try (InputStream expected = StyleSheets.getStyleSheet("xml", null).ioSupplier().get();
                 InputStream actual = config.getStyleSheet().get()) {
                assertThat(IOUtils.contentEquals(expected, actual)).as("'xml' does not match").isTrue();
            }
        });
    }
}
