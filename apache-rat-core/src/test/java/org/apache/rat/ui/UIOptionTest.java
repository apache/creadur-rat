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

import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.rat.OptionCollection;
import org.apache.rat.TestOption;
import org.apache.rat.utils.CasedString;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UIOptionTest {
    private UIOption<UIOptionCollectionTest.TestingUIOption> underTest;
    private UIOptionCollectionTest.TestingUIOptionCollection optionCollection;

    @Test
    void cleanup() {
        optionCollection = new UIOptionCollectionTest.TestingUIOptionCollection();
        underTest = new UIOptionCollectionTest.TestingUIOption.TestingUIOptionBuilder().option(new Option("a", false, "An option"))
                .optionCollection(optionCollection).build();
        String s = underTest.cleanup("The name is --output-licenses because I said so");
        assertThat(s).isEqualTo("The name is output.licenses because I said so");

        s = underTest.cleanup("The name is -A because I said so");
        assertThat(s).isEqualTo("The name is addLicense because I said so");
    }

    @Test
    void equalsTest() {
        TestOption opt = new TestOption.TestOptionBuilder().option(Option.builder("a").hasArg().build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        TestOption opt2 = new TestOption.TestOptionBuilder().option(Option.builder("a").hasArg().build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt).isEqualTo(opt2)
                .hasSameHashCodeAs(opt2)
                .isNotEqualTo(null);

        opt2 = new TestOption.TestOptionBuilder().option(Option.builder("a").hasArg()
                        .argName("file")
                .build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();
        assertThat(opt).isNotEqualTo(opt2)
                .hasSameHashCodeAs(opt2);

        opt2 = new TestOption.TestOptionBuilder() {
            @Override
            protected Function<Option, CasedString> getNameFactory() {
                return o -> new CasedString(CasedString.StringCase.CAMEL, "helloWorld");
            }
        }.option(Option.builder("a").hasArg()
                        .build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();
        assertThat(opt).isNotEqualTo(opt2)
                .doesNotHaveSameHashCodeAs(opt2);
    }

    @Test
    void defaultValueTest() {
        UIOptionCollection<TestOption> collection = mock(UIOptionCollection.class);
        TestOption opt = new TestOption.TestOptionBuilder().option(Option.builder("a").hasArg().build())
                .optionCollection(collection)
                .build();

        assertThat(opt.getDefaultValue()).isNull();

        when(collection.defaultValue(any(Option.class))).thenReturn("yeehaw");
        opt = new TestOption.TestOptionBuilder().option(Option.builder("a").hasArg().build())
                .optionCollection(collection)
                .build();
        assertThat(opt.getDefaultValue()).isEqualTo("yeehaw");
    }

    @Test
    void getCasedName() {
        TestOption opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").hasArg().build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();
        CasedString casedName = opt.getCasedName();
        assertThat(casedName).hasToString("hello-world");
    }

    @Test
    void getDescriptionName() {
        TestOption opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").hasArg().build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();
        assertThat(opt.getDescription()).isNull();

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").hasArg()
                .desc("This is the description").build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();
        assertThat(opt.getDescription()).isEqualTo("This is the description");
    }

    @Test
    void getArgName() {
        TestOption opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.getArgName()).isEmpty();

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").hasArg().build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.getArgName()).isEqualTo("Arg");

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").hasArg()
                .argName("file").build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.getArgName()).isEqualTo("File");

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").hasArg()
                        .argName("dummy").build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.getArgName()).isEqualTo("Arg");

    }

    @Test
    void getArgType() {
        TestOption opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.getArgType()).isEqualTo(OptionCollection.ArgumentType.NONE);

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").hasArg().build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.getArgType()).isEqualTo(OptionCollection.ArgumentType.ARG);

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").hasArg()
                        .argName("file").build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.getArgType()).isEqualTo(OptionCollection.ArgumentType.FILE);

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world").hasArg()
                        .argName("dummy").build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.getArgType()).isEqualTo(OptionCollection.ArgumentType.ARG);

    }

    @Test
    void isRequired() {
        TestOption opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world")
                .hasArg().build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.isRequired()).isFalse();

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world")
                        .hasArg().required(true).build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.isRequired()).isTrue();
    }

    @Test
    void argCheck() {
        TestOption opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world")
                        .build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.hasArg()).isFalse();
        assertThat(opt.hasArgs()).isFalse();

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world")
                        .hasArg().build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();
        assertThat(opt.hasArg()).isTrue();
        assertThat(opt.hasArgs()).isFalse();

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world")
                        .hasArgs().build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();
        assertThat(opt.hasArg()).isTrue();
        assertThat(opt.hasArgs()).isTrue();
    }

    @Test
    void getDeprecated() {
        TestOption opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world")
                        .build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();

        assertThat(opt.getDeprecated()).isEmpty();

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world")
                .deprecated().build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();
        assertThat(opt.getDeprecated()).isEqualTo("Deprecated");

        opt = new TestOption.TestOptionBuilder().option(Option.builder("hello-world")
                        .deprecated(new DeprecatedAttributes.Builder().setSince("fádo fádo").get()).build())
                .optionCollection(mock(UIOptionCollection.class))
                .build();
        assertThat(opt.getDeprecated()).isEqualTo("Deprecated since fádo fádo");

    }

}
