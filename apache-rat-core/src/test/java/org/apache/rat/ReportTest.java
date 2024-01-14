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
package org.apache.rat;



import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class ReportTest {
    @Test
    public void parseExclusionsForCLIUsage() {
        final FilenameFilter filter = Report
                .parseExclusions(Arrays.asList("", " # foo/bar", "foo", "##", " ./foo/bar"));
        assertNotNull(filter);
    }
    
    @Test
    public void testDefaultConfiguration() throws ParseException, IOException {
        String[] empty = {};
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), empty);
        ReportConfiguration config = Report.createConfiguration("", cl);
        ReportConfigurationTest.validateDefault(config);
    }

    @Test
    public void testOutputOption() throws Exception {
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[]{ "-o", "target/test" });
        ReportConfiguration config = Report.createConfiguration("target/test-classes/elements", cl);
        Reporter.report(config);
        File output = new File("target/test");
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("2 Unknown Licenses"));
        assertTrue(content.contains("target/test-classes/elements/Source.java"));
        assertTrue(content.contains("target/test-classes/elements/sub/Empty.txt"));
    }

    @Test
    public void testDefaultOutput() throws Exception {
        PrintStream origin = System.out;
        File output = new File("target/sysout");
        System.setOut(new PrintStream(output));
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[]{});
        ReportConfiguration config = Report.createConfiguration("target/test-classes/elements", cl);
        Reporter.report(config);
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("2 Unknown Licenses"));
        assertTrue(content.contains("target/test-classes/elements/Source.java"));
        assertTrue(content.contains("target/test-classes/elements/sub/Empty.txt"));
        System.setOut(origin);
    }

}
