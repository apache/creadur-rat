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
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class HgIgnoreProcessor extends DescendingFileProcessor {
    enum Type {REGEXP, GLOB};

    private Type state;
    private final String fileSeparator = FileSystems.getDefault().getSeparator();

    public HgIgnoreProcessor() {
        super(".hgignore", "#");
        state = Type.REGEXP;
    }

    protected List<String> process(File f) {
        final File dir = f.getParentFile();
        final List<String> result = new ArrayList<>();
        Pattern p = Pattern.compile("^\\s?syntax:\\s+(glob|regexp)\\s?");
        ExtendedIterator<String> iter = ExclusionUtils.asIterator(f, commentFilter);
        while (iter.hasNext()) {
            String line = iter.next();
            Matcher m = p.matcher(line.toLowerCase(Locale.ROOT));
            if (m.matches()) {
                state = Type.valueOf(m.group(1).toUpperCase());
            } else {
                switch (state) {
                    case GLOB:
                        result.add(new File(dir, line).getPath());
                        break;
                    case REGEXP:
                        String pattern = line.startsWith("^") ? line.substring(1) : ".*"+line;
                        String quoted = dir.getPath();
                        if (!quoted.endsWith(fileSeparator)) {
                            quoted = quoted + fileSeparator;
                        }
                        result.add(format("%%regex[\\Q%s\\E%s]", quoted, pattern));
                        break;
                }
            }
        }
        return result;
    }
}
