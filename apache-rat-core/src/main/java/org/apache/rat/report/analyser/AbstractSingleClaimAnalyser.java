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

import org.apache.rat.document.IDocument;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.BaseSubject;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.IObject;
import org.apache.rat.report.claim.IPredicate;
import org.apache.rat.report.claim.ISubject;

public abstract class AbstractSingleClaimAnalyser implements IDocumentAnalyser {

    private final IClaimReporter reporter;
    private final IPredicate predicate;
    private final boolean isLiteral;
    
    public AbstractSingleClaimAnalyser(final IClaimReporter reporter, final IPredicate predicate, 
            final boolean isLiteral) {
        super();
        this.reporter = reporter;
        this.predicate = predicate;
        this.isLiteral = isLiteral;
    }

    public void analyse(IDocument document) throws RatDocumentAnalysisException {
        final ISubject name = new BaseSubject(document);
        try {
            final IObject object = toObject(document);
            reporter.claim(name, predicate, object, isLiteral);
        } catch (RatReportFailedException e) {
            throw new RatReportAnalysisResultException(e);
        }
    }
    
    protected abstract IObject toObject(final IDocument document) throws RatDocumentAnalysisException;
}
