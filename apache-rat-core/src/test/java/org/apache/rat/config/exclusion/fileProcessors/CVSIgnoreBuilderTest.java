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

import java.util.ArrayList;
import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.utils.ExtendedIterator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CVSIgnoreBuilderTest extends AbstractIgnoreBuilderTest {

    @Test
    public void processExampleFileTest() throws IOException {
        String[] lines = {
                "thingone thingtwo", System.lineSeparator(), "one_fish", "two_fish", "", "red_* blue_*"};

        List<String> matching = Arrays.asList("thingone", "thingtwo", "one_fish", "two_fish", "red_fish", "blue_fish");
        List<String> notMatching = Arrays.asList("thing", "two", "fish_red", "subdir/two_fish", "subdir/red_fish", "subdir/blue_fish");

        writeFile(".cvsignore", Arrays.asList(lines));

        CVSIgnoreBuilder processor = new CVSIgnoreBuilder();
        DocumentNameMatcher matcher = MatcherSet.merge(processor.build(baseName)).createMatcher();

        assertCorrect(new CVSIgnoreBuilder(), matching, notMatching);
    }
}
