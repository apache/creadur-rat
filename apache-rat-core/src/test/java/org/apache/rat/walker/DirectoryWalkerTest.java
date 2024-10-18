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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.impl.FileDocument;
import org.apache.rat.document.impl.DocumentName;
import org.apache.rat.report.RatReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
        DocumentName documentName = new DocumentName(tempDir);
        return new FileDocument(documentName, tempDir, reportConfiguration.getNameMatcher(documentName));
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
    public void noFiltersTest() throws IOException, RatException {
        DirectoryWalker walker = new DirectoryWalker(toWalk());
        List<String> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));
        String[] expected = {"/regular/regularFile", "/regular/.hiddenFile", "/.hidden/regularFile", "/.hidden/.hiddenFile"};
        assertEquals(4, scanned.size());
        for (String ex : expected) {
            assertTrue(scanned.contains(ex), ()-> String.format("Missing %s", ex));
        }
    }

    @Test
    public void noHiddenFileFiltersTest() throws IOException, RatException {
        reportConfiguration.addExcludedCollection(StandardCollection.HIDDEN_FILE);
        DirectoryWalker walker = new DirectoryWalker(toWalk());
        List<String> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));
        String[] expected = {"/regular/regularFile", "/.hidden/regularFile"};
        assertEquals(2, scanned.size());
        for (String ex : expected) {
            assertTrue(scanned.contains(ex), ()-> String.format("Missing %s", ex));
        }
    }

    @Test
    public void noHiddenDirectoryFiltersTest() throws IOException, RatException {
        reportConfiguration.addExcludedCollection(StandardCollection.HIDDEN_DIR);
        DirectoryWalker walker = new DirectoryWalker(toWalk());
        List<String> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));
        String[] expected = {"/regular/regularFile", "/regular/.hiddenFile"};
         assertEquals(2, scanned.size());
        for (String ex : expected) {
            assertTrue(scanned.contains(ex), ()-> String.format("Missing %s", ex));
        }
    }

    @Test
    public void noHiddenDirectoryAndNoHiddenFileFiltersTest() throws IOException, RatException {
        reportConfiguration.addExcludedCollection(StandardCollection.HIDDEN_DIR);
        reportConfiguration.addExcludedCollection(StandardCollection.HIDDEN_FILE);
        DirectoryWalker walker = new DirectoryWalker(toWalk());
        List<String> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));
        String[] expected = {"/regular/regularFile"};
        assertEquals(1, scanned.size());
        for (String ex : expected) {
            assertTrue(scanned.contains(ex), ()-> String.format("Missing %s", ex));
        }
    }

    static class TestRatReport implements RatReport {

        private List<String> scanned;

        public TestRatReport(List<String> scanned) {
            this.scanned = scanned;
        }

        @Override
        public void startReport() {
            // no-op
        }

        @Override
        public void report(Document document) throws RatException {
            scanned.add(document.getName().localized("/"));
        }

        @Override
        public void endReport() {
            // no-op
        }
    }
}
