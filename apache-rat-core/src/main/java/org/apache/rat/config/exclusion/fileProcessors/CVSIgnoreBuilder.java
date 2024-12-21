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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.document.DocumentName;

/**
 * A file processor for the {@code .csvignore} file.
 */
public class CVSIgnoreBuilder extends MatcherSet.Builder {
    /**
     * The constructor.
     */
    public CVSIgnoreBuilder() {
        super(".cvsignore", (String) null);
    }

    @Override
    protected void process(final DocumentName documentName) {
        final File dir = new File(documentName.getName());
        Set<String> result = new HashSet<>();
        Iterator<String> iter = ExclusionUtils.asIterator(dir, StringUtils::isNotBlank);
        while (iter.hasNext()) {
            String line = iter.next();
            String[] parts = line.split("\\s+");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    result.add(localizePattern(documentName, part));
                }
            }
        }
        addIncluded(documentName.getBaseDocumentName(), result);
    }
}
