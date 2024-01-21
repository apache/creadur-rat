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
package org.apache.rat.report.claim.util;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.xml.writer.IXmlWriter;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;


public class ClaimReporterMultiplexer implements RatReport {
    private static final String RAT_REPORT = "rat-report";
    private static final String TIMESTAMP = "timestamp";
    
    private final IDocumentAnalyser analyser;
    private final List<? extends RatReport> reporters;
    private final IXmlWriter writer;
    private final boolean dryRun; 

    public ClaimReporterMultiplexer(final boolean dryRun, final IXmlWriter writer, final IDocumentAnalyser pAnalyser, final List<? extends RatReport> reporters) {
        analyser = pAnalyser;
        this.reporters = reporters;
        this.writer = writer;
        this.dryRun = dryRun;
    }

    public void report(Document document) throws RatException {
        if (!dryRun) {
            if (analyser != null) {
                try {
                    analyser.analyse(document);
                } catch (RatDocumentAnalysisException e) {
                    throw new RatException(e.getMessage(), e);
                }
            }
            for (RatReport report : reporters) {
                report.report(document);
            } 
        }
    }

    public void startReport() throws RatException {
        try {
            writer.openElement(RAT_REPORT)
                .attribute(TIMESTAMP,
                           DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT
                           .format(Calendar.getInstance()));
        } catch (IOException e) {
            throw new RatException("Cannot open start element", e);
        }
        for (RatReport report : reporters) {
            report.startReport();
        } 
    }

    public void endReport() throws RatException {
        for (RatReport report : reporters) {
            report.endReport();
        } 
        try {
            writer.closeDocument();
        } catch (IOException e) {
            throw new RatException("Cannot close last element", e);
        }
    }
}
