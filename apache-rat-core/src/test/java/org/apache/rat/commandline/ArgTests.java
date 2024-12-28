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
package org.apache.rat.commandline;

import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.rat.DeprecationReporter;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgTests {

    private CommandLine createCommandLine(String[] args) throws ParseException {
        Options opts = OptionCollection.buildOptions();
        return DefaultParser.builder().setDeprecatedHandler(DeprecationReporter.getLogReporter())
                .setAllowPartialMatching(true).build().parse(opts, args);
    }

    @ParameterizedTest
    @ValueSource(strings = { "rat.txt", "./rat.txt", "/rat.txt", "target/rat.test" })
    public void outputFleNameNoDirectoryTest(String name) throws ParseException, IOException {
        class OutputFileConfig extends ReportConfiguration  {
            private File actual = null;
            @Override
            public void setOut(File file) {
                actual = file;
            }
        }

        File expected = new File(name);

        CommandLine commandLine = createCommandLine(new String[] {"--output-file", name});
        OutputFileConfig configuration = new OutputFileConfig();
        ArgumentContext ctxt = new ArgumentContext(new File("."), configuration, commandLine);
        Arg.processArgs(ctxt);
        assertThat(configuration.actual.getAbsolutePath()).isEqualTo(expected.getCanonicalPath());
    }
}
