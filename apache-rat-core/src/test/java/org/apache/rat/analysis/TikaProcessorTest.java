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

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.rat.api.Document;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.document.DocumentName;
import org.apache.rat.utils.FileUtils;
import org.apache.rat.utils.DefaultLog;
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
import java.util.SortedSet;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TikaProcessorTest {

    @TempDir
    private static Path tempDir;

    private final DocumentName basedir;

    TikaProcessorTest() {
        basedir = DocumentName.builder(tempDir.toFile()).build();
    }

    private static File copyResource(DocumentName basedir, final String resourceName) throws IOException {
        final DocumentName outputName = DocumentName.builder(basedir).setName(resourceName).build();
        final File outputFile = outputName.asFile();
        FileUtils.mkDir(outputFile.getParentFile());
        try (InputStream input = TikaProcessorTest.class.getResourceAsStream(resourceName);
             OutputStream output = new FileOutputStream(outputFile)) {
            assertThat(input).isNotNull();
            IOUtils.copy(input, output);
        }
        return outputFile;
    }
    /**
     * Used to swallow a MalformedInputException and return false
     * because the encoding of the stream was different from the
     * platform's default encoding.
     *
     * @see <a href="https://issues.apache.org/jira/browse/RAT-81">RAT-81</a>
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
        Document doc = mkDocument("/binaries/UTF16_with_signature.xml");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }


    private FileDocument mkDocument(String fileName) throws IOException {
        return new FileDocument(basedir, copyResource(basedir, fileName), DocumentNameMatcher.MATCHES_ALL);
    }

    @Test
    public void UTF8_input() throws Exception {
        FileDocument doc = mkDocument("/binaries/UTF8_with_signature.xml");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @Test
    public void RAT178Test() {
        FileDocument doc = new FileDocument(new File("/not_a_real_file"), DocumentNameMatcher.MATCHES_ALL);
        assertThrows(RatDocumentAnalysisException.class, () ->TikaProcessor.process(doc));
    }

    @Test
    public void missNamedBinaryTest() throws Exception {
        FileDocument doc = mkDocument("/binaries/Image-png.not");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.BINARY, doc.getMetaData().getDocumentType());
    }

    @Test
    public void plainTextTest() throws Exception {
        FileDocument doc = mkDocument("/exampleData/Text.txt");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @Test
    public void emptyFileTest() throws Exception {
        FileDocument doc = mkDocument("/exampleData/sub/Empty.txt");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    @Test
    public void javaFileWithChineseCharacters_RAT301() throws Exception {
        FileDocument doc = mkDocument("/tikaFiles/standard/ChineseCommentsJava.java");
        TikaProcessor.process(doc);
        assertEquals(Document.Type.STANDARD, doc.getMetaData().getDocumentType());
    }

    public static List<FileDocument> getTikaTestFiles(final DocumentName basedir) throws IOException {
        final List<FileDocument> result = new ArrayList<>();
        final List<String> dirNames = new ArrayList<>();
        for (Document.Type docType : Document.Type.values()) {
            dirNames.add(docType.name().toLowerCase(Locale.ROOT));
        }
        final List<String> lines = IOUtils.readLines(TikaProcessorTest.class.getResource("/tikaFiles").openStream(),
                StandardCharsets.UTF_8);
        for (String line : lines) {
            if (dirNames.contains(line)) {
                final String path = "/tikaFiles/" + line;
                List<String> files = IOUtils.readLines(TikaProcessorTest.class.getResource(path).openStream(),
                        StandardCharsets.UTF_8);
                for (String file : files) {
                    result.add(new FileDocument(basedir, copyResource(basedir, path+"/"+file), DocumentNameMatcher.MATCHES_ALL));
                }
            } else {
                result.add(new FileDocument(basedir, copyResource(basedir, "/tikaFiles/"+line), DocumentNameMatcher.MATCHES_ALL));
            }
        }
        return result;
    }

    @Test
    public void testTikaFiles() throws RatDocumentAnalysisException, IOException {
        Pattern docTypeMatch  = Pattern.compile("/tikaFiles/([^/]+)/.*");
        List<FileDocument> tikaFiles = getTikaTestFiles(basedir);
        Map<String, Document.Type> unseenMime = TikaProcessor.getDocumentTypeMap();
        ClaimStatistic statistic = new ClaimStatistic();
        for (FileDocument document : tikaFiles) {
            String mimeType = TikaProcessor.process(document);
                    statistic.incCounter(document.getMetaData().getDocumentType(), 1);
                    String localizedName = document.getName().localized("/");
            Matcher matcher = docTypeMatch.matcher(localizedName);
            if (matcher.matches()) {
                String type = matcher.group(1);
                Document.Type expected = Document.Type.valueOf(type.toUpperCase(Locale.ROOT));
                assertThat(document.getMetaData().getDocumentType()).describedAs("Wrong type for " + document)
                        .isEqualTo(expected);
            }
                    unseenMime.remove(mimeType);
        }
        // TODO ensure that all mime-types are tested.
         //assertThat(unseenMime.keySet()).describedAs("Untested mime types").isEmpty();
        unseenMime.keySet().forEach( t -> DefaultLog.getInstance().warn("Untested mime-type: " + t));

        // TODO ensure that all document types are tested.
        for (Document.Type type : Document.Type.values()) {
            //assertThat(statistic.getCounter(type)).describedAs("Untested document type: " + type)
            //                .isGreaterThan(0);
            if (statistic.getCounter(type) == 0) {
                DefaultLog.getInstance().warn("Untested documentType: " + type);
            }
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
