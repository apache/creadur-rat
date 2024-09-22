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
package org.apache.rat.help;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rat.OptionCollection;
import org.apache.rat.Report;
import org.apache.rat.testhelpers.TextUtils;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelpTest {
    @Test
    public void verifyAllOptionsListed() {
        Options opts = OptionCollection.buildOptions();
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
    }

    @Test
    public void verifyArgumentsListed() {
        Options opts = OptionCollection.buildOptions();
        Set<String> argTypes = OptionCollection.getArgumentTypes().keySet();
        StringWriter out = new StringWriter();
        new Help(out).printUsage(opts);

        String result = out.toString();

        for (Option option : opts.getOptions()) {
            if (option.getArgName() != null) {
                assertTrue(argTypes.contains(option.getArgName()), () -> format("Argument 's' is missing from list", option.getArgName()));
                TextUtils.assertPatternInTarget(format("^<%s>", option.getArgName()), result);
            }
        }
    }
}
