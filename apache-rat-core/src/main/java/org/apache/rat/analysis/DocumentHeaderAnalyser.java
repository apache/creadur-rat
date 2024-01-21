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

import java.io.IOException;
import java.io.Reader;

import org.apache.rat.api.Document;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.license.ILicense;
import org.apache.rat.utils.Log;

import static java.lang.String.format;

/**
 * A Document analyzer that analyses document headers for a license.
 */
class DocumentHeaderAnalyser implements IDocumentAnalyser {

    /**
     * The license to analyse
     */
    private final ILicense license;
    /** the logger to use */
    private final Log log;

    /**
     * Constructs the HeaderAnalyser for the specific license.
     * 
     * @param license The license to analyse
     */
    public DocumentHeaderAnalyser(final Log log, final ILicense license) {
        super();
        this.license = license;
        this.log = log;
    }

    @Override
    public void analyse(Document document) throws RatDocumentAnalysisException {
        try (Reader reader = document.reader()) {
            log.debug(format("Processing: %s", document));
            HeaderCheckWorker worker = new HeaderCheckWorker(reader, license, document);
            worker.read();
        } catch (IOException e) {
            throw new RatDocumentAnalysisException("Cannot read header", e);
        } catch (RatHeaderAnalysisException e) {
            throw new RatDocumentAnalysisException("Cannot analyse header", e);
        }
    }

}
