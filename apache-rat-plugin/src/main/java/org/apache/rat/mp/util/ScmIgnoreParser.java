package org.apache.rat.mp.util;

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


import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.rat.config.SourceCodeManagementSystems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper to parse SCM ignore files to add entries as excludes during RAT runs.
 * Since we log errors it needs to reside inside of the maven plugin.
 */
public final class ScmIgnoreParser {
    private ScmIgnoreParser() {
        // prevent instantiation of utility class
    }

    private static final List<String> COMMENT_PREFIXES = Arrays.asList("#", "##", "//", "/**", "/*");

    /**
     * Parses excludes from the given SCM ignore file.
     *
     * @param log       Maven log to show output during RAT runs.
     * @param scmIgnore if <code>null</code> or invalid an empty list of exclusions is returned.
     * @return all exclusions (=non-comment lines) from the SCM ignore file.
     */
    public static List<String> getExcludesFromFile(final Log log, final File scmIgnore) {

        final List<String> exclusionLines = new ArrayList<>();

        if (scmIgnore != null && scmIgnore.exists() && scmIgnore.isFile()) {
            log.info("Parsing exclusions from " + scmIgnore);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(scmIgnore));
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
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
        return exclusionLines;
    }

    /**
     * Parse ignore files from all known SCMs that have ignore files.
     *
     * @param log     Show information via maven logger.
     * @param baseDir base directory from which to look for SCM ignores.
     * @return Exclusions from the SCM ignore files.
     */
    public static List<String> getExclusionsFromSCM(final Log log, final File baseDir) {
        List<String> exclusions = new ArrayList<>();
        for (SourceCodeManagementSystems scm : SourceCodeManagementSystems.values()) {
            if (scm.hasIgnoreFile()) {
                exclusions.addAll(getExcludesFromFile(log, new File(baseDir, scm.getIgnoreFile())));
            }
        }
        return exclusions;

    }

    /**
     * Determines whether the given line is a comment or not based on scanning
     * for prefixes
     * {@see COMMENT_PREFIXES}.
     *
     * @param line line to verify.
     * @return <code>true</code> if the given line is a commented out line.
     */
    static boolean isComment(final String line) {
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
}
