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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.rat.api.Document;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.document.guesser.NoteGuesser;
import org.apache.rat.utils.DefaultLog;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;

/**
 * A wrapping around the Tika processor.
 */
public final class TikaProcessor {

    /** The Tika parser */
    private static final Tika TIKA = new Tika();
    /** A map of mime type string to non-BINARY types.
     * "text" types are already handled somewhere else
     * BINARY unless listed here
     */
    private static final Map<String, Document.Type> DOCUMENT_TYPE_MAP;

    static {
        DOCUMENT_TYPE_MAP = new HashMap<>();
//        org.apache.tika.parser.epub.EpubParser
        DOCUMENT_TYPE_MAP.put("application/x-ibooks+zip", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/epub+zip", Document.Type.ARCHIVE);

        DOCUMENT_TYPE_MAP.put("application/vnd.wap.xhtml+xml", Document.Type.STANDARD);
        DOCUMENT_TYPE_MAP.put("application/x-asp", Document.Type.STANDARD);
        DOCUMENT_TYPE_MAP.put("application/xhtml+xml", Document.Type.STANDARD);

//        org.apache.tika.parser.pdf.PDFParser", Type.BINARY);
        DOCUMENT_TYPE_MAP.put("application/pdf", Document.Type.BINARY);
//org.apache.tika.parser.pkg.CompressorParser
        DOCUMENT_TYPE_MAP.put("application/zlib", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-gzip", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-bzip2", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-compress", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-java-pack200", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-lzma", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/deflate64", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-lz4", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-snappy", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-brotli", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/gzip", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-bzip", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-xz", Document.Type.ARCHIVE);
//org.apache.tika.parser.pkg.PackageParser
        DOCUMENT_TYPE_MAP.put("application/x-tar", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/java-archive", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-arj", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-archive", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/zip", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-cpio", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-tika-unix-dump", Document.Type.ARCHIVE);
        DOCUMENT_TYPE_MAP.put("application/x-7z-compressed", Document.Type.ARCHIVE);
//org.apache.tika.parser.pkg.RarParser
        DOCUMENT_TYPE_MAP.put("application/x-rar-compressed", Document.Type.ARCHIVE);

//        org.apache.tika.parser.xliff.XLIFF12Parser
        DOCUMENT_TYPE_MAP.put("application/x-xliff+xml", Document.Type.STANDARD);
//        org.apache.tika.parser.xliff.XLZParser
        DOCUMENT_TYPE_MAP.put("application/x-xliff+zip", Document.Type.ARCHIVE);
//        org.apache.tika.parser.xml.DcXMLParser
        DOCUMENT_TYPE_MAP.put("application/xml", Document.Type.STANDARD);
        DOCUMENT_TYPE_MAP.put("image/svg+xml", Document.Type.STANDARD);
//        org.apache.tika.parser.xml.FictionBookParser
        DOCUMENT_TYPE_MAP.put("application/x-fictionbook+xml", Document.Type.STANDARD);
    }

    private TikaProcessor() {
        // do not instantiate
    }

    /**
     * Creates a copy of the document type map.
     * Exposed for testing.
     * @return a copy of the document type map.
     */
    static Map<String, Document.Type> getDocumentTypeMap() {
        return new HashMap<>(DOCUMENT_TYPE_MAP);
    }

    /**
     * Ensures that the input stream supports {@code mark}.
     * @param stream the stream to test.
     * @return a stream that supports {@code mark}.
     */
    public static InputStream markSupportedInputStream(final InputStream stream) {
        return stream.markSupported() ? stream : new BufferedInputStream(stream);
    }

    /**
     * Process the input document.
     * @param document the Document to process.
     * @return the mimetype as a string.
     * @throws RatDocumentAnalysisException on error.
     */
    public static String process(final Document document) throws RatDocumentAnalysisException {
        try (InputStream stream = markSupportedInputStream(document.inputStream())) {
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, document.getName().getShortName());
            String result = TIKA.detect(stream, metadata);
            String[] parts = result.split("/");
            MediaType mediaType = new MediaType(parts[0], parts[1]);
            document.getMetaData().setMediaType(mediaType);
            if (!document.isIgnored()) {
                document.getMetaData()
                        .setDocumentType(fromMediaType(mediaType));
            }
            if (Document.Type.STANDARD == document.getMetaData().getDocumentType()) {
                try {
                    document.getMetaData().setCharset(detectCharset(stream, document.getName()));
                    if (NoteGuesser.isNote(document)) {
                        document.getMetaData().setDocumentType(Document.Type.NOTICE);
                    }
                } catch (UnsupportedCharsetException e) {
                    document.getMetaData().setDocumentType(Document.Type.UNKNOWN);
                }
            }
            return result;
        } catch (IOException e) {
            throw new RatDocumentAnalysisException(e);
        }
    }

    /**
     * Determine the character set for the input stream. Input stream must implement {@code mark}.
     * @param stream the stream to check.
     * @param documentName the name of the document being processed.
     * @return the detected character set or {@code null} if not detectable.
     * @throws IOException on IO error.
     * @throws UnsupportedCharsetException on unsupported charset.
     */
    private static Charset detectCharset(final InputStream stream, final DocumentName documentName) throws IOException, UnsupportedCharsetException {
        final int bytesForCharsetDetection = 256;
        CharsetDetector encodingDetector = new CharsetDetector(bytesForCharsetDetection);
        encodingDetector.setText(stream);
        CharsetMatch charsetMatch = encodingDetector.detect();
        if (charsetMatch != null) {
            try {
                return Charset.forName(charsetMatch.getName());
            } catch (UnsupportedCharsetException e) {
                DefaultLog.getInstance().warn(String.format("Unsupported character set '%s' in file '%s'",
                        charsetMatch.getName(), documentName));
                throw e;
            }
        }
        return null;
    }

    /**
     * Gets the Document.Type based on the MediaType.
     * @param mediaType the media type to check.
     * @return The document type.
     */
    public static Document.Type fromMediaType(final MediaType mediaType) {
        if ("text".equals(mediaType.getType())) {
            return Document.Type.STANDARD;
        }

        Document.Type result = DOCUMENT_TYPE_MAP.get(mediaType.toString());
        return result == null ? Document.Type.BINARY : result;
    }
}
