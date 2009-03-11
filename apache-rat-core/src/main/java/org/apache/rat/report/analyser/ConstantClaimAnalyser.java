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
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.report.claim.HeaderTypeObject;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.IObject;
import org.apache.rat.report.claim.IPredicate;

public class ConstantClaimAnalyser extends AbstractSingleClaimAnalyser {
    private final HeaderTypeObject object;

    public ConstantClaimAnalyser(final IClaimReporter reporter, final IPredicate predicate, 
            final HeaderTypeObject object, final boolean isLiteral) {
        super(reporter, predicate, isLiteral);
        this.object = object;
    }

    protected IObject toObject(IDocument document) throws RatDocumentAnalysisException {
        return object;
    }
}
