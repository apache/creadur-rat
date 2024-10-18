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

import org.apache.rat.utils.iterator.ExtendedIterator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HgIgnoreProcessorTest extends AbstractIgnoreProcessorTest {

    @Test
    public void processExampleFileTest() throws IOException {
        String[] lines = {
        "# use glob syntax.", "syntax: glob", "*.elc", "*.pyc", "*~", System.lineSeparator(),
            "# switch to regexp syntax.", "syntax: regexp", "^\\.pc/" };

        List<String> expected = ExtendedIterator.create(Arrays.asList("*.elc", "*.pyc", "*~").iterator())
                .map(s -> new File(baseDir, s).getPath()).toList();
        expected.add(format("%%regex[\\Q%s%s\\E%s]", baseDir.getPath(), File.separatorChar, "\\.pc/"));

        writeFile(".hgignore", Arrays.asList(lines));

        HgIgnoreProcessor processor = new HgIgnoreProcessor();
        List<String> actual = processor.apply(baseName);
        assertEquals(expected, actual);

    }

    @Test
    public void processDefaultFileTest() throws IOException {
        String[] lines = {"^[A-Z]*\\.txt", "[0-9]*\\.txt"};

        List<String> expected = ExtendedIterator.create(Arrays.asList("[A-Z]*\\.txt", ".*[0-9]*\\.txt").iterator())
                .map(s -> format("%%regex[\\Q%s%s\\E%s]", baseDir.getPath(), File.separatorChar, s)).toList();

        writeFile(".hgignore", Arrays.asList(lines));

        HgIgnoreProcessor processor = new HgIgnoreProcessor();
        List<String> actual = processor.apply(baseName);
        assertEquals(expected, actual);
    }
}
