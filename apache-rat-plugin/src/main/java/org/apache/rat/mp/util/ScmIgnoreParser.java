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


import org.apache.maven.plugin.logging.Log;
import org.apache.rat.config.SourceCodeManagementSystems;
import org.apache.rat.mp.util.ignore.GitIgnoreMatcher;
import org.apache.rat.mp.util.ignore.GlobIgnoreMatcher;
import org.apache.rat.mp.util.ignore.IgnoreMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper to parse SCM ignore files to add entries as excludes during RAT runs.
 * Since we log errors it needs to reside inside of the maven plugin.
 */
public final class ScmIgnoreParser {
    private ScmIgnoreParser() {
        // prevent instantiation of utility class
    }

    /**
     * Parse ignore files from all known SCMs that have ignore files.
     *
     * @param log     Show information via maven logger.
     * @param baseDir base directory from which to look for SCM ignores.
     * @return Exclusions from the SCM ignore files.
     */
    public static List<IgnoreMatcher> getExclusionsFromSCM(final Log log, final File baseDir) {
        List<IgnoreMatcher> ignoreMatchers = new ArrayList<>();
        for (SourceCodeManagementSystems scm : SourceCodeManagementSystems.values()) {
            switch (scm) {
                case GIT:
                    GitIgnoreMatcher gitIgnoreMatcher = new GitIgnoreMatcher(log, baseDir);
                    if (!gitIgnoreMatcher.isEmpty()) {
                        ignoreMatchers.add(gitIgnoreMatcher);
                    }
                    break;
                default:
                    if (scm.hasIgnoreFile()) {
                        GlobIgnoreMatcher ignoreMatcher = new GlobIgnoreMatcher(log, new File(baseDir, scm.getIgnoreFile()));
                        if (!ignoreMatcher.isEmpty()) {
                            ignoreMatchers.add(ignoreMatcher);
                        }
                    }
                    break;
            }
        }
        return ignoreMatchers;
    }


}
