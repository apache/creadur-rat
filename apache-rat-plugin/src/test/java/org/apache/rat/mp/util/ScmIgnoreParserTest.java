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

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.apache.rat.mp.util.ScmIgnoreParser.getExcludesFromFile;
import static org.apache.rat.mp.util.ScmIgnoreParser.getExclusionsFromSCM;
import static org.apache.rat.mp.util.ScmIgnoreParser.isComment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ScmIgnoreParserTest {
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    @Mock
    private Log log;

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


    @Test
    public void parseFromNonExistingFileOrDirectoryOrNull() {
        assertTrue(getExcludesFromFile(log, new File("./mustNotExist-RAT-171")).isEmpty());
        assertTrue(getExcludesFromFile(log, null).isEmpty());
        assertTrue(getExcludesFromFile(log, new File(".")).isEmpty());
    }

    @Test
    public void parseFromTargetDirectoryHopefullyWithoutSCMIgnores() {
        assertTrue(getExclusionsFromSCM(log, new File("./target")).isEmpty());
    }

    @Test
    public void parseFromEmptyIgnoreFile() throws IOException {
        File ignore = testFolder.newFile();
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
