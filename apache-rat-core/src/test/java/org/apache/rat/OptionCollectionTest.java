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

import java.nio.file.Path;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.document.DocumentName;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.IReportable;
import org.apache.rat.test.AbstractOptionsProvider;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class OptionCollectionTest {
    @TempDir
    static Path testPath;

    @AfterAll
    static void preserveData() {
        AbstractOptionsProvider.preserveData(testPath.toFile(), "optionTest");
    }

    /**
     * Defines the test method that is stored in a map.
     */
    @FunctionalInterface
    public interface OptionTest {
        void test();
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

    /**
     * Returns the command line format (with '--' prefix) for the Option.
     * @param opt the option to process.
     * @return the command line option.
     */
    private static String longOpt(Option opt) {
        return "--" + opt.getLongOpt();
    }

    @Test
    public void testDeprecatedUseLogged() throws IOException {
        TestingLog log = new TestingLog();
        try {
            DefaultLog.setInstance(log);
            String[] args = {"--dir", "target", "-a"};
            ReportConfiguration config = OptionCollection.parseCommands(testPath.toFile(), args, o -> fail("Help printed"), true);
            assertThat(config).isNotNull();
        } finally {
            DefaultLog.setInstance(null);
        }
        log.assertContainsExactly(1, "WARN: Option [-d, --dir] used. Deprecated for removal since 0.17: Use the standard '--'");
        log.assertContainsExactly(1, "WARN: Option [-a] used. Deprecated for removal since 0.17: Use --edit-license");
    }

    @Test
    public void testDirOptionCapturesDirectoryToScan() throws IOException {
        TestingLog log = new TestingLog();
        ReportConfiguration config;
        try {
            DefaultLog.setInstance(log);
            String[] args = {"--dir", testPath.toFile().getAbsolutePath()};
            config = OptionCollection.parseCommands(testPath.toFile(), args, (o) -> {
            }, true);
        } finally {
            DefaultLog.setInstance(null);
        }
        assertThat(config).isNotNull();
        log.assertContainsExactly(1,"WARN: Option [-d, --dir] used. Deprecated for removal since 0.17: Use the standard '--'");
    }

    @Test
    public void testShortenedOptions() throws IOException {
        String[] args = {"--output-lic", "ALL"};
        ReportConfiguration config = OptionCollection.parseCommands(testPath.toFile(), args, (o) -> {
        }, true);
        assertThat(config).isNotNull();
        assertThat(config.listLicenses()).isEqualTo(LicenseSetFactory.LicenseFilter.ALL);
    }

    @Test
    public void testDefaultConfiguration() throws ParseException {
        String[] empty = {};
        CommandLine cl = new DefaultParser().parse(OptionCollection.buildOptions(), empty);
        ArgumentContext context = new ArgumentContext(new File("."), cl);
        ReportConfiguration config = OptionCollection.createConfiguration(context);
        ReportConfigurationTest.validateDefault(config);
    }

    @ParameterizedTest
    @ValueSource(strings = { ".", "./", "target", "./target" })
    public void getReportableTest(String fName) throws IOException {
        File base = new File(fName);
        String expected = DocumentName.FSInfo.getDefault().normalize(base.getAbsolutePath());
        ReportConfiguration config = OptionCollection.parseCommands(testPath.toFile(), new String[]{fName}, o -> fail("Help called"), false);
        IReportable reportable = OptionCollection.getReportable(base, config);
        assertThat(reportable).as(() -> format("'%s' returned null", fName)).isNotNull();
        assertThat(reportable.getName().getName()).isEqualTo(expected);
    }

    /**
     * A parameterized test for the options.
     * @param name The name of the test.
     * @param test the option test to execute.
     */
    @ParameterizedTest( name = "{index} {0}")
    @ArgumentsSource(CliOptionsProvider.class)
    public void testOptionsUpdateConfig(String name, OptionTest test) {
        DefaultLog.getInstance().log(Log.Level.INFO, "Running test for: " + name);
        test.test();
    }

    /**
     * A class to provide the Options and tests to the testOptionsUpdateConfig.
     */
    static class CliOptionsProvider extends AbstractOptionsProvider implements ArgumentsProvider {

        /** A flag to determine if help was called */
        final AtomicBoolean helpCalled = new AtomicBoolean(false);

        @Override
        public void helpTest() {
            String[] args = { longOpt(OptionCollection.HELP) };
            try {
                ReportConfiguration config = OptionCollection.parseCommands(testPath.toFile(), args, o -> helpCalled.set(true), true);
                assertThat(config).as("Should not have config").isNull();
                assertThat(helpCalled.get()).as("Help was not called").isTrue();
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }

        /**
         * Constructor. Sets the baseDir and loads the testMap.
         */
        public CliOptionsProvider() {
            super(Collections.emptyList(), testPath.toFile());
        }

        /**
         * Generate a ReportConfiguration from a set of arguments.
         * Forces the {@code helpCalled} flag to be reset.
         * @param args the arguments.
         * @return A ReportConfiguration
         * @throws IOException on critical error.
         */
        protected final ReportConfiguration generateConfig(List<Pair<Option, String[]>> args) throws IOException {
            helpCalled.set(false);
            List<String> sArgs = new ArrayList<>();
            for (Pair<Option, String[]> pair : args) {
                if (pair.getKey() != null) {
                    sArgs.add("--" + pair.getKey().getLongOpt());
                    String[] oArgs = pair.getValue();
                    if (oArgs != null) {
                        Collections.addAll(sArgs, oArgs);
                    }
                }
            }
            ReportConfiguration config = OptionCollection.parseCommands(testPath.toFile(), sArgs.toArray(new String[0]), o -> helpCalled.set(true), true);
            assertThat(helpCalled.get()).as("Help was called").isFalse();
            return config;
        }
    }
}
