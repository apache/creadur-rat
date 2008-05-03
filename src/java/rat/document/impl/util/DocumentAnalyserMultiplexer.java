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
package org.apache.rat.document.impl.util;

import rat.document.IDocument;
import rat.document.IDocumentAnalyser;
import rat.document.RatDocumentAnalysisException;

public class DocumentAnalyserMultiplexer implements IDocumentAnalyser {

    private final IDocumentAnalyser[] analysers;
    
    public DocumentAnalyserMultiplexer(final IDocumentAnalyser[] analysers) {
        super();
        this.analysers = analysers;
    }

    public void analyse(IDocument document) throws RatDocumentAnalysisException {
        final int length = analysers.length;
        for (int i=0;i<length;i++) {
            analysers[i].analyse(document);
        }
    }

}
