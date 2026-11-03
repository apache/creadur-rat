/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.maven.tools;

import org.apache.commons.cli.Option;
import org.apache.rat.maven.MavenOption;
import org.apache.rat.maven.MavenOptionCollection;
import org.apache.rat.testhelpers.data.ReportTestDataProvider;
import org.apache.rat.testhelpers.data.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGeneratorTest {

    @TempDir
    private Path testPath;
    private TestGenerator testGenerator;
    private MavenOptionCollection mavenOptionCollection;
    TestGeneratorTest() {}

    @BeforeEach
    void setup() throws IOException {
        testGenerator = new TestGenerator("package.name", testPath.resolve("resources").toString(), testPath.resolve("source").toString());
        mavenOptionCollection = testGenerator.getMavenOptionCollection();
    }

    /**
     * Gets the mavens options collection for testing.
     * @return the MavenOptionsCollections
     */
    MavenOptionCollection getMavenOptionCollection() {
        return mavenOptionCollection;
    }

    private List<TestData> testsFor(Option option) {
        Set<TestData> testDataList = new ReportTestDataProvider().getOptionTests(mavenOptionCollection);
        return testDataList.stream().filter(td -> td.getOption() == null ? option == null : td.getOption().equals(option)).toList();
    }

    @ParameterizedTest
    @MethodSource("optionsSource")
    void test(MavenOption mavenOption) throws IOException {
        List<TestData> testDataList = testsFor(mavenOption.getOption());
        for (final TestData testData : testDataList) {
            String pomText = testGenerator.buildPom(testData);
            assertThat(pomText).contains("<" + mavenOption.getName());
        }
    }

    static List<MavenOption> optionsSource() {
        return new MavenOptionCollection().getMappedOptions().toList();
    }
}
