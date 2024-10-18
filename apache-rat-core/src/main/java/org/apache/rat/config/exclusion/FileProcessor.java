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
package org.apache.rat.config.exclusion;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.rat.config.exclusion.plexus.MatchPattern;
import org.apache.rat.config.exclusion.plexus.SelectorUtils;
import org.apache.rat.document.impl.DocumentName;
import org.apache.rat.utils.ExtendedIterator;

/**
 * An interface that defines the FileProcessor. The file processor reads the file specified in the DocumentName.
 * It must return a list of fully qualified strings for the {@link MatchPattern} to process. It may return either
 * Ant or Regex style strings, or a mixture of both. See {@link SelectorUtils} for a description of the formats.
 */
@FunctionalInterface
public interface FileProcessor extends Function<DocumentName, List<String>> {
    /** A String format pattern to print a regex string */
    String REGEX_FMT = "%%regex[%s]";

    /** an empty file processor returning no entries.*/
    FileProcessor EMPTY = DocumentName -> Collections.emptyList();

    /**
     * Create a virtual file processor out of a list of file patterns.
     * @param patterns the patterns to simulate the file from.
     * @return A file processor that processes the patterns.
     */
    static FileProcessor from(final Iterable<String> patterns) {
        return documentName -> ExtendedIterator.create(patterns.iterator())
                .map(entry -> FileProcessor.localizePattern(documentName, entry))
                .map(DocumentName::getName)
                .toList();
    }

    /**
     * Allows modification of the file entry to match the {@link MatchPattern} format.
     * Default implementation returns the @{code entry} argument.
     * @param documentName the name of the document that the file was read from.
     * @param entry the entry from that document.
     * @return the modified string or null to skip the string.
     */
    default String modifyEntry(DocumentName documentName, String entry) {
        return entry;
    }

    /**
     * Modifies the {@link MatchPattern} formatted {@code pattern} argument by expanding the pattern and
     * by adjusting the pattern to include the basename from the {@code basename} argument.
     * @param baseName the base name top work on.
     * @param pattern the pattern to format.
     * @return the completely formatted pattern
     */
    static DocumentName localizePattern(final DocumentName baseName, final String pattern) {
        boolean prefix = pattern.startsWith("!");
        String workingPattern = prefix ? pattern.substring(1) : pattern;
        String normalizedPattern = SelectorUtils.extractPattern(workingPattern, baseName.getDirectorySeparator());
        StringBuilder sb = new StringBuilder(prefix ? "!" : "");
        if (SelectorUtils.isRegexPrefixedPattern(workingPattern)) {
            sb.append(SelectorUtils.REGEX_HANDLER_PREFIX)
                    .append("\\Q").append(baseName.getBaseName())
                    .append(baseName.getDirectorySeparator())
                    .append("\\E").append(normalizedPattern)
                    .append(SelectorUtils.PATTERN_HANDLER_SUFFIX);

        } else {
            sb.append(baseName.getBaseName())
                    .append(baseName.getDirectorySeparator())
                    .append(normalizedPattern);
        }
        return new DocumentName(sb.toString(), baseName.getBaseName(), baseName.getDirectorySeparator(), baseName.isCaseSensitive());
    }
}
