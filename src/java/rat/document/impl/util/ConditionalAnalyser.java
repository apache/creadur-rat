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
package rat.document.impl.util;

import rat.document.IDocument;
import rat.document.IDocumentAnalyser;
import rat.document.IDocumentMatcher;
import rat.document.RatDocumentAnalysisException;

/**
 * Analyses documents matching given condition.
 *
 */
public class ConditionalAnalyser implements IDocumentMatcher, IDocumentAnalyser {

    private final IDocumentMatcher matcher;
    private final IDocumentAnalyser analyser;
    
    public ConditionalAnalyser(final IDocumentMatcher matcher, final IDocumentAnalyser analyser) {
        super();
        this.matcher = matcher;
        this.analyser = analyser;
    }

    public boolean matches(final IDocument document) throws RatDocumentAnalysisException {
        final boolean result = matcher.matches(document);
        if (result) {
            analyser.analyse(document);
        }
        return result;
    }

    public void analyse(final IDocument document) throws RatDocumentAnalysisException {
        matches(document);
    }
}
