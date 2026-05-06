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
import org.apache.commons.cli.ParseException;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.report.IReportable;
import org.apache.rat.ui.UIOption;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.CasedString;
import org.apache.rat.walker.ArchiveWalker;
import org.apache.rat.walker.DirectoryWalker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OptionCollectionParserTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    static Path testPath;

    private final TestOptionCollection optionCollection = new TestOptionCollection();
    private final OptionCollectionParser underTest = new OptionCollectionParser(optionCollection);

    @Test
    void parseCommands() throws IOException, ParseException {
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

    static class TestOption extends UIOption<TestOption> {

        /**
         * Constructor.
         *
         * @param optionCollection the collection the UIOption belongs to.
         * @param option           The CLI option
         */
        protected <C extends UIOptionCollection<TestOption>> TestOption(C optionCollection, Option option) {
            super(optionCollection, option, new CasedString(CasedString.StringCase.CAMEL, option.getKey()));
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
    }

    static class TestOptionCollection extends UIOptionCollection<TestOption> {
        /**
         * Construct the UIOptionCollection from the builder.
         */
        protected TestOptionCollection() {
            super(new TestCollectionBuilder());
        }

        static class TestCollectionBuilder extends UIOptionCollection.Builder<TestOption, TestCollectionBuilder> {
            TestCollectionBuilder() {
                super(TestOption::new);
            }
        }
    }
}
