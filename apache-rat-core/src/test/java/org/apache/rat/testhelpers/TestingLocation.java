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
package org.apache.rat.testhelpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.SortedSet;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

public class TestingLocation extends Document {

    public final String url;

    public TestingLocation() {
        this("name", "url");
    }

    public TestingLocation(String name) {
        this(name, "url");
    }

    public TestingLocation(String name, String url) {
        super(name);
        this.url = url;
    }

    public String getURL() {
        return url;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public SortedSet<Document> listChildren() {
        return Collections.emptySortedSet();
    }

    @Override
    public Reader reader() throws IOException {
        throw new UnsupportedOperationException("Opening Reader in TestingLocation");
    }

    @Override
    public InputStream inputStream() throws IOException {
        throw new UnsupportedOperationException("Opening inputStream in TestingLocation");
    }
}
