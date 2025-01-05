/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.document;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class ArchiveEntryName extends DocumentName {
    /** Then name of the document that contains this entry */
    private final DocumentName archiveFileName;

    private static DocumentName.Builder prepareBuilder(final DocumentName archiveFileName, final String archiveEntryName) {
        String root = archiveFileName.getName() + "#";
        FSInfo fsInfo = new FSInfo("archiveEntry", "/", true, Collections.singletonList(root));
        return DocumentName.builder(fsInfo)
                .setRoot(root)
                .setBaseName(root + "/")
                .setName(archiveEntryName);
    }
    public ArchiveEntryName(final DocumentName archiveFileName, final String archiveEntryName) {
        super(prepareBuilder(archiveFileName, archiveEntryName));
        this.archiveFileName = archiveFileName;
    }

    @Override
    public File asFile() {
        return archiveFileName.asFile();
    }

    @Override
    public Path asPath() {
        return Paths.get(archiveFileName.asPath().toString(), "#", super.asPath().toString());
    }

    @Override
    public DocumentName resolve(final String child) {
        return new ArchiveEntryName(this.archiveFileName, super.resolve(child).localized());
    }

    @Override
    public String getBaseName() {
        return archiveFileName.getName() + "#";
    }

    @Override
    boolean startsWithRootOrSeparator(final String candidate, final String root, final String separator) {
        return super.startsWithRootOrSeparator(candidate, root, separator);
    }

    @Override
    public String localized(final String dirSeparator) {
        String superLocal = super.localized(dirSeparator);
        superLocal = superLocal.substring(superLocal.lastIndexOf("#") + 1);
        return archiveFileName.localized(dirSeparator) + "#" + superLocal;
    }
}
