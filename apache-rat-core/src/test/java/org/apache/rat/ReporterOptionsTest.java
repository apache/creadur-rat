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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.rat.api.RatException;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.test.AbstractConfigurationOptionsProvider;
import org.apache.rat.test.AbstractOptionsProvider;
import org.apache.rat.testhelpers.FileUtils;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public final class ReporterOptionsTest {

    @TempDir
    static Path testPath;

    @AfterAll
    static void preserveData() {
        AbstractConfigurationOptionsProvider.preserveData(testPath.toFile(), "reporterOptionsTest");
    }

    /**
     * This method is a known workaround for
     * {@link <a href="https://github.com/junit-team/junit5/issues/2811">junit 5 issue #2811</a> }.
     */
    @AfterEach
    @EnabledOnOs(OS.WINDOWS)
    void cleanUp() {
        System.gc();
    }

    @BeforeEach
    void setup() {
        ReporterOptionsProvider.sourceDir = null;
    }

    /**
     * A parameterized test for the options.
     * @param name The name of the test.
     */
    @ParameterizedTest( name = "{index} {0}")
    @ArgumentsSource(ReporterOptionsProvider.class)
    void testOptionsUpdateConfig(String name, OptionCollectionTest.OptionTest test) {
        DefaultLog.getInstance().log(Log.Level.INFO, "Running test for: " + name);
        test.test();
    }

    @Test
    void testRat362() {
        File testDir = new File(testPath.toFile(), "RAT_362");
        String[] args = {"--output-style", "xml", "--input-exclude-parsed-scm", "GIT", "--", testDir.getAbsolutePath()};
        try {
            FileUtils.mkDir(testDir);
            FileUtils.writeFile(testDir, ".gitignore", "/foo.md");
            FileUtils.writeFile(testDir, "foo.md");
            ReportConfiguration config = OptionCollection.parseCommands(testDir, args, o -> fail("Help called"), true);
            Reporter reporter = new Reporter(config);
            ClaimStatistic claimStatistic = reporter.execute();
            XmlUtils.printDocument(System.out, reporter.getDocument());
            XPath xpath = XPathFactory.newInstance().newXPath();
            XmlUtils.assertIsPresent(reporter.getDocument(), xpath, "/rat-report/resource[@name='/foo.md']");
            XmlUtils.assertAttributes(reporter.getDocument(), xpath, "/rat-report/resource[@name='/foo.md']",
                    XmlUtils.mapOf("type", "IGNORED"));
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.STANDARDS)).isEqualTo(0);
            assertThat(claimStatistic.getCounter(ClaimStatistic.Counter.IGNORED)).isEqualTo(2);
        } catch (IOException | RatException | XPathExpressionException e) {
            fail(e);
        }
    }

}
