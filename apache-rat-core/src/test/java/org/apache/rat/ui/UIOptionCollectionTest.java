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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.rat.commandline.Arg;
import org.apache.rat.utils.CasedString;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UIOptionCollectionTest {

    public static final Option UI_OPTION = Option.builder("ui-option1").build();
    public static final Option DEPRECATED_UI_OPTION = Option.builder("ui-option2").deprecated().build();

    static class TestingUIOptionCollection extends UIOptionCollection<TestingUIOption> {
        public TestingUIOptionCollection() {
            super(new Builder());

        }
        private static class Builder extends UIOptionCollection.Builder<TestingUIOption, Builder> {
            Builder() {
                super(TestingUIOption::new);
                        uiOption(UI_OPTION)
                        .uiOption(DEPRECATED_UI_OPTION)
                        .unsupported(Arg.COUNTER_MAX)
                        .unsupported(Arg.EXCLUDE.option())
                        .defaultValue(UI_OPTION, "foo");

            }
        }
    }

    static class TestingUIOption extends UIOption<TestingUIOption> {

        TestingUIOption(final UIOptionCollection<TestingUIOption> collection, final Option option) {
            super(collection, option, ArgumentTracker.extractName(option).as(CasedString.StringCase.DOT));
        }

        @Override
        protected String cleanupName(Option option) {
            return new CasedString(CasedString.StringCase.KEBAB, ArgumentTracker.extractKey(option)).toCase(CasedString.StringCase.DOT);
        }

        @Override
        public String getExample() {
            return "The example for " + cleanupName(option);
        }

        @Override
        public String getText() {
            return "Short and long options for " + cleanupName(option);
        }
    }

    private final TestingUIOptionCollection underTest = new TestingUIOptionCollection();

    @Test
    void getMappedOption() {
        TestingUIOption one = underTest.getMappedOption(UI_OPTION).get();
        assertThat(one.option).isEqualTo(UI_OPTION);
        assertThat(one.getName()).isEqualTo("ui.option1");
        assertThat(one.isDeprecated()).isFalse();
        TestingUIOption two = underTest.getMappedOption(DEPRECATED_UI_OPTION).get();
        assertThat(two.option).isEqualTo(DEPRECATED_UI_OPTION);
        assertThat(two.getName()).isEqualTo("ui.option2");
        assertThat(two.isDeprecated()).isTrue();

        assertThat(underTest.getMappedOption(Arg.EXCLUDE.option())).isEmpty();
        for (Option option : Arg.COUNTER_MAX.group().getOptions()) {
            assertThat(underTest.getMappedOption(option)).isEmpty();
        }

        Optional<TestingUIOption> optConfig = underTest.getMappedOption(Arg.CONFIGURATION.option());
        assertThat(optConfig).isPresent();
        TestingUIOption config = optConfig.get();
        assertThat(config.option).isEqualTo(Arg.CONFIGURATION.option());
        assertThat(config.getName()).isEqualTo("config");

        optConfig = underTest.getMappedOption(Option.builder("foo").build());
        assertThat(optConfig).isEmpty();
    }

    @Test
    void getSelected() throws AlreadySelectedException {
        assertThat(underTest.isSelected(Arg.CONFIGURATION)).isFalse();
        assertThat(underTest.getSelected(Arg.CONFIGURATION)).isEmpty();

        Option option = Arg.CONFIGURATION.option();
        OptionGroup group = underTest.getOptions().getOptionGroup(Arg.CONFIGURATION.option());
        group.setSelected(option);

        assertThat(underTest.isSelected(Arg.CONFIGURATION)).isTrue();
        assertThat(underTest.getSelected(Arg.CONFIGURATION)).contains(option);
    }

    @Test
    void getUnsupportedOptions() {
        Options options = underTest.getUnsupportedOptions();
        List<Option> expected = new ArrayList<Option>();
        expected.addAll(Arg.COUNTER_MAX.group().getOptions());
        expected.add(Arg.EXCLUDE.option());
        assertThat(options.getOptions()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void getAdditionalOptions() {
        assertThat(underTest.additionalOptions().getOptions()).containsExactlyInAnyOrder(UI_OPTION, DEPRECATED_UI_OPTION);
    }

    @Test
    void defaultValue() {
        assertThat(underTest.defaultValue(UI_OPTION)).isEqualTo("foo");
        assertThat(underTest.defaultValue(DEPRECATED_UI_OPTION)).isNull();
    }
}
