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

import nl.basjes.gitignore.GitIgnoreFileSet;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.Optional;

public class GitIgnoreMatcher implements IgnoreMatcher {

    private final GitIgnoreFileSet gitIgnoreFileSet;

    public GitIgnoreMatcher(final Log log, final File projectBaseDir) {
        log.debug("Recursively loading .gitignore files in " + projectBaseDir);
        // This will walk the project tree and load all .gitignore files
        gitIgnoreFileSet = new GitIgnoreFileSet(projectBaseDir);
    }

    @Override
    public boolean isEmpty() {
        return gitIgnoreFileSet.isEmpty();
    }

    @Override
    public Optional<Boolean> isIgnoredFile(String filename) {
        Boolean isIgnoredFile = gitIgnoreFileSet.isIgnoredFile(filename);
        if (isIgnoredFile == null) {
            return Optional.empty();
        }
        return Optional.of(isIgnoredFile);
    }

    @Override
    public String toString() {
        return "Loaded .gitignore data:\n" + gitIgnoreFileSet;
    }
}
