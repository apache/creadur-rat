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

import org.apache.rat.document.impl.DocumentName;

/**
 * @see <a href='https://git-scm.com/docs/gitignore'>.gitignore documentation</a>
 */
public class GitFileProcessor extends DescendingFileProcessor {

    public GitFileProcessor() {
        super(".gitignore", "#");
    }

    @Override
    public String modifyEntry(final DocumentName documentName, final String entry) {
        // An optional prefix "!" which negates the pattern;
        boolean prefix = entry.startsWith("!");
        String pattern = prefix ? entry.substring(1) : entry;

        // If there is a separator at the beginning or middle (or both) of the pattern, then
        // the pattern is relative to the directory level of the particular .gitignore file itself.
        // Otherwise, the pattern may also match at any level below the .gitignore level.
        int slashPos = pattern.indexOf("/");
        if (slashPos == -1 || slashPos == pattern.length() - 1) {
            pattern = "**/" + pattern;
        }
        // If there is a separator at the end of the pattern then the pattern will only match directories,
        // otherwise the pattern can match both files and directories.
        if (pattern.endsWith("/")) {
            pattern = pattern + "**";
        }
        return prefix ? "!" + pattern : pattern;
    }

}
