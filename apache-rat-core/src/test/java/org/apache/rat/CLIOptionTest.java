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
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class CLIOptionTest {
    final CLIOptionCollection optionCollection = CLIOptionCollection.INSTANCE;
    final CLIOption optionA = new CLIOption(CLIOptionCollection.INSTANCE, new Option("a", false, "short key"));
    final CLIOption optionB = new CLIOption(CLIOptionCollection.INSTANCE, Option.builder("b").longOpt("bee").hasArg().desc("two key").build());
    final CLIOption optionC = new CLIOption(CLIOptionCollection.INSTANCE, Option.builder().longOpt("sea").hasArgs().type(File.class).desc("long key").build());
    final CLIOption optionD = new CLIOption(CLIOptionCollection.INSTANCE, Option.builder().longOpt("dee").hasArgs().argName("dede").desc("long key").build());

    @Test
    void getText() {
        assertThat(optionA.getText()).isEqualTo("-a");
        assertThat(optionB.getText()).isEqualTo("--bee or -b");
        assertThat(optionC.getText()).isEqualTo("--sea");
    }

    @Test
    void cleanupName() {
        assertThat(optionA.cleanupName(optionA.getOption())).isEqualTo("a");
        assertThat(optionA.cleanupName(optionB.getOption())).isEqualTo("bee");
        assertThat(optionA.cleanupName(optionC.getOption())).isEqualTo("sea");
    }

    @Test
    void getExample() {
        assertThat(optionA.getExample()).isEqualTo("-a");
        assertThat(optionB.getExample()).isEqualTo("--bee Arg");
        assertThat(optionC.getExample()).isEqualTo("--sea Arg [Arg2 [Arg3 [...]]] --");
        assertThat(optionD.getExample()).isEqualTo("--dee dede [dede2 [dede3 [...]]] --");
    }
}
