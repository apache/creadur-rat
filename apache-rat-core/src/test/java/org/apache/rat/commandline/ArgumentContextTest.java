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
package org.apache.rat.commandline;


import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.rat.OptionCollectionParserTest;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArgumentContextTest {
    private File f;

    @BeforeEach
    void setUp() {
        f = Files.newTemporaryFile();
    }

    @AfterEach
    void tearDown() {
        if (f != null) {
            f.delete();
        }
    }

    @Test
    void fileConfigOptsArgsConstructorTest() throws ParseException {
        Options opts = new Options();
        opts.addOption(Option.builder("one").build());
        opts.addOption(Option.builder("two").build());

        String[] args = {"-one", "three"};

        ReportConfiguration config = mock(ReportConfiguration.class);

        ArgumentContext ctxt = new ArgumentContext(f, config, opts, args);
        assertThat(ctxt.getConfiguration()).isEqualTo(config);
        assertThat(ctxt.getWorkingDirectory().asFile()).isEqualTo(f);
        assertThat(ctxt.getArgs()).containsExactly("three");
        assertThat(ctxt.getOptions()).containsExactly(opts.getOption("one"));
        assertThat(ctxt.hasOption("one")).isTrue();
        assertThat(ctxt.hasOption("two")).isFalse();
    }

    @Test
    void fileOptsArgsConstructorTest() throws ParseException {
        Options opts = new Options();
        opts.addOption(Option.builder("one").build());
        opts.addOption(Option.builder("two").build());

        String[] args = {"-one", "three"};

        ArgumentContext ctxt = new ArgumentContext(f, opts, args);
        assertThat(ctxt.getConfiguration()).isNotNull();
        assertThat(ctxt.getWorkingDirectory().asFile()).isEqualTo(f);
        assertThat(ctxt.getArgs()).containsExactly("three");
        assertThat(ctxt.getOptions()).containsExactly(opts.getOption("one"));
        assertThat(ctxt.hasOption("one")).isTrue();
        assertThat(ctxt.hasOption("two")).isFalse();
    }

    @Test
    void optionToStringTest() {
        Option option = mock(Option.class);
        when(option.getOpt()).thenReturn("short");

        String result = ArgumentContext.toString(option);
        assertThat(result).as("short name if no long provided").isEqualTo("Option[short]");

        when(option.getLongOpt()).thenReturn("long");
        result = ArgumentContext.toString(option);
        assertThat(result).as("long name if provided").isEqualTo("Option[long]");

        when(option.hasArg()).thenReturn(true);
        when(option.getLongOpt()).thenReturn(null);
        result = ArgumentContext.toString(option);
        assertThat(result).as("short name if no long provided").isEqualTo("Option[short v:[]]");

        when(option.getLongOpt()).thenReturn("long");
        result = ArgumentContext.toString(option);
        assertThat(result).as("long name if provided").isEqualTo("Option[long v:[]]");

        when(option.getValues()).thenReturn(new String[]{"v1"});
        result = ArgumentContext.toString(option);
        assertThat(result).as("single value").isEqualTo("Option[long v:['v1']]");

        when(option.getValues()).thenReturn(new String[]{"v1", "v2"});
        result = ArgumentContext.toString(option);
        assertThat(result).as("multiple values").isEqualTo("Option[long v:['v1' 'v2']]");
    }

    @Test
    void processArgsTest() throws ParseException {
        Options options = new Options();
        Option one = Option.builder("one").numberOfArgs(3).build();
        options.addOption(one);
        options.addOption(Option.builder("two").build());

        String[] args = {"-one", "three", "four", "five", "six"};

        ArgumentContext ctxt = new ArgumentContext(f, options, args);
        OptionCollectionParserTest.TestOptionCollection testOptionCollection = new OptionCollectionParserTest.TestOptionCollection();
        assertThatNoException().isThrownBy(() -> ctxt.processArgs(testOptionCollection));
    }

    @Test
    void getOptionValueTest() throws ParseException {
        Options opts = new Options();
        Option one = Option.builder("one").numberOfArgs(3).build();
        opts.addOption(one);
        opts.addOption(Option.builder("two").build());

        String[] args = {"-one", "three", "four", "five", "six"};

        ArgumentContext ctxt = new ArgumentContext(f, opts, args);
        assertThat(ctxt.getConfiguration()).isNotNull();
        assertThat(ctxt.getWorkingDirectory().asFile()).isEqualTo(f);
        assertThat(ctxt.getArgs()).containsExactly("six");
        assertThat(ctxt.getOptions()).containsExactly(opts.getOption("one"));
        assertThat(ctxt.hasOption(one)).isTrue();
        assertThat(ctxt.hasOption("two")).isFalse();
        assertThat(ctxt.getOptionValue(one)).isEqualTo("three");
        assertThat(ctxt.getOptionValues(one)).containsExactly("three", "four", "five");

    }

    @Test
    void getParsedOptionValueTest() throws ParseException {
        Options opts = new Options();
        Option one = Option.builder("one").numberOfArgs(2)
                .converter(Converters.TEXT_LIST_CONVERTER).build();
        opts.addOption(one);
        opts.addOption(Option.builder("two").build());

        String[] args = {"-one", "three,four,five", "tres,quatro,cinco", "six"};

        ArgumentContext ctxt = new ArgumentContext(f, opts, args);
        assertThat(ctxt.getConfiguration()).isNotNull();
        assertThat(ctxt.getWorkingDirectory().asFile()).isEqualTo(f);
        assertThat(ctxt.getArgs()).containsExactly("six");
        assertThat(ctxt.getOptions()).containsExactly(opts.getOption("one"));
        assertThat(ctxt.hasOption(one)).isTrue();
        assertThat(ctxt.hasOption("two")).isFalse();
        var o = ctxt.getParsedOptionValue(one);
        assertThat(o).isInstanceOf(List.class);
        List<String> lst = (List<String>) o;
        assertThat(lst).containsExactly("three", "four", "five");

        List<List<String>> lstLst = ctxt.getParsedOptionValues(one);
        assertThat(lstLst).containsExactly(List.of("three", "four", "five"),
                List.of("tres", "quatro", "cinco"));
    }

    @Test
    void parsedOptionValueWithDefaultTest() throws ParseException {
        Options opts = new Options();
        Option one = Option.builder("one").numberOfArgs(3).build();
        opts.addOption(one);
        Option two = Option.builder("two").build();
        opts.addOption(two);

        String[] args = {"-one", "three", "four", "five", "six"};

        ArgumentContext ctxt = new ArgumentContext(f, opts, args);
        assertThat(ctxt.getConfiguration()).isNotNull();
        assertThat(ctxt.getWorkingDirectory().asFile()).isEqualTo(f);
        assertThat(ctxt.getArgs()).containsExactly("six");
        assertThat(ctxt.getOptions()).containsExactly(opts.getOption("one"));
        assertThat(ctxt.hasOption(one)).isTrue();
        assertThat(ctxt.hasOption("two")).isFalse();
        assertThat(ctxt.getParsedOptionValue(two, () -> "explicit default")).isEqualTo("explicit default");
    }

    @Test
    void parsedOptionValuesWithDefaultTest() throws ParseException {
        Options opts = new Options();
        Option one = Option.builder("one").numberOfArgs(2)
                .converter(Converters.TEXT_LIST_CONVERTER).build();
        opts.addOption(one);
        opts.addOption(Option.builder("two").build());

        String[] args = {"six"};

        ArgumentContext ctxt = new ArgumentContext(f, opts, args);
        assertThat(ctxt.getConfiguration()).isNotNull();
        assertThat(ctxt.getWorkingDirectory().asFile()).isEqualTo(f);
        assertThat(ctxt.getArgs()).containsExactly("six");
        assertThat(ctxt.getOptions()).isEmpty();
        assertThat(ctxt.hasOption(one)).isFalse();
        assertThat(ctxt.hasOption("two")).isFalse();

        List<List<String>> lstLst = ctxt.getParsedOptionValues(one, () -> List.of(List.of("three", "four", "five"),
                List.of("tres", "quatro", "cinco")));
        assertThat(lstLst).containsExactly(List.of("three", "four", "five"),
                List.of("tres", "quatro", "cinco"));
    }

    @Test
    void logParseExceptionTest() throws ParseException {
        Options opts = new Options();
        Option one = Option.builder("one").numberOfArgs(3).build();
        opts.addOption(one);
        opts.addOption(Option.builder("two").build());

        String[] args = {"-one", "three", "four", "five", "six"};

        ArgumentContext ctxt = new ArgumentContext(f, opts, args);
        assertThat(ctxt.getConfiguration()).isNotNull();
        assertThat(ctxt.getWorkingDirectory().asFile()).isEqualTo(f);
        assertThat(ctxt.getArgs()).containsExactly("six");
        assertThat(ctxt.getOptions()).containsExactly(opts.getOption("one"));
        assertThat(ctxt.hasOption(one)).isTrue();
        assertThat(ctxt.hasOption("two")).isFalse();
        assertThat(ctxt.getOptionValue(one)).isEqualTo("three");
        assertThat(ctxt.getOptionValues(one)).containsExactly("three", "four", "five");

        Log log = DefaultLog.getInstance();
        try {
            TestingLog testingLog = new TestingLog();
            DefaultLog.setInstance(testingLog);
            ctxt.logParseException(new ParseException("Test exception"), one);
            assertThat(testingLog.getCaptured()).containsOnlyOnce("WARN: Invalid Option[one v:[]] specifically: Test exception");

            testingLog.clear();
            testingLog.setLevel(Log.Level.DEBUG);
            ctxt.logParseException(new ParseException("Test exception2"), one);
            assertThat(testingLog.getCaptured()).containsOnlyOnce("WARN: Invalid Option[one v:[]] specifically: Test exception2")
                    .containsOnlyOnce("DEBUG: org.apache.commons.cli.ParseException: Test exception2");
        } finally {
            DefaultLog.setInstance(log);
        }
    }

    @Test
    void commandLineDescriptionTest() throws ParseException {
        Options opts = new Options();
        Option one = Option.builder("one").numberOfArgs(3).build();
        opts.addOption(one);
        opts.addOption(Option.builder("two").build());

        String[] args = {"-one", "three", "four", "five", "six"};

        ArgumentContext ctxt = new ArgumentContext(f, opts, args);
        String cmdLine = ctxt.commandLineDescription();
        assertThat(cmdLine)
                .isEqualTo("CommandLine[ options: [Option[one v:['three' 'four' 'five']]] args: [six] ]");
    }
}
