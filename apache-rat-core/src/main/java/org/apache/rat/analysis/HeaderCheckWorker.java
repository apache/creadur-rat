package org.apache.rat.analysis;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.matchers.FullTextMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

/**
 * Reads from a stream to check license. <p> <strong>Note</strong> that this
 * class is not thread safe. </p>
 */
public class HeaderCheckWorker {

    /*
     * TODO revisit this class. It is only used in one place and can be moved inline
     * as the DocumentHeaderAnalyser states. However, it may also be possible to
     * make the entire set threadsafe so that multiple files can be checked
     * simultaneously.
     */
    /**
     * The default number of header lines to read while looking for the license
     * information.
     */
    public static final int DEFAULT_NUMBER_OF_RETAINED_HEADER_LINES = 50;

    private final int numberOfRetainedHeaderLines;
    private final BufferedReader reader;
    private final Collection<ILicense> licenses;
    private final Document document;

    /**
     * Read the input and perform the header check.
     *
     * @throws IOException on input failure
     */
    public static IHeaders readHeader(BufferedReader reader, int numberOfLines) throws IOException {
        final StringBuilder headers = new StringBuilder();
        int headerLinesRead = 0;
        String line;

        while (headerLinesRead < numberOfLines && (line = reader.readLine()) != null) {
            headers.append(line).append("\n");
        }
        final String raw = headers.toString();
        final String pruned = FullTextMatcher.prune(raw).toLowerCase(Locale.ENGLISH);
        return new IHeaders() {
            @Override
            public String raw() {
                return raw;
            }

            @Override
            public String pruned() {
                return pruned;
            }

            @Override
            public String toString() {
                return "HeaderCheckWorker";
            }
        };
    }

    /**
     * Convenience constructor wraps given <code>Reader</code> in a
     * <code>BufferedReader</code>.
     *
     * @param reader The reader on the document. not null.
     * @param license The license to check against. not null.
     * @param name The document that is being checked. possibly null
     */
    public HeaderCheckWorker(Reader reader, final Collection<ILicense> licenses, final Document name) {
        this(reader, DEFAULT_NUMBER_OF_RETAINED_HEADER_LINES, licenses, name);
    }

    /**
     * Constructs a check worker for the license against the specified document.
     *
     * @param reader The reader on the document. not null.
     * @param numberOfRetainedHeaderLine the maximum number of lines to read to find
     * the license information.
     * @param license The licenses to check against. not null.
     * @param document The document that is being checked. possibly null
     */
    public HeaderCheckWorker(Reader reader, int numberOfRetainedHeaderLine, final Collection<ILicense> licenses,
            final Document document) {
        Objects.requireNonNull(reader, "Reader may not be null");
        Objects.requireNonNull(licenses, "Licenses may not be null");
        if (numberOfRetainedHeaderLine < 0) {
            throw new ConfigurationException("numberOfRetainedHeaderLine may not be less than zero");
        }
        this.reader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        this.numberOfRetainedHeaderLines = numberOfRetainedHeaderLine;
        this.licenses = licenses;
        this.document = document;
    }

    /**
     * Read the input and perform the header check.
     *
     * @throws RatHeaderAnalysisException on IO Exception.
     */
    public void read() throws RatHeaderAnalysisException {
        try {
            final IHeaders headers = readHeader(reader, numberOfRetainedHeaderLines);
            licenses.stream().filter(lic -> lic.matches(headers)).forEach(document.getMetaData()::reportOnLicense);
            if (document.getMetaData().detectedLicense()) {
                if (document.getMetaData().licenses().anyMatch(
                        lic -> ILicenseFamily.GENTERATED_CATEGORY.equals(lic.getLicenseFamily().getFamilyCategory()))) {
                    document.getMetaData().setDocumentType(Document.Type.generated);
                }
            } else {
                document.getMetaData().reportOnLicense(UnknownLicense.INSTANCE);
                document.getMetaData().setSampleHeader(headers.raw());
            }
        } catch (IOException e) {
            throw new RatHeaderAnalysisException("Cannot read header for " + document, e);
        } finally {
            licenses.forEach(ILicense::reset);
        }
    }
}
