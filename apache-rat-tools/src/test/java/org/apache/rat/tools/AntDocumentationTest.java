/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.tools;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.documentation.options.AntOptionCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class AntDocumentationTest {

    private AntOptionCollection optionCollection;
    private AntDocumentation underTest;
    private Path testPath;
    private final Options options = new Options().addOption("one", false, "first option")
            .addOption("two", true, "hasOption")
            .addOption(Option.builder().longOpt("three").hasArgs().desc("third option").build());

    @BeforeEach
    void setup(@TempDir(cleanup = CleanupMode.NEVER) Path path) {
        optionCollection = AntOptionCollection.INSTANCE;
        testPath = path;
        underTest = new AntDocumentation(path.toFile());
    }

    @Test
    void writeAttributes() throws IOException {
        List<AntOption> antOptions = options.getOptions().stream().map(opt -> new AntOption(optionCollection, opt)).toList();
        underTest.writeAttributes(antOptions);
        File result = new File(testPath.toFile(), "report_attributes.txt");
        assertThat(result).exists();
        List<String> lines = IOUtils.readLines(new FileInputStream(result), StandardCharsets.UTF_8);
        assertThat(lines).contains("| one | first option | boolean | false |", "| two | hasOption | Arg | false |");
        assertThat(lines).doesNotContainSubsequence("three");
    }

    @Test
    void writeElements() throws IOException {
        List<AntOption> antOptions = options.getOptions().stream().map(opt -> new AntOption(optionCollection, opt)).toList();
        underTest.writeElements(antOptions);
        File result = new File(testPath.toFile(), "report_elements.txt");
        assertThat(result).exists();
        List<String> lines = IOUtils.readLines(new FileInputStream(result), StandardCharsets.UTF_8);
        assertThat(lines).contains("| threes | third option | Arg | false |");
        assertThat(lines).doesNotContainSubsequence("one", "two");
    }
}
