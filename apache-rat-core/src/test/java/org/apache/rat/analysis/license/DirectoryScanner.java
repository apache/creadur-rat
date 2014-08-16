/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.rat.analysis.license;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;

import org.apache.commons.io.IOUtils;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.apache.rat.test.utils.Resources;
import org.junit.Assert;

class DirectoryScanner {

    @SuppressWarnings("boxing") // OK in test code
    /**
     * Get list of files in a directory, and scan for license matches
     * @param directory the directory containing the files
     * @param matcher the license matcher
     * @param expected the expected result of the each scan
     * @throws Exception
     */
    public static void testFilesInDir(String directory, IHeaderMatcher matcher, boolean expected) throws Exception {
        final File[] resourceFiles = Resources.getResourceFiles(directory);
        if (resourceFiles.length == 0) {
            Assert.fail("No files found under "+directory);
        }
        for(File f : resourceFiles) {
            final Document subject = new MockLocation(f.toString());
            BufferedReader br = null;
            try {
                boolean result = false;
                br = Resources.getBufferedReader(f);
                String line;
                while(!result && (line = br.readLine()) != null) {
                    result = matcher.match(subject, line);
                }
                assertEquals(f.toString(), expected, result);
            } finally {
                matcher.reset();
                IOUtils.closeQuietly(br);
            }
        }
    }

}
