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
package org.apache.rat.report.analyser;

import java.io.IOException;

import rat.document.IDocument;
import rat.document.IDocumentCollection;
import rat.document.RatDocumentAnalysisException;
import rat.report.claim.IClaimReporter;

public final class ReadableArchiveAnalyser extends AbstractSingleClaimAnalyser {

    public static final String READABLE_ARCHIVE_VALUE = "readable";
    public static final String UNREADABLE_ARCHIVE_VALUE = "unreadable";
    
    
    public ReadableArchiveAnalyser(IClaimReporter reporter) {
        super(reporter, "archive-type", false);
    }

    protected CharSequence toObject(IDocument document) throws RatDocumentAnalysisException {
        boolean readable = false;
        try {
            final IDocumentCollection contents = document.readArchive();
            if (contents != null) {
                readable = true;
            }
        } catch (IOException e) {
            readable = false;
        }
        String result = UNREADABLE_ARCHIVE_VALUE;
        if (readable) {
            result = READABLE_ARCHIVE_VALUE;
        }
        return result;
    }

}
