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
import javax.print.Doc;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DocumentNameTest {

    @Test
    public void localizeTest() {
        DocumentName documentName = DocumentName.builder().setName("/a/b/c")
                .setBaseName("/a").setDirSeparator("/").setCaseSensitive(false).build();
        assertThat(documentName.localized()).isEqualTo("/b/c");
        assertThat(documentName.localized("-")).isEqualTo("-b-c");
    }

    @ParameterizedTest(name ="{index} {0}")
    @MethodSource("validBuilderData")
    void validBuilderTest(String testName, DocumentName.Builder builder, String root, String name, String baseName, String dirSeparator) {
        DocumentName underTest = builder.build();
        assertThat(underTest.getRoot()).as(testName).isEqualTo(root);
        assertThat(underTest.getDirectorySeparator()).as(testName).isEqualTo(dirSeparator);
        assertThat(underTest.getName()).as(testName).isEqualTo(root+dirSeparator+name);
        assertThat(underTest.getBaseName()).as(testName).isEqualTo(root+dirSeparator+baseName);
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

        lst.add(Arguments.of("foo/bar foo", DocumentName.builder().setDirSeparator("/")
                .setName("/foo/bar").setBaseName("foo"), "", "foo/bar", "foo", "/"));
        DocumentName.Builder builder = DocumentName.builder().setDirSeparator("\\").setName("\\foo\\bar").setBaseName("foo")
                .setRoot("C:");
        lst.add(Arguments.of("C:\\foo\\bar foo", builder, "C:", "foo\\bar", "foo", "\\"));

        return lst.stream();
    }

    @Test
    public void splitRootsTest() {
        Set<String> preserve = new HashSet<>(DocumentName.ROOTS);
        try {
            DocumentName.ROOTS.clear();
            DocumentName.ROOTS.add("C:\\");
            Pair<String, String> result = DocumentName.Builder.splitRoot("C:\\My\\path\\to\\a\\file.txt", "\\");
            assertThat(result.getLeft()).isEqualTo("C:");
            assertThat(result.getRight()).isEqualTo("My\\path\\to\\a\\file.txt");


            DocumentName.ROOTS.clear();
            DocumentName.ROOTS.add("/");
            result = DocumentName.Builder.splitRoot("/My/path/to/a/file.txt", "/");
            assertThat(result.getLeft()).isEqualTo("");
            assertThat(result.getRight()).isEqualTo("My/path/to/a/file.txt");

        } finally {
            DocumentName.ROOTS.clear();
            DocumentName.ROOTS.addAll(preserve);
        }
    }

    @Test
    public void archiveEntryNameTest() {
        Set<String> preserve = new HashSet<>(DocumentName.ROOTS);
        try {
            DocumentName.ROOTS.clear();
            DocumentName.ROOTS.add("C:\\");

            DocumentName archiveName = DocumentName.builder().setDirSeparator("\\")
                    .setName("C:\\archives\\anArchive.zip").setBaseName("archives").build();
            assertThat(archiveName.getRoot()).isEqualTo("C:");
            assertThat(archiveName.getDirectorySeparator()).isEqualTo("\\");
            assertThat(archiveName.getBaseName()).isEqualTo("C:\\archives");
            assertThat(archiveName.getName()).isEqualTo("C:\\archives\\anArchive.zip");
            assertThat(archiveName.localized()).isEqualTo("\\anArchive.zip");

            String entryName = "./anArchiveEntry.txt";
            DocumentName innerName = DocumentName.builder()
                    .setDirSeparator("/").setName(entryName)
                    .setBaseName(".").setCaseSensitive(true).build();
            assertThat(innerName.getRoot()).isEqualTo("");
            assertThat(innerName.getDirectorySeparator()).isEqualTo("/");
            assertThat(innerName.getBaseName()).isEqualTo("/.");
            assertThat(innerName.getName()).isEqualTo("/" + entryName);
            assertThat(innerName.localized()).isEqualTo("/anArchiveEntry.txt");

            String outerNameStr = format("%s#%s", archiveName.getName(), entryName);
            DocumentName outerName = DocumentName.builder(archiveName).setName(outerNameStr)
                    .setCaseSensitive(innerName.isCaseSensitive()).build();

            assertThat(outerName.getRoot()).isEqualTo("C:");
            assertThat(outerName.getDirectorySeparator()).isEqualTo("\\");
            assertThat(outerName.getBaseName()).isEqualTo("C:\\archives");
            assertThat(outerName.getName()).isEqualTo("C:\\archives\\anArchive.zip#./anArchiveEntry.txt");
            assertThat(outerName.localized()).isEqualTo("\\anArchive.zip#./anArchiveEntry.txt");
            assertThat(outerName.localized("/")).isEqualTo("/anArchive.zip#./anArchiveEntry.txt");

            // test with directory
            entryName = "./someDir/anArchiveEntry.txt";
            innerName = DocumentName.builder()
                    .setDirSeparator("/").setName(entryName)
                    .setBaseName(".").setCaseSensitive(true).build();
            assertThat(innerName.getRoot()).isEqualTo("");
            assertThat(innerName.getDirectorySeparator()).isEqualTo("/");
            assertThat(innerName.getBaseName()).isEqualTo("/.");
            assertThat(innerName.getName()).isEqualTo("/" + entryName);
            assertThat(innerName.localized()).isEqualTo("/someDir/anArchiveEntry.txt");

            outerNameStr = format("%s#%s", archiveName.getName(), entryName);
            outerName = DocumentName.builder(archiveName).setName(outerNameStr)
                    .setCaseSensitive(innerName.isCaseSensitive()).build();

            assertThat(outerName.getRoot()).isEqualTo("C:");
            assertThat(outerName.getDirectorySeparator()).isEqualTo("\\");
            assertThat(outerName.getBaseName()).isEqualTo("C:\\archives");
            assertThat(outerName.getName()).isEqualTo("C:\\archives\\anArchive.zip#./someDir/anArchiveEntry.txt");
            assertThat(outerName.localized()).isEqualTo("\\anArchive.zip#./someDir/anArchiveEntry.txt");
            assertThat(outerName.localized("/")).isEqualTo("/anArchive.zip#./someDir/anArchiveEntry.txt");
        } finally {
            DocumentName.ROOTS.clear();
            DocumentName.ROOTS.addAll(preserve);
        }
    }
}
