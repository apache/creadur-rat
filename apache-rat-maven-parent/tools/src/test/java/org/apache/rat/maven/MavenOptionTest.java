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

package org.apache.rat.maven;

import org.apache.commons.cli.Option;
import org.apache.rat.commandline.Arg;

import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.utils.CasedString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class MavenOptionTest {
    private static final String ESCAPED_DOUBLE_DASH = Pattern.quote("--");

    @Test
    void getDeprecatedTest() {
        MavenOptionCollection mavenOptionCollection = new MavenOptionCollection();
        for (Option option : Arg.getOptions().getOptions()) {
            if (option.isDeprecated()) {
                mavenOptionCollection.getMappedOption(option).ifPresent( mavenOption -> //
                        assertThat(mavenOption.getDeprecated()).doesNotContainPattern(ESCAPED_DOUBLE_DASH));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("namingTestData")
    public void namingTest(String expectedName, Option option) {
        CasedString mavenName = MavenOption.MavenOptionBuilder.createName(ArgumentTracker.extractKey(option));
        assertThat(mavenName.toString()).isEqualTo(expectedName);
    }

    static Stream<Arguments> namingTestData() {
        List<Arguments> lst  = new ArrayList<>();
        Option option = Option.builder().longOpt("multiple-args").hasArgs().build();
        lst.add(Arguments.of("multipleArgs", option));
        option = Option.builder().longOpt("multiple-arg-single-name").hasArgs().build();
        // this method does not change the pluralization.  That is done in the Collection test
        lst.add(Arguments.of("multipleArgSingleName", option));
        option = Option.builder().longOpt("multiple-args-approved").hasArgs().build();
        lst.add(Arguments.of("multipleArgsApproved", option));
        option = Option.builder().longOpt("multiple-args-denied").hasArgs().build();
        lst.add(Arguments.of("multipleArgsDenied", option));

        option = Option.builder().longOpt("single-args").hasArg().build();
        lst.add(Arguments.of("singleArgs", option));
        option = Option.builder().longOpt("single-arg-single-name").hasArg().build();
        lst.add(Arguments.of("singleArgSingleName", option));
        option = Option.builder().longOpt("single-arg-approved").hasArg().build();
        lst.add(Arguments.of("singleArgApproved", option));
        option = Option.builder().longOpt("single-args-denied").hasArg().build();
        lst.add(Arguments.of("singleArgsDenied", option));

        option = Option.builder().longOpt("no-args").build();
        lst.add(Arguments.of("noArgs", option));
        option = Option.builder().longOpt("no-arg-single-name").build();
        lst.add(Arguments.of("noArgSingleName", option));
        option = Option.builder().longOpt("no-args-approved").build();
        lst.add(Arguments.of("noArgsApproved", option));
        option = Option.builder().longOpt("no-args-denied").build();
        lst.add(Arguments.of("noArgsDenied", option));

        return lst.stream();
    }

}
