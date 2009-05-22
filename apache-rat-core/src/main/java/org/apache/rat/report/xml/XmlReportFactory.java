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
package org.apache.rat.report.xml;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.document.IDocument;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.impl.util.DocumentAnalyserMultiplexer;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.policy.DefaultPolicy;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.analyser.DefaultAnalyserFactory;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.impl.ClaimAggregator;
import org.apache.rat.report.claim.impl.xml.SimpleXmlClaimReporter;
import org.apache.rat.report.xml.writer.IXmlWriter;

/**
 * Creates reports.
 *
 */
public class XmlReportFactory {
    public static final RatReport createStandardReport(final IXmlWriter writer, 
            final IHeaderMatcher matcher, ClaimStatistic pStatistic) {
        return createStandardReport(writer, matcher, null, pStatistic);
    }
    
    public static final RatReport createStandardReport(final IXmlWriter writer, 
            final IHeaderMatcher matcher, final ILicenseFamily[] approvedLicenses,
            final ClaimStatistic pStatistic) {
        // TODO: this isn't very elegant :-/
        // TODO: should really pass in analysers but this means injecting reporter
        final IClaimReporter reporter;
        if (pStatistic == null) {
            reporter = new SimpleXmlClaimReporter(writer);
        } else {
            reporter = new ClaimAggregator(new SimpleXmlClaimReporter(writer));
        }
        final DefaultPolicy policy = new DefaultPolicy(approvedLicenses);
        
        final IDocumentAnalyser analyser = 
            DefaultAnalyserFactory.createDefaultAnalyser(reporter, matcher);
        final IDocumentAnalyser[] analysers = {analyser, policy};
        DocumentAnalyserMultiplexer analysisMultiplexer = new DocumentAnalyserMultiplexer(analysers);
        
        final RatReport result = new XmlReport(writer, analysisMultiplexer, reporter);
        if (pStatistic == null) {
            return result;
        }
        return new RatReport(){
            public void endReport() throws RatReportFailedException {
                result.endReport();
                ((ClaimAggregator) reporter).fillClaimStatistic(pStatistic);
            }
            public void report(IDocument pDocument)
                    throws RatReportFailedException {
                result.report(pDocument);
            }
            public void startReport() throws RatReportFailedException {
                result.startReport();
            }
        };
    }
}
