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
package org.apache.rat.cli;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rat.OptionCollection;
import org.apache.rat.testhelpers.TextUtils;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelpTest {
    @Test
    public void verifyAllOptionsListed() {
        Options opts = OptionCollection.buildOptions(CLIOption.ADDITIONAL_OPTIONS);
        StringWriter out = new StringWriter();
        new Help(out).printUsage(opts);

        String result = out.toString();

        for (Option option : opts.getOptions()) {
            if (option.getOpt() != null) {
                TextUtils.assertContains("-" + option.getOpt() + (option.getLongOpt() == null ? " " : ","), result);
            }
            if (option.getLongOpt() != null) {
                TextUtils.assertContains("--" + option.getLongOpt() + " ", result);
            }
        }

        assertThat(result).doesNotContain("..");
    }

    @Test
    public void verifyArgumentsListed() {
        Options opts = OptionCollection.buildOptions(CLIOption.ADDITIONAL_OPTIONS);
        Set<String> argTypes = Arrays.stream(OptionCollection.ArgumentType.values()).map(OptionCollection.ArgumentType::getDisplayName).collect(Collectors.toSet());
        StringWriter out = new StringWriter();
        new Help(out).printUsage(opts);
        String result = out.toString();

        for (Option option : opts.getOptions()) {
            if (option.getArgName() != null) {
                assertTrue(argTypes.contains(option.getArgName()), () -> format("Argument '%s' is missing from list", option.getArgName()));
                TextUtils.assertPatternInTarget(format("^<%s>", option.getArgName()), result);
            }
        }
        assertThat(result).doesNotContain("..");
    }
}
