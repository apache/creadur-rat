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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.rat.api.Document;


public class MonolithicFileDocument extends AbstractMonolithicDocument {
    private static final String FILE_URL_PREFIX = "file";

    private final File file;

    /**
     * @return Creates and returns a new instance.
     *
     * @param url The document is read from the given URL.
     */
    public static Document newInstance(final URL url) {
        if (FILE_URL_PREFIX.equals(url.getProtocol())) {
            final File f = new File(url.getFile());
            return new MonolithicFileDocument(f);
        }
        return new AbstractMonolithicDocument(url.toExternalForm()){
            public Reader reader() throws IOException {
                return new InputStreamReader(inputStream(), StandardCharsets.UTF_8);
           }

            public InputStream inputStream() throws IOException {
                return url.openStream();
            }
        };
    }

     public MonolithicFileDocument(final File file) {
        super(DocumentImplUtils.toName(file));
        this.file = file;
    }

     public Reader reader() throws IOException {
        return new FileReader(file);
    }

     public InputStream inputStream() throws IOException {
        return new FileInputStream(file);
    }
}
