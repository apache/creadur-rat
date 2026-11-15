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
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TikaProcessorTest {
    /**
     * Used to swallow a MalformedInputException and return false
     * because the encoding of the stream was different from the
     * platform's default encoding.
     *
     * @see <a href="https://issues.apache.org/jira/browse/RAT-81">RAT-81</a>
     */
    @Test
    void RAT81() {
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
    void UTF16_input() throws Exception {
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
    void UTF8_input() throws Exception {
        FileDocument doc = mkDocument("/binaries/UTF8_with_signature.xml");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @Test
    void RAT178Test() {
        FileDocument doc = new FileDocument(new File("/not_a_real_file"), DocumentNameMatcher.MATCHES_ALL);
        assertThrows(RatDocumentAnalysisException.class, () ->TikaProcessor.process(doc));
    }

    @Test
    void missNamedBinaryTest() throws Exception {
        FileDocument doc = mkDocument("/binaries/Image-png.not");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.BINARY, doc.getMetaData().getDocumentType());
    }

    @Test
    void plainTextTest() throws Exception {
        FileDocument doc = mkDocument(Resources.getExampleResource("exampleData/Text.txt"));
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @Test
    void emptyFileTest() throws Exception {
        FileDocument doc = mkDocument(Resources.getExampleResource("exampleData/sub/Empty.txt"));
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @Test
    void javaFileWithChineseCharacters_RAT301() throws Exception {
        FileDocument doc = mkDocument("/tikaFiles/standard/ChineseCommentsJava.java");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @ParameterizedTest
    @CsvSource({
        "text/plain, STANDARD",
        "text/x-rat-test, STANDARD",
        "application/x-sh, STANDARD",
        "application/x-bat, STANDARD",
        "application/x-asp, STANDARD",
        "application/xml, STANDARD",
        "application/pdf, BINARY",
        "application/pdf; version=1.7, BINARY",
        "application/zip, ARCHIVE",
        "application/gzip, ARCHIVE",
        "application/x-compress, ARCHIVE",
        "application/x-tar, ARCHIVE",
        "application/java-archive, ARCHIVE",
        "application/x-rat-test+xml, STANDARD",
        "image/x-rat-test+xml, STANDARD",
        "application/x-rat-test-zip, ARCHIVE",
        "application/x-rat-test-bzip2, ARCHIVE",
        "application/x-rat-test-gzip, ARCHIVE",
        "application/x-rat-test-compressed, ARCHIVE",
        "application/x-rat-test+zip, ARCHIVE"
    })
    void testFromMediaType(String mimeType, Document.Type expected) {
        assertThat(TikaProcessor.fromMediaType(MediaType.parse(mimeType))).contains(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "application/epub+zip, ARCHIVE",
        "application/x-ibooks+zip, ARCHIVE",
        "application/x-xliff+zip, ARCHIVE",
        "application/x-gzip, ARCHIVE",
        "application/x-bzip2, ARCHIVE",
        "application/x-7z-compressed, ARCHIVE",
        "application/x-rar-compressed, ARCHIVE",
        "application/vnd.wap.xhtml+xml, STANDARD",
        "application/xhtml+xml, STANDARD",
        "application/x-xliff+xml, STANDARD",
        "application/rls-services+xml, STANDARD",
        "application/xslt+xml, STANDARD",
        "image/svg+xml, STANDARD",
        "application/x-fictionbook+xml, STANDARD"
    })
    void testSubtypeClassificationWithoutMapEntry(String mimeType, Document.Type expected) {
        assertThat(TikaProcessor.getDocumentTypeMap()).doesNotContainKey(mimeType);
        assertThat(TikaProcessor.fromMediaType(MediaType.parse(mimeType))).contains(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "application/x-rat-unmapped",
        "application/octet-stream",
        "application/x-rat-test+xml-extra",
        "application/x-rat-test-zip-extra",
        "application/x-rat-test-bzip2-extra",
        "application/x-rat-test-gzip-extra",
        "application/x-rat-test-compressed-extra",
        "application/x-rat-test+zip-extra"
    })
    void testUnmappedMediaType(String mimeType) {
        assertThat(TikaProcessor.fromMediaType(MediaType.parse(mimeType))).isEmpty();
    }

    @Test
    void testTikaFiles() throws RatDocumentAnalysisException {
        File dir = new File("src/test/resources/tikaFiles");
        Map<String, Document.Type> unseenMime = TikaProcessor.getDocumentTypeMap();
        Set<String> fallbackMimeTypes = new TreeSet<>();
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
                    if (!TikaProcessor.fromMediaType(doc.getMetaData().getMediaType()).isPresent()) {
                        fallbackMimeTypes.add(mimeType);
                    }
                }
            }
        }
        System.out.println("** Untested mime types: " + unseenMime.keySet().size() +
                " of " + TikaProcessor.getDocumentTypeMap().size() + " **");
        unseenMime.keySet().forEach(System.out::println);
        System.out.println("** MIME types using the BINARY fallback **");
        fallbackMimeTypes.forEach(System.out::println);
        for (Document.Type type : Document.Type.values()) {
            System.out.format("Tested %s %s files%n", statistic.getCounter(type), type);
        }
    }

    @Test
    void testDetectionOfInvalidData() throws IOException {
        byte[] invalidData = new byte[] {
                0x00, (byte) 0xFF, 0x00, (byte) 0xFE,
                0x01, (byte) 0x80, 0x00, 0x7F
        };
        // as Tika works with a probabilistic encoding detection it does not return NO encoding
        assertThat(TikaProcessor.detectCharset(new ByteArrayInputStream(invalidData), null)).isEqualTo(Charset.forName("Windows-1258"));
    }

    @Test
    void testEmptyFileEncoding() throws IOException {
        byte[] empty = {};
        assertThat(TikaProcessor.detectCharset(new ByteArrayInputStream(empty), null)).isNull();
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
