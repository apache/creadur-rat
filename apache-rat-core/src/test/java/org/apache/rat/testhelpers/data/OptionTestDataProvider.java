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
package org.apache.rat.testhelpers.data;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.rat.ConfigurationException;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.writer.XmlWriter;

import org.apache.rat.testhelpers.FileUtils;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

import static org.apache.rat.testhelpers.FileUtils.writeFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Generates a list of TestData for options.
 * Use of this interface ensures consistent testing across the UIs. Each method
 * tests an Option from {@link OptionCollection} that must be implemented in the UI.
 */
public class OptionTestDataProvider {

    /** The list of exclude args */
    static final String[] EXCLUDE_ARGS = {"*.foo", "%regex[[A-Z]\\.bar]", "justbaz"};
    /** the list of include args */
    static final String[] INCLUDE_ARGS = {"B.bar", "justbaz"};

    static String dump(final Option option, final String fname, final DocumentNameMatcher matcher, final DocumentName name) {
        StringBuilder sb = new StringBuilder();
        matcher.decompose(name).forEach(s -> sb.append(s).append("\n"));
        return String.format("Argument and Name: %s %s%nMatcher decomposition:%n%s", option.getLongOpt(), fname, sb);
    }

    /**
     * Generates the map of UI TestData indexed by testName.
     * @return the map of UI TestData indexed by testName.
     */
    public Map<String, TestData> getUITestMap() {
        final ConfigurationException noLicenses = new ConfigurationException("At least one license must be defined");
        Map<String, TestData> result = getOptionTestMap();
        result.get("configuration-no-defaults").setException(noLicenses);
        result.get("licenses-approved/withoutDefaults").setException(noLicenses);
        result.get("licenses-approved-file/withoutDefaults").setException(noLicenses);
        result.get("license-families-approved/withoutDefaults").setException(noLicenses);
        result.get("license-families-approved-file/withoutDefaults").setException(noLicenses);
        return result;
    }

    /**
     * Generates a map of TestData indexed by the testName
     * @return the map of testName to Test Data.
     * @see #getOptionTests()
     */
    public Map<String, TestData> getOptionTestMap() {
        Map<String, TestData> map = new TreeMap<>();
        for (TestData test : getOptionTests()) {
            map.put(test.getTestName(), test);
        }
        return map;
    }

    /**
     * Generates a list of test data for Option testing.
     * This is different from UI testing as this is to test
     * that the command line is properly parsed into a configuration.
     * @return a list of TestData for the tests.
     */
    public List<TestData> getOptionTests() {
        List<TestData> result = new ArrayList<>();
        result.addAll(configTest());
        result.addAll(configurationNoDefaultsTest());
        result.addAll(counterMinTest());
        result.addAll(counterMaxTest());
        result.addAll(dryRunTest());
        result.addAll(editCopyrightTest());
        result.addAll(editLicenseTest());
        result.addAll(editOverwriteTest());
        result.addAll(helpLicenses());
        result.addAll(inputExcludeTest());
        result.addAll(inputExcludeFileTest());
        result.addAll(inputExcludeParsedScmTest());
        result.addAll(inputExcludeStdTest());
        result.addAll(inputExcludeSizeTest());
        result.addAll(inputIncludeTest());
        result.addAll(inputIncludeFileTest());
        result.addAll(inputIncludeStdTest());
        result.addAll(inputSourceTest());
        result.addAll(licenseFamiliesApprovedTest());
        result.addAll(licenseFamiliesApprovedFileTest());
        result.addAll(licenseFamiliesDeniedTest());
        result.addAll(licenseFamiliesDeniedFileTest());
        result.addAll(licensesApprovedTest());
        result.addAll(licensesApprovedFileTest());
        result.addAll(licensesDeniedTest());
        result.addAll(licensesDeniedFileTest());
        result.addAll(logLevelTest());
        result.addAll(outputArchiveTest());
        result.addAll(outputFamiliesTest());
        result.addAll(outputFileTest());
        result.addAll(outputLicensesTest());
        result.addAll(outputStandardTest());
        result.addAll(outputStyleTest());
        validate(result);
        return result;
    }

    private void validate(List<TestData> result) {
        Set<Option> options = new HashSet<>(Arg.getOptions(new Options()).getOptions());
        result.forEach(testData -> options.remove(testData.getOption()));
        // TODO fix this once deprecated options are removed
        options.forEach(opt -> DefaultLog.getInstance().warn("Option " + opt.getKey() + " was not tested."));
        //assertThat(options).describedAs("All options are not accounted for.").isEmpty();
    }

    // exclude tests
    private List<TestData> execExcludeTest(final Option option, final Supplier<String[]> args, Consumer<DocumentName> setupFiles) {
        String[] notExcluded = {"notbaz", "well._afile"};
        String[] excluded = {"some.foo", "B.bar", "justbaz"};
        Consumer<Path> preSetup = basePath -> setupFiles.accept(DocumentName.builder(basePath.toFile()).build());
        TestData test1 = new TestData("",
                Collections.singletonList(ImmutablePair.of(option, args.get())),
                preSetup,
                validatorData -> {
                    DocumentNameMatcher excluder = validatorData.getConfiguration().getDocumentExcluder(validatorData.getBaseName());
                    for (String fname : notExcluded) {
                        DocumentName docName = validatorData.mkDocName(fname);
                        assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
                    }
                    for (String fname : excluded) {
                        DocumentName docName = validatorData.mkDocName(fname);
                        assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
                    }
                });
        return Collections.singletonList(test1);
    }

    private List<TestData> excludeFileTest(final Option option) {
        Consumer<DocumentName> setup = baseDir -> writeFile(baseDir.asFile(), "exclude.txt", Arrays.asList(EXCLUDE_ARGS));
        Supplier<String[]> args = () -> new String[]{"exclude.txt"};
        return execExcludeTest(option, args, setup);
    }

    protected List<TestData> inputExcludeFileTest() {
        return excludeFileTest(Arg.EXCLUDE_FILE.find("input-exclude-file"));
    }

    protected List<TestData> inputExcludeTest() {
        return execExcludeTest(Arg.EXCLUDE.find("input-exclude"), () -> EXCLUDE_ARGS, x -> {});
    }

    protected List<TestData> inputExcludeStdTest() {
        Option option = Arg.EXCLUDE_STD.find("input-exclude-std");
        String[] excluded = {"afile~", ".#afile", "%afile%", "._afile"};
        String[] notExcluded = {"afile~more", "what.#afile", "%afile%withMore", "well._afile"};
        
        TestData test1 = new TestData(StandardCollection.MISC.name(),
                Collections.singletonList(ImmutablePair.of(option, new String[]{StandardCollection.MISC.name()})),
        DataUtils.NO_SETUP,
        validatorData -> {
            DocumentNameMatcher excluder = validatorData.getConfiguration().getDocumentExcluder(validatorData.getBaseName());
            for (String fname : excluded) {
                DocumentName docName = validatorData.mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
            }
            for (String fname : notExcluded) {
                DocumentName docName = validatorData.mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
            }
        });
        return Collections.singletonList(test1);
    }

    protected List<TestData> inputExcludeParsedScmTest() {
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

        TestData test1 = new TestData("Git", Collections.singletonList(ImmutablePair.of(option, args)),
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, ".gitignore", Arrays.asList(lines));
                    File dir = new File(baseDir, "red");
                    FileUtils.mkDir(dir);
                    dir = new File(baseDir, "blue");
                    dir = new File(dir, "fish");
                    FileUtils.mkDir(dir);
                },
                validatorData -> {
                    DocumentNameMatcher excluder = validatorData.getConfiguration().getDocumentExcluder(validatorData.getBaseName());
                    for (String fname : excluded) {
                        DocumentName docName = validatorData.mkDocName(fname);
                        assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
                    }
                    for (String fname : notExcluded) {
                        DocumentName docName = validatorData.mkDocName(fname);
                        assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
                    }
                });
        return Collections.singletonList(test1);
    }

    private List<TestData> inputExcludeSizeTest() {
        Option option = Arg.EXCLUDE_SIZE.option();
        String[] notExcluded = {"Hello.txt", "HelloWorld.txt"};
        String[] excluded = {"Hi.txt"};

        TestData test1 = new TestData("5", Collections.singletonList(ImmutablePair.of(option, new String[]{"5"})),
        basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "Hi.txt", Collections.singletonList("Hi"));
            writeFile(baseDir, "Hello.txt", Collections.singletonList("Hello"));
            writeFile(baseDir, "HelloWorld.txt", Collections.singletonList("HelloWorld"));
        },
                validatorData -> {

            DocumentNameMatcher excluder = validatorData.getConfiguration().getDocumentExcluder(validatorData.getBaseName());
            for (String fname : excluded) {
                DocumentName docName = validatorData.mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
            }
            for (String fname : notExcluded) {
                DocumentName docName = validatorData.mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
            }
        });
        return Collections.singletonList(test1);
    }

    // include tests
    private List<TestData> execIncludeTest(final Option option, String[] args, Consumer<DocumentName> setupFiles) {
        Option excludeOption = Arg.EXCLUDE.option();
        String[] notExcluded = {"B.bar", "justbaz", "notbaz"};
        String[] excluded = {"some.foo"};
        TestData test1 = new TestData("", Arrays.asList(ImmutablePair.of(option, args),
                ImmutablePair.of(excludeOption, EXCLUDE_ARGS)),
        basePath -> setupFiles.accept(DocumentName.builder(basePath.toFile()).build()),
        validatorData -> {
            DocumentNameMatcher excluder = validatorData.getConfiguration().getDocumentExcluder(validatorData.getBaseName());
            for (String fname : excluded) {
                DocumentName docName = validatorData.mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
            }
            for (String fname : notExcluded) {
                DocumentName docName = validatorData.mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
            }
        });
        return Collections.singletonList(test1);
    }

    private List<TestData> includeFileTest(final Option option) {
        Consumer<DocumentName> setup = baseDir -> writeFile(baseDir.asFile(), "include.txt", Arrays.asList(INCLUDE_ARGS));
        return execIncludeTest(option, new String[]{"include.txt"}, setup);
    }

    protected List<TestData> inputIncludeFileTest() {
        return includeFileTest(Arg.INCLUDE_FILE.find("input-include-file"));
    }


    protected List<TestData> inputIncludeTest() {
        return execIncludeTest(Arg.INCLUDE.find("input-include"), INCLUDE_ARGS, x -> {});
    }

    protected List<TestData> inputIncludeStdTest() {
        ImmutablePair<Option, String[]> excludes = ImmutablePair.of(Arg.EXCLUDE.find("input-exclude"),
                new String[]{"*~more", "*~"});
        Option option = Arg.INCLUDE_STD.find("input-include-std");
        String[] args = {StandardCollection.MISC.name()};
        String[] excluded = {"afile~more"};
        String[] notExcluded = {"afile~", ".#afile", "%afile%", "._afile", "what.#afile", "%afile%withMore", "well._afile"};

        TestData test1 = new TestData("", Arrays.asList(excludes, ImmutablePair.of(option, args)),
                DataUtils.NO_SETUP,
        validatorData -> {
            DocumentNameMatcher excluder = validatorData.getConfiguration().getDocumentExcluder(validatorData.getBaseName());
            for (String fname : excluded) {
                DocumentName docName = validatorData.mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isFalse();
            }
            for (String fname : notExcluded) {
                DocumentName docName = validatorData.mkDocName(fname);
                assertThat(excluder.matches(docName)).as(() -> dump(option, fname, excluder, docName)).isTrue();
            }
        });
        return Collections.singletonList(test1);
    }

    protected List<TestData> inputSourceTest() {
        Option option = Arg.SOURCE.find("input-source");
        TestData test1 = new TestData("", Collections.singletonList(ImmutablePair.of(option, new String[]{"inputFile.txt"})),
        dirPath -> {
            File input = dirPath.resolve("inputFile.txt").toFile();
            FileUtils.mkDir(input.getParentFile());
        },
                validatorData -> assertThat(validatorData.getConfiguration().hasSource()).isTrue());
        return Collections.singletonList(test1);
    }

    // LICENSE tests
    protected List<TestData> execLicensesApprovedTest(final Option option, String[] args, Consumer<Path> setup) {
        ImmutablePair<Option, String[]> arg1 = ImmutablePair.of(option, args);
        ImmutablePair<Option, String[]> arg2 = ImmutablePair.of(
                Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"),
                null);

        TestData test1 = new TestData("withDefaults", Collections.singletonList(arg1),
                setup,
                validatorData -> {
            SortedSet<String> result = validatorData.getConfiguration().getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).contains("one", "two");
        });


        TestData test2 = new TestData("withoutDefaults", Arrays.asList(arg1, arg2),
        setup,
        validatorData -> {
            SortedSet<String> result = validatorData.getConfiguration().getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).containsExactly("one", "two");
        });

        return Arrays.asList(test1, test2);
    }

    protected List<TestData> helpLicenses() {
        PrintStream origin = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);

        TestData test1 = new TestData("stdOut",
                Collections.singletonList(ImmutablePair.of(Arg.HELP_LICENSES.option(), null)),
                basePath -> System.setOut(out),
                validatorData -> {
                    System.setOut(origin);
                    String txt = baos.toString();
                    TextUtils.assertContains("====== Licenses ======", txt);
                    TextUtils.assertContains("====== Defined Matchers ======", txt);
                    TextUtils.assertContains("====== Defined Families ======", txt);
                });
        return Collections.singletonList(test1);
    }

    protected List<TestData> licensesApprovedFileTest() {
        return execLicensesApprovedTest(Arg.LICENSES_APPROVED_FILE.find("licenses-approved-file"),
                new String[]{"licensesApproved.txt"},
                basePath -> writeFile(basePath.toFile(), "licensesApproved.txt", Arrays.asList("one", "two")));
    }

    protected List<TestData> licensesApprovedTest() {
        return execLicensesApprovedTest(Arg.LICENSES_APPROVED.find("licenses-approved"),
                new String[]{"one, two"}, DataUtils.NO_SETUP);
    }

    private List<TestData> execLicensesDeniedTest(final Option option, final String[] args, Consumer<Path> setup) {
        TestData test1 = new TestData("ILLUMOS", Collections.singletonList(ImmutablePair.of(option, args)),
        setup,
        validatorData -> {
            assertThat(validatorData.getConfiguration().getLicenseIds(LicenseSetFactory.LicenseFilter.ALL)).contains("ILLUMOS");
            SortedSet<String> result = validatorData.getConfiguration().getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).doesNotContain("ILLUMOS");
        });
        return Collections.singletonList(test1);
    }

    protected List<TestData> licensesDeniedTest() {
        return execLicensesDeniedTest(Arg.LICENSES_DENIED.find("licenses-denied"), new String[]{"ILLUMOS"}, DataUtils.NO_SETUP);
    }

    protected List<TestData> licensesDeniedFileTest() {
        return execLicensesDeniedTest(Arg.LICENSES_DENIED_FILE.find("licenses-denied-file"),
                new String[]{"licensesDenied.txt"},
                (basePath) -> writeFile(basePath.toFile(), "licensesDenied.txt", Collections.singletonList("ILLUMOS")));
    }

    private List<TestData> execLicenseFamiliesApprovedTest(final Option option, final String[] args, Consumer<Path> setup) {

        String catz = ILicenseFamily.makeCategory("catz");

        TestData test1 = new TestData("withDefaults", Collections.singletonList(ImmutablePair.of(option, args)),
                setup,
                validatorData -> {
                    SortedSet<String> result = validatorData.getConfiguration().getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
                    assertThat(result).contains(catz);
                });

        TestData test2 = new TestData("withoutDefaults", Arrays.asList(ImmutablePair.of(option, args), ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"), null)),
                setup,
                validatorData -> {
                    SortedSet<String> result = validatorData.getConfiguration().getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
                    assertThat(result).containsExactly(catz);
                });
        return Arrays.asList(test1, test2);
    }

    protected List<TestData> licenseFamiliesApprovedFileTest() {
        return execLicenseFamiliesApprovedTest(Arg.FAMILIES_APPROVED_FILE.find("license-families-approved-file"),
                new String[]{"familiesApproved.txt"},
                basePath -> writeFile(basePath.toFile(), "familiesApproved.txt", Collections.singletonList("catz")));
    }

    protected List<TestData> licenseFamiliesApprovedTest() {
        return execLicenseFamiliesApprovedTest(Arg.FAMILIES_APPROVED.find("license-families-approved"),
                new String[]{"catz"}, DataUtils.NO_SETUP);
    }

    private List<TestData> execLicenseFamiliesDeniedTest(final Option option, final String[] args, Consumer<Path> setup) {
        String gpl = ILicenseFamily.makeCategory("GPL");
        TestData test1 = new TestData("GPL", Collections.singletonList(ImmutablePair.of(option, args)),
setup,
        validatorData -> {
            assertThat(validatorData.getConfiguration().getLicenseCategories(LicenseSetFactory.LicenseFilter.ALL)).contains(gpl);
            SortedSet<String> result = validatorData.getConfiguration().getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).doesNotContain(gpl);
        });
        return Collections.singletonList(test1);
    }

    protected List<TestData> licenseFamiliesDeniedFileTest() {
        return execLicenseFamiliesDeniedTest(Arg.FAMILIES_DENIED_FILE.find("license-families-denied-file"),
                new String[]{"familiesDenied.txt"},
        baseDir -> writeFile(baseDir.toFile(), "familiesDenied.txt", Collections.singletonList("GPL")));
    }

    protected List<TestData> licenseFamiliesDeniedTest() {
        return execLicenseFamiliesDeniedTest(Arg.FAMILIES_DENIED.find("license-families-denied"),
                new String[]{"GPL"}, DataUtils.NO_SETUP);
    }

    protected List<TestData> counterMaxTest() {
        Option option = Arg.COUNTER_MAX.option();

        TestData test1 = new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0));

        TestData test2 = new TestData("negativeValue", Collections.singletonList(ImmutablePair.of(option,
                new String[]{"Unapproved:-1", "ignored:1"})),
                DataUtils.NO_SETUP,
                validatorData -> {
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(Integer.MAX_VALUE);
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMax(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
                });

        TestData test3 = new TestData("standardValue", Collections.singletonList(ImmutablePair.of(option,
                new String[]{"Unapproved:5", "ignored:0"})),
DataUtils.NO_SETUP,
                validatorData -> {
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMax(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                });

        return Arrays.asList(test1, test2, test3);
    }

    protected List<TestData> counterMinTest() {
        Option option = Arg.COUNTER_MIN.option();

        TestData test1 = new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0));

        String[] args = {"Unapproved:1", "ignored:1"};
        TestData test2 = new TestData("capitalized", Collections.singletonList(ImmutablePair.of(option, args)),
                DataUtils.NO_SETUP,
                validatorData -> {
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
                });

        args = new String[]{"unapproved:5", "ignored:0"};
        TestData test3 = new TestData("lowerCase", Collections.singletonList(ImmutablePair.of(option, args)),
                DataUtils.NO_SETUP,
                validatorData -> {
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                });

        args = new String[]{"unapproved:-5", "ignored:0"};
        TestData test4 = new TestData("negativeValue", Collections.singletonList(ImmutablePair.of(option, args)),
                DataUtils.NO_SETUP,
                validatorData -> {
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(-5);
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                });

        return Arrays.asList(test1, test2, test3, test4);
    }

    private List<TestData> configTest(final Option option) {
        Consumer<Path> setupFiles = basePath -> {
            Path ratDir = basePath.resolve(".rat");
            FileUtils.mkDir(ratDir.toFile());
            Path oneXml = ratDir.resolve("One.xml");
            DataUtils.generateTextConfig(oneXml, "ONE", "one");

            Path twoXml = ratDir.resolve("Two.xml");
            DataUtils.generateTextConfig(twoXml, "TWO", "two");
        };
        String[] args = {".rat/One.xml", ".rat/Two.xml"};

        TestData test1 = new TestData("withDefaults", Collections.singletonList(ImmutablePair.of(option, args)),
                setupFiles,
                validatorData -> {
                    SortedSet<ILicense> set = validatorData.getConfiguration().getLicenses(LicenseSetFactory.LicenseFilter.ALL);
                    assertThat(set).hasSizeGreaterThan(2);
                    assertThat(LicenseSetFactory.search("ONE", "ONE", set)).isPresent();
                    assertThat(LicenseSetFactory.search("TWO", "TWO", set)).isPresent();
                });

        TestData test2 = new TestData("withoutDefaults", Arrays.asList(ImmutablePair.of(option, args),
                ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"), null)),
                setupFiles,
                validatorData -> {

                    SortedSet<ILicense> set = validatorData.getConfiguration().getLicenses(LicenseSetFactory.LicenseFilter.ALL);
                    assertThat(set).hasSize(2);
                    assertThat(LicenseSetFactory.search("ONE", "ONE", set)).isPresent();
                    assertThat(LicenseSetFactory.search("TWO", "TWO", set)).isPresent();
                });
        return Arrays.asList(test1, test2);
    }

    protected List<TestData> configTest() {
        return configTest(Arg.CONFIGURATION.find("config"));
    }

    private List<TestData> noDefaultsTest(final Option option) {
        TestData test1 = new TestData("", Collections.singletonList(ImmutablePair.of(option, null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isEmpty());

        TestData test2 = new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isNotEmpty());

        return Arrays.asList(test1, test2);
    }

    protected List<TestData> configurationNoDefaultsTest() {
        return noDefaultsTest(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"));
    }

    protected List<TestData> dryRunTest() {
        Option option = Arg.DRY_RUN.find("dry-run");
        TestData test1 = new TestData("stdRun", Collections.singletonList(ImmutablePair.of(option, null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isDryRun()).isTrue());

        TestData test2 = new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isDryRun()).isFalse());
        return Arrays.asList(test1, test2);
    }

    private List<TestData> editCopyrightTest(final Option option) {
        TestData test1 = new TestData("noEditLicense", Collections.singletonList(ImmutablePair.of(option, new String[]{"MyCopyright"})),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getCopyrightMessage()).as("Copyright without --edit-license should not work").isNull());

        TestData test2 = new TestData("MyCopyright", Arrays.asList(ImmutablePair.of(option, new String[]{"MyCopyright"}),
                ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getCopyrightMessage()).isEqualTo("MyCopyright"));
return Arrays.asList(test1, test2);
    }

    protected List<TestData> editCopyrightTest() {
        return editCopyrightTest(Arg.EDIT_COPYRIGHT.find("edit-copyright"));
    }

    private List<TestData> editLicenseTest(final Option option) {
        TestData test1 = new TestData("", Collections.singletonList(ImmutablePair.of(option, null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicenses()).isTrue());

        TestData test2 = new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicenses()).isFalse());
        return Arrays.asList(test1, test2);
    }

    protected List<TestData> editLicenseTest() {
        return editLicenseTest(Arg.EDIT_ADD.find("edit-license"));
    }

    private List<TestData>  overwriteTest(final Option option) {
        TestData test1 = new TestData("noEditLicense", Collections.singletonList(ImmutablePair.of(option, null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicensesForced())
                        .describedAs("Without edit-license should be false").isFalse());

        TestData test = new TestData("", Arrays.asList(ImmutablePair.of(option, null),
                ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicensesForced()).isTrue());
        return Arrays.asList(test1, test);
    }

    protected List<TestData> editOverwriteTest() {
        return overwriteTest(Arg.EDIT_OVERWRITE.find("edit-overwrite"));
    }

    protected List<TestData> logLevelTest() {
        Option option = Arg.LOG_LEVEL.find("log-level");
        List<TestData> result = new ArrayList<>();
        Log.Level logLevel = DefaultLog.getInstance().getLevel();

        for (final Log.Level level : Log.Level.values()) {
            TestData testData = new TestData(level.name(),
                    Collections.singletonList(ImmutablePair.of(option, new String[]{level.name()})),
                    DataUtils.NO_SETUP,
                    validatorData -> {
                        try {
                            assertThat(DefaultLog.getInstance().getLevel()).isEqualTo(level);
                        } finally {
                            DefaultLog.getInstance().setLevel(logLevel);
                        }
                    });
            result.add(testData);
        }
        return result;
    }

    private List<TestData> archiveTest(final Option option) {
        List<TestData> result = new ArrayList<>();
        for (ReportConfiguration.Processing processing : ReportConfiguration.Processing.values()) {
            TestData test = new TestData(processing.name(),
                    Collections.singletonList(ImmutablePair.of(option, new String[]{processing.name()})),
                    DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getArchiveProcessing()).isEqualTo(processing));
            result.add(test);
        }
        return result;
    }

    protected List<TestData> outputArchiveTest() {
        return archiveTest(Arg.OUTPUT_ARCHIVE.find("output-archive"));
    }

    private List<TestData> listFamilies(final Option option) {
        List<TestData> result = new ArrayList<>();
        for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
            TestData test = new TestData(filter.name(),
                    Collections.singletonList(ImmutablePair.of(option, new String[]{filter.name()})),
                    DataUtils.NO_SETUP,
                    validatorData -> assertThat(validatorData.getConfiguration().listFamilies()).isEqualTo(filter));
            result.add(test);
        }
        return result;
    }

    protected List<TestData> outputFamiliesTest() {
        return listFamilies(Arg.OUTPUT_FAMILIES.find("output-families"));
    }

    private List<TestData> outTest(final Option option) {
        String[] args = new String[]{"outexample"};
        TestData test1 = new TestData("", Collections.singletonList(ImmutablePair.of(option, args)),
        DataUtils.NO_SETUP,
        validatorData -> {
            try (OutputStream os = validatorData.getConfiguration().getOutput().get()) {
                os.write("Hello world".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            DocumentName outputDocument = validatorData.mkDocName("outexample");

            try {
                assertThat(TextUtils.readFile(outputDocument.asFile())).contains("Hello world");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return Collections.singletonList(test1);
    }

    protected List<TestData> outputFileTest() {
        return outTest(Arg.OUTPUT_FILE.find("output-file"));
    }

    private List<TestData> listLicenses(final Option option) {
        List<TestData> result = new ArrayList<>();
        for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
            TestData test = new TestData(filter.name(), Collections.singletonList(ImmutablePair.of(option, new String[]{filter.name()})),
            DataUtils.NO_SETUP,
            validatorData -> assertThat(validatorData.getConfiguration().listLicenses()).isEqualTo(filter));
            result.add(test);
        }
        return result;
    }

    protected List<TestData> outputLicensesTest() {
        return listLicenses(Arg.OUTPUT_LICENSES.find("output-licenses"));
    }

    private List<TestData> standardTest(final Option option) {
        List<TestData> result = new ArrayList<>();
            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                TestData test = new TestData(proc.name(), Collections.singletonList(ImmutablePair.of(option, new String[]{proc.name()})),
                        DataUtils.NO_SETUP,
                        validatorData -> assertThat(validatorData.getConfiguration().getStandardProcessing()).isEqualTo(proc));
                result.add(test);
            }
            return result;
    }

    protected List<TestData> outputStandardTest() {
        return standardTest(Arg.OUTPUT_STANDARD.find("output-standard"));
    }

    private List<TestData> styleSheetTest(final Option option) {
        List<TestData> result = new ArrayList<>();

        for (StyleSheets sheet : StyleSheets.values()) {
            TestData test = new TestData(sheet.name(), Collections.singletonList(ImmutablePair.of(option, new String[]{sheet.arg()})),
                    DataUtils.NO_SETUP,
                    validatorData -> {
                        try (InputStream expected = sheet.getStyleSheet().get();
                             InputStream actual = validatorData.getConfiguration().getStyleSheet().get()) {
                            assertThat(IOUtils.contentEquals(expected, actual)).as(() -> String.format("'%s' does not match", sheet)).isTrue();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            result.add(test);
        }

        TestData test = new TestData("fileStyleSheet", Collections.singletonList(ImmutablePair.of(option, new String[]{"fileStyleSheet.xslt"})),
                basePath -> {
                    FileUtils.mkDir(basePath.toFile());
                    DocumentName name = DocumentName.builder().setName("fileStyleSheet.xslt")
                            .setBaseName(basePath.toString()).build();
                    try (FileWriter fileWriter = new FileWriter(new File(name.getBaseName(), name.getName()));
                         XmlWriter writer = new XmlWriter(fileWriter)) {
                        writer.startDocument()
                                .comment(DataUtils.ASF_TEXT)
                                .openElement("xsl:stylesheet")
                                .attribute("version", "1.0")
                                .attribute("xmlns:xsl", "http://www.w3.org/1999/XSL/Transform")
                                .openElement("xsl:template")
                                .attribute("match", "@*|node()")
                                .openElement("xsl:copy")
                                .closeDocument();
                    } catch (IOException e) {
                        fail(e.getMessage(), e);
                    }
                },

                validatorData -> {
                    try (InputStream expected = StyleSheets.getStyleSheet("fileStyleSheet.xslt", validatorData.getBaseName()).get();
                         InputStream actual = validatorData.getConfiguration().getStyleSheet().get()) {
                        assertThat(IOUtils.contentEquals(expected, actual)).as(() -> "'fileStyleSheet.xslt' does not match").isTrue();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        result.add(test);
        return result;
    }

    protected List<TestData> outputStyleTest() {
        return styleSheetTest(Arg.OUTPUT_STYLE.find("output-style"));
    }

}
