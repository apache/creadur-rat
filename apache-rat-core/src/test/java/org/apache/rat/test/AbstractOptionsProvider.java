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

import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.OptionCollectionTest;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReportTest;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.impl.DocumentNameMatcher;
import org.apache.rat.document.impl.DocumentName;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log.Level;
import org.apache.rat.utils.iterator.WrappedIterator;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

import static org.apache.rat.commandline.Arg.HELP_LICENSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A list of methods that an OptionsProvider in a test case must support.
 * Use of this interface ensures consistent testing across the UIs. Each method
 * tests an Option from OptionCollection that must be implemented in the UI.
 * Each method in this interface tests an Option in {@link org.apache.rat.OptionCollection}.
 */
public abstract class AbstractOptionsProvider {
    /** A map of test Options to tests */
    protected final Map<String, OptionCollectionTest.OptionTest> testMap = new TreeMap<>();

    protected static final String[] EXCLUDE_ARGS = { "*.foo", "%regex[[A-Z]\\.bar]", "justbaz"};
    protected static final String[] INCLUDE_ARGS = { "B.bar", "justbaz" };
    /**
     * The directory to place test data in.
     * We do not use temp file here as we want the evidence to survive failure.
     */
    protected final File baseDir;

    protected DocumentName baseName() {
        return new DocumentName(baseDir);
    }

    protected AbstractOptionsProvider(Collection<String> unsupportedArgs) {
        baseDir = new File("target/optionTools");
        baseDir.mkdirs();

        testMap.put("addLicense", this::addLicenseTest);
        testMap.put("config", this::configTest);
        testMap.put("configuration-no-defaults", this::configurationNoDefaultsTest);
        testMap.put("copyright", this::copyrightTest);
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
        testMap.put("input-include", this::inputIncludeTest);
        testMap.put("input-include-file", this::inputIncludeFileTest);
        testMap.put("input-include-std", this::inputIncludeStdTest);
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

    private void verifyAllMethodsDefinedAndNeeded(Collection<String> unsupportedArgs) {
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

    protected abstract ReportConfiguration generateConfig(Pair<Option, String[]>... args) throws IOException;

    protected File writeFile(String name, Iterable<String> lines) {
        File file = new File(baseDir, name);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            lines.forEach(writer::println);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
        return file;
    }

    protected DocumentName mkDocName(String name) {
        return new DocumentName(new File(baseDir, name), new DocumentName(baseDir));
    }

    /* tests to be implemented */
    protected abstract void helpTest();

    // exclude tests
    private void execExcludeTest(Option option, String[] args) {
        String[] notExcluded = { "notbaz", "well._afile"  };
        String[] excluded = { "some.foo", "B.bar", "justbaz"};
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            DocumentNameMatcher matcher = config.getNameMatcher(baseName());
            for (String fname : notExcluded) {
                assertTrue(matcher.matches(mkDocName(fname)), () -> option.getKey() + " " + fname);
            }
            for (String fname : excluded) {
                assertFalse(matcher.matches(mkDocName(fname)), () -> option.getKey() + " " + fname);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void excludeFileTest(Option option) {
        File outputFile = writeFile("exclude.txt", Arrays.asList(EXCLUDE_ARGS));
        execExcludeTest(option, new String[] {outputFile.getPath()});
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
        String[] args = { StandardCollection.MISC.name() };
        String[] excluded = { "afile~", ".#afile", "%afile%", "._afile" };
        String[] notExcluded = { "afile~more",   "what.#afile", "%afile%withMore", "well._afile" };
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            DocumentNameMatcher matcher = config.getNameMatcher(baseName());
            for (String fname : excluded) {
                assertFalse(matcher.matches(mkDocName(fname)), () -> option.getKey() + " " + fname);
            }
            for (String fname : notExcluded) {
                assertTrue(matcher.matches(mkDocName(fname)), () -> option.getKey() + " " + fname);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void inputExcludeParsedScmTest() {
        Option option = Arg.EXCLUDE_PARSE_SCM.find("input-exclude-parsed-scm");
        String[] args = { "GIT" };
        String[] lines = {
                "# somethings",
                "!thingone", "thing*", System.lineSeparator(),
                "# some fish",
                "**/fish", "*_fish",
                "# some colorful directories",
                "red/", "blue/*/"};
        String[] notExcluded = { "thingone", "dir/fish_two"};
        String[] excluded = { "thingtwo", "dir/fish", "red/fish", "blue/fish" };

        writeFile(".gitignore", Arrays.asList(lines));

        List<String> expected = WrappedIterator.create(Arrays.asList("thing*", "**/fish", "*_fish", "red/**", "blue/*/**").iterator())
                .map(s -> new File(baseDir, s).getPath()).toList();
        expected.add(0, "!" + new File(baseDir, "thingone").getPath());
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
            DocumentNameMatcher matcher = config.getNameMatcher(baseName());
            for (String fname : excluded) {
                assertFalse(matcher.matches(mkDocName(fname)), () -> option.getKey() + " " + fname);
            }
            for (String fname : notExcluded) {
                assertTrue(matcher.matches(mkDocName(fname)), () -> option.getKey() + " " + fname);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    // include tests
    private void execIncludeTest(Option option, String[] args) {
        Option excludeOption = Arg.EXCLUDE.option();
        String[] notExcluded = {"B.bar", "justbaz", "notbaz"};
        String[] excluded = {"some.foo"};
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, args),
                    ImmutablePair.of(excludeOption, EXCLUDE_ARGS));
            DocumentNameMatcher matcher = config.getNameMatcher(baseName());
            for (String fname : excluded) {
                assertFalse(matcher.matches(mkDocName(fname)), () -> option.getKey() + " " + fname);
            }
            for (String fname : notExcluded) {
                assertTrue(matcher.matches(mkDocName(fname)), () -> option.getKey() + " " + fname);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void includeFileTest(Option option) {
        File outputFile = writeFile("include.txt", Arrays.asList(INCLUDE_ARGS));
        execIncludeTest(option, new String[] {outputFile.getPath()});
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
                new String[] { "*~more", "*~" });
        Option option = Arg.INCLUDE_STD.find("input-include-std");
        String[] args = { StandardCollection.MISC.name() };
        String[] excluded = { "afile~more" };
        String[] notExcluded = { "afile~", ".#afile", "%afile%", "._afile", "what.#afile", "%afile%withMore", "well._afile" };
        try {
            ReportConfiguration config = generateConfig(excludes, ImmutablePair.of(option, args));
            DocumentNameMatcher matcher = config.getNameMatcher(baseName());
            for (String fname : excluded) {
                assertFalse(matcher.matches(mkDocName(fname)), () -> option.getKey() + " " + fname);
            }
            for (String fname : notExcluded) {
                assertTrue(matcher.matches(mkDocName(fname)), () -> option.getKey() + " " + fname);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    // LICENSE tests
    protected void execLicensesApprovedTest(Option option, String[] args) {
        Pair<Option,String[]>  arg1 = ImmutablePair.of(option, args);
        try {
            ReportConfiguration config = generateConfig(arg1);
            SortedSet<String> result = config.getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).contains("one", "two");
        } catch (IOException e) {
            fail(e.getMessage());
        }

        Pair<Option,String[]> arg2 = ImmutablePair.of(
                Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"),
                null
        );

        try {
            ReportConfiguration config = generateConfig(arg1, arg2 );
            SortedSet<String> result = config.getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).containsExactly("one", "two");
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void helpLicenses() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream origin = System.out;
        try (PrintStream out = new PrintStream(output)){
            System.setOut(out);
            ReportConfiguration config = generateConfig(ImmutablePair.of(HELP_LICENSES.option(), null));
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
                new String[] { outputFile.getPath()});
    }

    protected void licensesApprovedTest() {
        execLicensesApprovedTest(Arg.LICENSES_APPROVED.find("licenses-approved"),
                new String[] { "one", "two"});
    }

    private void execLicensesDeniedTest(Option option, String[] args) {
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
        execLicensesDeniedTest(Arg.LICENSES_DENIED.find("licenses-denied"), new String[] {"ILLUMOS"});
    }

    protected void licensesDeniedFileTest() {
        File outputFile = writeFile("licensesDenied.txt", Collections.singletonList("ILLUMOS"));
        execLicensesDeniedTest(Arg.LICENSES_DENIED_FILE.find("licenses-denied-file"), new String[] {outputFile.getPath()});
    }

    private void execLicenseFamiliesApprovedTest(Option option, String[] args) {
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
                new String[] { outputFile.getPath() });
    }

    protected void licenseFamiliesApprovedTest() {
        execLicenseFamiliesApprovedTest(Arg.FAMILIES_APPROVED.find("license-families-approved"),
                new String[] {"catz"});
    }

    private void execLicenseFamiliesDeniedTest(Option option, String[] args) {
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
                new String[] { outputFile.getPath() });
    }

    protected void licenseFamiliesDeniedTest() {
        execLicenseFamiliesDeniedTest(Arg.FAMILIES_DENIED.find("license-families-denied"),
                new String[] { "GPL" });
    }

    private void configTest(Option option) {
        String[] args = {"src/test/resources/OptionTools/One.xml", "src/test/resources/OptionTools/Two.xml"};
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, args);
        try {
            ReportConfiguration config = generateConfig(arg1);
            SortedSet<ILicense> set = config.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
            assertTrue(set.size() > 2);
            assertTrue(LicenseSetFactory.search("ONE", "ONE", set).isPresent());
            assertTrue(LicenseSetFactory.search("TWO", "TWO", set).isPresent());

            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"), null);

            config = generateConfig(arg1, arg2);
            set = config.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
            assertEquals(2, set.size());
            assertTrue(LicenseSetFactory.search("ONE", "ONE", set).isPresent());
            assertTrue(LicenseSetFactory.search("TWO", "TWO", set).isPresent());
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

    private void noDefaultsTest(Option arg) {
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(arg, null));
            assertTrue(config.getLicenses(LicenseSetFactory.LicenseFilter.ALL).isEmpty());
            config = generateConfig(ImmutablePair.nullPair());
            assertFalse(config.getLicenses(LicenseSetFactory.LicenseFilter.ALL).isEmpty());
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
            assertTrue(config.isDryRun());
            config = generateConfig(ImmutablePair.nullPair());
            assertFalse(config.isDryRun());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void editCopyrightTest(Option option) {
        try {
            Pair<Option, String[]> arg1 = ImmutablePair.of(option, new String[]{"MyCopyright"});
            ReportConfiguration config = generateConfig(arg1);
            assertNull(config.getCopyrightMessage(), "Copyright without --edit-license should not work");
            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null);
            config = generateConfig(arg1, arg2);
            assertEquals("MyCopyright", config.getCopyrightMessage());
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

    private void editLicenseTest(Option option) {
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(option, null));
            assertTrue(config.isAddingLicenses());
            config = generateConfig(ImmutablePair.nullPair());
            assertFalse(config.isAddingLicenses());
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

    private void overwriteTest(Option option) {
        Pair<Option, String[]> arg1 = ImmutablePair.of(option, null);
        try {
            ReportConfiguration config = generateConfig(arg1);
            assertFalse(config.isAddingLicensesForced());
            Pair<Option, String[]> arg2 = ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null);

            config = generateConfig(arg1, arg2);
            assertTrue(config.isAddingLicensesForced());
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
        Level logLevel = ((DefaultLog) DefaultLog.getInstance()).getLevel();
        try {
            for (Level level : Level.values()) {
                try {
                    args[0] = level.name();
                    ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                    assertEquals(level, ((DefaultLog) DefaultLog.getInstance()).getLevel());
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        } finally {
            ((DefaultLog) DefaultLog.getInstance()).setLevel(logLevel);
        }
    }

    private void archiveTest(Option option) {
        String[] args = {null};
        try {
            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                args[0] = proc.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertEquals(proc, config.getArchiveProcessing());
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void outputArchiveTest() {
        archiveTest(Arg.OUTPUT_ARCHIVE.find("output-archive"));
    }

    private void listFamilies(Option option) {
        String[] args = {null};
        for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
            try {
                args[0] = filter.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertEquals(filter, config.listFamilies());
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

    private void outTest(Option option) {
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
                assertEquals("Hello world", reader.readLine());
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

    private void listLicenses(Option option) {
        String[] args = {null};
        for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
            try {
                args[0] = filter.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertEquals(filter, config.listLicenses());
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

    private void standardTest(Option option) {
        String[] args = { null};
        try {
            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                args[0] = proc.name();
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                assertEquals(proc, config.getStandardProcessing());
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void outputStandardTest() {
        standardTest(Arg.OUTPUT_STANDARD.find("output-standard"));
    }

    private void styleSheetTest(Option option) {
        // copy the dummy stylesheet so that  we have a local file for users of the testing jar.
        File file = new File(baseDir, "stylesheet-" + option.getLongOpt());
        try (
            InputStream in = ReportTest.class.getResourceAsStream("MatcherContainerResource.txt");
            OutputStream out = Files.newOutputStream(file.toPath())) {
            IOUtils.copy(in, out);
        } catch (IOException e) {
            fail("Could not copy MatcherContainerResource.txt: "+e.getMessage());
        }
        // run the test
        String[] args = {null};
        try {
            for (String sheet : new String[]{"plain-rat", "missing-headers", "unapproved-licenses", file.getAbsolutePath()}) {
                args[0] = sheet;
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                try (InputStream expected = StyleSheets.getStyleSheet(sheet).get();
                     InputStream actual = config.getStyleSheet().get()) {
                    assertTrue(IOUtils.contentEquals(expected, actual), () -> String.format("'%s' does not match", sheet));
                }
            }
        } catch (IOException e) {
            fail(e.getMessage(), e);
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
            DocumentNameMatcher matcher = config.getNameMatcher(baseName());
            assertTrue(matcher.matches(mkDocName(".file")), ".file");
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    protected void xmlTest() {
        try {
            ReportConfiguration config = generateConfig(ImmutablePair.of(Arg.OUTPUT_STYLE.find("xml"), null));
            try (InputStream expected = StyleSheets.getStyleSheet("xml").get();
                 InputStream actual = config.getStyleSheet().get()) {
                assertTrue(IOUtils.contentEquals(expected, actual), "'xml' does not match");
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

        final public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
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
                        System.out.println("The following tests are excluded: '"+String.join( "', '", missingTests )+"'");
                    }
                return lst.stream();
            }
}
