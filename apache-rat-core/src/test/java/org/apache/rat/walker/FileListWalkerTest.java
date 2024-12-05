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
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.report.RatReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileListWalkerTest {

    private static File source;
    private static DocumentName regularName;
    private static DocumentName hiddenName;
    private static DocumentName anotherName;

    @TempDir
    private static File tempDir;

    private static File fileWriter(File dir, String name, String contents) throws IOException {
        File result = new File(dir, name);
        try (FileWriter writer = new FileWriter(result)) {
            writer.write(contents);
            writer.flush();
        }
        return result;
    }

    @BeforeAll
    public static void setUp() throws Exception {
        /*
        Create a directory structure like this:
            working
                source.txt
                regular
                    regularFile
                    .hiddenFile
                .hidden
                    regularFile
                    .hiddenFile
            other
                anotherFile
         */
        File working = new File(tempDir, "working");
        working.mkdir();
        File other = new File(tempDir, "other");
        other.mkdir();
        File anotherFile = fileWriter(other, "anotherFile", "just another file");
        File p = anotherFile;
        while (p.getParentFile() != null) {
            p = p.getParentFile();
        }
        DocumentName rootName = DocumentName.builder(p).build();
        anotherName = DocumentName.builder(anotherFile).setBaseName(p).build();


        source = new File(working, "source.txt");
        DocumentName sourceName = DocumentName.builder(source).setBaseName(working.getAbsolutePath()).build();
        File regular = new File(working, "regular");
        regular.mkdir();
        fileWriter(regular, "regularFile", "regular file");
        fileWriter(regular, ".hiddenFile", "hidden file");
        regularName = DocumentName.builder(new File(regular, ".hiddenFile")).setBaseName(rootName.getBaseName()).build();

        File hidden = new File(working, ".hidden");
        hidden.mkdir();
        fileWriter(hidden, "regularFile", "regular file");
        fileWriter(hidden, ".hiddenFile", "hidden file");
        File hiddenFile = new File(hidden, "regularFile");
        hiddenName = DocumentName.builder(hiddenFile).setBaseName(sourceName.getBaseName()).build();


        source = new File(working, "source.txt");

        try (FileWriter writer = new FileWriter(source)) {
            writer.write(regularName.localized("/"));
            writer.write(System.lineSeparator());
            writer.write(hiddenName.localized("/").substring(1));
            writer.write(System.lineSeparator());
            writer.write(anotherName.localized("/"));
            writer.write(System.lineSeparator());
            writer.flush();
            System.out.flush();
        }

        hiddenName = DocumentName.builder(hiddenFile).setBaseName(rootName.getBaseName()).build();
    }
    
    @Test
    public void readFilesTest() throws RatException {
        FileListWalker walker = new FileListWalker(new FileDocument(source, DocumentNameMatcher.MATCHES_ALL));
        List<String> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));
        String[] expected = {regularName.localized("/"), hiddenName.localized("/"),
        anotherName.localized("/")};
        assertEquals(3, scanned.size());
        for (String ex : expected) {
            assertTrue(scanned.contains(ex), ()-> String.format("Missing %s from %s", ex, String.join(", ", scanned)));
        }
    }

    static class TestRatReport implements RatReport {

        private final List<String> scanned;

        public TestRatReport(List<String> scanned) {
            this.scanned = scanned;
        }

        @Override
        public void startReport() {
            // no-op
        }

        @Override
        public void report(Document document) {
            scanned.add(document.getName().localized("/"));
        }

        @Override
        public void endReport() {
            // no-op
        }
    }
}
