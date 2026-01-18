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
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.document.DocumentName;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.IReportable;
import org.apache.rat.testhelpers.BaseOptionCollection;
import org.apache.rat.testhelpers.data.OptionTestDataProvider;
import org.apache.rat.testhelpers.data.TestData;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;

import static java.lang.String.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class OptionCollectionParserTest {
    @TempDir
    static Path testPath;

    private static final OptionTestDataProvider optionTestDataProvider = new OptionTestDataProvider();

    private final OptionCollectionParser collectionParser = new OptionCollectionParser(new BaseOptionCollection());
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
        return optionTestDataProvider.getOptionTests(new BaseOptionCollection()).stream().map(testData ->
                Arguments.of(testData.getTestName(), testData));
    }

    @Test
    public void testDirOptionCapturesDirectoryToScan() throws IOException, ParseException {
        TestingLog log = new TestingLog();
        ArgumentContext ctxt;
        try {
            DefaultLog.setInstance(log);
            String[] args = {"--dir", testPath.toFile().getAbsolutePath()};
            ctxt = collectionParser.parseCommands(testPath.toFile(), args);
        } finally {
            DefaultLog.setInstance(null);
        }
        assertThat(ctxt).isNotNull();
    }

    @Test
    public void testShortenedOptions() throws IOException, ParseException {
        String[] args = {"--output-lic", "ALL"};
        ArgumentContext ctxt = collectionParser.parseCommands(testPath.toFile(), args);
        assertThat(ctxt).isNotNull();
        assertThat(ctxt.getConfiguration().listLicenses()).isEqualTo(LicenseSetFactory.LicenseFilter.ALL);
    }

    @Test
    public void testDefaultConfiguration() throws ParseException {
        String[] empty = {};
        BaseOptionCollection optionCollection = new BaseOptionCollection();
        CommandLine cl = new DefaultParser().parse(optionCollection.getOptions(), empty);
        ArgumentContext context = new ArgumentContext(new File("."), cl);
        ReportConfiguration config = collectionParser.populateConfiguration(context);
        ReportConfigurationTest.validateDefault(config);
    }

    @ParameterizedTest
    @ValueSource(strings = { ".", "./", "target", "./target" })
    public void getReportableTest(String fName) throws IOException, ParseException {
        File base = new File(fName);
        String expected = DocumentName.FSInfo.getDefault().normalize(base.getAbsolutePath());
        ArgumentContext ctxt = collectionParser.parseCommands(testPath.toFile(), new String[]{fName});
        IReportable reportable = collectionParser.getReportable(base, ctxt.getConfiguration());
        assertThat(reportable).as(() -> format("'%s' returned null", fName)).isNotNull();
        assertThat(reportable.name().getName()).isEqualTo(expected);
    }

    /**
     * A parameterized test for the options.
     * @param name The name of the test.
     * @param test the option test to execute.
     */
    @ParameterizedTest( name = "{index} {0}")
    @MethodSource("getTestData")
    public void testOptionsUpdateConfig(String name, TestData test) {
        test.setupFiles(testPath);

        try {
            ArgumentContext ctxt = collectionParser.parseCommands(testPath.toFile(), test.getCommandLine());
        } catch (IOException | ParseException e) {
            fail(e.getMessage(), e);
        }
    }

//    /**
//     * A class to provide the Options and tests to the testOptionsUpdateConfig.
//     */
//    static class CliOptionsProvider extends AbstractConfigurationOptionsProvider implements ArgumentsProvider {
//
//
//        @Override
//        public void helpTest() {
//            String[] args = { OptionFormatter.longOpt(OptionCollection.HELP) };
//            try {
//                ReportConfiguration config = OptionCollection.parseCommands(testPath.toFile(), args, o -> helpCalled.set(true), true);
//                assertThat(config).as("Should not have config").isNull();
//                assertThat(helpCalled.get()).as("Help was not called").isTrue();
//            } catch (IOException | ParseException e) {
//                fail(e.getMessage());
//            }
//        }
//
//        /**
//         * Constructor. Sets the baseDir and loads the testMap.
//         */
//        public CliOptionsProvider() {
//            super(Collections.emptyList(), testPath.toFile());
//        }
//
//        /**
//         * Generate a ReportConfiguration from a set of arguments.
//         * Forces the {@code helpCalled} flag to be reset.
//         * @param args the arguments.
//         * @return A ReportConfiguration
//         * @throws IOException on critical error.
//         */
//        protected final ReportConfiguration generateConfig(List<Pair<Option, String[]>> args) throws IOException {
//            helpCalled.set(false);
//            List<String> sArgs = new ArrayList<>();
//            for (Pair<Option, String[]> pair : args) {
//                if (pair.getKey() != null) {
//                    sArgs.add("--" + pair.getKey().getLongOpt());
//                    String[] oArgs = pair.getValue();
//                    if (oArgs != null) {
//                        Collections.addAll(sArgs, oArgs);
//                    }
//                }
//            }
//            try {
//                ReportConfiguration config = OptionCollection.parseCommands(testPath.toFile(), sArgs.toArray(new String[0]), o -> helpCalled.set(true), true);
//                assertThat(helpCalled.get()).as("Help was called").isFalse();
//                return config;
//            } catch (ParseException e) {
//                throw new IOException(e);
//            }
//        }
//    }
}
