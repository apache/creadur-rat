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
package org.apache.rat.walker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.FileDocument;
import org.apache.rat.document.DocumentName;
import org.apache.rat.report.RatReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectoryWalkerTest {

    private ReportConfiguration reportConfiguration;

    @TempDir
    private static File tempDir;

    private static void fileWriter(File dir, String name, String contents) throws IOException {
        try (FileWriter writer = new FileWriter(new File(dir, name))) {
            writer.write(contents);
            writer.flush();
        }
    }

    @BeforeEach
    public void beforeEach() {
        reportConfiguration = new ReportConfiguration();
    }

    public Document toWalk() {
        DocumentName documentName = DocumentName.builder(tempDir).build();
        return new FileDocument(documentName, tempDir, reportConfiguration.getDocumentExcluder(documentName));
    }

    @BeforeAll
    public static void setUp() throws Exception {

        /*
        Create a directory structure like this:

            regular
                regularFile
                .hiddenFile
            .hidden
                regularFile
                .hiddenFile
         */
        File regular = new File(tempDir, "regular");
        regular.mkdir();
        fileWriter(regular, "regularFile", "regular file");
        fileWriter(regular, ".hiddenFile", "hidden file");

        File hidden = new File(tempDir, ".hidden");
        hidden.mkdir();
        fileWriter(hidden, "regularFile", "regular file");
        fileWriter(hidden, ".hiddenFile", "hidden file");
    }
    
    @Test
    public void noFiltersTest() throws RatException {
        DirectoryWalker walker = new DirectoryWalker(toWalk());
        List<Document> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));
        String[] expected = {"/regular/regularFile", "/regular/.hiddenFile", "/.hidden/regularFile", "/.hidden/.hiddenFile"};
        List<String> actual = scanned.stream().filter(d -> !d.isIgnored()).map(d -> d.getName().localized("/")).collect(Collectors.toList());
        assertThat(actual).size().isEqualTo(4);
        for (String ex : expected) {
            assertThat(actual).as(()-> String.format("Missing %s", ex)).contains(ex);
        }
    }

    @Test
    public void noHiddenFileFiltersTest() throws RatException {
        reportConfiguration.addExcludedCollection(StandardCollection.HIDDEN_FILE);
        DirectoryWalker walker = new DirectoryWalker(toWalk());
        List<Document> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));
        List<String> actual = scanned.stream().filter(d -> !d.isIgnored()).map(d -> d.getName().localized("/")).collect(Collectors.toList());
        String[] expected = {"/regular/regularFile", "/.hidden/regularFile"};
        assertThat(actual.size()).isEqualTo(2);
        for (String ex : expected) {
            assertThat(actual).as(()-> String.format("Missing %s", ex)).contains(ex);
        }

        actual = scanned.stream().filter(Document::isIgnored).map(d -> d.getName().localized("/")).collect(Collectors.toList());
        expected = new String[] {"/regular/.hiddenFile", "/.hidden/.hiddenFile"};
        assertThat(actual.size()).isEqualTo(2);
        for (String ex : expected) {
            assertThat(actual).as(()-> String.format("Missing ignored %s", ex)).contains(ex);
        }
    }

    @Test
    public void noHiddenDirectoryFiltersTest() throws RatException {
        reportConfiguration.addExcludedCollection(StandardCollection.HIDDEN_DIR);
        DirectoryWalker walker = new DirectoryWalker(toWalk());
        List<Document> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));
        List<String> actual = scanned.stream().filter(d -> !d.isIgnored()).map(d -> d.getName().localized("/")).collect(Collectors.toList());
        String[] expected = {"/regular/regularFile", "/regular/.hiddenFile"};
        assertThat(actual.size()).isEqualTo(2);
        for (String ex : expected) {
            assertThat(actual).as(()-> String.format("Missing %s", ex)).contains(ex);
        }

        List<Document> excluded = scanned.stream().filter(Document::isIgnored).collect(Collectors.toList());
        assertThat(excluded.size()).isEqualTo(1);
        Document d = excluded.get(0);
        assertThat(d.getName().localized("/")).isEqualTo("/.hidden");
        assertThat(d.isIgnored()).isTrue();
    }

    @Test
    public void noHiddenDirectoryAndNoHiddenFileFiltersTest() throws RatException {
        reportConfiguration.addExcludedCollection(StandardCollection.HIDDEN_DIR);
        reportConfiguration.addExcludedCollection(StandardCollection.HIDDEN_FILE);
        DirectoryWalker walker = new DirectoryWalker(toWalk());
        List<Document> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));
        List<String> actual = scanned.stream().filter(d -> !d.isIgnored()).map(d -> d.getName().localized("/")).collect(Collectors.toList());
        String[] expected = {"/regular/regularFile"};
        assertThat(actual.size()).isEqualTo(1);
        for (String ex : expected) {
            assertThat(actual).as(()-> String.format("Missing %s", ex)).contains(ex);
        }
    }

    static class TestRatReport implements RatReport {

        private final List<Document> scanned;

        public TestRatReport(List<Document> scanned) {
            this.scanned = scanned;
        }

        @Override
        public void startReport() {
            // no-op
        }

        @Override
        public void report(Document document) {
            scanned.add(document);
        }

        @Override
        public void endReport() {
            // no-op
        }
    }
}
