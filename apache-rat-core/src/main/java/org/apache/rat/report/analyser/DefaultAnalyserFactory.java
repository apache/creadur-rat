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

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.document.IDocument;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.document.impl.guesser.ArchiveGuesser;
import org.apache.rat.document.impl.guesser.BinaryGuesser;
import org.apache.rat.document.impl.guesser.NoteGuesser;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.FileType;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.impl.FileTypeClaim;

/**
 * Creates default analysers.
 *
 */
public class DefaultAnalyserFactory {
  
    public static final IDocumentAnalyser createDefaultAnalyser(final IClaimReporter reporter, 
            final IHeaderMatcher matcher) {
        
        return new DefaultAnalyser(reporter, matcher);
    }
    
    private final static class DefaultAnalyser implements IDocumentAnalyser {

        private final IClaimReporter reporter;
        private final IHeaderMatcher matcher;
        
        public DefaultAnalyser(final IClaimReporter reporter, final IHeaderMatcher matcher) {
            super();
            this.reporter = reporter;
            this.matcher = matcher;
        }

        public void analyse(IDocument document) throws RatDocumentAnalysisException {
            try {
                reporter.report(document);
                final FileType type;
                if (NoteGuesser.isNote(document)) {
                    type = FileType.NOTICE;
                } else if (ArchiveGuesser.isArchive(document)) {
                    type = FileType.ARCHIVE;
                } else if (BinaryGuesser.isBinary(document)) {
                    type = FileType.BINARY;
                } else {
                    type = FileType.STANDARD;
                    final DocumentHeaderAnalyser headerAnalyser = new DocumentHeaderAnalyser(matcher, reporter);
                    headerAnalyser.analyse(document);
                }
                reporter.claim(new FileTypeClaim(document, type));
            } catch (RatReportFailedException e) {
                throw new RatReportAnalysisResultException(e);
            }
        }        
    }
}
