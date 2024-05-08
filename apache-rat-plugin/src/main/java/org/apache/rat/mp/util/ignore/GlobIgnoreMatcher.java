package org.apache.rat.mp.util.ignore;

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

import org.apache.maven.plugin.logging.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GlobIgnoreMatcher implements IgnoreMatcher {

    final List<String> exclusionLines = new ArrayList<>();

    private static final List<String> COMMENT_PREFIXES = Arrays.asList("#", "##", "//", "/**", "/*");

    public GlobIgnoreMatcher() {
        // No rules to load
    }

    public GlobIgnoreMatcher(final Log log, final File scmIgnore) {
        loadFile(log, scmIgnore);
    }

    /**
     * Add a single rule to the set
     * @param rule The line that matches some files
     */
    public void addRule(final String rule) {
        if (!exclusionLines.contains(rule)) {
            exclusionLines.add(rule);
        }
    }

    /**
     * Add a set of rules to the set
     * @param rules The line that matches some files
     */
    public void addRules(final Collection<String> rules) {
        for (String rule : rules) {
            if (!exclusionLines.contains(rule)) {
                exclusionLines.add(rule);
            }
        }
    }

    /**
     * Parses excludes from the given SCM ignore file.
     *
     * @param log       Maven log to show output during RAT runs.
     * @param scmIgnore if <code>null</code> or invalid an empty list of exclusions is returned.
     */
    public void loadFile(final Log log, final File scmIgnore) {
        if (scmIgnore != null && scmIgnore.exists() && scmIgnore.isFile()) {
            log.debug("Parsing exclusions from " + scmIgnore);

            try (BufferedReader reader = new BufferedReader(new FileReader(scmIgnore))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!isComment(line)) {
                        exclusionLines.add(line);
                        log.debug("Added " + line);
                    }
                }
            } catch (final IOException e) {
                log.warn("Cannot parse " + scmIgnore + " for exclusions. Will skip this file.");
                log.debug("Skip parsing " + scmIgnore + " due to " + e.getMessage());
            }
        }
    }

    /**
     * Determines whether the given line is a comment or not based on scanning
     * for prefixes
     * @see #COMMENT_PREFIXES
     *
     * @param line line to verify.
     * @return <code>true</code> if the given line is a commented out line.
     */
    public static boolean isComment(final String line) {
        if (line == null || line.length() <= 0) {
            return false;
        }

        final String trimLine = line.trim();
        for (String prefix : COMMENT_PREFIXES) {
            if (trimLine.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getExclusionLines() {
        return Collections.unmodifiableList(exclusionLines);
    }

    @Override
    public boolean isEmpty() {
        return exclusionLines.isEmpty();
    }

    @Override
    public Optional<Boolean> isIgnoredFile(String filename) {
        // Not used for Glob Rules; using the DirectoryScanner instead
        // It CAN be moved here if so desired.
        return Optional.empty() ;
    }
}
