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
package org.apache.rat.config.exclusion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FSInfoTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class FileProcessorTest {

    FileProcessorTest() {}

    @ParameterizedTest(name="{index} {1}")
    @MethodSource("localizePatternData")
    void localizePatternTest(DocumentName baseName, String pattern, String expectedStr) {
        assertThat(ExclusionUtils.localizePattern(baseName, pattern)).isEqualTo(expectedStr);
    }

    public static Stream<Arguments> localizePatternData() throws IOException {
        List<Arguments> lst = new ArrayList<>();
        Map<String, String> patterns = new HashMap<>();
        patterns.put("file", "%1$sbaseDir%1$sfile");
        patterns.put("!file", "!%1$sbaseDir%1$sfile");
        patterns.put("%regex[file]", "%%regex[\\Q%1$sbaseDir%1$s\\Efile]");
        patterns.put("!%regex[file]", "!%%regex[\\Q%1$sbaseDir%1$s\\Efile]");
        patterns.put("%ant[file]", "%1$sbaseDir%1$sfile");
        patterns.put("!%ant[file]", "!%1$sbaseDir%1$sfile");

        patterns.put("file/**", "%1$sbaseDir%1$sfile%1$s**");
        patterns.put("!file/**", "!%1$sbaseDir%1$sfile%1$s**");
        patterns.put("%regex[file/.*]", "%%regex[\\Q%1$sbaseDir%1$s\\Efile/.*]");
        patterns.put("!%regex[file/.*]", "!%%regex[\\Q%1$sbaseDir%1$s\\Efile/.*]");
        patterns.put("%ant[file/**]", "%1$sbaseDir%1$sfile%1$s**");
        patterns.put("!%ant[file/**]", "!%1$sbaseDir%1$sfile%1$s**");


        DocumentName baseName = DocumentName.builder(FSInfoTest.UNIX).setName("fileBeingRead").setBaseName("baseDir").build();
        for (Map.Entry<String, String> pattern : patterns.entrySet()) {
            lst.add(Arguments.of(baseName, pattern.getKey(), String.format(pattern.getValue(), UNIX.dirSeparator())));
        }


        baseName = DocumentName.builder(FSInfoTest.WINDOWS).setName("fileBeingRead").setBaseName("baseDir").build();
        for (Map.Entry<String, String> pattern : patterns.entrySet()) {
            lst.add(Arguments.of(baseName, pattern.getKey(), String.format(pattern.getValue(), WINDOWS.dirSeparator())));
        }

        baseName = DocumentName.builder(FSInfoTest.OSX).setName("fileBeingRead").setBaseName("baseDir").build();
        for (Map.Entry<String, String> pattern : patterns.entrySet()) {
            lst.add(Arguments.of(baseName, pattern.getKey(), String.format(pattern.getValue(), OSX.dirSeparator())));
        }
        return lst.stream();
    }
}
