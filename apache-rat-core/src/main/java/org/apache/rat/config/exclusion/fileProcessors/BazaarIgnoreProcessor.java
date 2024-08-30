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

import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.utils.iterator.ExtendedIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class BazaarIgnoreProcessor extends DescendingFileProcessor {

    public BazaarIgnoreProcessor() {
        super(".bzignore", "#");
    }

    protected List<String> process(File f) {
        final File dir = f.getParentFile();
        final List<String> result = new ArrayList<>();
        ExtendedIterator<String> iter = ExclusionUtils.asIterator(f, commentFilter);
        while (iter.hasNext()) {
            String line = iter.next();
            if (line.startsWith("RE:")) {
                line = line.substring("RE:".length()).trim();
                String pattern = line.startsWith("^") ? line.substring(1) : line;
                result.add(format("%%regex[%s]", new File(dir, pattern).getPath()));
            } else {
                result.add(new File(dir, line).getPath());
            }
        }
        return result;
    }
}
