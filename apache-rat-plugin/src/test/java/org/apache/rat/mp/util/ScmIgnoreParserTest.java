package org.apache.rat.mp.util;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.apache.rat.mp.util.ScmIgnoreParser.getExclusionsFromSCM;
import static org.apache.rat.mp.util.ignore.GlobIgnoreMatcher.isComment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.rat.mp.util.ignore.GlobIgnoreMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

public class ScmIgnoreParserTest {
     
	@TempDir
	private File tempDir;
	
	private Log log = Mockito.mock(Log.class);

    private static final String IGNORE_EXAMPLE = "**/*.java\r\n## Justus commentos\r\nignoredDirectory";

    @Test
    public void parseAsNoComments() {
        assertFalse(isComment(null));
        assertFalse(isComment(""));
        assertFalse(isComment("This is a  normal line"));
        assertFalse(isComment("**/ignoreMe/*"));
        assertFalse(isComment("C:\\No Space In FileNames Please"));
    }

    @Test
    public void parseAsComments() {
        assertTrue(isComment(" # comment that is"));
        assertTrue(isComment("## comment that is"));
        assertTrue(isComment("## comment that is ## "));
        assertTrue(isComment("     // comment that is ## "));
        assertTrue(isComment("     /** comment that is **/ "));
        assertTrue(isComment("     /* comment that is */ "));
    }

    public List<String> getExcludesFromFile(final Log log, final File scmIgnore) {
        return new GlobIgnoreMatcher(log, scmIgnore).getExclusionLines();
    }

    @Test
    public void parseFromNonExistingFileOrDirectoryOrNull() {
        assertTrue(getExcludesFromFile(log, new File("./mustNotExist-RAT-171")).isEmpty());
        assertTrue(getExcludesFromFile(log, null).isEmpty());
        assertTrue(getExcludesFromFile(log, new File(".")).isEmpty());
    }

    @Test
    public void parseFromTargetDirectoryHopefullyWithoutSCMIgnores() {
        // The target directory contains ignore files from other tests
        assertTrue(getExclusionsFromSCM(log, new File("./target/classes")).isEmpty());
    }

    @Test
    public void parseFromEmptyIgnoreFile() throws IOException {
    	File ignore  = Files.createTempFile(tempDir.getName(), "sip").toFile();

    	assertTrue(ignore.exists());
    	writeToFile(IGNORE_EXAMPLE, ignore);

    	final List<String> excludes = getExcludesFromFile(log, ignore);
    	assertFalse(excludes.isEmpty());
    	assertEquals(2, excludes.size());
    }

    private static void writeToFile(String contents, File file) throws IOException {
        BufferedWriter bw = null;
        try {
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write(contents);
        } finally {
            IOUtils.closeQuietly(bw);
        }
    }


}
