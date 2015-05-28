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

import org.apache.commons.io.IOUtils;
import org.apache.rat.api.Document;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.RatDocumentAnalysisException;

import java.io.IOException;
import java.io.Reader;

public class DocumentHeaderAnalyser implements IDocumentAnalyser {

    private final IHeaderMatcher matcher;
    
    public DocumentHeaderAnalyser(final IHeaderMatcher matcher) {
        super();
        this.matcher = matcher;
    }

    public void analyse(Document document) throws RatDocumentAnalysisException {
        Reader reader = null;
        try {
            reader = document.reader();
            // TODO: worker function should be moved into this class
            HeaderCheckWorker worker = new HeaderCheckWorker(reader, matcher, document);
            worker.read();
        } catch (IOException e) {
            throw new RatDocumentAnalysisException("Cannot read header", e);
        } catch (RatHeaderAnalysisException e) {
            throw new RatDocumentAnalysisException("Cannot analyse header", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
     }

}
