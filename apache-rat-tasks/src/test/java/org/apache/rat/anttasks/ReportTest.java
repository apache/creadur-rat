/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.anttasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;

import junit.framework.Assert;

public class ReportTest extends AbstractRatAntTaskTest {
    private static final File antFile = new File("src/test/resources/antunit/report-junit.xml").getAbsoluteFile();

    @Override
    protected File getAntFile() {
        return antFile;
    }

    public void testWithReportSentToAnt() throws Exception {
        executeTarget("testWithReportSentToAnt");
        assertLogMatches("AL +\\Q" + getAntFileName() + "\\E");
    }

    public void testWithReportSentToFile() throws Exception {
        final File reportFile = new File(getTempDir(), "selftest.report");
        if(!getTempDir().mkdirs() && !getTempDir().isDirectory()) {
            throw new IOException("Could not create temporary directory " + getTempDir());
        }
        final String alLine = "AL +\\Q" + getAntFileName() + "\\E";
        if (reportFile.isFile()  &&  !reportFile.delete()) {
            throw new IOException("Unable to remove report file " + reportFile);
        }
        executeTarget("testWithReportSentToFile");
        assertLogDoesntMatch(alLine);
        Assert.assertTrue("Expected report file " + reportFile, reportFile.isFile());
        assertFileMatches(reportFile, alLine);
    }

    public void testWithALUnknown() throws Exception {
        executeTarget("testWithALUnknown");
        assertLogDoesntMatch("AL +\\Q" + getAntFileName() + "\\E");
        assertLogMatches("\\!\\?\\?\\?\\?\\? +\\Q" + getAntFileName() + "\\E");
    }

    public void testCustomMatcher() throws Exception {
        executeTarget("testCustomMatcher");
        assertLogDoesntMatch("AL +\\Q" + getAntFileName() + "\\E");
        assertLogMatches("EXMPL +\\Q" + getAntFileName() + "\\E");
    }

    public void testNoResources() throws Exception {
        try {
            executeTarget("testNoResources");
            fail("Expected Exception");
        } catch (BuildException e) {
            final String expect = "You must specify at least one file";
            assertTrue("Expected " + expect + ", got " + e.getMessage(),
                       e.getMessage().indexOf(expect) != -1);
        }
    }

    public void testNoLicenseMatchers() throws Exception {
        try {
            executeTarget("testNoLicenseMatchers");
            fail("Expected Exception");
        } catch (BuildException e) {
            final String expect = "at least one license";
            assertTrue("Expected " + expect + ", got " + e.getMessage(),
                       e.getMessage().indexOf(expect) != -1);
        }
    }

    private String getAntFileName() {
        return getAntFile().getPath().replace('\\', '/');
    }

    private String getFirstLine(File pFile) throws IOException {
        final FileInputStream fis = new FileInputStream(pFile);
        final InputStreamReader reader = new InputStreamReader(fis, "UTF8");
        final BufferedReader breader = new BufferedReader(reader);
        final String result = breader.readLine();
        breader.close();
        return result;
    }

    public void testAddLicenseHeaders() throws Exception {
        executeTarget("testAddLicenseHeaders");

        final File origFile = new File("target/anttasks/it-sources/index.apt");
        final String origFirstLine = getFirstLine(origFile);
        assertTrue(origFirstLine.indexOf("--") != -1);
        assertTrue(origFirstLine.indexOf("~~") == -1);
        final File modifiedFile = new File("target/anttasks/it-sources/index.apt.new");
        final String modifiedFirstLine = getFirstLine(modifiedFile);
        assertTrue(modifiedFirstLine.indexOf("--") == -1);
        assertTrue(modifiedFirstLine.indexOf("~~") != -1);
    }
}
