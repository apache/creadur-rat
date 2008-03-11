/*
 * Copyright 2006 Robert Burrell Donkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package rat.report.analyser;

import rat.document.IDocument;
import rat.document.IDocumentAnalyser;
import rat.document.RatDocumentAnalysisException;
import rat.report.RatReportFailedException;
import rat.report.claim.IClaimReporter;

public abstract class AbstractSingleClaimAnalyser implements IDocumentAnalyser {

    private final IClaimReporter reporter;
    private final CharSequence predicate;
    private final boolean isLiteral;
    
    public AbstractSingleClaimAnalyser(final IClaimReporter reporter, final CharSequence predicate, 
            final boolean isLiteral) {
        super();
        this.reporter = reporter;
        this.predicate = predicate;
        this.isLiteral = isLiteral;
    }

    public void analyse(IDocument document) throws RatDocumentAnalysisException {
        final String name = document.getName();
        try {
            final CharSequence object = toObject(document);
            reporter.claim(name, predicate, object, isLiteral);
        } catch (RatReportFailedException e) {
            throw new RatReportAnalysisResultException(e);
        }
    }
    
    protected abstract CharSequence toObject(final IDocument document) throws RatDocumentAnalysisException;
}
