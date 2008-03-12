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
package rat.document.impl.zip;

import java.io.File;
import java.io.IOException;

import rat.document.IDocumentCollection;
import rat.document.impl.CompositeDocument;

/**
 * Document which is a zip file.
 *
 */
public class ZipFileDocument extends CompositeDocument {

    private final File file;
    private IDocumentCollection contents;
    
    public ZipFileDocument(final File file) {
        super(file.getPath());
        this.file = file;
    }
    
    public synchronized IDocumentCollection readArchive() throws IOException {
        if (contents == null) {
            contents = ZipDocumentFactory.load(file);
        }
        return contents;
    }
}
