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

import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.OptionCollectionTest;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReporterTest;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcherTest;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log.Level;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.apache.rat.commandline.Arg.HELP_LICENSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * A list of methods that an OptionsProvider in a test case must support.
 * Use of this interface ensures consistent testing across the UIs. Each method
 * tests an Option from OptionCollection that must be implemented in the UI.
 * Each method in this interface tests an Option in {@link org.apache.rat.OptionCollection}.
 */
public abstract class AbstractOptionsProvider implements ArgumentsProvider {
    /**
     * A map of test Options to tests.
     */
    protected final Map<String, OptionCollectionTest.OptionTest> testMap = new TreeMap<>();
    /** The list of exclude args */
    protected static final String[] EXCLUDE_ARGS = {"*.foo", "%regex[[A-Z]\\.bar]", "justbaz"};
    /** the list of include args */
    protected static final String[] INCLUDE_ARGS = {"B.bar", "justbaz"};
    /**
     * The directory to place test data in.
     */
    protected File baseDir;

    /**
     * Copy the runtime data to the "target" directory.
     * @param baseDir the base directory to copy to.
     * @param targetDir the directory relative to the base directory to copy to.
     */
    public static void preserveData(File baseDir, String targetDir) {
        final Path recordPath = FileSystems.getDefault().getPath("target", targetDir);
        recordPath.toFile().mkdirs();
        try {
            FileUtils.copyDirectory(baseDir, recordPath.toFile());
        } catch (IOException e) {
            System.err.format("Unable to copy data from %s to %s%n", baseDir, recordPath);
        }
    }

    /**
     * Gets the document name based on the baseDir.
     * @return The document name based on the baseDir.
     */
    protected DocumentName baseName() {
        return DocumentName.builder(baseDir).build();
    }

    /**
     * Copies the test data to the specified directory.
     * @param baseDir the directory to copy the /src/test/resources to.
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

    protected AbstractOptionsProvider(final Collection<String> unsupportedArgs, final File baseDir) {
        this.baseDir = setup(baseDir);
        testMap.put("addLicense", this::addLicenseTest);
        testMap.put("config", this::configTest);
        testMap.put("configuration-no-defaults", this::configurationNoDefaultsTest);
        testMap.put("copyright", this::copyrightTest);
        testMap.put("counter-min", this::counterMinTest);
        testMap.put("counter-max", this::counterMaxTest);
        testMap.put("dir", () -> DefaultLog.getInstance().info("--dir has no valid test"));
        testMap.put("dry-run", this::dryRunTest);
        testMap.put("edit-copyright", this::editCopyrightTest);
        testMap.put("edit-license", this::editLicensesTest);
        testMap.put("edit-overwrite", this::editOverwriteTest);
        testMap.put("exclude", this::excludeTest);
        testMap.put("exclude-file", this::excludeFileTest);
        testMap.put("force", this::forceTest);
        testMap.put("help", this::helpTest);
        testMap.put("help-licenses", this::helpLicenses);
        testMap.put("include", this::includeTest);
        testMap.put("includes-file", this::includesFileTest);
        testMap.put("input-exclude", this::inputExcludeTest);
        testMap.put("input-exclude-file", this::inputExcludeFileTest);
        testMap.put("input-exclude-parsed-scm", this::inputExcludeParsedScmTest);
        testMap.put("input-exclude-std", this::inputExcludeStdTest);
        testMap.put("input-exclude-size", this::inputExcludeSizeTest);
        testMap.put("input-include", this::inputIncludeTest);
        testMap.put("input-include-file", this::inputIncludeFileTest);
        testMap.put("input-include-std", this::inputIncludeStdTest);
        testMap.put("input-source", this::inputSourceTest);
        testMap.put("license-families-approved", this::licenseFamiliesApprovedTest);
        testMap.put("license-families-approved-file", this::licenseFamiliesApprovedFileTest);
        testMap.put("license-families-denied", this::licenseFamiliesDeniedTest);
        testMap.put("license-families-denied-file", this::licenseFamiliesDeniedFileTest);
        testMap.put("licenses", this::licensesTest);
        testMap.put("licenses-approved", this::licensesApprovedTest);
        testMap.put("licenses-approved-file", this::licensesApprovedFileTest);
        testMap.put("licenses-denied", this::licensesDeniedTest);
        testMap.put("licenses-denied-file", this::licensesDeniedFileTest);
        testMap.put("list-families", this::listFamiliesTest);
        testMap.put("list-licenses", this::listLicensesTest);
        testMap.put("log-level", this::logLevelTest);
        testMap.put("no-default-licenses", this::noDefaultsTest);
        testMap.put("out", this::outTest);
        testMap.put("output-archive", this::outputArchiveTest);
        testMap.put("output-families", this::outputFamiliesTest);
        testMap.put("output-file", this::outputFileTest);
        testMap.put("output-licenses", this::outputLicensesTest);
        testMap.put("output-standard", this::outputStandardTest);
        testMap.put("output-style", this::outputStyleTest);
        testMap.put("scan-hidden-directories", this::scanHiddenDirectoriesTest);
        testMap.put("stylesheet", this::styleSheetTest);
        testMap.put("xml", this::xmlTest);
        unsupportedArgs.forEach(testMap::remove);
        verifyAllMethodsDefinedAndNeeded(unsupportedArgs);
    }

    private void verifyAllMethodsDefinedAndNeeded(final Collection<String> unsupportedArgs) {
        // verify all options have functions.
        final List<String> argNames = new ArrayList<>();
        Arg.getOptions().getOptions().forEach(o -> {
            if (o.getLongOpt() != null) {
                argNames.add(o.getLongOpt());
            }
        });
        argNames.removeAll(unsupportedArgs);
        argNames.removeAll(testMap.keySet());
        if (!argNames.isEmpty()) {
            fail("Missing methods for: " + String.join(", ", argNames));
        }

        // verify all functions have options.
        argNames.clear();
        argNames.addAll(testMap.keySet());
        argNames.remove("help");
        Arg.getOptions().getOptions().forEach(o -> {
            if (o.getLongOpt() != null) {
                argNames.remove(o.getLongOpt());
            }
        });
        if (!argNames.isEmpty()) {
            fail("Extra methods defined: " + String.join(", ", argNames));
        }
        unsupportedArgs.forEach(testMap::remove);
    }

    @SafeVarargs
    protected final ReportConfiguration generateConfig(final Pair<Option, String[]>... args) throws IOException {
        List<Pair<Option, String[]>> options = Arrays.asList(args);
        return generateConfig(options);
    }

    /**
     * Create the report configuration from the argument pairs.
     * There must be at least one arg. It may be `ImmutablePair.nullPair()`.
     *
     * @param args Pairs comprising the argument option and the values for the option.
     * @return The generated ReportConfiguration.
     * @throws IOException on error.
     */
    protected abstract ReportConfiguration generateConfig(final List<Pair<Option, String[]>> args) throws IOException;

    protected File writeFile(final String name, final Iterable<String> lines) {
        File file = new File(baseDir, name);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            lines.forEach(writer::println);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return file;
    }

    protected DocumentName mkDocName(final String name) {
        return DocumentName.builder(new File(baseDir, name)).build();
    }

    /** Help test */
    protected abstract void helpTest();

    /** Display the option and value under test */
    private String displayArgAndName(final Option option, final String fname) {
        return String.format("%s %s", option.getLongOpt(), fname);
    }

    private String dump(final DocumentNameMatcher nameMatcher, final DocumentName name) {
        StringBuilder sb = new StringBuilder();
        nameMatcher.decompose(name).forEach(s -> sb.append(s).append("\n"));
        return sb.toString();
    }

    private String dump(final Option option, final String fname, final DocumentNameMatcher matcher, final DocumentName name) {
        return String.format("Argument and Name: %s%nMatcher decomposition:%n%s", displayArgAndName(option, fname),
                DocumentNameMatcherTest.processDecompose(matcher, name));
    }

    // exclude tests
    private void execExcludeTest(final Option option, final String[] args) {
        String[] notExcluded = {"notbaz", "well._afile"};
        String[] excluded = {"some.foo", "B.bar", "justbaz"};
        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
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
        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
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
        dir.mkdirs();
        dir = new File(baseDir, "blue");
        dir = new File(dir, "fish");
        dir.mkdirs();

        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void inputExcludeSizeTest() {
        Option option = Arg.EXCLUDE_SIZE.option();
        String[] args = {"5"};
        writeFile("Hi.txt", Collections.singletonList("Hi"));
        writeFile("Hello.txt", Collections.singletonList("Hello"));
        writeFile("HelloWorld.txt", Collections.singletonList("HelloWorld"));

        String[] notExcluded = {"Hello.txt", "HelloWorld.txt"};
        String[] excluded = {"Hi.txt"};

        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    // include tests
    private void execIncludeTest(final Option option, final String[] args) {
        Option excludeOption = Arg.EXCLUDE.option();
        String[] notExcluded = {"B.bar", "justbaz", "notbaz"};
        String[] excluded = {"some.foo"};
        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
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
        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void inputSourceTest() {
        Option option = Arg.SOURCE.find("input-source");
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, new String[]{baseDir.getAbsolutePath()}));
            assertThat(config.hasSource()).isTrue();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    // LICENSE tests
    protected void execLicensesApprovedTest(final Option option, String[] args) {
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, args);
        try {
            ReportConfiguration config = generateConfig(arg1);
            SortedSet<String> result = config.getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).contains("one", "two");
        } catch (IOException e) {
            fail(e.getMessage());
        }

        Pair<Option, String[]> arg2 = ImmutablePair.of(
                Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"),
                null
        );

        try {
            ReportConfiguration config = generateConfig(arg1, arg2);
            SortedSet<String> result = config.getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).containsExactly("one", "two");
        } catch (IOException e) {
            fail(e.getMessage());
        }
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
                new String[]{"one", "two"});
    }

    private void execLicensesDeniedTest(final Option option, final String[] args) {
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            assertThat(config.getLicenseIds(LicenseSetFactory.LicenseFilter.ALL)).contains("ILLUMOS");
            SortedSet<String> result = config.getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).doesNotContain("ILLUMOS");
        } catch (IOException e) {
            fail(e.getMessage());
        }
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
        try {
            ReportConfiguration config = generateConfig(arg1);
            SortedSet<String> result = config.getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).contains(catz);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"), null);
        try {
            ReportConfiguration config = generateConfig(arg1, arg2);
            SortedSet<String> result = config.getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).containsExactly(catz);
        } catch (IOException e) {
            fail(e.getMessage());
        }
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
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            assertThat(config.getLicenseCategories(LicenseSetFactory.LicenseFilter.ALL)).contains(gpl);
            SortedSet<String> result = config.getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).doesNotContain(gpl);
        } catch (IOException e) {
            fail(e.getMessage());
        }
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

        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void counterMinTest() {
        Option option = Arg.COUNTER_MIN.option();
        String[] args = {null, null};

        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void configTest(final Option option) {
        String[] args = {"src/test/resources/OptionTools/One.xml", "src/test/resources/OptionTools/Two.xml"};
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, args);
        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void licensesTest() {
        configTest(Arg.CONFIGURATION.find("licenses"));
    }

    protected void configTest() {
        configTest(Arg.CONFIGURATION.find("config"));
    }

    private void noDefaultsTest(final Option arg) {
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(arg, null));
            assertThat(config.getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isEmpty();
            config = generateConfig(ImmutablePair.nullPair());
            assertThat(config.getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isNotEmpty();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void noDefaultsTest() {
        noDefaultsTest(Arg.CONFIGURATION_NO_DEFAULTS.find("no-default-licenses"));
    }

    protected void configurationNoDefaultsTest() {
        noDefaultsTest(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"));
    }

    protected void dryRunTest() {
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(Arg.DRY_RUN.find("dry-run"), null));
            assertThat(config.isDryRun()).isTrue();
            config = generateConfig(ImmutablePair.nullPair());
            assertThat(config.isDryRun()).isFalse();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void editCopyrightTest(final Option option) {
        try {
            Pair<Option, String[]> arg1 = ImmutablePair.of(option, new String[]{"MyCopyright"});
            ReportConfiguration config = generateConfig(arg1);
            assertThat(config.getCopyrightMessage()).as("Copyright without --edit-license should not work").isNull();
            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null);
            config = generateConfig(arg1, arg2);
            assertThat(config.getCopyrightMessage()).isEqualTo("MyCopyright");
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void copyrightTest() {
        editCopyrightTest(Arg.EDIT_COPYRIGHT.find("copyright"));
    }

    protected void editCopyrightTest() {
        editCopyrightTest(Arg.EDIT_COPYRIGHT.find("edit-copyright"));
    }

    private void editLicenseTest(final Option option) {
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, null));
            assertThat(config.isAddingLicenses()).isTrue();
            config = generateConfig(ImmutablePair.nullPair());
            assertThat(config.isAddingLicenses()).isFalse();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void addLicenseTest() {
        editLicenseTest(Arg.EDIT_ADD.find("addLicense"));
    }

    protected void editLicensesTest() {
        editLicenseTest(Arg.EDIT_ADD.find("edit-license"));
    }

    private void overwriteTest(final Option option) {
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, null);
        try {
            ReportConfiguration config = generateConfig(arg1);
            assertThat(config.isAddingLicensesForced()).isFalse();
            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null);

            config = generateConfig(arg1, arg2);
            assertThat(config.isAddingLicensesForced()).isTrue();
        } catch (IOException e) {
            fail(e.getMessage());
        }
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
        try {
            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                args[0] = proc.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertThat(config.getArchiveProcessing()).isEqualTo(proc);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void outputArchiveTest() {
        archiveTest(Arg.OUTPUT_ARCHIVE.find("output-archive"));
    }

    private void listFamilies(final Option option) {
        String[] args = {null};
        for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
            try {
                args[0] = filter.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertThat(config.listFamilies()).isEqualTo(filter);
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
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
        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void outTest() {
        outTest(Arg.OUTPUT_FILE.find("out"));
    }

    protected void outputFileTest() {
        outTest(Arg.OUTPUT_FILE.find("output-file"));
    }

    private void listLicenses(final Option option) {
        String[] args = {null};
        for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
            try {
                args[0] = filter.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertThat(config.listLicenses()).isEqualTo(filter);
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
    }

    protected void listLicensesTest() {
        listLicenses(Arg.OUTPUT_LICENSES.find("list-licenses"));
    }

    protected void outputLicensesTest() {
        listLicenses(Arg.OUTPUT_LICENSES.find("output-licenses"));
    }

    private void standardTest(final Option option) {
        String[] args = {null};
        try {
            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                args[0] = proc.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertThat(config.getStandardProcessing()).isEqualTo(proc);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void outputStandardTest() {
        standardTest(Arg.OUTPUT_STANDARD.find("output-standard"));
    }

    private void styleSheetTest(final Option option) {
        // copy the dummy stylesheet so that we have a local file for users of the testing jar.
        File file = new File(baseDir, "stylesheet-" + option.getLongOpt());
        try (
                InputStream in = ReporterTest.class.getResourceAsStream("MatcherContainerResource.txt");
                OutputStream out = Files.newOutputStream(file.toPath())) {
            IOUtils.copy(in, out);
        } catch (IOException e) {
            fail("Could not copy MatcherContainerResource.txt: " + e.getMessage());
        }
        // run the test
        String[] args = {null};
        try {
            for (String sheet : new String[]{"plain-rat", "missing-headers", "unapproved-licenses", file.getAbsolutePath()}) {
                args[0] = sheet;
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                try (InputStream expected = StyleSheets.getStyleSheet(sheet).get();
                     InputStream actual = config.getStyleSheet().get()) {
                    assertThat(IOUtils.contentEquals(expected, actual)).as(() -> String.format("'%s' does not match", sheet)).isTrue();
                }
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void styleSheetTest() {
        styleSheetTest(Arg.OUTPUT_STYLE.find("stylesheet"));
    }

    protected void outputStyleTest() {
        styleSheetTest(Arg.OUTPUT_STYLE.find("output-style"));
    }

    protected void scanHiddenDirectoriesTest() {
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(Arg.INCLUDE_STD.find("scan-hidden-directories"), null));
            DocumentNameMatcher excluder = config.getDocumentExcluder(baseName());
            assertThat(excluder.matches(mkDocName(".file"))).as(".file").isTrue();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void xmlTest() {
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(Arg.OUTPUT_STYLE.find("xml"), null));
            try (InputStream expected = StyleSheets.getStyleSheet("xml").get();
                 InputStream actual = config.getStyleSheet().get()) {
                assertThat(IOUtils.contentEquals(expected, actual)).as("'xml' does not match").isTrue();
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Override
    final public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
        List<Arguments> lst = new ArrayList<>();
        List<String> missingTests = new ArrayList<>();

        for (String key : OptionsList.getKeys()) {
            OptionCollectionTest.OptionTest test = testMap.get(key);
            if (test == null) {
                missingTests.add(key);
            } else {
                lst.add(Arguments.of(key, test));
            }
        }
        if (!missingTests.isEmpty()) {
            System.out.println("The following tests are excluded: '" + String.join("', '", missingTests) + "'");
        }
        return lst.stream();
    }
}
