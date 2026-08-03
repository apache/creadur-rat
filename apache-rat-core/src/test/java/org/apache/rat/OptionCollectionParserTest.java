/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   https://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.ui.UIOption;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionCollectionParserTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    static Path testPath;

    private final TestOptionCollection optionCollection = new TestOptionCollection();
    private final OptionCollectionParser<TestOption> underTest = new OptionCollectionParser<>(optionCollection);

    @Test
    void parseCommands() throws RatException {
        String[] args = {"arg1", "arg2"};
        ArgumentContext ctxt = underTest.parseCommands(testPath.toFile(), args);
        assertThat(ctxt.getCommandLine().getArgList()).containsExactly(args);

        String[] cmds = new String[] {"--input-exclude-size", "5", "arg1", "arg2"};
        ctxt = underTest.parseCommands(testPath.toFile(), cmds);
        StringBuilder sb = new StringBuilder();
        ctxt.getConfiguration().reportExclusions(sb);
        assertThat(sb.toString()).contains("Excluding File size < 5 bytes.");
        assertThat(ctxt.getCommandLine().getArgList()).containsExactly(args);
    }

    @Test
    void parseCommandLineParseExceptionTest() {
        Options options = new Options();
        options.addOption(Option.builder("req").required().build());
        TestingLog testingLog = new TestingLog();
        try {
            DefaultLog.setInstance(testingLog);
            assertThatThrownBy(() -> underTest.parseCommandLine(options, new String[0]))
                    .isInstanceOf(ParseException.class);
        } finally {
            DefaultLog.setInstance(null);
        }
        assertThat(testingLog.getCaptured()).containsOnlyOnce("Please use the \"--help\" option to see a list of valid commands and options.");
    }

    @Test
    void printHelpExceptionTest() throws ParseException {
        Options options = new Options();
        ReportConfiguration cfg = new ReportConfiguration();
        ArgumentContext ctxt = new ArgumentContext(testPath.toFile(), cfg, options, new String[0]);
        cfg.setOut(new ReportConfiguration.IODescriptor("Bad Supplier", () -> { throw new IOException("Bad Supplier");}));
        assertThatThrownBy(() -> underTest.printHelp(ctxt))
                .isInstanceOf(RatException.class)
                .hasMessageContaining("Unable to print help: Bad Supplier");
    }

    /**
     * A UIOption implementation to support testing.
     */
    static class TestOption extends UIOption<TestOption> {

        /**
         * Constructor.
         *
         * @param optionCollection the collection the UIOption belongs to.
         * @param option           The CLI option
         */
        protected <C extends UIOptionCollection<TestOption>> TestOption(TestOptionBuilder builder) {
            super(builder);
        }

        @Override
        protected String cleanupName(Option option) {
            return "clean" + option.toString();
        }

        @Override
        public String getExample() {
            return "example " + option.toString();
        }

        @Override
        public String getText() {
            return "text for " + option.toString();
        }

        public static class TestOptionBuilder extends UIOption.Builder<TestOption, TestOptionBuilder> {

            @Override
            protected Function<Option, CasedString> getNameFactory() {
                return ArgumentTracker::extractName;
            }

            @Override
            protected TestOption doBuild() {
                return new TestOption(this);
            }
        }
    }

    /**
     * A UIOptionCollection implementation for testing.  Contains TestOptions.
     */
    static class TestOptionCollection extends UIOptionCollection<TestOption> {
        /**
         * Construct the UIOptionCollection from the builder.
         */
        protected TestOptionCollection() {
            super(new TestCollectionBuilder());
        }

        static class TestCollectionBuilder extends UIOptionCollection.Builder<TestOption, TestCollectionBuilder> {
            TestCollectionBuilder() {
                super(TestOption.TestOptionBuilder::new);
            }
        }
    }
}
