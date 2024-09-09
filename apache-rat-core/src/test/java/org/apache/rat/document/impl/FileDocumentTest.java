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

import org.apache.rat.api.Document;
import org.apache.rat.test.utils.Resources;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileDocumentTest {
    private Document document;
    private File file;
    
    @BeforeEach
    public void setUp() throws Exception {
        File basedir = basedir = new File(Files.currentFolder(), Resources.SRC_TEST_RESOURCES);
        file = Resources.getResourceFile("elements/Source.java");
        document = new FileDocument(new DocumentName(basedir), file, p -> true);
    }

    @Test
    public void reader() throws Exception {
        Reader reader = document.reader();
        assertNotNull(reader, "Reader should be returned");
        assertEquals("package elements;", 
                 new BufferedReader(reader).readLine(), "First file line expected");
    }

    @Test
    public void getName() {
        final DocumentName name = document.getName();
        assertNotNull(name, "Name is set");
    }
}
