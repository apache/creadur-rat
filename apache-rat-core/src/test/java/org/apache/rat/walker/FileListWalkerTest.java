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
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FileDocument;
import org.apache.rat.report.RatReport;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileListWalkerTest {

    private ReportConfiguration reportConfiguration;
    private static File source;
    private static DocumentName tempName;
    private static DocumentName regularName;
    private static DocumentName hiddenName;


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
        tempName = new DocumentName(tempDir);

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
        regularName = new DocumentName(new File(regular, ".hiddenFile"), tempName).changeDirectorySeparator("/");

        File hidden = new File(tempDir, ".hidden");
        hidden.mkdir();
        fileWriter(hidden, "regularFile", "regular file");
        fileWriter(hidden, ".hiddenFile", "hidden file");
        hiddenName = new DocumentName(new File(hidden, "regularFile"), tempName).changeDirectorySeparator("/");

        source = new File(tempDir, "source.txt");
        try (FileWriter writer = new FileWriter(source)) {
            writer.write(hidden.getPath()+"/regularFile");
            writer.write(System.lineSeparator());
            writer.write("regular/.hiddenFile");
            writer.write(System.lineSeparator());
            writer.flush();
        }
        tempName = new DocumentName(tempDir);
    }
    
    @Test
    public void readFilesTest() throws RatException {
        DocumentName name = new DocumentName(source, new DocumentName(source.getParentFile()));
        FileListWalker walker = new FileListWalker(new FileDocument(name, source, x -> true));
        List<String> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));
        String[] expected = {regularName.getName(), hiddenName.getName()};
        assertEquals(2, scanned.size());
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
