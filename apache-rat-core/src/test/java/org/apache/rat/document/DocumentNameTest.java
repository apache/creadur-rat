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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.document.DocumentName.FSInfo;

import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.apache.rat.document.FSInfoTest.OSX;
import static org.apache.rat.document.FSInfoTest.UNIX;
import static org.apache.rat.document.FSInfoTest.WINDOWS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

public class DocumentNameTest {

    public static DocumentName mkName(Path tempDir, FSInfo fsInfo) {
        File docFile = mkFile(tempDir.toFile(), fsInfo);
        DocumentName result = DocumentName.builder(fsInfo).setName(docFile).build();
        DocumentName mocked = Mockito.spy(result);

        String fn = result.localized(FileSystems.getDefault().getSeparator());
        File file = tempDir.resolve(fn.substring(1)).toFile();
        File mockedFile = mkFile(file, fsInfo);
        when(mocked.asFile()).thenReturn(mockedFile);

        assertThat(mocked.asFile()).isEqualTo(mockedFile);
        return mocked;
    }

    private static File[] listFiles(File file, FSInfo fsInfo) {
        File[] fileList = file.listFiles();
        if (fileList == null) {
            return fileList;
        }
        return Arrays.stream(fileList).map(f -> mkFile(f, fsInfo)).toArray(File[]::new);
    }

    private static File[] listFiles(File file, FSInfo fsInfo, FileFilter filter) {
        File[] fileList = file.listFiles();
        if (fileList == null) {
            return fileList;
        }
        return Arrays.stream(fileList).map(f -> mkFile(f, fsInfo)).filter(filter::accept).toArray(File[]::new);
    }

    private static File[] listFiles(File file, FSInfo fsInfo, FilenameFilter filter) {
        File[] fileList = file.listFiles();
        if (fileList == null) {
            return fileList;
        }
        return Arrays.stream(fileList).map(f -> mkFile(f, fsInfo)).filter(x -> filter.accept(x, x.getName())).toArray(File[]::new);
    }

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

    public static DocumentName mkName(Path tempDir, DocumentName baseDir, String pth) throws IOException {
        DocumentName result = baseDir.resolve(ExclusionUtils.convertSeparator(pth, "/", baseDir.getDirectorySeparator()));
        DocumentName mocked = Mockito.spy(result);

        String fn = result.localized(FileSystems.getDefault().getSeparator());
        File file = tempDir.resolve(fn.substring(1)).toFile();
        File parent = file.getParentFile();
        if (parent.exists() && !parent.isDirectory()) {
            parent.delete();
        }
        parent.mkdirs();
        if (file.exists()) {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else {
                FileUtils.delete(file);
            }
        }
        file.createNewFile();
        when(mocked.asFile()).thenReturn(file);
        return mocked;
    }

    @ParameterizedTest(name = "{index} {0} {2}")
    @MethodSource("resolveTestData")
    void resolveTest(String testName, DocumentName base, String toResolve, DocumentName expected) {
       DocumentName actual = base.resolve(toResolve);
       assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> resolveTestData() {
        List<Arguments> lst = new ArrayList<>();

        DocumentName base = DocumentName.builder(UNIX).setName("/dir/unix").setBaseName("/").build();

        DocumentName expected = DocumentName.builder(UNIX).setName("/dir/unix/relative").setBaseName("/").build();
        lst.add(Arguments.of("unix", base, "relative", expected));

        expected = DocumentName.builder(UNIX).setName("/from/root").setBaseName("/").build();
        lst.add(Arguments.of("unix", base, "/from/root", expected));

        expected = DocumentName.builder(UNIX).setName("dir/up/and/down").setBaseName("/").build();
        lst.add(Arguments.of("unix", base, "../up/and/down", expected));

        expected = DocumentName.builder(UNIX).setName("/from/root").setBaseName("/").build();
        lst.add(Arguments.of("unix", base, "\\from\\root", expected));

        expected = DocumentName.builder(UNIX).setName("dir/up/and/down").setBaseName("/").build();
        lst.add(Arguments.of("unix", base, "..\\up\\and\\down", expected));

        // WINDOWS
        base = DocumentName.builder(WINDOWS).setName("\\dir\\windows").setBaseName("C:\\").build();

        expected = DocumentName.builder(WINDOWS).setName("\\dir\\windows\\relative").setBaseName("C:\\").build();
        lst.add(Arguments.of("windows", base, "relative", expected));

        expected = DocumentName.builder(WINDOWS).setName("\\from\\root").setBaseName("C:\\").build();
        lst.add(Arguments.of("windows", base, "/from/root", expected));

        expected = DocumentName.builder(WINDOWS).setName("dir\\up\\and\\down").setBaseName("C:\\").build();
        lst.add(Arguments.of("windows", base, "../up/and/down", expected));

        expected = DocumentName.builder(WINDOWS).setName("\\from\\root").setBaseName("C:\\").build();
        lst.add(Arguments.of("windows", base, "\\from\\root", expected));

        expected = DocumentName.builder(WINDOWS).setName("dir\\up\\and\\down").setBaseName("C:\\").build();
        lst.add(Arguments.of("windows", base, "..\\up\\and\\down", expected));

        // OSX
        base = DocumentName.builder(OSX).setName("/dir/osx").setBaseName("/").build();

        expected = DocumentName.builder(OSX).setName("/dir/osx/relative").setBaseName("/").build();
        lst.add(Arguments.of("osx", base, "relative", expected));

        expected = DocumentName.builder(OSX).setName("/from/root").setBaseName("/").build();
        lst.add(Arguments.of("osx", base, "/from/root", expected));

        expected = DocumentName.builder(OSX).setName("dir/up/and/down").setBaseName("/").build();
        lst.add(Arguments.of("osx", base, "../up/and/down", expected));

        expected = DocumentName.builder(OSX).setName("/from/root").setBaseName("/").build();
        lst.add(Arguments.of("osx", base, "\\from\\root", expected));

        expected = DocumentName.builder(OSX).setName("dir/up/and/down").setBaseName("/").build();
        lst.add(Arguments.of("osx", base, "..\\up\\and\\down", expected));

        return lst.stream();
    }

    @Test
    void localizeTest() {
        DocumentName documentName = DocumentName.builder(UNIX).setName("/a/b/c")
                .setBaseName("/a").build();
        assertThat(documentName.localized()).isEqualTo("/b/c");
        assertThat(documentName.localized("-")).isEqualTo("-b-c");

        documentName = DocumentName.builder(WINDOWS).setName("\\a\\b\\c")
                .setBaseName("\\a").build();
        assertThat(documentName.localized()).isEqualTo("\\b\\c");
        assertThat(documentName.localized("-")).isEqualTo("-b-c");

        documentName = DocumentName.builder(OSX).setName("/a/b/c")
                .setBaseName("/a").build();
        assertThat(documentName.localized()).isEqualTo("/b/c");
        assertThat(documentName.localized("-")).isEqualTo("-b-c");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("validBuilderData")
    void validBuilderTest(String testName, DocumentName.Builder builder, String root, String name, String baseName, String dirSeparator) {
        DocumentName underTest = builder.build();
        assertThat(underTest.getRoot()).as(testName).isEqualTo(root);
        assertThat(underTest.getDirectorySeparator()).as(testName).isEqualTo(dirSeparator);
        assertThat(underTest.getName()).as(testName).isEqualTo(root + dirSeparator + name);
        assertThat(underTest.getBaseName()).as(testName).isEqualTo(root + dirSeparator + baseName);
    }

    private static Stream<Arguments> validBuilderData() {
        List<Arguments> lst = new ArrayList<>();
        File f = Files.newTemporaryFile();

        Set<String> roots = new HashSet<>();
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

        File r = new File(root.isEmpty() ? File.separator : root);
        lst.add(Arguments.of("setName(root)", DocumentName.builder().setName(r), root, "", "", File.separator));
        lst.add(Arguments.of("Builder(root)", DocumentName.builder(r), root, "", "", File.separator));


        lst.add(Arguments.of("foo/bar foo", DocumentName.builder(UNIX)
                .setName("/foo/bar").setBaseName("foo"), "", "foo/bar", "foo", "/"));

        DocumentName.Builder builder = DocumentName.builder(WINDOWS).setName("\\foo\\bar").setBaseName("C:\\foo")
                .setRoot("C:");
        lst.add(Arguments.of("\\foo\\bar foo", builder, "C:", "foo\\bar", "foo", "\\"));

        lst.add(Arguments.of("foo/bar foo", DocumentName.builder(OSX)
                .setName("/foo/bar").setBaseName("foo"), "", "foo/bar", "foo", "/"));

        return lst.stream();
    }

    @Test
    void splitRootsTest() {
        Pair<String, String> result = DocumentName.builder(WINDOWS).splitRoot("C:\\My\\path\\to\\a\\file.txt");
        assertThat(result.getLeft()).isEqualTo("C:");
        assertThat(result.getRight()).isEqualTo("My\\path\\to\\a\\file.txt");

        result = DocumentName.builder(UNIX).splitRoot("/My/path/to/a/file.txt");
        assertThat(result.getLeft()).isEqualTo("");
        assertThat(result.getRight()).isEqualTo("My/path/to/a/file.txt");

        result = DocumentName.builder(OSX).splitRoot("/My/path/to/a/file.txt");
        assertThat(result.getLeft()).isEqualTo("");
        assertThat(result.getRight()).isEqualTo("My/path/to/a/file.txt");
    }

    @Test
    void archiveEntryNameTest() {
        String entryName = "./anArchiveEntry.txt";
        DocumentName archiveName = DocumentName.builder(WINDOWS)
                .setName("C:\\archives\\anArchive.zip").setBaseName("C:\\archives").build();

        assertThat(archiveName.getRoot()).isEqualTo("C:");
        assertThat(archiveName.getDirectorySeparator()).isEqualTo("\\");
        assertThat(archiveName.getBaseName()).isEqualTo("C:\\archives");
        assertThat(archiveName.getName()).isEqualTo("C:\\archives\\anArchive.zip");
        assertThat(archiveName.localized()).isEqualTo("\\anArchive.zip");

        ArchiveEntryName archiveEntryName = new ArchiveEntryName(archiveName, entryName);

        assertThat(archiveEntryName.getRoot()).isEqualTo(archiveName.getName()+"#");
        assertThat(archiveEntryName.getDirectorySeparator()).isEqualTo("/");
        assertThat(archiveEntryName.getBaseName()).isEqualTo("C:\\archives\\anArchive.zip#");
        assertThat(archiveEntryName.getName()).isEqualTo("C:\\archives\\anArchive.zip#/anArchiveEntry.txt");
        assertThat(archiveEntryName.localized()).isEqualTo("/anArchiveEntry.txt");
        assertThat(archiveEntryName.localized("/")).isEqualTo("/anArchive.zip#/anArchiveEntry.txt");

        // test with directory
        entryName = "./someDir/anArchiveEntry.txt";
        archiveEntryName = new ArchiveEntryName(archiveName, entryName);

        assertThat(archiveEntryName.getRoot()).isEqualTo(archiveName.getName()+"#");
        assertThat(archiveEntryName.getDirectorySeparator()).isEqualTo("/");
        assertThat(archiveEntryName.getBaseName()).isEqualTo("C:\\archives\\anArchive.zip#");
        assertThat(archiveEntryName.getName()).isEqualTo("C:\\archives\\anArchive.zip#/someDir/anArchiveEntry.txt");
        assertThat(archiveEntryName.localized()).isEqualTo("/someDir/anArchiveEntry.txt");
        assertThat(archiveEntryName.localized("/")).isEqualTo("/anArchive.zip#/someDir/anArchiveEntry.txt");
    }
}
