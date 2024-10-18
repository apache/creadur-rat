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
package org.apache.rat.config.exclusion.fileProcessors;

import org.apache.rat.document.impl.DocumentName;
import org.apache.rat.utils.iterator.ExtendedIterator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DescendingFileProcessorTest extends AbstractIgnoreProcessorTest {

    @Test
    public void singleDirectoryTest() throws IOException {
        String[] lines = {"*.ext", "fname.*", "**/fname.ext"};

        List<String> expected = ExtendedIterator.create(Arrays.asList(lines).iterator())
                .map(s -> new File(baseDir, s).getPath()).toList();

        writeFile("test.txt", Arrays.asList(lines));
        DocumentName documentName = new DocumentName(baseDir);

        DescendingFileProcessor processor = new DescendingFileProcessor("test.txt", "#");
        List<String> actual = processor.apply(baseName);
        assertEquals(expected, actual);
    }

    @Test
    public void layeredDirectoryTest() throws IOException {
        String[] lines = {"*.ext", "fname.*", "**/fname.ext"};

        List<String> expected = ExtendedIterator.create(Arrays.asList(lines).iterator())
                .map(s -> new File(baseDir, s).getPath()).toList();

        writeFile("test.txt", Arrays.asList(lines));

        File subdir = new File(baseDir, "subdir");
        assertTrue(subdir.mkdirs(), "Could not make subdirectory");

        writeFile("subdir/test.txt", Collections.singletonList("foo.*"));
        expected.add(new File(subdir, "foo.*").getPath());

        DescendingFileProcessor processor = new DescendingFileProcessor("test.txt", "#");
        List<String> actual = processor.apply(baseName);
        assertEquals(expected, actual);
    }
}
