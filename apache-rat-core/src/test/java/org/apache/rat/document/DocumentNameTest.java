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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentNameTest {

    @Test
    public void localizeTest() {
        DocumentName documentName = DocumentName.builder().setName("/a/b/c")
                .setBaseName("/a").setDirSeparator("/").setCaseSensitive(false).build();
        assertEquals("/b/c", documentName.localized());
        assertEquals("-b-c", documentName.localized("-"));
    }

    @ParameterizedTest(name ="{index} {0}")
    @MethodSource("validBuilderData")
    void validBuilderTest(String testName, DocumentName.Builder builder, String root, String name, String baseName, String dirSeparator) {
        DocumentName underTest = builder.build();
        assertThat(underTest.getRoot()).isEqualTo(root);
        assertThat(underTest.getDirectorySeparator()).isEqualTo(dirSeparator);
        assertThat(underTest.getName()).isEqualTo(root+dirSeparator+name);
        assertThat(underTest.getBaseName()).isEqualTo(root+dirSeparator+baseName);
    }

    private static Stream<Arguments> validBuilderData() {
        List<Arguments> lst = new ArrayList<>();
        File f = Files.newTemporaryFile();

        Set<String> roots = new HashSet<String>();
        File[] rootary = File.listRoots();
        if (rootary != null) {
            for (File root : rootary) {
                String name = root.getPath();
                roots.add(name);
            }
        }

        String name = f.getAbsolutePath();
        String root = "";
        for (String sysRoot : roots) {
            if (name.startsWith(sysRoot)) {
                name = name.substring(sysRoot.length());
                if (sysRoot.endsWith(File.separator)) {
                    root = sysRoot.substring(0, sysRoot.length() - File.separator.length());
                }
                break;
            }
        }

        File p = f.getParentFile();
        String baseName = p.getAbsolutePath().substring(root.length());
        if (baseName.startsWith(File.separator)) {
            baseName = baseName.substring(File.separator.length());
        }
        lst.add(Arguments.of("setName(file)", DocumentName.builder().setName(f), root, name, baseName, File.separator));
        lst.add(Arguments.of("Builder(file)", DocumentName.builder(f), root, name, baseName, File.separator));

        lst.add(Arguments.of("setName(dir)", DocumentName.builder().setName(p), root, baseName, baseName, File.separator));
        lst.add(Arguments.of("Builder(dir)", DocumentName.builder(p), root, baseName, baseName, File.separator));

        File r = new File(root.equals("") ? File.separator : root );
        lst.add(Arguments.of("setName(root)", DocumentName.builder().setName(r), root, "", "", File.separator));
        lst.add(Arguments.of("Builder(root)", DocumentName.builder(r), root, "", "", File.separator));

        lst.add(Arguments.of("foo/bar foo", DocumentName.builder()
                .setName("/foo/bar").setBaseName("foo"), "", "foo/bar", "foo", "/"));
        DocumentName.Builder builder = DocumentName.builder().setName("\\foo\\bar").setBaseName("foo")
                .setDirSeparator("\\").setRoot("C:");
        lst.add(Arguments.of("C:\\foo\\bar foo", builder, "C:", "foo\\bar", "foo", "\\"));

        return lst.stream();
    }

}
