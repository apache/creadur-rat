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
package org.apache.rat.document;

import java.io.FileFilter;

/**
 * Creates a patch matcher based on the directory.
 */
@FunctionalInterface
public interface DocumentNameMatcherSupplier {
    DocumentNameMatcher get(DocumentName dir);

    /**
     * Creates a DocumentNameMatcherSupplier from a file filter.
     * @param fileFilter the file filter to process.
     * @return a DocumentNameMatcherSupplier.
     */
    static DocumentNameMatcherSupplier from(final FileFilter fileFilter) {
        DocumentNameMatcher nameMatcher = DocumentNameMatcher.from(fileFilter);
        return docName -> new TraceableDocumentNameMatcher(fileFilter::toString, nameMatcher);
    }

    /**
     * Create a DocumentNameMatcherSupplier from a DocumentNameMatcher and a name.
     * @param name the name for the matcher.
     * @param nameMatcher the matcher.
     * @return A DocumentNameMatcherDupplier.
     */
    static DocumentNameMatcherSupplier from(final String name, final DocumentNameMatcher nameMatcher) {
        TraceableDocumentNameMatcher tmatcher = TraceableDocumentNameMatcher.make(() -> name, nameMatcher);
        return docName -> tmatcher;
    }
}
