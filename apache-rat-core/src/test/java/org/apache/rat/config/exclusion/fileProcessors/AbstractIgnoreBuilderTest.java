/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.config.exclusion.fileProcessors;

import java.nio.file.Path;
import java.util.List;
import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.config.exclusion.plexus.SelectorUtils;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.DocumentNameMatcherTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The base class for FileProcessor builder tests.
 * Provides supporting methods for creating test files and for validating results.
 */
public class AbstractIgnoreBuilderTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    protected Path tmpPath;
    protected DocumentName baseName;

    @BeforeEach
    protected void setup() {
        baseName = DocumentName.builder(tmpPath.toFile()).build();
    }

    @AfterEach
    @EnabledOnOs(OS.WINDOWS)
    void reset() {
        baseName = null;
    }

    /**
     * Writes a text file to the baseDir directory.
     * @param name the name of the file.
     * @param lines the lines to write to the file.
     * @return the File that was written.
     * @throws IOException if file cannot be created.
     */
    protected File writeFile(String name, Iterable<String> lines) throws IOException {
        File file = new File(tmpPath.toFile(), name);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            lines.forEach(writer::println);
        }
        return file;
    }

    /**
     * Asserts the correctness of the excluder. An excluder returns false if the document name is matched.
     * @param builder An FileProcessorBuilder that will create the excluder.
     * @param matching the matching strings.
     * @param notMatching the non-matching strings.
     */
    protected void assertCorrect(AbstractFileProcessorBuilder builder, Iterable<String> matching, Iterable<String> notMatching) {
        assertCorrect(builder.build(baseName), baseName, matching, notMatching);
    }

    /**
     * Asserts the correctness of the matcher.
     * @param matcherSets the list of matchers to create the DocumentNameMatcher from.
     * @param baseDir the base directory for the excluder test.
     * @param matching the matching strings (i.e. that should be ignored)
     * @param notMatching the non-matching strings (i.e. that should be checked)
     */
    protected void assertCorrect(List<MatcherSet> matcherSets, DocumentName baseDir, Iterable<String> matching, Iterable<String> notMatching) {
        // An excluder returns false if the document name is matched.
        DocumentNameMatcher excluder = MatcherSet.merge(matcherSets).createMatcher();
        for (String name : matching) {
            DocumentName docName = baseDir.resolve(SelectorUtils.extractPattern(name, baseDir.getDirectorySeparator()));
            assertThat(excluder.matches(docName)).as(() -> DocumentNameMatcherTest.processDecompose(excluder, docName)).isFalse();
        }
        for (String name : notMatching) {
            DocumentName docName = baseDir.resolve(SelectorUtils.extractPattern(name, baseDir.getDirectorySeparator()));
            assertThat(excluder.matches(docName)).as(() -> DocumentNameMatcherTest.processDecompose(excluder, docName)).isTrue();
        }
    }
}
