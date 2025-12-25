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
package org.apache.rat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.cli.ParseException;
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.testhelpers.FileUtils;
import org.apache.rat.testhelpers.data.OptionTestDataProvider;
import org.apache.rat.testhelpers.data.TestData;
import org.apache.rat.testhelpers.data.ValidatorData;
import org.apache.rat.testhelpers.XmlUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public final class ReporterOptionsTest {

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
        return optionTestDataProvider.getOptionTests().stream().map(testData ->
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
        test.setupFiles(basePath);
        ArgumentContext ctxt = OptionCollection.parseCommands(basePath.toFile(), test.getCommandLine());
        Reporter.Output output = new Reporter(ctxt.getConfiguration()).execute();
        ValidatorData data = new ValidatorData(output, basePath.toString());
        test.getValidator().accept(data);
    }

    @Test
    void testRat362() {
        File testDir = new File(testPath.toFile(), "RAT_362");
        String[] args = {"--output-style", "xml", "--input-exclude-parsed-scm", "GIT", "--", testDir.getAbsolutePath()};
        try {
            FileUtils.mkDir(testDir);
            FileUtils.writeFile(testDir, ".gitignore", "/foo.md");
            FileUtils.writeFile(testDir, "foo.md");
            ArgumentContext ctxt = OptionCollection.parseCommands(testDir, args);
            Reporter reporter = new Reporter(ctxt.getConfiguration());
            Reporter.Output output = reporter.execute();
            XmlUtils.printDocument(System.out, output.getDocument());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XmlUtils.assertIsPresent(output.getDocument(), xpath, "/rat-report/resource[@name='/foo.md']");
            XmlUtils.assertAttributes(output.getDocument(), xpath, "/rat-report/resource[@name='/foo.md']",
                    XmlUtils.mapOf("type", "IGNORED"));
            assertThat(output.getStatistic().getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(0);
            assertThat(output.getStatistic().getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(2);
        } catch (IOException | ParseException | RatException | XPathExpressionException e) {
            fail(e);
        }
    }

}
