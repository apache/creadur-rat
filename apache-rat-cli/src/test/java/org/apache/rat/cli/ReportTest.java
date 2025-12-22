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
package org.apache.rat.cli;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.rat.OptionCollection;
import org.apache.rat.commandline.Arg;
import org.apache.rat.testhelpers.FileUtils;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.data.DataUtils;
import org.apache.rat.testhelpers.data.ReportTestDataProvider;
import org.apache.rat.testhelpers.data.TestData;
import org.apache.rat.testhelpers.data.ValidatorData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;


public class ReportTest {
    @TempDir
    static Path testPath;

    private static final ReportTestDataProvider reportTestDataProvider = new ReportTestDataProvider();

    /**
     * This method is a known workaround for
     * {@link <a href="https://github.com/junit-team/junit5/issues/2811">junit 5 issue #2811</a> }.
     */
    @AfterEach
    @EnabledOnOs(OS.WINDOWS)
    void cleanUp() {
        System.gc();
    }


    static Stream<Arguments> getTestData() {
        Map<String, TestData> tests = reportTestDataProvider.getOptionTestMap(Collections.emptyList());
        TestData test = new TestData("", Arrays.asList(ImmutablePair.of(CLIOption.HELP, new String[]{}),
                ImmutablePair.of(Arg.OUTPUT_FILE.option(), new String[]{"helpText"})),
                DataUtils.NO_SETUP,
                validatorData -> {
                    try {
                        String result = TextUtils.readFile(validatorData.baseDir.resolve("helpText").toFile());
                        for (Option option : OptionCollection.buildOptions(CLIOption.ADDITIONAL_OPTIONS).getOptions()) {
                            if (option.getOpt() != null) {
                                TextUtils.assertContains("-" + option.getOpt() + (option.getLongOpt() == null ? " " : ","), result);
                            }
                            if (option.getLongOpt() != null) {
                                TextUtils.assertContains("--" + option.getLongOpt() + " ", result);
                            }
                        }

                        assertThat(result).doesNotContain("..");
                    } catch (Exception e) {
                        fail(e.getMessage(), e);
                    }
                }
        );
        tests.put(test.getTestName(), test);
        return tests.values().stream().map(testData ->
                Arguments.of(testData.getTestName(), testData));
    }

    /**
     * A parameterized test for the options.
     * @param name The name of the test.
     */
    @ParameterizedTest( name = "{index} {0}")
    @MethodSource("getTestData")
    void testOptionsUpdateConfig(String name, TestData test) throws Exception {
        Path basePath = testPath.resolve(test.getTestName());
        FileUtils.mkDir(basePath.toFile());
        test.setupFiles(basePath);
        if (test.getExpectedException() != null) {
            assertThatThrownBy(() -> Report.generateReport(basePath.toFile(), test.getCommandLine(basePath.toString()))
                    ).hasMessageContaining(test.getExpectedException().getMessage());
        } else {
            Report.CLIOutput result = Report.generateReport(basePath.toFile(), test.getCommandLine(basePath.toString()));
            ValidatorData data = new ValidatorData(
                    result.output, result.configuration, basePath.toString());
            test.getValidator().accept(data);
        }
    }
}
