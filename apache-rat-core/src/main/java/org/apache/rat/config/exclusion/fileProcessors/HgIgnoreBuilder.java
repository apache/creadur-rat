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

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.document.DocumentName;

import static java.lang.String.format;

/**
 * A processor for the {@code .hgignore} files.
 * @see <a href="https://wiki.mercurial-scm.org/.hgignore">Mecurial how to ignore files</a>
 * @see <a href="https://www.selenic.com/mercurial/hgignore.5.html">syntax for Mercurial ignore files</a>
 */
public final class  HgIgnoreBuilder extends AbstractFileProcessorBuilder {
    /**
     * The state enumeration for the processor. When processing the file the processor changes
     * syntax state depending on the input.
     */
    enum Syntax {
        /** Regular expression processing */
        REGEXP,
        /** Glob processing */
        GLOB
    }

    /** The line that changes the syntax processing state.*/
    private static final Pattern SYNTAX_CHECK = Pattern.compile("^\\s?syntax:\\s+(glob|regexp)\\s?");
    /** The syntax state */
    private Syntax state;

    /**
     * Constructs the .hgignore processor.
     */
    public HgIgnoreBuilder() {
        super(".hgignore", "#", true);
        state = Syntax.REGEXP;
    }

    @Override
    protected MatcherSet process(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName dirBasedName, final DocumentName documentName) {
        state = Syntax.REGEXP;
        return super.process(matcherSetConsumer, dirBasedName, documentName);
    }

    @Override
    public Optional<String> modifyEntry(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName documentName, final String entry) {
        Matcher m = SYNTAX_CHECK.matcher(entry.toLowerCase(Locale.ROOT));
        if (m.matches()) {
            state = Syntax.valueOf(m.group(1).toUpperCase());
            return Optional.empty();
        }
        /*
         Neither glob nor regexp patterns are rooted. A glob-syntax pattern of the form *.c will match a file ending in .c
         in any directory, and a regexp pattern of the form \.c$ will do the same. To root a regexp pattern, start it with ^.
         */
        if (state == Syntax.REGEXP) {
            String pattern = entry.startsWith("^") ? entry.substring(1) : ".*" + entry;
            return Optional.of(format(REGEX_FMT, pattern));
        } else {
            if (entry.startsWith("*")) {
                return Optional.of("**/"+entry);
            }
        }
        return Optional.of(entry);
    }
}
