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
package org.apache.rat.document;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FSInfoTest {
    public static final DocumentName.FSInfo DEFAULT;
    public static final DocumentName.FSInfo OSX;
    public static final DocumentName.FSInfo UNIX;
    public static final DocumentName.FSInfo WINDOWS;

    static {
        try (FileSystem osx = Jimfs.newFileSystem(Configuration.osX());
             FileSystem unix = Jimfs.newFileSystem(Configuration.unix());
             FileSystem windows = Jimfs.newFileSystem(Configuration.windows().toBuilder()
                     .setRoots("C:\\", "D:\\").build())) {
            OSX = new DocumentName.FSInfo("osx", osx);
            UNIX = new DocumentName.FSInfo("unix", unix);
            WINDOWS = new DocumentName.FSInfo("windows", windows);
            DEFAULT = new DocumentName.FSInfo("default", FileSystems.getDefault());
        } catch (IOException e) {
            throw new RuntimeException("Unable to creat FSInfo objects: " + e.getMessage(), e);
        }
    }

    public static final DocumentName.FSInfo[] TEST_SUITE = {UNIX, WINDOWS, OSX};

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void rootForRespectsCaseSensitivity(boolean caseSensitive) {
        DocumentName.FSInfo info = new DocumentName.FSInfo("test", "\\", caseSensitive, List.of("C:\\", "F:\\"));

        assertThat(info.rootFor("F:\\workspace\\target\\rat.txt")).contains("F:\\");
        assertThat(info.rootFor("f:\\workspace\\target\\rat.txt"))
                .isEqualTo(caseSensitive ? Optional.empty() : Optional.of("F:\\"));
        assertThat(info.rootFor("c:\\workspace\\target\\rat.txt"))
                .isEqualTo(caseSensitive ? Optional.empty() : Optional.of("C:\\"));
        assertThat(info.rootFor("G:\\workspace\\target\\rat.txt")).isEmpty();
        assertThat(info.rootFor("target\\rat.txt")).isEmpty();
    }

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void normalizeTest(DocumentName.FSInfo info) {
        String sep = info.dirSeparator();
        Map<String, String> strings = new TreeMap<>();
        strings.put("hello", "hello");
        strings.put("hello/", "hello");
        strings.put("/hello", sep+"hello");
        strings.put("/hello/../goodbye", sep+"goodbye");
        strings.put("hello/../goodbye", "goodbye");
        strings.put("/hello/./goodbye", sep+"hello"+sep+"goodbye");
        strings.put("hello/./goodbye", "hello"+sep+"goodbye");
        strings.put("\\hello\\..\\goodbye", sep+"goodbye");
        strings.put("hello\\..\\goodbye", "goodbye");
        strings.put("\\hello\\.\\goodbye", sep+"hello"+sep+"goodbye");
        strings.put("hello\\.\\goodbye", "hello"+sep+"goodbye");

        strings.put(info.roots()[0]+"hello", info.roots()[0]+"hello");
        String pattern = "a/b\\c";
        strings.put(pattern, pattern.replace(sep.equals("/") ? "\\" : "/", sep));
        strings.put("", "");
        strings.put(" ", "");
        strings.put(".", "");
        strings.put(" .", "");
        strings.put(" . ", "");
        strings.put("/", sep);
        strings.put("\\", sep);

        assertThat(info.normalize(null)).as("[null]").isEmpty();
        for (Map.Entry<String, String> entry : strings.entrySet()) {
            assertThat(info.normalize(entry.getKey())).as(String.format("[%s]", entry.getKey()))
                    .isEqualTo(entry.getValue());
        }

        for (String pattern2 : List.of( "/..", "hello/../..", "/hello/../..")) {
                assertThatThrownBy(() -> info.normalize(pattern2)).as(String.format("[%s]", pattern2))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Unable to create path before root");
                }
    }

    static List<Arguments> tokenizingData() {
        List<Arguments> data = new ArrayList<>();
        for (DocumentName.FSInfo fsInfo : TEST_SUITE) {
            String sep = fsInfo.dirSeparator();
            String notSep = sep.equals("/") ? "\\" : "/";
            data.add(Arguments.of(fsInfo, "hello", List.of("hello")));
            data.add(Arguments.of(fsInfo, "hello" + sep, List.of("hello")));
            data.add(Arguments.of(fsInfo, "hello" + sep + "world", List.of("hello", "world")));
            data.add(Arguments.of(fsInfo, sep + "hello", List.of("", "hello")));
            data.add(Arguments.of(fsInfo, sep + "hello" + sep + ".." + sep + "goodbye", List.of("", "hello", "..", "goodbye")));
            data.add(Arguments.of(fsInfo, "hello" + sep + ".." + sep + "goodbye", List.of("hello", "..", "goodbye")));
            data.add(Arguments.of(fsInfo, notSep + "hello" + sep + "goodbye", List.of(notSep + "hello", "goodbye")));
        }
        return data;
    }

    @ParameterizedTest
    @MethodSource("tokenizingData")
    void tokenizeTest(DocumentName.FSInfo info, String token, List<String> split) {
            assertThat(info.tokenize(token))
                    .containsExactlyElementsOf(split);
    }

    @ParameterizedTest
    @MethodSource("tokenizingData")
    void mkPathTest(DocumentName.FSInfo info, String path, List<String> segments) {
        if (path.endsWith(info.dirSeparator())) {
            assertThat(info.mkPath(segments.toArray(new String[0]))).isEqualTo(path.substring(0, path.length()-1));
        } else {
            assertThat(info.mkPath(segments.toArray(new String[0]))).isEqualTo(path);
        }
    }

    @Test
    void compareTests() {
        for (int i = 0; i < TEST_SUITE.length; i++) {
            for (int j = 0; j < TEST_SUITE.length; j++) {
                DocumentName.FSInfo underTest = TEST_SUITE[i];
                DocumentName.FSInfo compareTo = TEST_SUITE[j];
                String name = String.format("%s with %s ", underTest, compareTo);
                if (i == j) {
                    assertThat(underTest).as(name + "equality").isEqualTo(compareTo);
                    assertThat(underTest.hashCode()).as(name + "hashCode").hasSameHashCodeAs(compareTo);
                    assertThat(underTest).as(name + "compareTo").isEqualByComparingTo(compareTo);
                } else {
                    assertThat(underTest).as(name + "equality").isNotEqualTo(compareTo);
                    assertThat(underTest).as(name + "compareTo").isNotEqualByComparingTo(compareTo);
                }
            }
        }
    }
}
