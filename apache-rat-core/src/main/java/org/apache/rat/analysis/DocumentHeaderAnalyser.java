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
import java.util.Collection;

import org.apache.rat.api.Document;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.license.ILicense;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * A Document analyzer that analyses document headers for a license.
 */
class DocumentHeaderAnalyser implements IDocumentAnalyser {

    /** The license to analyse */
    private final Collection<ILicense> licenses;


    /**
     * Constructs the HeaderAnalyser for the specific license.
     * @param licenses The licenses to analyse
     */
    DocumentHeaderAnalyser(final Collection<ILicense> licenses) {
        super();
        this.licenses = licenses;
    }

    @Override
    public void analyse(final Document document) {
        try (Reader reader = document.reader()) {
            DefaultLog.getInstance().debug(format("Processing: %s", document));
            HeaderCheckWorker worker = new HeaderCheckWorker(reader, licenses, document);
            worker.read();
        } catch (IOException e) {
            DefaultLog.getInstance().warn(String.format("Cannot read header of %s", document));
            document.getMetaData().setDocumentType(Document.Type.UNKNOWN);
        } catch (RatHeaderAnalysisException e) {
            DefaultLog.getInstance().warn(String.format("Cannot process header of %s", document));
            document.getMetaData().setDocumentType(Document.Type.UNKNOWN);
        }
    }
}
