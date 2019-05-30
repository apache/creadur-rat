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

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildFileRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public abstract class AbstractRatAntTaskTest {
    private static final File tempDir = new File("target/anttasks");
    @Rule
    public final BuildFileRule buildRule = new BuildFileRule();

    protected abstract File getAntFile();

    protected File getTempDir() {
        return tempDir;
    }

    @Before
    public void setUp() {
        buildRule.configureProject(getAntFile().getPath());
    }

    protected void assertLogDoesNotMatch(String pPattern) {
        final String log = buildRule.getLog();
        Assert.assertFalse("Log matches the pattern: " + pPattern + ", got " + log,
                isMatching(pPattern, log));
    }

    protected void assertLogMatches(String pPattern) {
        final String log = buildRule.getLog();
        Assert.assertTrue("Log doesn't match string: " + pPattern + ", got " + log,
                isMatching(pPattern, log));
    }

    private boolean isMatching(final String pPattern, final String pValue) {
        return Pattern.compile(pPattern).matcher(pValue).find();
    }

    private String load(File pFile) throws IOException {
        FileReader fr = new FileReader(pFile);
        try {
            final StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            for (;;) {
                int res = fr.read(buffer);
                if (res == -1) {
                    fr.close();
                    fr = null;
                    return sb.toString();
                }
                if (res > 0) {
                    sb.append(buffer, 0, res);
                }
            }
        } finally {
            IOUtils.closeQuietly(fr);
        }
    }

    protected void assertFileMatches(File pFile, String pPattern)
            throws IOException {
        final String content = load(pFile);
        Assert.assertTrue("File " + pFile
                + " doesn't match the pattern " + pPattern
                + ", got " + content,
                isMatching(pPattern, content));
    }
}
