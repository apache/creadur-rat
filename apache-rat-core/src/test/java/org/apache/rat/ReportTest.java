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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ReportTest {
    @TempDir
    static File tempDirectory;

    @Test
    public void testOutputOption() throws Exception {
        File output = new File(tempDirectory, "test");
        CommandLine cl = new DefaultParser().parse(OptionCollection.buildOptions(), new String[]{"-o", output.getCanonicalPath()});
        ReportConfiguration config = OptionCollection.createConfiguration(DefaultLog.getInstance(), "target/test-classes/elements", cl);
        new Reporter(config).output();
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("2 Unknown Licenses"));
        assertTrue(content.contains("target/test-classes/elements/Source.java"));
        assertTrue(content.contains("target/test-classes/elements/sub/Empty.txt"));
    }

    @Test
    public void helpTest() {
        Options opts = OptionCollection.buildOptions();
        StringWriter out = new StringWriter();
        Report.printUsage(new PrintWriter(out), opts);

        String result = out.toString();

        TextUtils.assertContains("-a ", result);
        TextUtils.assertContains("-A,--addLicense ", result);
        TextUtils.assertContains("-c,--copyright <arg> ", result);
        TextUtils.assertContains("--config <File> ", result);
        TextUtils.assertContains("--configuration-no-defaults", result);
        TextUtils.assertContains("-d,--dir <DirOrArchive> ", result);
        TextUtils.assertContains("--dry-run ", result);
        TextUtils.assertContains("-e,--exclude <Expression> ", result);
        TextUtils.assertContains("-E,--exclude-file <File> ", result);
        TextUtils.assertContains("--edit-copyright <arg> ", result);
        TextUtils.assertContains("--edit-license ", result);
        TextUtils.assertContains("--edit-overwrite ", result);
        TextUtils.assertContains("-f,--force ", result);
        TextUtils.assertContains("-h,--help ", result);
        TextUtils.assertContains("--input-exclude <Expression> ", result);
        TextUtils.assertContains("--input-exclude-file <File> ", result);
        TextUtils.assertContains("--license-families-approved <FamilyID> ", result);
        TextUtils.assertContains("--license-families-approved-file <File> ", result);
        TextUtils.assertContains("--license-families-denied <FamilyID> ", result);
        TextUtils.assertContains("--license-families-denied-file <File> ", result);
        TextUtils.assertContains("--licenses <File> ", result);
        TextUtils.assertContains("--licenses-approved <LicenseID> ", result);
        TextUtils.assertContains("--licenses-approved-file <File> ", result);
        TextUtils.assertContains("--licenses-denied <LicenseID> ", result);
        TextUtils.assertContains("--licenses-denied-file <File> ", result);
        TextUtils.assertContains("--list-families <LicenseFilter> ", result);
        TextUtils.assertContains("--list-licenses <LicenseFilter> ", result);
        TextUtils.assertContains("--log-level <LogLevel> ", result);
        TextUtils.assertContains("--no-default-licenses ", result);
        TextUtils.assertContains("-o,--out <File> ", result);
        TextUtils.assertContains("--output-archive <ProcessingType> ", result);
        TextUtils.assertContains("--output-families <LicenseFilter> ", result);
        TextUtils.assertContains("--output-file <File> ", result);
        TextUtils.assertContains("--output-licenses <LicenseFilter> ", result);
        TextUtils.assertContains("--output-standard <ProcessingType> ", result);
        TextUtils.assertContains("--output-style <StyleSheet> ", result);
        TextUtils.assertContains("-s,--stylesheet <StyleSheet> ", result);
        TextUtils.assertContains("--scan-hidden-directories ", result);
        TextUtils.assertContains("-x,--xml ", result);
        TextUtils.assertPatternInTarget("^<DirOrArchive>", result);
        TextUtils.assertPatternInTarget("^<Expression>", result);
        TextUtils.assertPatternInTarget("^<FamilyID>", result);
        TextUtils.assertPatternInTarget("^<File>", result);
        TextUtils.assertPatternInTarget("^<LicenseFilter>", result);
        TextUtils.assertPatternInTarget("^<LicenseID>", result);
        TextUtils.assertPatternInTarget("^<ProcessingType>", result);
        TextUtils.assertPatternInTarget("^<StyleSheet>", result);
    }
}
