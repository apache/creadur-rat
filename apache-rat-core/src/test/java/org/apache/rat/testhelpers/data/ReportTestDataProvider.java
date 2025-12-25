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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.config.results.ClaimValidator;
import org.apache.rat.document.DocumentName;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.writer.XmlWriter;
import org.apache.rat.testhelpers.FileUtils;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.w3c.dom.Document;

import static org.apache.rat.testhelpers.FileUtils.writeFile;
import static org.apache.rat.testhelpers.data.DataUtils.NO_SETUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Generates a list of TestData for executing the Report.
 * Use of this interface ensures consistent testing across the UIs. Each method
 * tests an Option from {@link OptionCollection} that must be implemented in the UI.
 * This differes from the {@link OptionTestDataProvider} in that tests from this set
 * expect that execptions will be thrown during execution, and tests the xml output.
 */
public class ReportTestDataProvider {

    public static final RatException NO_LICENSES_EXCEPTION = new RatException("At least one license must be defined");

    public final List<ImmutablePair<Option, String[]>> NO_OPTIONS = Collections.singletonList(ImmutablePair.nullPair());

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    private final Consumer<Path> mkRat = basePath -> {
        File baseDir = basePath.toFile();
        File ratDir = new File(baseDir, ".rat");
        FileUtils.mkDir(ratDir);
    };

    /**
     * Generates a map of TestData indexed by the testName
     * @param unsupportedTests Options that are not supported by the item under test.
     * @return the map of testName to Test Data.
     * @see #getOptionTests(Collection)
     */
    public Map<String, TestData> getOptionTestMap(Collection<Option> unsupportedTests) {
        Map<String, TestData> map = new HashMap<>();
        for (TestData test : getOptionTests(unsupportedTests)) {
            map.put(test.getTestName(), test);
        }
        return map;
    }

    /**
     * Generates a list of test data for Option testing.
     * This is different from UI testing as this is to test
     * that the command line is properly parsed into a configuration.
     * @param unsupportedOptions a collection of options that are not supported by the item under test.
     * @return a list of TestData for the tests.
     */
    public List<TestData> getOptionTests(final Collection<Option> unsupportedOptions) {
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
        result.removeIf(test -> unsupportedOptions.contains(test.getOption()));
        result.sort(Comparator.comparing(TestData::getTestName));
        return result;
    }

    private void validate(List<TestData> result) {
        Set<Option> options = new HashSet<>(Arg.getOptions(new Options()).getOptions());
        result.stream().forEach(testData -> options.remove(testData.getOption()));
        // TODO fix this once deprecated options are removed
        options.forEach(opt -> DefaultLog.getInstance().warn("Option " + opt.getKey() + " was not tested."));
        //assertThat(options).describedAs("All options are not accounted for.").isEmpty();
    }

    private void assertStandardFile(Document document, String fname) {
        try {
            XmlUtils.assertIsPresent(document, xpath,
                    String.format("/rat-report/resource[@name='/%s'][@type='STANDARD']", fname));
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertIgnoredFile(Document document, String fname) {
        try {
            XmlUtils.assertIsPresent(document, xpath,
                    String.format("/rat-report/resource[@name='/%s'][@type='IGNORED']", fname));
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    // exclude tests
    private List<TestData> execExcludeTest(final Option option, final Supplier<String[]> args, Consumer<Path> setupFiles) {
        Consumer<Path> setup = setupFiles.andThen(mkRat).andThen(basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "notbaz");
            writeFile(baseDir, "well._afile");
            writeFile(baseDir, "some.foo");
            writeFile(baseDir, "B.bar");
            writeFile(baseDir, "justbaz");
        });

        TestData test1 = new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                setup,
                validatorData -> {
                    ClaimStatistic claimStatistic = validatorData.getStatistic();
                    assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(5);
                    assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
                });
        String[] ignored = {"B.bar", "justbaz", "some.foo", ".rat"};
        String[] standard = {"well._afile", "notbaz"};
        TestData test2 = new TestData("", Collections.singletonList(ImmutablePair.of(option, args.get())),
                setup,
                validatorData -> {
                    ClaimStatistic claimStatistic = validatorData.getStatistic();
                    assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(standard.length);
                    assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(ignored.length);
                    for (String fileName : ignored) {
                        assertIgnoredFile(validatorData.getDocument(), fileName);
                    }
                    for (String fileName : standard) {
                        assertStandardFile(validatorData.getDocument(), fileName);
                    }
                });
        return Arrays.asList(test1, test2);
    }

    private List<TestData> excludeFileTest(final Option option) {
        Consumer<Path> setup = mkRat.andThen(baseDir -> {
            File dir = baseDir.resolve(".rat").toFile();
            writeFile(dir, "exclude.txt", Arrays.asList(OptionTestDataProvider.EXCLUDE_ARGS));
        });
        Supplier<String[]> args = () -> new String[]{".rat/exclude.txt"};
        return execExcludeTest(option, args, setup);
    }

    protected List<TestData> inputExcludeFileTest() {
        return excludeFileTest(Arg.EXCLUDE_FILE.find("input-exclude-file"));
    }

    protected List<TestData> inputExcludeTest() {
        return execExcludeTest(Arg.EXCLUDE.find("input-exclude"), () -> OptionTestDataProvider.EXCLUDE_ARGS, x -> {});
    }

    protected List<TestData> inputExcludeStdTest() {
        Option option = Arg.EXCLUDE_STD.find("input-exclude-std");
        String[] args = {StandardCollection.MAVEN.name()};
        String[] defaultExcluded = {"afile~", ".#afile", "%afile%", "._afile"};
        String[] defaultIncluded = {"afile~more", "what.#afile", "%afile%withMore", "well._afile"};
        String mavenFile = "build.log";
        Consumer<Path> setup = basePath -> {
            File baseDir = basePath.toFile();
            for (String fileName : defaultExcluded) {
                writeFile(baseDir, fileName);
            }
            for (String fileName : defaultIncluded) {
                writeFile(baseDir, fileName);
            }
            writeFile(baseDir, mavenFile);
        };
        TestData test1 = new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(defaultIncluded.length + 1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(defaultExcluded.length);
                    for (String fileName : defaultIncluded) {
                        assertStandardFile(validatorData.getDocument(), fileName);
                    }
                    for (String fileName : defaultExcluded) {
                        assertIgnoredFile(validatorData.getDocument(), fileName);
                    }
                    assertStandardFile(validatorData.getDocument(), mavenFile);
                });


        TestData test2 = new TestData("", Collections.singletonList(ImmutablePair.of(option, args)),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(defaultIncluded.length);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(defaultExcluded.length + 1);
                    for (String fileName : defaultIncluded) {
                        assertStandardFile(validatorData.getDocument(), fileName);
                    }
                    for (String fileName : defaultExcluded) {
                        assertIgnoredFile(validatorData.getDocument(), fileName);
                    }
                    assertIgnoredFile(validatorData.getDocument(), mavenFile);
                });
        return Arrays.asList(test1, test2);
    }

    protected List<TestData> inputExcludeParsedScmTest() {
        Option option = Arg.EXCLUDE_PARSE_SCM.find("input-exclude-parsed-scm");



        Consumer<Path> setup = basePath -> {
            File baseDir = basePath.toFile();
            String[] lines = {
                    "# somethings",
                    "!thingone", "thing*", System.lineSeparator(),
                    "# some fish",
                    "**/fish", "*_fish",
                    "# some colorful directories",
                    "red/", "blue/*/"};
            writeFile(baseDir, ".gitignore", Arrays.asList(lines));
            writeFile(baseDir, "thingone");
            writeFile(baseDir, "thingtwo");
            File dir = new File(baseDir, "dir");
            FileUtils.mkDir(dir);
            FileUtils.writeFile(dir, "fish_two");
            FileUtils.writeFile(dir, "fish");

            dir = new File(baseDir, "red");
            FileUtils.mkDir(dir);
            FileUtils.writeFile(dir, "fish");

            dir = new File(baseDir, "blue/fish");
            FileUtils.mkDir(dir);
            FileUtils.writeFile(dir, "dory");

            dir = new File(baseDir, "some");
            FileUtils.mkDir(dir);
            FileUtils.writeFile(dir, "fish");
            FileUtils.writeFile(dir, "things");
            FileUtils.writeFile(dir, "thingone");

            dir = new File(baseDir, "another");
            FileUtils.mkDir(dir);
            FileUtils.writeFile(dir, "red_fish");
        };

        TestData test1 = new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
            setup,
            validatorData -> {
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(11);
                // .gitignore is ignored by default as it is hidden
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
            });

    TestData test2 = new TestData("GIT", Collections.singletonList(ImmutablePair.of(option, new String[] {"GIT"})),
            setup,
            validatorData -> {
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
                // .gitignore is ignored by default as it is hidden
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(8);
            });

    return Arrays.asList(test1, test2);

}

    private List<TestData> inputExcludeSizeTest() {
        Option option = Arg.EXCLUDE_SIZE.option();
        String[] notExcluded = {"Hello.txt", "HelloWorld.txt"};
        String[] excluded = {"Hi.txt"};

        Consumer<Path> setup = basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "Hi.txt", Collections.singletonList("Hi"));
            writeFile(baseDir, "Hello.txt", Collections.singletonList("Hello"));
            writeFile(baseDir, "HelloWorld.txt", Collections.singletonList("HelloWorld"));
        };

        TestData test1 = new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.of(null, null)),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                    for (String fname : excluded) {
                        assertStandardFile(validatorData.getDocument(), fname);
                    }
                    for (String fname : notExcluded) {
                        assertStandardFile(validatorData.getDocument(), fname);
                    }
                });

        TestData test2 = new TestData("", Collections.singletonList(ImmutablePair.of(option, new String[]{"5"})),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
                    for (String fname : excluded) {
                        assertIgnoredFile(validatorData.getDocument(), fname);
                    }
                    for (String fname : notExcluded) {
                        assertStandardFile(validatorData.getDocument(), fname);
                    }
                });
        return Arrays.asList(test1, test2);
    }

    // include tests
    private List<TestData> execIncludeTest(final Option option, String[] args, Consumer<Path> setupFiles) {
        Option excludeOption = Arg.EXCLUDE.option();
        String[] notExcluded = {"B.bar", "justbaz", "notbaz"};
        String[] excluded = {"some.foo"};
        Consumer<Path> setup = setupFiles.andThen(basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "notbaz");
            writeFile(baseDir, "some.foo");
            writeFile(baseDir, "B.bar");
            writeFile(baseDir, "justbaz");
        });

        // standard without options.
        TestData test1 = new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(4);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
                });

        // verify exclude removes the files
        TestData test2 = new TestData("includeTestProof", Collections.singletonList(ImmutablePair.of(excludeOption, OptionTestDataProvider.EXCLUDE_ARGS)),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                    // .gitignore is ignored by default as it is hidden but not counted
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(4);
                });

        TestData test3 = new TestData("", Arrays.asList(ImmutablePair.of(option, args), ImmutablePair.of(excludeOption, OptionTestDataProvider.EXCLUDE_ARGS)),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
                    // .gitignore is ignored by default as it is hidden but not counted
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(2);
                });
        return Arrays.asList(test1, test2, test3);
    }

    private List<TestData> includeFileTest(final Option option) {
        Consumer<Path> setup = mkRat.andThen(basePath -> {
            File dir = basePath.resolve(".rat").toFile();
            writeFile(dir, "include.txt", Arrays.asList(OptionTestDataProvider.INCLUDE_ARGS));
        });
        return execIncludeTest(option, new String[]{".rat/include.txt"}, setup);
    }

    protected List<TestData> inputIncludeFileTest() {
        return includeFileTest(Arg.INCLUDE_FILE.find("input-include-file"));
    }


    protected List<TestData> inputIncludeTest() {
        return execIncludeTest(Arg.INCLUDE.find("input-include"), OptionTestDataProvider.INCLUDE_ARGS, mkRat);
    }

    protected List<TestData> inputIncludeStdTest() {
        Option option = Arg.INCLUDE_STD.find("input-include-std");
        Consumer<Path> setup = basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "afile~more");
            writeFile(baseDir, "afile~");
            writeFile(baseDir, ".#afile");
            writeFile(baseDir, "%afile%");
            writeFile(baseDir, "._afile");
            writeFile(baseDir, "what.#afile");
            writeFile(baseDir, "%afile%withMore");
            writeFile(baseDir, "well._afile");
            writeFile(baseDir, ".hiddenFile", "The hidden file");
            File hiddenDir = new File(baseDir, ".hiddenDir");
            FileUtils.mkDir(hiddenDir);
            writeFile(hiddenDir, "aFile", "File in hidden directory");
        };
        ImmutablePair<Option, String[]> excludes = ImmutablePair.of(Arg.EXCLUDE.find("input-exclude"),
                new String[]{"*~more", "*~"});

        TestData test1 = new TestData("/includeStdValidation", Collections.singletonList(excludes),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(4);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(6);
                });

        TestData test2 = new TestData(StandardCollection.MISC.name().toLowerCase(Locale.ROOT), Arrays.asList(ImmutablePair.of(option, new String[]{StandardCollection.MISC.name()}), excludes),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(8);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(2);
                });

        TestData test3 = new TestData(StandardCollection.HIDDEN_FILE.name().toLowerCase(Locale.ROOT), Arrays.asList(ImmutablePair.of(option, new String[]{StandardCollection.HIDDEN_FILE.name()}), excludes),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS))
                            .isEqualTo(6);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED))
                            .isEqualTo(4);
                    assertStandardFile(validatorData.getDocument(), "._afile");
                    assertIgnoredFile(validatorData.getDocument(), ".hiddenDir");
                });
        TestData test4 = new TestData(StandardCollection.HIDDEN_DIR.name().toLowerCase(Locale.ROOT),
                Arrays.asList(ImmutablePair.of(option, new String[]{StandardCollection.HIDDEN_DIR.name()}), excludes),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(5);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(5);
                    assertIgnoredFile(validatorData.getDocument(), "._afile");
                    assertStandardFile(validatorData.getDocument(), ".hiddenDir/aFile");
                });
        return Arrays.asList(test1, test2, test3, test4);
    }

    protected List<TestData> inputSourceTest() {
        Option option = Arg.SOURCE.find("input-source");
        Consumer<Path> setup = basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "codefile");
            writeFile(baseDir, "intput.txt", "codefile");
            writeFile(baseDir, "notcodeFile");
        };

        TestData test1 = new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                });

        TestData test2 = new TestData("", Collections.singletonList(ImmutablePair.of(option, new String[]{"intput.txt"})),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                });
        return Arrays.asList(test1, test2);
    }

    // LICENSE tests
    protected List<TestData> execLicensesApprovedTest(final Option option, String[] args, Consumer<Path> extraSetup) {
        Consumer<Path> setup = extraSetup.andThen(
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "catz.txt", "SPDX-License-Identifier: catz");
                    writeFile(baseDir, "apl.txt", "SPDX-License-Identifier: Apache-2.0");
                }
        );

        TestData test1 = new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
                });

        TestData test2 = new TestData("withoutLicenseDef", Collections.singletonList(ImmutablePair.of(option, args)),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
                });

        Option configOpt = Arg.CONFIGURATION.option();
        TestData test3 = new TestData("withLicenseDef", Arrays.asList(ImmutablePair.of(option, args),
                ImmutablePair.of(configOpt, new String[]{".rat/catz.xml"})),
        setup.andThen(mkRat).andThen(
                basePath -> {
                    Path ratDir = basePath.resolve(".rat");
                    DataUtils.generateSpdxConfig(basePath.resolve(".rat").resolve("catz.xml"), "catz", "catz");
                }),
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(2);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);
                });
        return Arrays.asList(test1, test2, test3);
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
                new String[]{".rat/licensesApproved.txt"},
                mkRat.andThen(
                        basePath -> writeFile(basePath.resolve(".rat").toFile(), "licensesApproved.txt", Arrays.asList("catz"))));
    }

    protected List<TestData> licensesApprovedTest() {
        return execLicensesApprovedTest(Arg.LICENSES_APPROVED.find("licenses-approved"),
                new String[]{"catz"}, NO_SETUP);
    }

    private List<TestData> execLicensesDeniedTest(final Option option, final String[] args, Consumer<Path> setupFiles) {
        Consumer<Path> setup = setupFiles.andThen(basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "illumousFile.java", "The contents of this file are " +
                    "subject to the terms of the Common Development and Distribution License (the \"License\") You " +
                    "may not use this file except in compliance with the License.");
        });
        TestData test1 = new TestData("ILLUMOS", Collections.singletonList(ImmutablePair.of(option, args)),
                setup,
                validatorData -> {
                
                ClaimStatistic claimStatistic = validatorData.getStatistic();
                ClaimValidator validator = validatorData.getConfiguration().getClaimValidator();
                assertThat(validator.listIssues(claimStatistic)).containsExactly("UNAPPROVED");
            });
        return Collections.singletonList(test1);
    }

    protected List<TestData> licensesDeniedTest() {
        return execLicensesDeniedTest(Arg.LICENSES_DENIED.find("licenses-denied"), new String[]{"ILLUMOS"}, NO_SETUP);
    }

    protected List<TestData> licensesDeniedFileTest() {
        return execLicensesDeniedTest(Arg.LICENSES_DENIED_FILE.find("licenses-denied-file"),
                new String[]{"licensesDenied.txt"},
                (basePath) -> writeFile(basePath.toFile(), "licensesDenied.txt", Collections.singletonList("ILLUMOS")));
    }

    private List<TestData> execLicenseFamiliesApprovedTest(final Option option, final String[] args, Consumer<Path> extraSetup) {
        Consumer<Path> setup = extraSetup.andThen(
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "catz.txt", "SPDX-License-Identifier: catz");
                });

        TestData test1 = new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(0);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
                });

        TestData test2 = new TestData("withoutLicenseDef", Collections.singletonList(ImmutablePair.of(option, args)),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(0);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
                });
        Option configOpt = Arg.CONFIGURATION.option();
        TestData test3 = new TestData("withLicenseDef", Arrays.asList(ImmutablePair.of(option, args),
                ImmutablePair.of(configOpt, new String[]{".rat/catz.xml"})),
                setup.andThen(mkRat).andThen(
                        basePath -> {
                            Path ratDir = basePath.resolve(".rat");
                            Path catzXml = ratDir.resolve("catz.xml");
                            DataUtils.generateSpdxConfig(catzXml, "catz", "catz");
                        }),
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);
                });
        return Arrays.asList(test1, test2, test3);
    }

    protected List<TestData> licenseFamiliesApprovedFileTest() {
        return execLicenseFamiliesApprovedTest(Arg.FAMILIES_APPROVED_FILE.find("license-families-approved-file"),
                new String[]{".rat/familiesApproved.txt"},
                mkRat.andThen( basePath -> {
                    writeFile(basePath.resolve(".rat").toFile(), "familiesApproved.txt", Collections.singletonList("catz"));
                }));
    }

    protected List<TestData> licenseFamiliesApprovedTest() {
        return execLicenseFamiliesApprovedTest(Arg.FAMILIES_APPROVED.find("license-families-approved"),
                new String[]{"catz"}, NO_SETUP);
    }

    private List<TestData> execLicenseFamiliesDeniedTest(final Option option, final String[] args, Consumer<Path> extraSetup) {

        Consumer<Path> setup = extraSetup.andThen(
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "bsd.txt", "SPDX-License-Identifier: BSD-3-Clause");
                });

        TestData test1 = new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);
                });

        TestData test2 = new TestData("", Collections.singletonList(ImmutablePair.of(option, args)),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(0);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
                });

        return Arrays.asList(test1, test2);
    }

    protected List<TestData> licenseFamiliesDeniedFileTest() {
        return execLicenseFamiliesDeniedTest(Arg.FAMILIES_DENIED_FILE.find("license-families-denied-file"),
                new String[]{".rat/familiesDenied.txt"},
                mkRat.andThen(
        baseDir -> writeFile(baseDir.resolve(".rat").toFile(), "familiesDenied.txt", Collections.singletonList("BSD-3"))));
    }

    protected List<TestData> licenseFamiliesDeniedTest() {
        return execLicenseFamiliesDeniedTest(Arg.FAMILIES_DENIED.find("license-families-denied"),
                new String[]{"BSD-3"}, NO_SETUP);
    }

    protected List<TestData> counterMaxTest() {
        Option option = Arg.COUNTER_MAX.option();

        TestData test1 = new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.of(null, null)),
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Unapproved\n",
                            "*/\n\n", "class Test {}\n"));
                },
                validatorData -> {
                    ClaimStatistic claimStatistic = validatorData.getStatistic();
                    ClaimValidator validator = validatorData.getConfiguration().getClaimValidator();
                    assertThat(validator.listIssues(claimStatistic)).containsExactly("UNAPPROVED");
                });


        TestData test2 = new TestData("Unapproved1", Collections.singletonList(ImmutablePair.of(option, new String[]{"Unapproved:1"})),
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Unapproved\n",
                            "*/\n\n", "class Test {}\n"));
                },
                validatorData -> {
                    ClaimStatistic claimStatistic = validatorData.getStatistic();
                    ClaimValidator validator = validatorData.getConfiguration().getClaimValidator();
                    assertThat(validator.listIssues(claimStatistic)).isEmpty();
                });
        return Arrays.asList(test1, test2);
    }

    protected List<TestData> counterMinTest() {
        Option option = Arg.COUNTER_MIN.option();

        TestData test1 = new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Unapproved\n",
                            "*/\n\n", "class Test {}\n"));
                },
                validatorData -> {
                    ClaimStatistic claimStatistic = validatorData.getStatistic();
                    ClaimValidator validator = validatorData.getConfiguration().getClaimValidator();
                    assertThat(validator.listIssues(claimStatistic)).containsExactly("UNAPPROVED");
                });

        TestData test2 = new TestData("", Collections.singletonList(ImmutablePair.of(option, new String[]{"Unapproved:1"})),
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Unapproved\n",
                            "*/\n\n", "class Test {}\n"));
                },
                validatorData -> {
                    ClaimStatistic claimStatistic = validatorData.getStatistic();
                    ClaimValidator validator = validatorData.getConfiguration().getClaimValidator();
                    assertThat(validator.listIssues(claimStatistic)).isEmpty();
                });
        return Arrays.asList(test1, test2);
    }

    private List<TestData> configTest(final Option option) {
        Consumer<Path> setup = mkRat.andThen( basePath -> {
            Path ratDir = basePath.resolve(".rat");
            Path oneXml = ratDir.resolve("One.xml");
            DataUtils.generateTextConfig(oneXml, "ONE", "one");

            Path twoXml = ratDir.resolve("Two.xml");
            DataUtils.generateTextConfig(twoXml, "TWO", "two");

            File baseDir = basePath.toFile();
            writeFile(baseDir, "bsd.txt", "SPDX-License-Identifier: BSD-3-Clause");
            writeFile(baseDir, "one.txt", "one is the lonelest number");
        });
        String[] args = {".rat/One.xml", ".rat/Two.xml"};

        ImmutablePair<Option, String[]> underTest = ImmutablePair.of(option, args);

        TestData test1 = new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
                });

        TestData test2 = new TestData("withDefaults", Collections.singletonList(underTest),
            setup,
            validatorData -> {
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(2);
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);
            });

        TestData test3 = new TestData("noDefaults", Arrays.asList(underTest,
                ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"), null)),
            setup,
            validatorData -> {
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
            });
        return Arrays.asList(test1, test2, test3);
    }

    protected List<TestData> configTest() {
        return configTest(Arg.CONFIGURATION.find("config"));
    }

    private List<TestData> noDefaultsTest(final Option option) {
        TestData test1 = new TestData("", Collections.singletonList(ImmutablePair.of(option, null)),
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Apache-2.0\n",
                            "*/\n\n", "class Test {}\n"));
                },
                validatorData -> assertThat(validatorData.getConfiguration().getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isEmpty());
        test1.setException(NO_LICENSES_EXCEPTION);
        return Collections.singletonList(test1);
    }

    protected List<TestData> configurationNoDefaultsTest() {
        return noDefaultsTest(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"));
    }

    protected List<TestData> dryRunTest() {
        Option option = Arg.DRY_RUN.find("dry-run");
        TestData test1 = new TestData("stdRun", Collections.singletonList(ImmutablePair.of(option, null)),
                NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isDryRun()).isTrue());

        TestData test2 = new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isDryRun()).isFalse());
        return Arrays.asList(test1, test2);
    }

    private List<TestData> editCopyrightTest(final Option option) {
        Consumer<Path> setup = basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "Missing.java", Arrays.asList("/* no license */\n\n", "class Test {}\n"));
        };
        ImmutablePair<Option, String[]> copyright = ImmutablePair.of(option, new String[]{"MyCopyright"});
        ImmutablePair<Option, String[]> editLicense = ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null);

        TestData test1 = new TestData("noEditLicense", Collections.singletonList(copyright),
                setup,
                validatorData -> {
                    try {
                        String actualText = TextUtils.readFile(validatorData.getBaseDir().resolve("Missing.java").toFile());
                        TextUtils.assertNotContains("MyCopyright", actualText);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

        TestData test2 = new TestData(DataUtils.asDirName(editLicense.getLeft()), Arrays.asList(copyright, editLicense),
                setup,
                validatorData -> {
                    try {
                        String actualText = TextUtils.readFile(validatorData.getBaseDir().resolve("Missing.java").toFile());
                        TextUtils.assertNotContains("MyCopyright", actualText);
                        assertThat(validatorData.getBaseDir().resolve("Missing.java.new")).exists();
                        actualText = TextUtils.readFile(validatorData.getBaseDir().resolve("Missing.java.new").toFile());
                        TextUtils.assertContains("MyCopyright", actualText);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
        Option dryRun = Arg.DRY_RUN.option();
        TestData test3 = new TestData(DataUtils.asDirName(dryRun),
                Arrays.asList(copyright, ImmutablePair.of(dryRun, null), editLicense),
                setup,
                validatorData -> {
                    try {
                        String actualText = TextUtils.readFile(validatorData.getBaseDir().resolve("Missing.java").toFile());
                        TextUtils.assertNotContains("MyCopyright", actualText);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    assertThat(validatorData.getBaseDir().resolve("Missing.java.new")).doesNotExist();
                });
        Option overwrite = Arg.EDIT_OVERWRITE.option();
        TestData test4 = new TestData(DataUtils.asDirName(overwrite), Arrays.asList(copyright, editLicense, ImmutablePair.of(overwrite, null)),
                setup,
                validatorData -> {
                    try {
                        String actualText = TextUtils.readFile(validatorData.getBaseDir().resolve("Missing.java").toFile());
                        TextUtils.assertContains("MyCopyright", actualText);
                        assertThat(validatorData.getBaseDir().resolve("Missing.java.new")).doesNotExist();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
        return Arrays.asList(test1, test2, test3, test4);
    }

    protected List<TestData> editCopyrightTest() {
        return editCopyrightTest(Arg.EDIT_COPYRIGHT.find("edit-copyright"));
    }

    private List<TestData> editLicenseTest(final Option option) {
        TestData test1 = new TestData("", Collections.singletonList(ImmutablePair.of(option, null)),
            basePath -> {
                File baseDir = basePath.toFile();
                writeFile(baseDir, "NoLicense.java", "class NoLicense {}");
            },
                validatorData -> {
            try {
                assertThat(validatorData.getStatistic()).isNotNull();
                File javaFile = validatorData.getBaseDir().resolve("NoLicense.java").toFile();
                String contents = String.join("\n", IOUtils.readLines(new FileReader(javaFile)));
                assertThat(contents).isEqualTo("class NoLicense {}");
                File resultFile = validatorData.getBaseDir().resolve("NoLicense.java.new").toFile();
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
                });
        return Collections.singletonList(test1);
    }

    protected List<TestData> editLicenseTest() {
        return editLicenseTest(Arg.EDIT_ADD.find("edit-license"));
    }

    private List<TestData>  overwriteTest(final Option option) {
        TestData test1 = new TestData("noEditLicense", Collections.singletonList(ImmutablePair.of(option, null)),
                NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicensesForced())
                        .describedAs("Without edit-license should be false").isFalse());

        TestData test = new TestData("", Arrays.asList(ImmutablePair.of(option, null),
                ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null)),
                NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicensesForced()).isTrue());
        return Arrays.asList(test1, test);
    }

    protected List<TestData> editOverwriteTest() {
        return overwriteTest(Arg.EDIT_OVERWRITE.find("edit-overwrite"));
    }

    protected List<TestData> logLevelTest() {
        final Option option = Arg.LOG_LEVEL.find("log-level");
        final TestingLog testingLog = new TestingLog();

        Consumer<Path> setup = basePath -> {
            DefaultLog.setInstance(testingLog);
            testingLog.clear();
        };

        TestData test1 = new TestData(Log.Level.INFO.name(),
                Collections.singletonList(ImmutablePair.of(option, new String[]{Log.Level.INFO.name()})),
                setup,
                validatorData -> {
                    try {
                        testingLog.assertNotContains("DEBUG");
                    } finally {
                        DefaultLog.setInstance(null);
                    }
                });


        TestData test2 = new TestData(Log.Level.DEBUG.name(),
                Collections.singletonList(ImmutablePair.of(option, new String[]{Log.Level.DEBUG.name()})),
                setup,
                validatorData -> {
                    try {
                        testingLog.assertContains("DEBUG");
                    } finally {
                        DefaultLog.setInstance(null);
                    }
                });

        return Arrays.asList(test1, test2);
    }

    private List<TestData> archiveTest(final Option option) {
        List<TestData> result = new ArrayList<>();
        for (ReportConfiguration.Processing processing : ReportConfiguration.Processing.values()) {
            TestData test = new TestData(processing.name().toLowerCase(Locale.ROOT),
                    Collections.singletonList(ImmutablePair.of(option, new String[]{processing.name()})),
                    basePath -> {
                        File localArchive = new File(basePath.toFile(), "dummy.jar");
                        try (InputStream in = ReportTestDataProvider.class.getResourceAsStream("/tikaFiles/archive/dummy.jar");
                             OutputStream out = Files.newOutputStream(localArchive.toPath())) {
                            IOUtils.copy(in, out);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    validatorData -> {
                        Document document = validatorData.getDocument();
                        try {
                            XmlUtils.assertIsPresent(processing.name(), document, xpath, "/rat-report/resource[@name='/dummy.jar']");
                            switch (processing) {
                                case ABSENCE:
                                    XmlUtils.assertIsPresent(processing.name(), document, xpath, "/rat-report/resource[@name='/dummy.jar']/license[@family='AL   ']");
                                    XmlUtils.assertIsPresent(processing.name(), document, xpath, "/rat-report/resource[@name='/dummy.jar']/license[@family='?????']");
                                    break;
                                case PRESENCE:
                                    XmlUtils.assertIsPresent(processing.name(), document, xpath, "/rat-report/resource[@name='/dummy.jar']/license[@family='AL   ']");
                                    XmlUtils.assertIsNotPresent(processing.name(), document, xpath, "/rat-report/resource[@name='/dummy.jar']/license[@family='?????']");
                                    break;
                                case NOTIFICATION:
                                    XmlUtils.assertIsNotPresent(processing.name(), document, xpath, "/rat-report/resource[@name='/dummy.jar']/license[@family='AL   ']");
                                    XmlUtils.assertIsNotPresent(processing.name(), document, xpath, "/rat-report/resource[@name='/dummy.jar']/license[@family='?????']");
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unexpected processing " + processing);
                            }
                        } catch (XPathExpressionException e) {
                            throw new RuntimeException(e);
                        }
                    });
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
            TestData test = new TestData(filter.name().toLowerCase(Locale.ROOT),
                    Collections.singletonList(ImmutablePair.of(option, new String[]{filter.name()})),
                    NO_SETUP,
                    validatorData -> {
                        Document document = validatorData.getDocument();
                        try {
                            switch (filter) {
                                case ALL:
                                    XmlUtils.assertIsPresent(filter.name(), document, xpath, "/rat-report/rat-config/families/family[@id='AL']");
                                    XmlUtils.assertIsPresent(filter.name(), document, xpath, "/rat-report/rat-config/families/family[@id='GPL']");
                                    break;
                                case APPROVED:
                                    XmlUtils.assertIsPresent(filter.name(), document, xpath, "/rat-report/rat-config/families/family[@id='AL']");
                                    XmlUtils.assertIsNotPresent(filter.name(), document, xpath, "/rat-report/rat-config/families/family[@id='GPL']");
                                    break;
                                case NONE:
                                    XmlUtils.assertIsNotPresent(filter.name(), document, xpath, "/rat-report/rat-config/families/family[@id='AL']");
                                    XmlUtils.assertIsNotPresent(filter.name(), document, xpath, "/rat-report/rat-config/families/family[@id='GPL']");
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unexpected filter: " + filter);
                            }
                        } catch (XPathExpressionException e) {
                            throw new RuntimeException(e);
                        }
                    });
            result.add(test);
        }
        return result;
    }


    protected List<TestData> outputFamiliesTest() {
        return listFamilies(Arg.OUTPUT_FAMILIES.find("output-families"));
    }

    private List<TestData> outTest(final Option option) {
        TestData test1 = new TestData("", Collections.singletonList(ImmutablePair.of(option, new String[]{"outexample"})),
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "apl.txt", "SPDX-License-Identifier: Apache-2.0");
                },
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);

                    File outFile = validatorData.getBaseDir().resolve("outexample").toFile();
                    try {
                        String actualText = TextUtils.readFile(outFile);
                        TextUtils.assertContainsExactly(1, "Apache License 2.0: 1 ", actualText);
                        TextUtils.assertContainsExactly(1, "STANDARD: 1 ", actualText);
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
                    NO_SETUP,
                    validatorData -> {
                        Document document = validatorData.getDocument();
                        try {
                            switch (filter) {
                                case ALL:
                                    XmlUtils.assertIsPresent(filter.name(), document, xpath, "/rat-report/rat-config/licenses/license[@id='AL2.0']");
                                    XmlUtils.assertIsPresent(filter.name(), document, xpath, "/rat-report/rat-config/licenses/license[@id='GPL1']");
                                    break;
                                case APPROVED:
                                    XmlUtils.assertIsPresent(filter.name(), document, xpath, "/rat-report/rat-config/licenses/license[@id='AL2.0']");
                                    XmlUtils.assertIsNotPresent(filter.name(), document, xpath, "/rat-report/rat-config/licenses/license[@id='GPL1']");
                                    break;
                                case NONE:
                                    XmlUtils.assertIsNotPresent(filter.name(), document, xpath, "/rat-report/rat-config/licenses/license[@id='AL2.0']");
                                    XmlUtils.assertIsNotPresent(filter.name(), document, xpath, "/rat-report/rat-config/licenses/license[@id='GPL1']");
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unexpected filter: " + filter);
                            }
                        } catch (XPathExpressionException e) {
                            throw new RuntimeException(e);
                        }
                    });
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
            TestData test = new TestData(proc.name().toLowerCase(Locale.ROOT), Collections.singletonList(ImmutablePair.of(option, new String[]{proc.name()})),
                    basePath -> {
                        File baseDir = basePath.toFile();
                        writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Apache-2.0\n",
                                "*/\n\n", "class Test {}\n"));
                        writeFile(baseDir, "Missing.java", Arrays.asList("/* no license */\n\n", "class Test {}\n"));
                    },
                    validatorData -> {
                        Document document = validatorData.getDocument();
                        String testDoc = "/rat-report/resource[@name='/Test.java']";
                        String missingDoc = "/rat-report/resource[@name='/Missing.java']";
                        try {
                            XmlUtils.assertIsPresent(proc.name(), document, xpath, testDoc);
                            XmlUtils.assertIsPresent(proc.name(), document, xpath, missingDoc);

                            switch (proc) {
                                case ABSENCE:
                                    XmlUtils.assertIsPresent(proc.name(), document, xpath, testDoc + "/license[@family='AL   ']");
                                    XmlUtils.assertIsPresent(proc.name(), document, xpath, missingDoc + "/license[@family='?????']");
                                    break;
                                case PRESENCE:
                                    XmlUtils.assertIsPresent(proc.name(), document, xpath, testDoc + "/license[@family='AL   ']");
                                    XmlUtils.assertIsNotPresent(proc.name(), document, xpath, missingDoc + "/license[@family='?????']");
                                    break;
                                case NOTIFICATION:
                                    XmlUtils.assertIsNotPresent(proc.name(), document, xpath, testDoc + "/license[@family='AL   ']");
                                    XmlUtils.assertIsNotPresent(proc.name(), document, xpath, missingDoc + "/license[@family='?????']");
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unexpected processing " + proc);
                            }
                        } catch (XPathExpressionException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
            result.add(test);
        }
        return result;
    }

    protected List<TestData> outputStandardTest() {
        return standardTest(Arg.OUTPUT_STANDARD.find("output-standard"));
    }

    private List<TestData> styleSheetTest(final Option option) {
        List<TestData> result = new ArrayList<>();
        ImmutablePair<Option, String[]> outOption = ImmutablePair.of(Arg.OUTPUT_FILE.option(), new String[]{"outputFile"});

        Consumer<Path> createFile = basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Apache-2.0\n",
                    "*/\n\n", "class Test {}\n"));
            writeFile(baseDir, "Missing.java", Arrays.asList("/* no license */\n\n", "class Test {}\n"));
        };

        for (StyleSheets sheet : StyleSheets.values()) {
            TestData test = new TestData(sheet.name().toLowerCase(Locale.ROOT), Arrays.asList(ImmutablePair.of(option, new String[]{sheet.arg()}),
                    outOption),
                    createFile,
                    validatorData -> {
                        try (InputStream expected = sheet.getStyleSheet().get();
                             InputStream actual = validatorData.getConfiguration().getStyleSheet().get()) {
                            assertThat(IOUtils.contentEquals(expected, actual)).as(() -> String.format("'%s' does not match", sheet)).isTrue();

                            String actualText = TextUtils.readFile(validatorData.getBaseDir().resolve("outputFile").toFile());
                            switch (sheet) {
                                case MISSING_HEADERS:
                                    TextUtils.assertContainsExactly(1, "Files with missing headers:" + System.lineSeparator() +
                                            "  /Missing.java", actualText);
                                    break;
                                case PLAIN:
                                    TextUtils.assertContainsExactly(1, "Unknown license: 1 ", actualText);
                                    TextUtils.assertContainsExactly(1, "?????: 1 ", actualText);
                                    break;
                                case XML:
                                    TextUtils.assertContainsExactly(1, "<resource encoding=\"ISO-8859-1\" mediaType=\"text/x-java-source\" name=\"/Test.java\" type=\"STANDARD\">", actualText);
                                    TextUtils.assertContainsExactly(1, "<resource encoding=\"ISO-8859-2\" mediaType=\"text/x-java-source\" name=\"/Missing.java\" type=\"STANDARD\">", actualText);
                                    break;
                                case UNAPPROVED_LICENSES:
                                    TextUtils.assertContainsExactly(1, "Files with unapproved licenses:" + System.lineSeparator() + "  /Missing.java", actualText);
                                    break;
                                default:
                                    fail("No test for stylesheet " + sheet);
                                    break;
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            result.add(test);
        }

        TestData test = new TestData("fileStyleSheet",
                Arrays.asList(ImmutablePair.of(option, new String[]{"fileStyleSheet.xslt"}), outOption),
                basePath -> {
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
                                .content("Hello World")
                                .closeDocument();
                    } catch (IOException e) {
                        fail(e.getMessage(), e);
                    }
                },

                validatorData -> {
                    try (InputStream expected = StyleSheets.getStyleSheet("fileStyleSheet.xslt", validatorData.getBaseName()).get();
                         InputStream actual = validatorData.getConfiguration().getStyleSheet().get()) {
                        assertThat(IOUtils.contentEquals(expected, actual)).as(() -> "'fileStyleSheet.xslt' does not match").isTrue();

                        String actualText = TextUtils.readFile(validatorData.getBaseDir().resolve("outputFile").toFile());
                        TextUtils.assertContainsExactly(1, "Hello World", actualText);
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
