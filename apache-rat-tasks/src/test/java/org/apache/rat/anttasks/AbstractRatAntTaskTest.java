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

import java.util.regex.Matcher;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildFileRule;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractRatAntTaskTest {
    private static final File tempDir = new File("target/anttasks");

    public final BuildFileRule buildRule = new BuildFileRule();

    protected abstract File getAntFile();

    protected File getTempDir() {
        return tempDir;
    }

    @BeforeEach
    public void setUp() {
        buildRule.configureProject(getAntFile().getPath());
        buildRule.getProject().setProperty("output.dir", tempDir.getAbsolutePath());
        buildRule.getProject().setProperty("resource.dir", getAntFile().getParent());
    }

    protected void assertLogDoesNotMatch(String pPattern) {
        final String log = buildRule.getLog();
        final Matcher matcher = Pattern.compile(pPattern).matcher(log);

        assertThat(matcher.find()).describedAs("Log matches the pattern: {}",pPattern).isFalse();
    }

    protected void assertLogMatches(String pPattern) {
        final String log = buildRule.getLog();
        final Matcher matcher = Pattern.compile(pPattern).matcher(log);
        assertThat(matcher.find()).describedAs("Log does not the pattern: {}",pPattern).isTrue();
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
        final Matcher matcher = Pattern.compile(pPattern).matcher(content);
        assertThat(matcher.find()).describedAs("File {} doesn't match the pattern:{} ", pFile, pPattern).isTrue();
    }
}
