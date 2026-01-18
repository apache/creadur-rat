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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.rat.ConfigurationException;
import org.apache.rat.OptionCollectionParser;
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

import org.apache.rat.ui.AbstractOptionCollection;
import org.apache.rat.utils.FileUtils;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

import static org.apache.rat.utils.FileUtils.writeFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Generates a list of TestData for options.
 * Use of this interface ensures consistent testing across the UIs. Each method
 * tests an Option from {@link OptionCollectionParser} that must be implemented in the UI.
 */
public class OptionTestDataProvider extends AbstractTestDataProvider {

    static String dump(final Option option, final String fname, final DocumentNameMatcher matcher, final DocumentName name) {
        StringBuilder sb = new StringBuilder();
        matcher.decompose(name).forEach(s -> sb.append(s).append("\n"));
        return String.format("Argument and Name: %s %s%nMatcher decomposition:%n%s", option.getLongOpt(), fname, sb);
    }

    /**
     * Generates the map of UI TestData indexed by testName.
     * @return the map of UI TestData indexed by testName.
     */
    public Map<String, TestData> getUITestMap(final AbstractOptionCollection<?> optionCollection) {
        final ConfigurationException noLicenses = new ConfigurationException("At least one license must be defined");
        Map<String, TestData> result = getOptionTestMap(optionCollection);
        result.get("configuration-no-defaults").setException(noLicenses);
        result.get("licenses-approved/withoutDefaults").setException(noLicenses);
        result.get("licenses-approved-file/withoutDefaults").setException(noLicenses);
        result.get("license-families-approved/withoutDefaults").setException(noLicenses);
        result.get("license-families-approved-file/withoutDefaults").setException(noLicenses);
        return result;
    }


    // exclude tests
    private TestData execExcludeTest(final Option option, final Supplier<String[]> args, Consumer<DocumentName> setupFiles) {
        String[] notExcluded = {"notbaz", "well._afile"};
        String[] excluded = {"some.foo", "B.bar", "justbaz"};
        Consumer<Path> preSetup = basePath -> setupFiles.accept(DocumentName.builder(basePath.toFile()).build());
        return new TestData("",
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
    }

    private TestData execIncludeTest(final Option option, String[] args, Consumer<DocumentName> setupFiles) {
        Option excludeOption = Arg.EXCLUDE.option();
        String[] notExcluded = {"B.bar", "justbaz", "notbaz"};
        String[] excluded = {"some.foo"};
        return new TestData("", Arrays.asList(ImmutablePair.of(option, args),
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

    }

    private void execLicensesApprovedTest(final Set<TestData> result, final Option option, final String[] args, final Consumer<Path> setup) {
        ImmutablePair<Option, String[]> arg1 = ImmutablePair.of(option, args);

        result.add(new TestData("withDefaults", Collections.singletonList(arg1),
                setup,
                validatorData -> {
            SortedSet<String> ids = validatorData.getConfiguration().getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(ids).contains("one", "two");
        }));

        if (!Arg.CONFIGURATION_NO_DEFAULTS.isEmpty()) {
            ImmutablePair<Option, String[]> arg2 = ImmutablePair.of(
                    Arg.CONFIGURATION_NO_DEFAULTS.option(),
                    null);
            result.add(new TestData("withoutDefaults", Arrays.asList(arg1, arg2),
                    setup,
                    validatorData -> {
                        SortedSet<String> ids = validatorData.getConfiguration().getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
                        assertThat(ids).containsExactly("one", "two");
                    }));
        }
    }

    private TestData execLicensesDeniedTest(final Option option, final String[] args, Consumer<Path> setup) {
        return new TestData("ILLUMOS", Collections.singletonList(ImmutablePair.of(option, args)),
        setup,
        validatorData -> {
            assertThat(validatorData.getConfiguration().getLicenseIds(LicenseSetFactory.LicenseFilter.ALL)).contains("ILLUMOS");
            SortedSet<String> result = validatorData.getConfiguration().getLicenseIds(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).doesNotContain("ILLUMOS");
        });
    }

    private void execLicenseFamiliesApprovedTest(final Set<TestData> result, final Option option, final String[] args, Consumer<Path> setup) {
        String catz = ILicenseFamily.makeCategory("catz");

        result.add(new TestData("withDefaults", Collections.singletonList(ImmutablePair.of(option, args)),
                setup,
                validatorData -> {
                    SortedSet<String> categories = validatorData.getConfiguration().getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
                    assertThat(categories).contains(catz);
                }));

        result.add(new TestData("withoutDefaults", Arrays.asList(ImmutablePair.of(option, args), ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"), null)),
                setup,
                validatorData -> {
                    SortedSet<String> categories = validatorData.getConfiguration().getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
                    assertThat(categories).containsExactly(catz);
                }));
    }

    private TestData execLicenseFamiliesDeniedTest(final Option option, final String[] args, Consumer<Path> setup) {
        String gpl = ILicenseFamily.makeCategory("GPL");
       return new TestData("GPL", Collections.singletonList(ImmutablePair.of(option, args)),
setup,
        validatorData -> {
            assertThat(validatorData.getConfiguration().getLicenseCategories(LicenseSetFactory.LicenseFilter.ALL)).contains(gpl);
            SortedSet<String> result = validatorData.getConfiguration().getLicenseCategories(LicenseSetFactory.LicenseFilter.APPROVED);
            assertThat(result).doesNotContain(gpl);
        });
    }

    @Override
    protected void inputExcludeFileTest(final Set<TestData> result, final Option option) {
        Consumer<DocumentName> setup = baseDir -> writeFile(baseDir.asFile(), "exclude.txt", Arrays.asList(EXCLUDE_ARGS));
        Supplier<String[]> args = () -> new String[]{"exclude.txt"};
        result.add(execExcludeTest(option, args, setup));
    }

    @Override
    protected void inputExcludeTest(final Set<TestData> result, final Option option) {
        result.add(execExcludeTest(option, () -> EXCLUDE_ARGS, x -> {}));
    }

    @Override
    protected void inputExcludeStdTest(final Set<TestData> result, final Option option) {
        String[] excluded = {"afile~", ".#afile", "%afile%", "._afile"};
        String[] notExcluded = {"afile~more", "what.#afile", "%afile%withMore", "well._afile"};

        result.add(new TestData(StandardCollection.MISC.name(),
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
                }));
    }

    @Override
    protected void inputExcludeParsedScmTest(final Set<TestData> result, final Option option) {
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

        result.add(new TestData("Git", Collections.singletonList(ImmutablePair.of(option, args)),
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
                }));
    }

    @Override
    protected void inputExcludeSizeTest(final Set<TestData> result, final Option option) {
        String[] notExcluded = {"Hello.txt", "HelloWorld.txt"};
        String[] excluded = {"Hi.txt"};

        result.add(new TestData("5", Collections.singletonList(ImmutablePair.of(option, new String[]{"5"})),
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
                }));
    }

    @Override
    protected void inputIncludeFileTest(final Set<TestData> result, final Option option) {
        Consumer<DocumentName> setup = baseDir -> writeFile(baseDir.asFile(), "include.txt", Arrays.asList(INCLUDE_ARGS));
        result.add(execIncludeTest(option, new String[]{"include.txt"}, setup));
    }

    @Override
    protected void inputIncludeTest(final Set<TestData> result, final Option option) {
        result.add(execIncludeTest(option, INCLUDE_ARGS, x -> {}));
    }

    @Override
    protected void inputIncludeStdTest(final Set<TestData> result, final Option option) {
        if (Arg.EXCLUDE.isEmpty()) {
            throw new RuntimeException(String.format("%s can not be tested without an available valid EXCLUDE option", option.getKey()));
        }
        ImmutablePair<Option, String[]> excludes = ImmutablePair.of(Arg.EXCLUDE.option(),
                new String[]{"*~more", "*~"});
        String[] args = {StandardCollection.MISC.name()};
        String[] excluded = {"afile~more"};
        String[] notExcluded = {"afile~", ".#afile", "%afile%", "._afile", "what.#afile", "%afile%withMore", "well._afile"};

        result.add(new TestData("", Arrays.asList(excludes, ImmutablePair.of(option, args)),
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
                }));
    }

    @Override
    protected void inputSourceTest(final Set<TestData> result, final Option option) {
        result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, new String[]{"inputFile.txt"})),
                dirPath -> {
                    File input = dirPath.resolve("inputFile.txt").toFile();
                    FileUtils.mkDir(input.getParentFile());
                },
                validatorData -> assertThat(validatorData.getConfiguration().hasSource()).isTrue()));
    }

    @Override
    protected void helpLicenses(final Set<TestData> result, final Option option) {
        PrintStream origin = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);

        result.add(new TestData("stdOut",
                Collections.singletonList(ImmutablePair.of(option, null)),
                basePath -> System.setOut(out),
                validatorData -> {
                    System.setOut(origin);
                    String txt = baos.toString();
                    TextUtils.assertContains("====== Licenses ======", txt);
                    TextUtils.assertContains("====== Defined Matchers ======", txt);
                    TextUtils.assertContains("====== Defined Families ======", txt);
                }));
    }

    @Override
    protected void licensesApprovedFileTest(final Set<TestData> result, final Option option) {
        execLicensesApprovedTest(result, option,
                new String[]{"licensesApproved.txt"},
                basePath -> writeFile(basePath.toFile(), "licensesApproved.txt", Arrays.asList("one", "two")));
    }

    @Override
    protected void licensesApprovedTest(final Set<TestData> result, final Option option) {
        execLicensesApprovedTest(result, option, new String[]{"one, two"}, DataUtils.NO_SETUP);
    }

    @Override
    protected void licensesDeniedTest(final Set<TestData> result, final Option option) {
        result.add(execLicensesDeniedTest(option, new String[]{"ILLUMOS"}, DataUtils.NO_SETUP));
    }

    @Override
    protected void licensesDeniedFileTest(final Set<TestData> result, final Option option) {
        result.add(execLicensesDeniedTest(option, new String[]{"licensesDenied.txt"},
                (basePath) -> writeFile(basePath.toFile(), "licensesDenied.txt", Collections.singletonList("ILLUMOS"))));
    }

    @Override
    protected void licenseFamiliesApprovedFileTest(final Set<TestData> result, final Option option) {
        execLicenseFamiliesApprovedTest(result, option, new String[]{"familiesApproved.txt"},
                basePath -> writeFile(basePath.toFile(), "familiesApproved.txt", Collections.singletonList("catz")));
    }

    @Override
    protected void licenseFamiliesApprovedTest(final Set<TestData> result, final Option option) {
        execLicenseFamiliesApprovedTest(result, option, new String[]{"catz"}, DataUtils.NO_SETUP);
    }

    @Override
    protected void licenseFamiliesDeniedFileTest(final Set<TestData> result, final Option option) {
        result.add(execLicenseFamiliesDeniedTest(option, new String[]{"familiesDenied.txt"},
                baseDir -> writeFile(baseDir.toFile(), "familiesDenied.txt", Collections.singletonList("GPL"))));
    }

    @Override
    protected void licenseFamiliesDeniedTest(final Set<TestData> result, final Option option) {
        result.add(execLicenseFamiliesDeniedTest(option, new String[]{"GPL"}, DataUtils.NO_SETUP));
    }

    @Override
    protected void counterMaxTest(final Set<TestData> result, final Option option) {
        result.add(new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0)));


        result.add(new TestData("negativeValue", Collections.singletonList(ImmutablePair.of(option,
                new String[]{"Unapproved:-1", "ignored:1"})),
                DataUtils.NO_SETUP,
                validatorData -> {
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(Integer.MAX_VALUE);
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMax(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
                }));

        result.add(new TestData("standardValue", Collections.singletonList(ImmutablePair.of(option,
                new String[]{"Unapproved:5", "ignored:0"})),
                DataUtils.NO_SETUP,
                validatorData -> {
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMax(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                }));
    }

    @Override
    protected void counterMinTest(final Set<TestData> result, final Option option) {
        result.add(new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0)));

        String[] args = {"Unapproved:1", "ignored:1"};
        result.add(new TestData("capitalized", Collections.singletonList(ImmutablePair.of(option, args)),
                DataUtils.NO_SETUP,
                validatorData -> {
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
                }));

        args = new String[]{"unapproved:5", "ignored:0"};
        result.add(new TestData("lowerCase", Collections.singletonList(ImmutablePair.of(option, args)),
                DataUtils.NO_SETUP,
                validatorData -> {
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                }));

        args = new String[]{"unapproved:-5", "ignored:0"};
        result.add(new TestData("negativeValue", Collections.singletonList(ImmutablePair.of(option, args)),
                DataUtils.NO_SETUP,
                validatorData -> {
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(-5);
                    assertThat(validatorData.getConfiguration().getClaimValidator().getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                }));
    }

    @Override
    protected void configTest(final Set<TestData> result, final Option option) {
        Consumer<Path> setupFiles = basePath -> {
            Path ratDir = basePath.resolve(".rat");
            FileUtils.mkDir(ratDir.toFile());
            Path oneXml = ratDir.resolve("One.xml");
            DataUtils.generateTextConfig(oneXml, "ONE", "one");

            Path twoXml = ratDir.resolve("Two.xml");
            DataUtils.generateTextConfig(twoXml, "TWO", "two");
        };
        String[] args = {".rat/One.xml", ".rat/Two.xml"};

        result.add(new TestData("withDefaults", Collections.singletonList(ImmutablePair.of(option, args)),
                setupFiles,
                validatorData -> {
                    SortedSet<ILicense> set = validatorData.getConfiguration().getLicenses(LicenseSetFactory.LicenseFilter.ALL);
                    assertThat(set).hasSizeGreaterThan(2);
                    assertThat(LicenseSetFactory.search("ONE", "ONE", set)).isPresent();
                    assertThat(LicenseSetFactory.search("TWO", "TWO", set)).isPresent();
                }));

        if (!Arg.CONFIGURATION_NO_DEFAULTS.isEmpty()) {
            result.add(new TestData("withoutDefaults", Arrays.asList(ImmutablePair.of(option, args),
                    ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.option(), null)),
                    setupFiles,
                    validatorData -> {

                        SortedSet<ILicense> set = validatorData.getConfiguration().getLicenses(LicenseSetFactory.LicenseFilter.ALL);
                        assertThat(set).hasSize(2);
                        assertThat(LicenseSetFactory.search("ONE", "ONE", set)).isPresent();
                        assertThat(LicenseSetFactory.search("TWO", "TWO", set)).isPresent();
                    }));
        }
    }

    @Override
    protected void configurationNoDefaultsTest(final Set<TestData> result, final Option option) {
        result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isEmpty()));

        result.add(new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isNotEmpty()));
    }

    @Override
    protected void dryRunTest(final Set<TestData> result, final Option option) {
        result.add(new TestData("stdRun", Collections.singletonList(ImmutablePair.of(option, null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isDryRun()).isTrue()));

        result.add(new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isDryRun()).isFalse()));
    }

    @Override
    protected void editCopyrightTest(final Set<TestData> result, final Option option) {
        if (Arg.EDIT_ADD.isEmpty()) {
            throw new RuntimeException(String.format("%s can not be tested without a valid EDIT_ADD option", option.getKey()));
        }
        result.add(new TestData("noEditLicense", Collections.singletonList(ImmutablePair.of(option, new String[]{"MyCopyright"})),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getCopyrightMessage()).as("Copyright without --edit-license should not work").isNull()));

        result.add(new TestData("MyCopyright", Arrays.asList(ImmutablePair.of(option, new String[]{"MyCopyright"}),
                ImmutablePair.of(Arg.EDIT_ADD.option(), null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().getCopyrightMessage()).isEqualTo("MyCopyright")));
    }

    @Override
    protected void editLicenseTest(final Set<TestData> result, final Option option) {
        result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicenses()).isTrue()));

        result.add(new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicenses()).isFalse()));
    }

    @Override
    protected void editOverwriteTest(final Set<TestData> result, final Option option) {
        result.add(new TestData("noEditLicense", Collections.singletonList(ImmutablePair.of(option, null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicensesForced())
                        .describedAs("Without edit-license should be false").isFalse()));

        if (Arg.EDIT_ADD.isEmpty()) {
            throw new RuntimeException(String.format("%s can not be tested without a valid EDIT_ADD option", option.getKey()));
        }
        result.add(new TestData("", Arrays.asList(ImmutablePair.of(option, null),
                ImmutablePair.of(Arg.EDIT_ADD.option(), null)),
                DataUtils.NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicensesForced()).isTrue()));
    }

    @Override
    protected void logLevelTest(final Set<TestData> result, final Option option) {
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
    }

    @Override
    protected void outputArchiveTest(final Set<TestData> result, final Option option) {
        for (ReportConfiguration.Processing processing : ReportConfiguration.Processing.values()) {
            TestData test = new TestData(processing.name(),
                    Collections.singletonList(ImmutablePair.of(option, new String[]{processing.name()})),
                    DataUtils.NO_SETUP,
                    validatorData -> assertThat(validatorData.getConfiguration().getArchiveProcessing()).isEqualTo(processing));
            result.add(test);
        }
    }

    @Override
    protected void outputFamiliesTest(final Set<TestData> result, final Option option) {
        for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
            TestData test = new TestData(filter.name(),
                    Collections.singletonList(ImmutablePair.of(option, new String[]{filter.name()})),
                    DataUtils.NO_SETUP,
                    validatorData -> assertThat(validatorData.getConfiguration().listFamilies()).isEqualTo(filter));
            result.add(test);
        }
    }

    @Override
    protected void outputFileTest(final Set<TestData> result, final Option option) {
        String[] args = new String[]{"outexample"};
        result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, args)),
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
                }));
    }

    @Override
    protected void outputLicensesTest(final Set<TestData> result, final Option option) {
        for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
            TestData test = new TestData(filter.name(), Collections.singletonList(ImmutablePair.of(option, new String[]{filter.name()})),
                    DataUtils.NO_SETUP,
                    validatorData -> assertThat(validatorData.getConfiguration().listLicenses()).isEqualTo(filter));
            result.add(test);
        }
    }

    @Override
    protected void outputStandardTest(final Set<TestData> result, final Option option) {
        for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
            TestData test = new TestData(proc.name(), Collections.singletonList(ImmutablePair.of(option, new String[]{proc.name()})),
                    DataUtils.NO_SETUP,
                    validatorData -> assertThat(validatorData.getConfiguration().getStandardProcessing()).isEqualTo(proc));
            result.add(test);
        }
    }

    @Override
    protected void outputStyleTest(final Set<TestData> result, final Option option) {
        if (!option.hasArg()) {
            result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, null)),
                    DataUtils.NO_SETUP,
                    validatorData -> {
                        try (InputStream expected = StyleSheets.XML.getStyleSheet().get();
                             InputStream actual = validatorData.getConfiguration().getStyleSheet().get()) {
                            assertThat(IOUtils.contentEquals(expected, actual)).as(() -> String.format("'%s' does not match", StyleSheets.XML)).isTrue();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
        } else {
            for (StyleSheets sheet : StyleSheets.values()) {
                result.add(new TestData(sheet.name(), Collections.singletonList(ImmutablePair.of(option, new String[]{sheet.arg()})),
                        DataUtils.NO_SETUP,
                        validatorData -> {
                            try (InputStream expected = sheet.getStyleSheet().get();
                                 InputStream actual = validatorData.getConfiguration().getStyleSheet().get()) {
                                assertThat(IOUtils.contentEquals(expected, actual)).as(() -> String.format("'%s' does not match", sheet)).isTrue();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }));
            }

            result.add(new TestData("fileStyleSheet", Collections.singletonList(ImmutablePair.of(option, new String[]{"fileStyleSheet.xslt"})),
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
                    }));
        }
    }
}
