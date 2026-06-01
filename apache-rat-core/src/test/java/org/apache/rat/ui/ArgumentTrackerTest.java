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
package org.apache.rat.ui;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.cli.Option;
import org.apache.rat.commandline.Arg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.apache.rat.ui.UIOptionCollectionTest.TestingUIOptionCollection;
import static org.apache.rat.ui.UIOptionCollectionTest.TestingUIOption;

class ArgumentTrackerTest {
    private ArgumentTracker underTest;
    private TestingUIOptionCollection testingUIOptionCollection;

    @BeforeEach
    void setUp() {
        testingUIOptionCollection = new TestingUIOptionCollection();
        underTest = new ArgumentTracker(testingUIOptionCollection);
    }

    @Test
    void extractKey() {
        assertThat(ArgumentTracker.extractKey(Option.builder().longOpt("foo").build())).isEqualTo("foo");
        assertThat(ArgumentTracker.extractKey(Option.builder("b").build())).isEqualTo("b");
        assertThat(ArgumentTracker.extractKey(Option.builder("b").longOpt("foo").build())).isEqualTo("foo");
        assertThatThrownBy(() -> ArgumentTracker.extractKey(Option.builder().build())) // NOSONAR
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Either opt or longOpt must be specified");
    }

    @Test
    void args() {
        // no args to start
        assertThat(underTest.args()).isEmpty();
        Option option = findOptionWithArgs(1);
        String string1 = String.format("--%s foo", ArgumentTracker.extractKey(option));
        TestingUIOption mappedOption = testingUIOptionCollection.getMappedOption(option).get();
        underTest.setArg(mappedOption, "foo");
        option = findOptionWithArgs(2);
        String string2 = String.format("--%s bar baz", ArgumentTracker.extractKey(option));
        mappedOption = testingUIOptionCollection.getMappedOption(option).get();
        underTest.addArg(mappedOption, "bar");
        underTest.addArg(mappedOption, "baz");
        String join = String.join(" ", underTest.args());
        assertThat(join).contains(string1, string2);
    }

    @Test
    void setArg() {
        Option option = findOptionWithArgs(1);
        TestingUIOption mappedOption = testingUIOptionCollection.getMappedOption(option).get();
        underTest.setArg(mappedOption, "foo");
        assertThat(underTest.getArg(mappedOption.keyValue())).contains(List.of("foo"));
    }

    private Option findOptionWithArgs(int number) {
        Predicate<Option> filter;
        if (number <= 0) {
            filter = opt -> !opt.hasArg();
        } else if (number == 1) {
            filter = opt -> opt.hasArg() && ! opt.hasArgs();
        } else {
            filter = Option::hasArgs;
        }
        return Arrays.stream(Arg.values()).map(Arg::option).filter(filter).findAny().orElseThrow();
    }

    @Test
    void addArg() {
        Option option = findOptionWithArgs(2);
        TestingUIOption mappedOption = testingUIOptionCollection.getMappedOption(option).get();
        underTest.addArg(mappedOption, "foo");
        assertThat(underTest.getArg(mappedOption.keyValue())).contains(List.of("foo"));
        underTest.addArg(mappedOption, "bar");
        assertThat(underTest.getArg(mappedOption.keyValue())).contains(List.of("foo", "bar"));
    }

    @Test
    void setOverridesAddArg() {
        Option option = findOptionWithArgs(2);
        TestingUIOption mappedOption = testingUIOptionCollection.getMappedOption(option).get();
        underTest.addArg(mappedOption, "foo");
        assertThat(underTest.getArg(mappedOption.keyValue())).contains(List.of("foo"));
        underTest.addArg(mappedOption, "bar");
        assertThat(underTest.getArg(mappedOption.keyValue())).contains(List.of("foo", "bar"));
        underTest.setArg(mappedOption, "baz");
        assertThat(underTest.getArg(mappedOption.keyValue())).contains(List.of("baz"));
    }

    @Test
    void invalidAbstractOption() {
        Option option = Option.builder().longOpt("notAValidOption").build();
        TestingUIOption invalidOption = new TestingUIOption(testingUIOptionCollection, option);
        underTest.addArg(invalidOption, "foo");
        assertThat(underTest.getArg(invalidOption.keyValue())).isEmpty();
    }

}
