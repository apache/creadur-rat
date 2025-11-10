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
package org.apache.rat.tools;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.IOUtils;
import org.apache.rat.testhelpers.TextUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class NamingTest {

    private final File file = new File("target/testNaming.txt");

    @BeforeEach
    void cleanUpTestData() {
        file.delete();
    }

    @Test
    public void testAnt() throws IOException, ParseException {
        Naming.main(new String[]{"--ant", file.getAbsolutePath()});
        String result = readFile(file);
        TextUtils.assertContains("Ant", result);
        TextUtils.assertContains("Description", result);
        TextUtils.assertNotContains("Maven", result);
        TextUtils.assertNotContains("CLI", result);
        TextUtils.assertNotContains("[Deprecated ", result);
    }

    @Test
    public void testMaven() throws IOException, ParseException {
        Naming.main(new String[]{"--maven", file.getAbsolutePath()});
        String result = readFile(file);
        TextUtils.assertContains("Maven", result);
        TextUtils.assertContains("Description", result);
        TextUtils.assertNotContains("Ant", result);
        TextUtils.assertNotContains("CLI", result);
        TextUtils.assertNotContains("[Deprecated ", result);
    }

    @Test
    public void testCli() throws IOException, ParseException {
        Naming.main(new String[]{"--cli", file.getAbsolutePath()});
        String result = readFile(file);
        TextUtils.assertContains("CLI", result);
        TextUtils.assertContains("Description", result);
        TextUtils.assertNotContains("Ant", result);
        TextUtils.assertNotContains("Maven", result);
        TextUtils.assertNotContains("[Deprecated ", result);
    }

    @Test
    @Disabled("No deprecated options exist at this time")
    public void testCliDeprecated() throws IOException, ParseException {
        Naming.main(new String[]{"--cli", "--include-deprecated", file.getAbsolutePath()});
        String result = readFile(file);
        TextUtils.assertContains("CLI", result);
        TextUtils.assertContains("Description", result);
        TextUtils.assertNotContains("Maven", result);
        TextUtils.assertNotContains("Ant", result);
        TextUtils.assertContains("[Deprecated ", result);
    }

    @Test
    public void testAntCsv() throws IOException, ParseException {
        Naming.main(new String[]{"--ant", "--csv", file.getAbsolutePath()});
        String result = readFile(file);
        TextUtils.assertContains("Ant", result);
        TextUtils.assertContains("Description", result);
        TextUtils.assertNotContains("Maven", result);
        TextUtils.assertNotContains("CLI", result);
        TextUtils.assertNotContains("[Deprecated ", result);

        try (CSVParser parser = readCSV(file)) {
            assertContains("Ant", parser.getHeaderNames());
            assertContains("Description", parser.getHeaderNames());
            assertNotContains("Maven", parser.getHeaderNames());
            assertNotContains("CLI", parser.getHeaderNames());
            assertNotContains("[Deprecated ", parser.getHeaderNames());
        }
    }

    @Test
    public void testMavenCsv() throws IOException, ParseException {
        Naming.main(new String[]{"--maven", "--csv", file.getAbsolutePath()});
        String result = readFile(file);
        TextUtils.assertContains("Maven", result);
        TextUtils.assertContains("Description", result);
        TextUtils.assertNotContains("Ant", result);
        TextUtils.assertNotContains("CLI", result);
        TextUtils.assertNotContains("[Deprecated ", result);

        try (CSVParser parser = readCSV(file)) {
            assertContains("Maven", parser.getHeaderNames());
            assertContains("Description", parser.getHeaderNames());
            assertNotContains("Ant", parser.getHeaderNames());
            assertNotContains("CLI", parser.getHeaderNames());
            assertNotContains("[Deprecated ", parser.getHeaderNames());
        }
    }

    @Test
    public void testCliCsv() throws IOException, ParseException {
        Naming.main(new String[]{"--cli", "--csv", file.getAbsolutePath()});
        String result = readFile(file);
        TextUtils.assertContains("CLI", result);
        TextUtils.assertContains("Description", result);
        TextUtils.assertNotContains("Ant", result);
        TextUtils.assertNotContains("Maven", result);
        TextUtils.assertNotContains("[Deprecated ", result);

        try (CSVParser parser = readCSV(file)) {
            assertContains("CLI", parser.getHeaderNames());
            assertContains("Description", parser.getHeaderNames());
            assertNotContains("Maven", parser.getHeaderNames());
            assertNotContains("Ant", parser.getHeaderNames());
            assertNotContains("[Deprecated ", parser.getHeaderNames());
        }
    }

    @Test
    @Disabled("No deprecated options exist at this time")
    public void testCliCsvDeprecated() throws IOException, ParseException {
        Naming.main(new String[]{"--cli", "--csv", "--include-deprecated", file.getAbsolutePath()});
        String result = readFile(file);
        TextUtils.assertContains("CLI", result);
        TextUtils.assertContains("Description", result);
        TextUtils.assertNotContains("Maven", result);
        TextUtils.assertNotContains("Ant", result);
        TextUtils.assertContains("[Deprecated ", result);

        try (CSVParser parser = readCSV(file)) {
            assertContains("CLI", parser.getHeaderNames());
            assertContains("Description", parser.getHeaderNames());
            assertNotContains("Maven", parser.getHeaderNames());
            assertNotContains("Ant", parser.getHeaderNames());
            assertNotContains("[Deprecated ", parser.getHeaderNames());
            assertTrue( parser.stream().anyMatch( rec -> rec.stream().anyMatch(s -> s.startsWith("[Deprecated"))), "Missing Deprecated data");
        }
    }

    private String readFile(File f) throws IOException {
        return String.join("\n", IOUtils.readLines(Files.newInputStream(f.toPath()), StandardCharsets.UTF_8));
    }

    private CSVParser readCSV(File f) throws IOException {
        return CSVFormat.DEFAULT.builder().setHeader().get().parse(new InputStreamReader(Files.newInputStream(f.toPath())));
    }

    private void assertContains(String expected, List<String> actual) {
        assertTrue(actual.contains(expected), () -> "Missing " + expected);
    }

    private void assertNotContains(String expected, List<String> actual) {
        assertFalse(actual.contains(expected), () -> "Contains " + expected);
    }

    @Test
    public void testNamingGenerationWithoutParameters() {
        assertDoesNotThrow(() -> Naming.main(null));
        assertDoesNotThrow(() -> Naming.main(new String[]{}));
    }
}
