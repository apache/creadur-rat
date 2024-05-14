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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.SortedSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.testhelpers.TextUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ReportTest {
    @TempDir
    File tempDirectory;

    private ReportConfiguration createConfig(String... args) throws IOException, ParseException {
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), args);
        return Report.createConfiguration("target/test-classes/elements", cl);
    }

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
        File output = new File(tempDirectory, "test");
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[]{"-o", output.getCanonicalPath()});
        ReportConfiguration config = Report.createConfiguration("target/test-classes/elements", cl);
        new Reporter(config).output();
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("2 Unknown Licenses"));
        assertTrue(content.contains("target/test-classes/elements/Source.java"));
        assertTrue(content.contains("target/test-classes/elements/sub/Empty.txt"));
    }

    @Test
    public void helpTest() {
        Options opts = Report.buildOptions();
        StringWriter out = new StringWriter();
        Report.printUsage(new PrintWriter(out), opts);

        String result = out.toString();
        System.out.println(result);

        TextUtils.assertContains("-a ", result);
        TextUtils.assertContains("-A,--addLicense ", result);
        TextUtils.assertContains("--archive <ProcessingType> ", result);
        TextUtils.assertContains("-c,--copyright <arg> ", result);
        TextUtils.assertContains("-d,--dir <DirOrArchive> ", result);
        TextUtils.assertContains("--dry-run ", result);
        TextUtils.assertContains("-e,--exclude <Expression> ", result);
        TextUtils.assertContains("-E,--exclude-file <FileOrURI> ", result);
        TextUtils.assertContains("-f,--force ", result);
        TextUtils.assertContains("-h,--help ", result);
        TextUtils.assertContains("--licenses <FileOrURI> ", result);
        TextUtils.assertContains("--list-families <LicenseFilter> ", result);
        TextUtils.assertContains("--list-licenses <LicenseFilter> ", result);
        TextUtils.assertContains("--log-level <LogLevel> ", result);
        TextUtils.assertContains("--no-default-licenses ", result);
        TextUtils.assertContains("-o,--out <arg> ", result);
        TextUtils.assertContains("-s,--stylesheet <StyleSheet> ", result);
        TextUtils.assertContains("--scan-hidden-directories ", result);
        TextUtils.assertContains("-x,--xml ", result);
    }

    private static String shortOpt(Option opt) {
        return "-" + opt.getOpt();
    }

    private static String longOpt(Option opt) {
        return "--" + opt.getLongOpt();
    }

    @Test
    public void testXMLOutput() throws Exception {
        ReportConfiguration config = createConfig();
        assertTrue(config.isStyleReport());

        config = createConfig(shortOpt(Report.XML));
        assertFalse(config.isStyleReport());

        config = createConfig(longOpt(Report.XML));
        assertFalse(config.isStyleReport());
    }

    @Test
    public void LicensesOptionTest() throws Exception {
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[]{"-licenses", "target/test-classes/report/LicenseOne.xml"});
        ReportConfiguration config = Report.createConfiguration("", cl);
        SortedSet<ILicense> set = config.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
        assertTrue(LicenseSetFactory.search("LiOne", "LiOne", set).isPresent());
        assertFalse(LicenseSetFactory.search("LiOne", "LiTwo", set).isPresent(),"LiOne/LiTwo");
        assertFalse(LicenseSetFactory.search("LiTwo", "LiTwo", set).isPresent(), "LiTwo");
    }

    @Test
    public void LicensesOptionNoDefaultsTest() throws Exception {
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[] {"--no-default", "--licenses", "target/test-classes/report/LicenseOne.xml", "--licenses", "target/test-classes/report/LicenseTwo.xml"});
        ReportConfiguration config = Report.createConfiguration("", cl);
        SortedSet<ILicense> set = config.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
        assertEquals(2, set.size());
        assertTrue(LicenseSetFactory.search("LiOne", "LiOne", set).isPresent(), "LiOne");
        assertFalse(LicenseSetFactory.search("LiOne", "LiTwo", set).isPresent(), "LiOne/LiTwo");
        assertTrue(LicenseSetFactory.search("LiTwo", "LiTwo", set).isPresent(), "LiTwo");
    }
}
