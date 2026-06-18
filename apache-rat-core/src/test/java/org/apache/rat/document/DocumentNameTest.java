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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.document.DocumentName.FSInfo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

public class DocumentNameTest {
    private static final FSInfo[] TEST_SUITE = FSInfoTest.TEST_SUITE;

    /**
     * Create a list of mocked files from the specified directory.
     * @param directory the native directory to read.
     * @param fsInfo the file system to mock the files in.
     * @return an array of mocked files in the file system.
     */
    private static File[] listFiles(File directory, FSInfo fsInfo) {
        File[] fileList = directory.listFiles();
        return fileList == null ? null : Arrays.stream(fileList).map(f -> mkFile(f, fsInfo)).toArray(File[]::new);
    }

    /**
     * Create an array of mocked files from the specified directory then apply applying a file filter.
     * @param directory the native directory to read files from.
     * @param fsInfo the file system to create the mocked files in.
     * @param filter the filter to apply to the mocked files.
     * @return the array of mocked files that pass the filter.
     */
    private static File[] listFiles(File directory, FSInfo fsInfo, FileFilter filter) {
        File[] fileList = directory.listFiles();
        return fileList == null ? null : Arrays.stream(fileList).map(f -> mkFile(f, fsInfo)).filter(filter::accept).toArray(File[]::new);
    }

    /**
     * Create an array of mocked files from the specified directory then apply applying a file filter.
     * @param directory the native directory to read files from.
     * @param fsInfo the file system to create the mocked files in.
     * @param filter the filter to apply to the mocked files.
     * @return the array of mocked files that pass the filter.
     */
    private static File[] listFiles(File directory, FSInfo fsInfo, FilenameFilter filter) {
        File[] fileList = directory.listFiles();
        return fileList == null ? null : Arrays.stream(fileList).map(f -> mkFile(f, fsInfo)).filter(x -> filter.accept(x, x.getName())).toArray(File[]::new);
    }

    /**
     * Creates a mocked file on the specified file system with the
     * @param file the name of the native file.
     * @param fsInfo the file system to mock the file in.
     * @return the mocked file in the specified file system
     */
    public static File mkFile(final File file, final FSInfo fsInfo) {
        File mockedFile = mock(File.class);
        when(mockedFile.listFiles()).thenAnswer( env -> listFiles(file, fsInfo));
        when(mockedFile.listFiles(any(FilenameFilter.class))).thenAnswer( env -> listFiles(file, fsInfo, env.getArgument(0, FilenameFilter.class)));
        when(mockedFile.listFiles(any(FileFilter.class))).thenAnswer(env -> listFiles(file, fsInfo, env.getArgument(0, FileFilter.class)));
        when(mockedFile.getName()).thenReturn(ExclusionUtils.convertSeparator(file.getName(), FSInfoTest.DEFAULT.dirSeparator(), fsInfo.dirSeparator()));
        when(mockedFile.getAbsolutePath()).thenReturn(ExclusionUtils.convertSeparator(file.getAbsolutePath(), FSInfoTest.DEFAULT.dirSeparator(), fsInfo.dirSeparator()));
        when(mockedFile.isFile()).thenAnswer(env -> file.isFile());
        when(mockedFile.exists()).thenAnswer(emv -> file.exists());
        when(mockedFile.isDirectory()).thenAnswer(env -> file.isDirectory());

        return mockedFile;
    }

    /**
     * Verifies that {@code resolve()} works correctly.
     * @param fsInfo the file system under test.
     * @param base the DocumentName to resolve from.
     * @param toResolve the string to resolve.
     * @param expected the expected DocumentName after resolution.
     */
    @ParameterizedTest(name = "{index} {0} {1} {3}")
    @MethodSource("resolveTestData")
    void resolveTest(DocumentName.FSInfo fsInfo, String root, DocumentName base, String toResolve, DocumentName expected) {
       DocumentName actual = base.resolve(toResolve);
       assertThat(actual.getName()).isEqualTo(expected.getName());
    }

    private static Stream<Arguments> resolveTestData() {
        List<Arguments> lst = new ArrayList<>();
        DocumentName base;
        DocumentName expected;
        for (DocumentName.FSInfo fsInfo : TEST_SUITE) {
            String root = fsInfo.roots()[0];
            for (String baseName : List.of(root, root + fsInfo.mkPath("from", "base"))) {
                for (String testRoot : fsInfo.roots()) {
                    String name = testRoot + fsInfo.mkPath("", "dir", fsInfo.toString());
                    base = DocumentName.builder(fsInfo).setName(name).setBaseName(baseName).build();
                    lst.add(Arguments.of(fsInfo, base.getName(),  base, null, base));

                    lst.add(Arguments.of(fsInfo, base.getName(), base, "", base));

                    lst.add(Arguments.of(fsInfo, base.getName(), base, "  ", base));

                    expected = DocumentName.builder(fsInfo).setName(fsInfo.mkPath(name, "relative")).setBaseName(baseName).build();
                    lst.add(Arguments.of(fsInfo, base.getName(), base, "relative", expected));

                    expected = DocumentName.builder(fsInfo).setRoot(testRoot).setName(fsInfo.mkPath("", "from", "root")).setBaseName(baseName).build();
                    lst.add(Arguments.of(fsInfo, base.getName(), base, fsInfo.mkPath("", "from", "root"), expected));

                    expected = DocumentName.builder(fsInfo).setRoot(testRoot).setName(fsInfo.mkPath("dir", "up", "and", "down")).setBaseName(baseName).build();
                    lst.add(Arguments.of(fsInfo, base.getName(), base, fsInfo.mkPath("..", "up", "and", "down"), expected));

                    expected = DocumentName.builder(fsInfo).setRoot(testRoot).setName(fsInfo.mkPath("", "from", "root")).setBaseName(baseName).build();
                    String wrongSeparator = fsInfo.dirSeparator().equals("/") ? "\\" : "/";
                    lst.add(Arguments.of(fsInfo, base.getName(), base, String.join(wrongSeparator, "", "from", "root"), expected));

                    expected = DocumentName.builder(fsInfo).setRoot(testRoot).setName(fsInfo.mkPath("dir", "up", "and", "down")).setBaseName(baseName).build();
                    lst.add(Arguments.of(fsInfo, base.getName(), base, String.join(wrongSeparator, "..", "up", "and", "down"), expected));
                }
            }
        }
        return lst.stream();
    }

    @Test
    void resolveWithMultipleRootsTest() {
        // these tests exist because windows has multiple roots.
        DocumentName.FSInfo fsInfo = FSInfoTest.WINDOWS;
        DocumentName base = DocumentName.builder(fsInfo).setName(fsInfo.mkPath("", "dir", fsInfo.toString()))
                .setBaseName("").build();

        String resolveName = fsInfo.roots()[1] + fsInfo.mkPath("dir", fsInfo.toString());
        assertThatThrownBy(() -> base.resolve(resolveName))
                .as(resolveName)
                .hasMessageContaining(String.format("%s does not start with %s", resolveName, base.getName()))
                .isInstanceOf(IllegalArgumentException.class);

        String resolveName2 = fsInfo.roots()[0] + fsInfo.mkPath("dir", fsInfo.toString(), "thing");
        assertThat(base.resolve(resolveName2).getName())
                .isEqualTo(resolveName2);

    }

    void testNoRootSpecified() {
        // no root specified
        assertThat(DocumentName.startsWithRootOrSeparator("X:/candidate", null, "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator("X:/candidate", "", "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator("X;/candidate", "  ", "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator("/candidate", null, "/")).isTrue();
        assertThat(DocumentName.startsWithRootOrSeparator("/candidate", "", "/")).isTrue();
        assertThat(DocumentName.startsWithRootOrSeparator("/candidate", "  ", "/")).isTrue();
        assertThat(DocumentName.startsWithRootOrSeparator("candidate", null, "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator("candidate", "", "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator("candidate", "  ", "/")).isFalse();
    }

    @Test
    void startsWithRootOrSeparatorTest() {
        assertThat(DocumentName.startsWithRootOrSeparator("X:/candidate", "X:/", "/")).isTrue();
        assertThat(DocumentName.startsWithRootOrSeparator("X:/candidate", "Y:/", "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator("/candidate", "X:/", "/")).isTrue();
        assertThat(DocumentName.startsWithRootOrSeparator("\\candidate", "Y:/", "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator("candidate", "Y:/", "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator("Y:candidate", "Y:/", "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator(null, "Y:/", "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator("", "Y:/", "/")).isFalse();
        assertThat(DocumentName.startsWithRootOrSeparator("  ", "Y:/", "/")).isFalse();

        testNoRootSpecified();
    }

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void localizeTest(FSInfo fsInfo) {
        DocumentName documentName = DocumentName.builder(fsInfo).setName(
                fsInfo.mkPath("", "a", "b", "c"))
                .setBaseName(fsInfo.mkPath("", "a")).build();

        assertThat(documentName.localized()).isEqualTo(fsInfo.mkPath("", "b", "c"));
        assertThat(documentName.localized("-")).isEqualTo("-b-c");

        documentName = DocumentName.builder(fsInfo).setName(
                        fsInfo.mkPath("", "a", "b", "c"))
                .setBaseName(fsInfo.mkPath("", "z")).build();

        assertThat(documentName.localized()).isEqualTo(fsInfo.mkPath("", "a", "b", "c"));

        if (fsInfo.roots().length > 1) {
            documentName = DocumentName.builder(fsInfo).setName(
                            fsInfo.mkPath("", "a", "b", "c"))
                    .setBaseName(fsInfo.mkPath("", "z"))
                    .setRoot(fsInfo.roots()[1]).build();
            assertThat(documentName.localized()).isEqualTo(fsInfo.mkPath("", "a", "b", "c"));
        }
    }

    @Test
    void asFileTest() throws IOException {
        File expected = File.createTempFile("docNameTest", ".txt");
        try (FileWriter fw = new FileWriter(expected, StandardCharsets.UTF_8)) {
            fw.write("Hello world");
        }
        DocumentName underTest = DocumentName.builder(expected).build();
        File actual = underTest.asFile();
        try (FileReader fr = new FileReader(actual, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(fr)) {
            assertThat(br.readLine()).isEqualTo("Hello world");
        }
    }

    @Test
    void asPathTest() throws IOException {
        File expected = File.createTempFile("docNameTest", ".txt");
        try (FileWriter fw = new FileWriter(expected, StandardCharsets.UTF_8)) {
            fw.write("Hello world");
        }
        DocumentName underTest = DocumentName.builder(expected).build();
        Path actual = underTest.asPath();
        Path root = Path.of(underTest.getRoot());
        File file = root.resolve(actual).toFile();
        try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(fr)) {
            assertThat(br.readLine()).isEqualTo("Hello world");
        }
    }


    @ParameterizedTest(name = "{index} {0} {1}")
    @MethodSource("archiveEntryTestData")
    void archiveEntryNameTest(String os, String testName, DocumentName archiveName, String root, String separator, String baseName,
                              String localizedName) {
        assertThat(archiveName.getRoot()).as("root").isEqualTo(root);
        assertThat(archiveName.getDirectorySeparator()).as("separator").isEqualTo(separator);
        assertThat(archiveName.getBaseName()).as("baseName").isEqualTo(baseName);
        assertThat(archiveName.localized()).as("localized").isEqualTo(localizedName);
        assertThat(archiveName.getName()).as("name").isEqualTo(baseName + localizedName);
        if (!separator.equals(archiveName.fsInfo().dirSeparator()))
        {
            String newBaseName = separator.equals("/") ? baseName.replace('\\', '/') : baseName.replace('/', '\\');
            assertThat(archiveName.localized(separator)).as("localized(x)").isEqualTo(newBaseName + localizedName);
        }
    }

    static List<Arguments> archiveEntryTestData() {
        List<Arguments> lst = new ArrayList<>();

        for (FSInfo fsInfo :  FSInfoTest.TEST_SUITE) {
            String os = fsInfo.toString();
            String root = fsInfo.roots()[0];
            String baseName = String.format(String.format("%sarchives", root));
            String simpleName = String.format("%sanArchive.zip", fsInfo.dirSeparator());
            String entryName = "./anArchiveEntry.txt";
            DocumentName archiveName = DocumentName.builder(fsInfo).setName(baseName + simpleName).setBaseName(baseName).build();
            lst.add(Arguments.of(os, "archive name", archiveName, root, fsInfo.dirSeparator(), baseName, simpleName));

            ArchiveEntryName archiveEntryName = new ArchiveEntryName(archiveName, entryName);
            baseName = archiveName.getName() + "#";
            root = baseName + "/";
            lst.add(Arguments.of(os, "archive entry name", archiveEntryName, root, "/", baseName, "/anArchiveEntry.txt"));

            // test with directory
            entryName = "./someDir/anArchiveEntry.txt";
            archiveEntryName = new ArchiveEntryName(archiveName, entryName);

            lst.add(Arguments.of(os, "archive entry with directory", archiveEntryName, root, "/", baseName, "/someDir/anArchiveEntry.txt"));
        }
        return lst;
    }
}
