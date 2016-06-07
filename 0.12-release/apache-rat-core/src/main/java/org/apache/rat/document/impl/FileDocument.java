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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

/**
 * Document wrapping a file of undetermined composition.
 *
 */
public class FileDocument implements Document {

    private final File file;
    private final String name;
    private final MetaData metaData = new MetaData();
    
    public FileDocument(final File file) {
        super();
        this.file = file;
        name = DocumentImplUtils.toName(file);
    }

    public boolean isComposite() {
        return DocumentImplUtils.isZip(file);
    }

    public Reader reader() throws IOException {
        return new FileReader(file);
    }

    public String getName() {
        return name;
    }

    public MetaData getMetaData() {
        return metaData;
    }    
    
    public InputStream inputStream() throws IOException {
        return new FileInputStream(file);
    }

    /**
     * Representations suitable for logging.
     * @return a <code>String</code> representation 
     * of this object.
     */
    @Override
    public String toString()
    {
        return "FileDocument ( "
            + "file = " + this.file + " "
            + "name = " + this.name + " "
            + "metaData = " + this.metaData + " "
            + " )";
    }
    
    
}
