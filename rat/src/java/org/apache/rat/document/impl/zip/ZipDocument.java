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
package org.apache.rat.document.impl.zip;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.rat.document.IDocument;
import org.apache.rat.document.IDocumentCollection;
import org.apache.rat.document.UnreadableArchiveException;

class ZipDocument implements IDocument {

    private final ZipEntry entry;
    private final ZipFile zipFile;
    
    public ZipDocument(final ZipEntry entry, final ZipFile zipFile) {
        super();
        this.entry = entry;
        this.zipFile = zipFile;
    }

    public Reader reader() throws IOException {
        final InputStream input = zipFile.getInputStream(entry);
        final InputStreamReader result = new InputStreamReader(input);
        return result;
    }

    public String getName() {
        return ZipUtils.getName(entry);
    }

    public String getURL() {
        return ZipUtils.getUrl(entry);
    }

    public String toString() {
        final StringBuffer buffer = new StringBuffer("[ZipDocument '");
        buffer.append(getName());
        buffer.append("']");
        return buffer.toString();
    }

    public IDocumentCollection readArchive() throws IOException {
        throw new UnreadableArchiveException();
    }
}
