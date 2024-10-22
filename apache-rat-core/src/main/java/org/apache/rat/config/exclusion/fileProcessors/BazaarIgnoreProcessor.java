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

import static java.lang.String.format;

/**
 * A processor for {@code .bzrignore} files.
 */
public final class BazaarIgnoreProcessor extends DescendingFileProcessor {
    /**
     * Constructor.
     */
    public BazaarIgnoreProcessor() {
        super(".bzrignore", "#");
    }

    @Override
    public String modifyEntry(final DocumentName baseName, final String entry) {
        if (entry.startsWith("RE:")) {
            String line = entry.substring("RE:".length()).trim();
            String pattern = line.startsWith("^") ? line.substring(1) : line;
            return format(REGEX_FMT, pattern);
        }
        return entry;
    }
}
