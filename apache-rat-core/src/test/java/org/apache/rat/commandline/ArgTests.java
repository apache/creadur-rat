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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.rat.CLIOptionCollection;
import org.apache.rat.DeprecationReporter;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.document.DocumentName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgTests {

    private CommandLine createCommandLine(String[] args) throws ParseException {
        Options opts = OptionCollection.buildOptions();
        return DefaultParser.builder().setDeprecatedHandler(DeprecationReporter.getLogReporter())
                .setAllowPartialMatching(true).build().parse(opts, args);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "rat.txt", "./rat.txt", "/rat.txt", "target/rat.test" })
    public void outputFileNameNoDirectoryTest(String name) throws ParseException {
        class OutputFileConfig extends ReportConfiguration {
            private File actual = null;

            @Override
            public void setOut(File file) {
                actual = file;
            }
        }

        DocumentName.FSInfo fsInfo = DocumentName.FSInfo.getDefault();
        String fileName = name.replace("/", fsInfo.dirSeparator());
        File localFile = new File(".");
        DocumentName localFileName = DocumentName.builder(localFile).build();
        Path workingPath = localFile.getAbsoluteFile().toPath();
        String expected = fsInfo.normalize(workingPath.resolve("./" + fileName).toString());

        OutputFileConfig configuration = new OutputFileConfig();
        ArgumentContext ctxt = new ArgumentContext(localFile, configuration, CLIOptionCollection.INSTANCE.getOptions(), new String[]{"--output-file", fileName});
        Arg.processArgs(ctxt, CLIOptionCollection.INSTANCE);
        if (name.equals("/rat.txt")) {
            assertThat(fsInfo.normalize(configuration.actual.getAbsolutePath())).isEqualTo(localFileName.getRoot() + "rat.txt");
        } else {
            assertThat(fsInfo.normalize(configuration.actual.toString())).isEqualTo(expected);
        }
    }
}
