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

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.rat.document.impl.DocumentName;

import static java.lang.String.format;

/**
 * A processor for the {@code .hdignore} files.
 */
public final class HgIgnoreProcessor extends DescendingFileProcessor {
    /**
     * The state enumeration for the processor.  When processing the file the processor changes
     * syntax state depending on the input.
     */
    enum Syntax {
        /** Regular expression processing */
        REGEXP,
        /** Glob processing */
        GLOB };
    /** The line that changes the syntax processing state.*/
    private static final Pattern SYNTAX_CHECK = Pattern.compile("^\\s?syntax:\\s+(glob|regexp)\\s?");
    /** the syntax state */
    private Syntax state;

    /**
     * Constructs the .hgignore processor.
     */
    public HgIgnoreProcessor() {
        super(".hgignore", "#");
        state = Syntax.REGEXP;
    }

    @Override
    protected List<String> process(final DocumentName baseName) {
        state = Syntax.REGEXP;
        return super.process(baseName);
    }

    @Override
    public String modifyEntry(final DocumentName baseName, final String entry) {
        Matcher m = SYNTAX_CHECK.matcher(entry.toLowerCase(Locale.ROOT));
        if (m.matches()) {
            state = Syntax.valueOf(m.group(1).toUpperCase());
            return null;
        }
        if (state == Syntax.REGEXP) {
            String pattern = entry.startsWith("^") ? entry.substring(1) : ".*" + entry;
            return format(REGEX_FMT, pattern);
        }
        return entry;
    }
}
