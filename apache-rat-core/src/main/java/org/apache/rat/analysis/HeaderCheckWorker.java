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
import java.util.Objects;

import org.apache.rat.ConfigurationException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.license.ILicense;

/**
 * Reads from a stream to check license.
 * <p>
 * <strong>Note</strong> that this class is not thread safe.
 * </p>
 */
class HeaderCheckWorker {

    /* TODO revisit this class.  It is only used in one place and can be moved inline as the DocumentHeaderAnalyser states.
     * However, it may also be possible to make the entire set threadsafe so that multiple files can be checked simultaneously.
     */
    /**
     * The default number of header lines to read while looking for the license
     * information.
     */
    public static final int DEFAULT_NUMBER_OF_RETAINED_HEADER_LINES = 50;

    private final int numberOfRetainedHeaderLines;
    private final BufferedReader reader;
    private final ILicense license;
    private final Document document;

    private int headerLinesToRead;
    private boolean finished = false;

    /**
     * Convenience constructor wraps given <code>Reader</code> in a
     * <code>BufferedReader</code>.
     * 
     * @param reader The reader on the document. not null.
     * @param license The license to check against. not null.
     * @param name The document that is being checked. possibly null
     */
    public HeaderCheckWorker(Reader reader, final ILicense license, final Document name) {
        this(reader, DEFAULT_NUMBER_OF_RETAINED_HEADER_LINES, license, name);
    }

    /**
     * Constructs a check worker for the license against the specified document.
     * 
     * @param reader The reader on the document. not null.
     * @param numberOfRetainedHeaderLine the maximum number of lines to read to find
     * the license information.
     * @param license The license to check against. not null.
     * @param name The document that is being checked. possibly null
     */
    public HeaderCheckWorker(Reader reader, int numberOfRetainedHeaderLine, final ILicense license,
            final Document name) {
        Objects.requireNonNull(reader, "Reader may not be null");
        Objects.requireNonNull(license, "License may not be null");
        if (numberOfRetainedHeaderLine < 0) {
            throw new ConfigurationException("numberOfRetainedHeaderLine may not be less than zero");
        }
        this.reader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        this.numberOfRetainedHeaderLines = numberOfRetainedHeaderLine;
        this.license = license;
        this.document = name;
    }

    /**
     * @return {@code true} if the header check is complete.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Read the input and perform the header check.
     * 
     * @throws RatHeaderAnalysisException on IO Exception.
     */
    public void read() throws RatHeaderAnalysisException {
        if (!finished) {
            final StringBuilder headers = new StringBuilder();
            headerLinesToRead = numberOfRetainedHeaderLines;
            try {
                while (readLine(headers)) {
                    // do nothing
                }
                if (license.finalizeState().asBoolean()) {
                    document.getMetaData().reportOnLicense(license);
                } else {
                    document.getMetaData().reportOnLicense(UnknownLicense.INSTANCE);
                    document.getMetaData().set(new MetaData.Datum(MetaData.RAT_URL_HEADER_SAMPLE, headers.toString()));
                }
            } catch (IOException e) {
                throw new RatHeaderAnalysisException("Cannot read header for " + document, e);
            }
            license.reset();
        }
        finished = true;
    }

    boolean readLine(StringBuilder headers) throws IOException {
        String line = reader.readLine();
        boolean result = line != null;
        if (result) {
            if (headerLinesToRead-- > 0) {
                headers.append(line);
                headers.append('\n');
            }
            switch (license.matches(line)) {
            case t:
                result = false;
                break;
            case f:
            case i:
                result = true;
                break;
            }
        }
        return result;
    }
}
