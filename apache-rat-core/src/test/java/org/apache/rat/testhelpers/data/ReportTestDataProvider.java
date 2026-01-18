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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.rat.OptionCollectionParser;
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
import org.apache.rat.utils.FileUtils;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.w3c.dom.Document;

import static org.apache.rat.utils.FileUtils.writeFile;
import static org.apache.rat.testhelpers.data.DataUtils.NO_SETUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Generates a list of TestData for executing the Report.
 * Use of this interface ensures consistent testing across the UIs. Each method
 * tests an Option from {@link OptionCollectionParser} that must be implemented in the UI.
 * This differes from the {@link OptionTestDataProvider} in that tests from this set
 * expect that execptions will be thrown during execution, and tests the xml output.
 */
public class ReportTestDataProvider extends AbstractTestDataProvider {

    public static final RatException NO_LICENSES_EXCEPTION = new RatException("At least one license must be defined");

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    private final Consumer<Path> mkRat = basePath -> {
        File baseDir = basePath.toFile();
        File ratDir = new File(baseDir, ".rat");
        FileUtils.mkDir(ratDir);
    };

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

    protected void inputExcludeFileTest(final Set<TestData> result, final Option option) {
        Consumer<Path> setup = mkRat.andThen(baseDir -> {
            File dir = baseDir.resolve(".rat").toFile();
            writeFile(dir, "exclude.txt", Arrays.asList(AbstractTestDataProvider.EXCLUDE_ARGS));
        });
        Supplier<String[]> args = () -> new String[]{".rat/exclude.txt"};
        result.addAll(execExcludeTest(option, args, setup));
    }


    protected void inputExcludeTest(final Set<TestData> result, final Option option) {
        result.addAll(execExcludeTest(option, () -> AbstractTestDataProvider.EXCLUDE_ARGS, x -> {}));
    }

    protected void inputExcludeStdTest(final Set<TestData> result, final Option option) {
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
        result.add(new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
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
                }));


        result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, args)),
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
                }));
    }

    protected void inputExcludeParsedScmTest(final Set<TestData> result, final Option option) {
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

        result.add(new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.nullPair()),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(11);
                    // .gitignore is ignored by default as it is hidden
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                }));

        result.add(new TestData("GIT", Collections.singletonList(ImmutablePair.of(option, new String[]{"GIT"})),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
                    // .gitignore is ignored by default as it is hidden
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(8);
                }));
    }

    protected void inputExcludeSizeTest(final Set<TestData> result, final Option option) {
        String[] notExcluded = {"Hello.txt", "HelloWorld.txt"};
        String[] excluded = {"Hi.txt"};

        Consumer<Path> setup = basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "Hi.txt", Collections.singletonList("Hi"));
            writeFile(baseDir, "Hello.txt", Collections.singletonList("Hello"));
            writeFile(baseDir, "HelloWorld.txt", Collections.singletonList("HelloWorld"));
        };

        result.add(new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.of(null, null)),
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
                }));

        result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, new String[]{"5"})),
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
                }));
    }

    // include tests
    private List<TestData> execIncludeTest(final Option option, String[] args, Consumer<Path> setupFiles) {
        Option excludeOption = Arg.EXCLUDE.option();
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
        TestData test2 = new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.of(excludeOption, AbstractTestDataProvider.EXCLUDE_ARGS)),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                    // .gitignore is ignored by default as it is hidden but not counted
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(4);
                });

        TestData test3 = new TestData("", Arrays.asList(ImmutablePair.of(option, args), ImmutablePair.of(excludeOption, AbstractTestDataProvider.EXCLUDE_ARGS)),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
                    // .gitignore is ignored by default as it is hidden but not counted
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(2);
                });
        return Arrays.asList(test1, test2, test3);
    }

    protected void inputIncludeFileTest(final Set<TestData> result, final Option option) {
        Consumer<Path> setup = mkRat.andThen(basePath -> {
            File dir = basePath.resolve(".rat").toFile();
            writeFile(dir, "include.txt", Arrays.asList(AbstractTestDataProvider.INCLUDE_ARGS));
        });
        result.addAll(execIncludeTest(option, new String[]{".rat/include.txt"}, setup));
    }


    protected void inputIncludeTest(final Set<TestData> result, final Option option) {
        result.addAll(execIncludeTest(option, AbstractTestDataProvider.INCLUDE_ARGS, mkRat));
    }

    protected void inputIncludeStdTest(final Set<TestData> result, final Option option) {
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
        if (Arg.EXCLUDE.isEmpty()) {
            throw new RuntimeException(String.format("Can not test %s if there are no exclude file options supported", option.getKey()));
        }
        ImmutablePair<Option, String[]> excludes = ImmutablePair.of(Arg.EXCLUDE.option(),
                new String[]{"*~more", "*~"});


        result.add(new TestData("includeStdValidation", Collections.singletonList(excludes),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(4);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(6);
                }));

        if (!option.hasArg()) {
            if (option.getKey().equals("scan-hidden-directories")) {
                result.add(new TestData("",
                        Arrays.asList(ImmutablePair.of(option, null), excludes),
                        setup,
                        validatorData -> {
                            assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(5);
                            assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(5);
                            assertIgnoredFile(validatorData.getDocument(), "._afile");
                            assertStandardFile(validatorData.getDocument(), ".hiddenDir/aFile");
                        }));
            } else {
                throw new RuntimeException("Unknown option: " + option.getKey());
            }
        } else {
            result.add(new TestData(StandardCollection.MISC.name().toLowerCase(Locale.ROOT), Arrays.asList(ImmutablePair.of(option, new String[]{StandardCollection.MISC.name()}), excludes),
                    setup,
                    validatorData -> {

                        assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(8);
                        assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(2);
                    }));

            result.add(new TestData(StandardCollection.HIDDEN_FILE.name().toLowerCase(Locale.ROOT),
                    Arrays.asList(ImmutablePair.of(option, new String[]{StandardCollection.HIDDEN_FILE.name()}), excludes),
                    setup,
                    validatorData -> {
                        assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS))
                                .isEqualTo(6);
                        assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED))
                                .isEqualTo(4);
                        assertStandardFile(validatorData.getDocument(), "._afile");
                        assertIgnoredFile(validatorData.getDocument(), ".hiddenDir");
                    }));

            result.add(new TestData(StandardCollection.HIDDEN_DIR.name().toLowerCase(Locale.ROOT),
                    Arrays.asList(ImmutablePair.of(option, new String[]{StandardCollection.HIDDEN_DIR.name()}), excludes),
                    setup,
                    validatorData -> {
                        assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(5);
                        assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(5);
                        assertIgnoredFile(validatorData.getDocument(), "._afile");
                        assertStandardFile(validatorData.getDocument(), ".hiddenDir/aFile");
                    }));
        }
    }

    protected void inputSourceTest(final Set<TestData> result, final Option option) {
        Consumer<Path> setup = basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "codefile");
            writeFile(baseDir, "intput.txt", "codefile");
            writeFile(baseDir, "notcodeFile");
        };

        result.add(new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(3);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                }));

        result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, new String[]{"intput.txt"})),
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(0);
                }));
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
                basePath -> DataUtils.generateSpdxConfig(basePath.resolve(".rat").resolve("catz.xml"), "catz", "catz")),
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(2);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);
                });
        return Arrays.asList(test1, test2, test3);
    }

    protected void helpLicenses(final Set<TestData> result, final Option option) {
        PrintStream origin = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);

        result.add( new TestData("stdOut",
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

    protected void licensesApprovedFileTest(final Set<TestData> result, final Option option) {
        result.addAll(execLicensesApprovedTest(option, new String[]{".rat/licensesApproved.txt"},
                mkRat.andThen(
                        basePath -> writeFile(basePath.resolve(".rat").toFile(), "licensesApproved.txt", List.of("catz")))));
    }

    protected void licensesApprovedTest(final Set<TestData> result, final Option option) {
        result.addAll(execLicensesApprovedTest(option, new String[]{"catz"}, NO_SETUP));
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

    protected void licensesDeniedTest(final Set<TestData> result, final Option option) {
        result.addAll(execLicensesDeniedTest(option, new String[]{"ILLUMOS"}, NO_SETUP));
    }

    protected void licensesDeniedFileTest(final Set<TestData> result, final Option option) {
        result.addAll(execLicensesDeniedTest(option, new String[]{"licensesDenied.txt"},
                (basePath) -> writeFile(basePath.toFile(), "licensesDenied.txt", Collections.singletonList("ILLUMOS"))));
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

    protected void licenseFamiliesApprovedFileTest(final Set<TestData> result, final Option option) {
        result.addAll(execLicenseFamiliesApprovedTest(option, new String[]{".rat/familiesApproved.txt"},
                mkRat.andThen( basePath -> writeFile(basePath.resolve(".rat").toFile(), "familiesApproved.txt", Collections.singletonList("catz")))));
    }

    protected void licenseFamiliesApprovedTest(final Set<TestData> result, final Option option) {
        result.addAll(execLicenseFamiliesApprovedTest(option, new String[]{"catz"}, NO_SETUP));
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

    protected void licenseFamiliesDeniedFileTest(final Set<TestData> result, final Option option) {
        result.addAll(execLicenseFamiliesDeniedTest(option, new String[]{".rat/familiesDenied.txt"},
                mkRat.andThen(
        baseDir -> writeFile(baseDir.resolve(".rat").toFile(), "familiesDenied.txt", Collections.singletonList("BSD-3")))));
    }

    protected void licenseFamiliesDeniedTest(final Set<TestData> result, final Option option) {
        result.addAll(execLicenseFamiliesDeniedTest(option, new String[]{"BSD-3"}, NO_SETUP));
    }

    protected void counterMaxTest(final Set<TestData> result, final Option option) {
       result.add(new TestData(DataUtils.asDirName(option), Collections.singletonList(ImmutablePair.of(null, null)),
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Unapproved\n",
                            "*/\n\n", "class Test {}\n"));
                },
                validatorData -> {
                    ClaimStatistic claimStatistic = validatorData.getStatistic();
                    ClaimValidator validator = validatorData.getConfiguration().getClaimValidator();
                    assertThat(validator.listIssues(claimStatistic)).containsExactly("UNAPPROVED");
                }));

        result.add(new TestData("Unapproved1", Collections.singletonList(ImmutablePair.of(option, new String[]{"Unapproved:1"})),
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Unapproved\n",
                            "*/\n\n", "class Test {}\n"));
                },
                validatorData -> {
                    ClaimStatistic claimStatistic = validatorData.getStatistic();
                    ClaimValidator validator = validatorData.getConfiguration().getClaimValidator();
                    assertThat(validator.listIssues(claimStatistic)).isEmpty();
                }));
    }

    protected void counterMinTest(final Set<TestData> result, final Option option) {
        result.add(new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Unapproved\n",
                            "*/\n\n", "class Test {}\n"));
                },
                validatorData -> {
                    ClaimStatistic claimStatistic = validatorData.getStatistic();
                    ClaimValidator validator = validatorData.getConfiguration().getClaimValidator();
                    assertThat(validator.listIssues(claimStatistic)).containsExactly("UNAPPROVED");
                }));

        result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, new String[]{"Unapproved:1"})),
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Unapproved\n",
                            "*/\n\n", "class Test {}\n"));
                },
                validatorData -> {
                    ClaimStatistic claimStatistic = validatorData.getStatistic();
                    ClaimValidator validator = validatorData.getConfiguration().getClaimValidator();
                    assertThat(validator.listIssues(claimStatistic)).isEmpty();
                }));
    }

    /**
     * Add results to the result list.
     * @param result the result list.
     * @param option configuration option we are testing.
     */
    protected void configTest(final Set<TestData> result, final Option option) {
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

        result.add(new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                setup,
                validatorData -> {
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
                    assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
                }));

        result.add(new TestData("withDefaults", Collections.singletonList(underTest),
            setup,
            validatorData -> {
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(2);
                assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(0);
            }));

        if (!Arg.CONFIGURATION_NO_DEFAULTS.isEmpty()) {
            result.add(new TestData("noDefaults", Arrays.asList(underTest,
                    ImmutablePair.of(Arg.CONFIGURATION_NO_DEFAULTS.find("configuration-no-defaults"), null)),
                    setup,
                    validatorData -> {
                        assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(2);
                        assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.APPROVED)).isEqualTo(1);
                        assertThat(validatorData.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(1);
                    }));
        }
    }

    protected void configurationNoDefaultsTest(final Set<TestData> result, final Option option) {
        TestData test1 = new TestData("", Collections.singletonList(ImmutablePair.of(option, null)),
                basePath -> {
                    File baseDir = basePath.toFile();
                    writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Apache-2.0\n",
                            "*/\n\n", "class Test {}\n"));
                },
                validatorData -> assertThat(validatorData.getConfiguration().getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isEmpty());
        test1.setException(NO_LICENSES_EXCEPTION);
        result.add(test1);
    }

    protected void dryRunTest(final Set<TestData> result, final Option option) {
        result.add(new TestData("stdRun", Collections.singletonList(ImmutablePair.of(option, null)),
                NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isDryRun()).isTrue()));

        result.add(new TestData(DataUtils.asDirName(option), NO_OPTIONS,
                NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isDryRun()).isFalse()));
    }

    protected void editCopyrightTest(final Set<TestData> result, final Option option) {
        Consumer<Path> setup = basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "Missing.java", Arrays.asList("/* no license */\n\n", "class Test {}\n"));
        };
        ImmutablePair<Option, String[]> copyright = ImmutablePair.of(option, new String[]{"MyCopyright"});
        if (Arg.EDIT_ADD.isEmpty()) {
            throw new RuntimeException("Can not execute copyright tests without an EDIT_ADD option avialable");
        }
        ImmutablePair<Option, String[]> editLicense = ImmutablePair.of(Arg.EDIT_ADD.option(), null);

        result.add(new TestData("noEditLicense", Collections.singletonList(copyright),
                setup,
                validatorData -> {
                    try {
                        String actualText = TextUtils.readFile(validatorData.getBaseDir().resolve("Missing.java").toFile());
                        TextUtils.assertNotContains("MyCopyright", actualText);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }));

        result.add(new TestData(DataUtils.asDirName(editLicense.getLeft()), Arrays.asList(copyright, editLicense),
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
                }));

        if (!Arg.DRY_RUN.isEmpty()) {
            Option dryRun = Arg.DRY_RUN.option();
            result.add(new TestData(DataUtils.asDirName(dryRun),
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
                    }));
        }

        if (!Arg.EDIT_OVERWRITE.isEmpty()) {
            Option overwrite = Arg.EDIT_OVERWRITE.option();
            result.add(new TestData(DataUtils.asDirName(overwrite), Arrays.asList(copyright, editLicense, ImmutablePair.of(overwrite, null)),
                    setup,
                    validatorData -> {
                        try {
                            String actualText = TextUtils.readFile(validatorData.getBaseDir().resolve("Missing.java").toFile());
                            TextUtils.assertContains("MyCopyright", actualText);
                            assertThat(validatorData.getBaseDir().resolve("Missing.java.new")).doesNotExist();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }));
        }
    }

    protected void editLicenseTest(final Set<TestData> result, final Option option) {
        result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, null)),
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
                assertThat(contents).isEqualTo("""
                        /*
                         * Licensed to the Apache Software Foundation (ASF) under one
                         * or more contributor license agreements.  See the NOTICE file
                         * distributed with this work for additional information
                         * regarding copyright ownership.  The ASF licenses this file
                         * to you under the Apache License, Version 2.0 (the
                         * "License"); you may not use this file except in compliance
                         * with the License.  You may obtain a copy of the License at
                         *\s
                         *   http://www.apache.org/licenses/LICENSE-2.0
                         *\s
                         * Unless required by applicable law or agreed to in writing,
                         * software distributed under the License is distributed on an
                         * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
                         * KIND, either express or implied.  See the License for the
                         * specific language governing permissions and limitations
                         * under the License.
                         */
                        
                        class NoLicense {}""");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
                }));
    }

    protected void editOverwriteTest(final Set<TestData> result, final Option option) {
        result.add(new TestData("noEditLicense", Collections.singletonList(ImmutablePair.of(option, null)),
                NO_SETUP,
                validatorData -> assertThat(validatorData.getConfiguration().isAddingLicensesForced())
                        .describedAs("Without edit-license should be false").isFalse()));

        if (!Arg.EDIT_ADD.isEmpty()) {
            result.add(new TestData("", Arrays.asList(ImmutablePair.of(option, null),
                    ImmutablePair.of(Arg.EDIT_ADD.find("edit-license"), null)),
                    NO_SETUP,
                    validatorData -> assertThat(validatorData.getConfiguration().isAddingLicensesForced()).isTrue()));
        }
    }

    protected void logLevelTest(final Set<TestData> result, final Option option) {
        final TestingLog testingLog = new TestingLog();

        Consumer<Path> setup = basePath -> {
            DefaultLog.setInstance(testingLog);
            testingLog.clear();
        };

        result.add(new TestData(Log.Level.INFO.name(),
                Collections.singletonList(ImmutablePair.of(option, new String[]{Log.Level.INFO.name()})),
                setup,
                validatorData -> {
                    try {
                        testingLog.assertNotContains("DEBUG");
                    } finally {
                        DefaultLog.setInstance(null);
                    }
                }));

        result.add(new TestData(Log.Level.DEBUG.name(),
                Collections.singletonList(ImmutablePair.of(option, new String[]{Log.Level.DEBUG.name()})),
                setup,
                validatorData -> {
                    try {
                        testingLog.assertContains("DEBUG");
                    } finally {
                        DefaultLog.setInstance(null);
                    }
                }));
    }

    protected void outputArchiveTest(final Set<TestData> result, final Option option) {
        for (ReportConfiguration.Processing processing : ReportConfiguration.Processing.values()) {
            result.add(new TestData(processing.name().toLowerCase(Locale.ROOT),
                    Collections.singletonList(ImmutablePair.of(option, new String[]{processing.name()})),
                    basePath -> {
                        File localArchive = new File(basePath.toFile(), "dummy.jar");
                        try (InputStream in = ReportTestDataProvider.class.getResourceAsStream("/tikaFiles/archive/dummy.jar");
                             OutputStream out = Files.newOutputStream(localArchive.toPath())) {
                            Objects.requireNonNull(in);
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
                    })
            );
        }
    }

    protected void outputFamiliesTest(final Set<TestData> result, final Option option) {
        for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
            result.add(new TestData(filter.name().toLowerCase(Locale.ROOT),
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
                    })
            );
        }
    }

    protected void outputFileTest(final Set<TestData> result, final Option option) {
        result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, new String[]{"outexample"})),
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
                })
        );
    }

    protected void outputLicensesTest(final Set<TestData> result, final Option option) {
        for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
            result.add(new TestData(filter.name(), Collections.singletonList(ImmutablePair.of(option, new String[]{filter.name()})),
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
                    }));
        }
    }


    protected void outputStandardTest(final Set<TestData> result, final Option option) {
        for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
            result.add(new TestData(proc.name().toLowerCase(Locale.ROOT), Collections.singletonList(ImmutablePair.of(option, new String[]{proc.name()})),
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
                    }));
        }
    }


    protected void outputStyleTest(final Set<TestData> result, final Option option) {
        Consumer<Path> createFile = basePath -> {
            File baseDir = basePath.toFile();
            writeFile(baseDir, "Test.java", Arrays.asList("/*\n", "SPDX-License-Identifier: Apache-2.0\n",
                    "*/\n\n", "class Test {}\n"));
            writeFile(baseDir, "Missing.java", Arrays.asList("/* no license */\n\n", "class Test {}\n"));
        };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!option.hasArg()) {
            if (option.getLongOpt().equals("xml")) {
                result.add(new TestData("", Collections.singletonList(ImmutablePair.of(option, null)),
                        createFile,
                        validatorData -> {
                            try (InputStream expected = StyleSheets.XML.getStyleSheet().get();
                                 InputStream actual = validatorData.getConfiguration().getStyleSheet().get()) {
                                assertThat(IOUtils.contentEquals(expected, actual)).as(() -> String.format("'%s' does not match", StyleSheets.XML)).isTrue();
                                baos.reset();
                                validatorData.getOutput().format(validatorData.getConfiguration().getStyleSheet(), () -> baos);
                                String actualText = baos.toString();
                                TextUtils.assertContainsExactly(1, "<resource encoding=\"ISO-8859-1\" mediaType=\"text/x-java-source\" name=\"/Test.java\" type=\"STANDARD\">", actualText);
                                TextUtils.assertContainsExactly(1, "<resource encoding=\"ISO-8859-2\" mediaType=\"text/x-java-source\" name=\"/Missing.java\" type=\"STANDARD\">", actualText);
                            } catch (IOException | RatException e) {
                                throw new RuntimeException(e);
                            }
                        }));
            }
        } else {
            for (StyleSheets sheet : StyleSheets.values()) {
                result.add(new TestData(sheet.name().toLowerCase(Locale.ROOT), Collections.singletonList(ImmutablePair.of(option, new String[]{sheet.arg()})),
                        createFile,
                        validatorData -> {
                            try (InputStream expected = sheet.getStyleSheet().get();
                                 InputStream actual = validatorData.getConfiguration().getStyleSheet().get()) {
                                assertThat(IOUtils.contentEquals(expected, actual)).as(() -> String.format("'%s' does not match", sheet)).isTrue();
                                baos.reset();
                                validatorData.getOutput().format(validatorData.getConfiguration().getStyleSheet(), () -> baos);
                                String actualText = baos.toString();
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
                                    case XHTML5:
                                        TextUtils.assertPatternInTarget("<td>Approved<\\/td>\\s+<td>1<\\/td>\\s+<td>A count of approved licenses.<\\/td>", actualText);
                                        break;
                                    default:
                                        fail("No test for stylesheet " + sheet);
                                        break;
                                }
                            } catch (IOException | RatException e) {
                                throw new RuntimeException(e);
                            }
                        }));
            }

            result.add(new TestData("fileStyleSheet",
                    Collections.singletonList(ImmutablePair.of(option, new String[]{"fileStyleSheet.xslt"})),
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
                            baos.reset();
                            validatorData.getOutput().format(validatorData.getConfiguration().getStyleSheet(), () -> baos);
                            String actualText = baos.toString();
                            TextUtils.assertContainsExactly(1, "Hello World", actualText);
                        } catch (IOException | RatException e) {
                            throw new RuntimeException(e);
                        }
                    }));
        }
    }
}
