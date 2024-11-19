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
package org.apache.rat.analysis;

import org.apache.rat.api.Document;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.document.DocumentName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TikaProcessorTest {
    /**
     * Used to swallow a MalformedInputException and return false
     * because the encoding of the stream was different from the
     * platform's default encoding.
     *
     * @see "RAT-81"
     */
    @Test
    public void RAT81() {
        // create a document that throws a MalformedInputException
        Document doc = mkDocument(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new MalformedInputException(0);
            }
        }, DocumentNameMatcher.MATCHES_ALL);
        assertThrows(RatDocumentAnalysisException.class, () -> TikaProcessor.process(doc));
    }

    @Test
    public void UTF16_input() throws Exception {
        Document doc = mkDocument(Resources.getResourceStream("/binaries/UTF16_with_signature.xml"),
                DocumentNameMatcher.MATCHES_ALL);
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    private FileDocument mkDocument(File f) {
        return new FileDocument(DocumentName.builder(f).build(), f, DocumentNameMatcher.MATCHES_ALL);
    }

    private FileDocument mkDocument(String fileName) throws IOException {
        return mkDocument(Resources.getResourceFile(fileName));
    }

    @Test
    public void UTF8_input() throws Exception {
        FileDocument doc = mkDocument("/binaries/UTF8_with_signature.xml");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @Test
    public void missNamedBinaryTest() throws Exception {
        FileDocument doc = mkDocument("/binaries/Image-png.not");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.BINARY, doc.getMetaData().getDocumentType());
    }

    @Test
    public void plainTextTest() throws Exception {
        FileDocument doc = mkDocument("/elements/Text.txt");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @Test
    public void emptyFileTest() throws Exception {
        FileDocument doc = mkDocument("/elements/sub/Empty.txt");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @Test
    public void javaFileWithChineseCharacters_RAT301() throws Exception {
        FileDocument doc = mkDocument("/tikaFiles/standard/ChineseCommentsJava.java");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @Test
    public void testTikaFiles() throws RatDocumentAnalysisException {
        File dir = new File("src/test/resources/tikaFiles");
        Map<String, Document.Type> unseenMime = TikaProcessor.getDocumentTypeMap();
        ClaimStatistic statistic = new ClaimStatistic();
        for (Document.Type docType : Document.Type.values()) {
            File typeDir = new File(dir, docType.name().toLowerCase(Locale.ROOT));
            if (typeDir.isDirectory()) {
                for (File file : Objects.requireNonNull(typeDir.listFiles())) {
                    Document doc = mkDocument(file);
                    String mimeType = TikaProcessor.process(doc);
                    statistic.incCounter(doc.getMetaData().getDocumentType(), 1);
                    assertEquals(docType, doc.getMetaData().getDocumentType(), () -> "Wrong type for " + file.toString());
                    unseenMime.remove(mimeType);
                }
            }
        }
        System.out.println( "untested mime types");
        unseenMime.keySet().forEach(System.out::println);
        for (Document.Type type : Document.Type.values()) {
            System.out.format("Tested %s %s files%n", statistic.getCounter(type), type);
        }
    }

    /**
     * Build a document with the specific input stream
     * @return a document with the specific input stream
     */
    private static Document mkDocument(final InputStream stream, DocumentNameMatcher nameMatcher) {

        return new Document(DocumentName.builder().setName("Testing Document").setBaseName("/").build(), nameMatcher) {

            @Override
            public Reader reader() {
                return new InputStreamReader(inputStream(), StandardCharsets.UTF_8);
            }

            @Override
            public InputStream inputStream() {
                return stream;
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public SortedSet<Document> listChildren() {
                return Collections.emptySortedSet();
            }
        };
    }
}
