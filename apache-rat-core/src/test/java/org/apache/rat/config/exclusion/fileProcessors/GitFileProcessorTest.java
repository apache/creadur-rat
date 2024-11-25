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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.utils.ExtendedIterator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitFileProcessorTest extends AbstractIgnoreProcessorTest {

    @Test
    public void processExampleFileTest() throws IOException {
        String[] lines = {
                "# somethings",
                "!thingone", "thing*", System.lineSeparator(),
                "# some fish",
                "**/fish", "*_fish",
                "# some colorful directories",
                "red/", "blue/*/"};

        List<String> expected = ExtendedIterator.create(Arrays.asList("**/thing*", "**/fish", "**/*_fish").iterator())
                .map(s -> new File(baseDir, s).getPath()).addTo(new ArrayList<>());
        expected.add(0, "!"+new File(baseDir, "**/thingone").getPath());
        // "thingone",
        writeFile(".gitignore", Arrays.asList(lines));

        GitFileProcessor processor = new GitFileProcessor();
        List<String> actual = processor.apply(baseName);
        assertEquals(expected, actual);
        actual.clear();
        processor.customDocumentNameMatchers().forEach(x -> actual.add(x.toString()));
        expected.clear();
        ExtendedIterator.create(Arrays.asList("**/red", "blue/*").iterator())
                .map(s -> new File(baseDir, s).getPath()).map(s -> String.format("and(isDirectory, %s)", s))
                        .forEachRemaining(expected::add);
        assertEquals(expected, actual);

    }

    // see https://git-scm.com/docs/gitignore
    @ParameterizedTest
    @MethodSource("modifyEntryData")
    public void modifyEntryTest(String source, String expected) throws IOException {
        GitFileProcessor underTest = new GitFileProcessor();
        DocumentName testName = DocumentName.builder().setName("GitFileProcessorTest").setBaseName("testDir").build();
        if (source.endsWith("/")) {
            assertThat(underTest.modifyEntry(testName, source)).isEqualTo(null);
            Iterator<DocumentNameMatcher> iter = underTest.customDocumentNameMatchers().iterator();
            assertThat(iter).hasNext();
            assertThat(iter.next().toString()).isEqualTo(String.format("and(isDirectory, %s)", expected));
        } else {
            assertThat(underTest.modifyEntry(testName, source)).isEqualTo(expected);
            assertThat(underTest.customDocumentNameMatchers().iterator().hasNext()).isFalse();
        }
    }

    private static Stream<Arguments> modifyEntryData() {
        List<Arguments> lst = new ArrayList<>();

        lst.add(Arguments.of("\\#filename", "**/#filename"));

        lst.add(Arguments.of("!#filename", "!**/#filename"));
        lst.add(Arguments.of("\\#filename", "**/#filename"));
        lst.add(Arguments.of("!#filename", "!**/#filename"));
        lst.add(Arguments.of("/filename", "filename"));
        lst.add(Arguments.of("file/name", "file/name"));
        lst.add(Arguments.of("/file/name", "file/name"));
        lst.add(Arguments.of("filename", "**/filename"));
        lst.add(Arguments.of("filename/", "/testDir/**/filename"));
        lst.add(Arguments.of("/filename/", "/testDir/filename"));

        return lst.stream();
    }
}
