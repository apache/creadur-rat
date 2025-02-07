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

import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.apache.rat.api.Document;
import org.apache.rat.test.utils.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

public class FileDocumentTest {
    private Document document;

    @TempDir
    private static Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {

        File basedir = new File(tempDir.toFile(), Resources.SRC_TEST_RESOURCES);
        basedir.mkdirs();
        File sourceData = Resources.getExampleResource("exampleData/Source.java");
        File file = new File(basedir, "Source.java");
        FileUtils.copyFile(sourceData, file);
        assertThat(file).exists();

        DocumentName docName = DocumentName.builder(basedir).build();
        document = new FileDocument(docName, file, DocumentNameMatcher.MATCHES_ALL);
    }

    @Test
    public void reader() throws Exception {
        Reader reader = document.reader();
        assertThat(reader).isNotNull();
        assertThat(new BufferedReader(reader).readLine()).isEqualTo("package elements;");
    }

    @Test
    public void getName() {
        final DocumentName name = document.getName();
        assertThat(name).isNotNull();
    }
}
