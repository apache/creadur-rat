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
import rat.document.RatDocumentAnalysisException;
import rat.report.claim.IClaimReporter;

public class ConstantClaimAnalyser extends AbstractSingleClaimAnalyser {

    private final CharSequence object;
    public ConstantClaimAnalyser(final IClaimReporter reporter, final CharSequence predicate, 
            final CharSequence object, final boolean isLiteral) {
        super(reporter, predicate, isLiteral);
        this.object = object;
    }

    protected CharSequence toObject(IDocument document) throws RatDocumentAnalysisException {
        return object;
    }

}
