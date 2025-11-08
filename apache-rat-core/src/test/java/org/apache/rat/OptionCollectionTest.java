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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.document.DocumentName;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.IReportable;
import org.apache.rat.test.AbstractConfigurationOptionsProvider;
import org.apache.rat.test.utils.OptionFormatter;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.utils.CasedString;
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
        AbstractConfigurationOptionsProvider.preserveData(testPath.toFile(), "optionTest");
    }

    /**
     * Defines the test method that is stored in a map.
     */
    @FunctionalInterface
    public interface OptionTest {
        /**
         * Executes the test and uses fail or asserts to generate failures.
         */
        void exec();

        /**
         * Execute the test and ensure any failures have the test name added.
         */
        default void test() {
            try {
                exec();
            } catch (AssertionError e) {
                throw new AssertionError(formatMsg(e.getMessage()), e);
            }
        }

        /**
         * Formats the messages by adding the test name.
         * @param msg the message to reformat.
         * @return the formatted message.
         */
        default String formatMsg(String msg) {
            return String.format("%s: %s", this, msg);
        }

        /**
         * Creates a named OptionTest.
         * @param name the name of the test.
         * @param test the test to execute.
         * @return a named option test.
         */
        static OptionTest namedTest(String name, OptionTest test) {
            return new OptionTest() {
                @Override
                public void exec() {
                    test.exec();
                }
                @Override
                public String toString() {
                    return name;
                }
            };
        }
    }

    /**
     * A test function. Used to annotate methods in test providers.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface TestFunction {
    }

    /**
     * Process methods in a test provider.
     * Tests are detected by looking for the {@link TestFunction} annotation.
     * @param testProvider the test provider
     * @return a map of named tests to a named OptionTest.
     */
    public static Map<String, OptionTest> processTestFunctionAnnotations(Object testProvider) {
        final int testLength = 4;
        final Class<?> clazz = testProvider.getClass();
        final Map<String, OptionTest> result = new TreeMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(TestFunction.class) && method.getParameterCount() == 0 && method.getReturnType() == void.class) {
                String name = method.getName();
                if (name.endsWith("Test")) {
                    name = name.substring(0, name.length() - testLength);
                }
                if (name.startsWith("test")) {
                    name = name.substring(testLength);
                }
                name = new CasedString(CasedString.StringCase.CAMEL, name).toCase(CasedString.StringCase.KEBAB).toLowerCase(Locale.ROOT);
                result.put(name, OptionTest.namedTest(name, () -> {
                            try {
                                method.invoke(testProvider);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new AssertionError(e);
                            }
                        }
                ));
            }
        }
        return result;
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

    @Test
    public void testDeprecatedUseLogged() throws ParseException {
        Options opts = OptionCollection.buildOptions();
        opts.addOption(Option.builder().longOpt("deprecated-option").deprecated(DeprecatedAttributes.builder().setDescription("Deprecation reason.")
                .setForRemoval(true).setSince("1.0.0-SNAPSHOT").get()).build());

        TestingLog log = new TestingLog();
        try {
            DefaultLog.setInstance(log);
            CommandLine commandLine = OptionCollection.parseCommandLine(opts, new String[]{"--deprecated-option", "."});
            commandLine.hasOption("--deprecated-option");
        } finally {
            DefaultLog.setInstance(null);
        }
        log.assertContainsExactly(1, "WARN: Option [--deprecated-option] used. Deprecated for removal since 1.0.0-SNAPSHOT: Deprecation reason.");
    }

    @Test
    public void testShortenedOptions() throws IOException, ParseException {
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
    public void getReportableTest(String fName) throws IOException, ParseException {
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
    static class CliOptionsProvider extends AbstractConfigurationOptionsProvider implements ArgumentsProvider {

        /** A flag to determine if help was called */
        final AtomicBoolean helpCalled = new AtomicBoolean(false);

        @Override
        public void helpTest() {
            String[] args = { OptionFormatter.longOpt(OptionCollection.HELP) };
            try {
                ReportConfiguration config = OptionCollection.parseCommands(testPath.toFile(), args, o -> helpCalled.set(true), true);
                assertThat(config).as("Should not have config").isNull();
                assertThat(helpCalled.get()).as("Help was not called").isTrue();
            } catch (IOException | ParseException e) {
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
        protected final ReportConfiguration generateConfig(List<Pair<Option, String[]>> args) throws IOException, ParseException {
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
