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
import java.util.stream.Stream;

import org.apache.rat.Reporter;
import org.apache.rat.utils.FileUtils;
import org.apache.rat.testhelpers.data.OptionTestDataProvider;

import org.apache.rat.testhelpers.data.TestData;
import org.apache.rat.testhelpers.data.ValidatorData;
import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class ReportOptionsTest {
    @TempDir
    static Path testPath;

    private static final OptionTestDataProvider optionTestDataProvider = new OptionTestDataProvider();

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
        return optionTestDataProvider.getUITestMap(new CLIOptionCollection()).values().stream().map(testData ->
                Arguments.of(testData.getTestName(), testData));
        // TODO add help test
    }

    /**
     * A parameterized test for the options.
     * @param name The name of the test.
     */
    @ParameterizedTest( name = "{index} {0}")
    @MethodSource("getTestData")
    void testOptionsUpdateConfig(String name, TestData test) throws Exception {
        Path basePath = testPath.resolve(test.getTestName());
        CLIOptionCollection optionCollection = new CLIOptionCollection();
        FileUtils.mkDir(basePath.toFile());
        test.setupFiles(basePath);
        if (test.getExpectedException() != null) {
            assertThatThrownBy(() -> Report.generateReport(optionCollection, basePath.toFile(), test.getCommandLine(basePath.toString()))
                    ).hasMessageContaining(test.getExpectedException().getMessage());
        } else {
            Reporter.Output result = Report.generateReport(optionCollection, basePath.toFile(), test.getCommandLine(basePath.toString()));
            ValidatorData data = new ValidatorData(result, basePath.toString());
            test.getValidator().accept(data);
        }
    }
}
