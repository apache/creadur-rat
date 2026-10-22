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

import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.rat.commandline.Arg;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.utils.CasedString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class MavenOptionCollectionTest {

    @Test
    void deprecatedOptionTest() {
        Optional<MavenOption> opt = new MavenOptionCollection().getMappedOption(Arg.EDIT_COPYRIGHT.find("copyright"));
        assertThat(opt).isPresent();
        assertThat(opt.get().getDeprecated()).contains("<editCopyright>");

        Option deprecatedOption = Option.builder().longOpt("dep-opt")
                .deprecated(DeprecatedAttributes.builder()
                        .setDescription("Yep it is deprecated use --other-opt instead")
                        .setForRemoval(true)
                        .setSince("1.0")
                        .get())
                .desc("This is the --dep-opt description that talks about --other-opt.")
                .build();

        Option otherOpt = Option.builder().longOpt("other-opt")
                .desc("this is the --other-opt description.").build();

        try {

            MavenOptionCollection collection = new MavenOptionCollection.Builder()
                    .uiOption(deprecatedOption)
                    .uiOption(otherOpt)
                    .build();

            Collection<Option> opts = collection.getOptions().getOptions();
            assertThat(opts).contains(otherOpt);
            assertThat(opts).contains(deprecatedOption);

            Optional<MavenOption> depOpt = collection.getMappedOption(deprecatedOption);
            assertThat(depOpt).isPresent();
            MavenOption depMaven = depOpt.get();
            Optional<MavenOption> otherMaven = collection.getMappedOption(otherOpt);
            assertThat(otherMaven).isPresent();

            assertThat(depMaven.getDeprecated()).contains("<otherOpt>");
            assertThat(depMaven.getMethodName()).isEqualTo("setDepOpt");
            assertThat(depMaven.getDescription()).isEqualTo("This is the <depOpt> description that talks about <otherOpt>.");
            assertThat(depMaven.getName()).isEqualTo("depOpt");
        } finally {
            assertThat(new MavenOptionCollection().additionalOptions().getOptions()).isEmpty();
        }
    }


    @ParameterizedTest
    @MethodSource("namingTestData")
    public void namingTest(String expectedName, Option option) {
        String mavenName = new MavenOptionCollection().rename(option);
        assertThat(mavenName).isEqualTo(expectedName);
    }

    static Stream<Arguments> namingTestData() {
        List<Arguments> lst  = new ArrayList<>();
        Option option = Option.builder().longOpt("multiple-args").hasArgs().build();
        lst.add(Arguments.of("multipleArgs", option));
        option = Option.builder().longOpt("multiple-arg-single-name").hasArgs().build();
        lst.add(Arguments.of("multipleArgSingleNames", option));
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
