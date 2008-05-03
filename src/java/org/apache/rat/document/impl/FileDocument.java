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
package org.apache.rat.document.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.rat.document.IDocument;
import org.apache.rat.document.IDocumentCollection;
import org.apache.rat.document.IFileUnarchiver;

/**
 * Document wrapping a file of undetermined composition.
 *
 */
public class FileDocument implements IDocument {

    private final File file;
    private final String name;
    private IFileUnarchiver unarchiver;
    
    public FileDocument(final File file, IFileUnarchiver unarchiver) {
        super();
        this.file = file;
        this.unarchiver = unarchiver;
        name = DocumentImplUtils.toName(file);
    }

    public IDocumentCollection readArchive() throws IOException {
        final IDocumentCollection result = unarchiver.unarchive(file);
        return result;
    }

    public Reader reader() throws IOException {
        return new FileReader(file);
    }

    public String getName() {
        return name;
    }

    
}
