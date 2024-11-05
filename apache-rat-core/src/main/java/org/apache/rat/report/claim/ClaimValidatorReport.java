/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.report.claim;

import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.RatException;
import org.apache.rat.config.results.ClaimValidator;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.xml.XmlElements;
import org.apache.rat.report.xml.writer.IXmlWriter;

/**
 * A RatReport that published the results of the Claim validator.
 */
public class ClaimValidatorReport implements RatReport {
    /**
     * The XmlElements tool to work with.
     */
    private final XmlElements elements;
    /**
     * The claim statistics to report
     */
    private final ClaimStatistic statistic;
    /**
     * The validator for the statistics.
     */
    private final ClaimValidator validator;

    /**
     * Constructor.
     * @param writer the XMLWriter to write with.
     * @param statistic the Claim statistics to report.
     * @param configuration the configuration for the run.
     */
    public ClaimValidatorReport(final IXmlWriter writer, final ClaimStatistic statistic, final ReportConfiguration configuration) {
        this.elements = new XmlElements(writer);
        this.statistic = statistic;
        this.validator = configuration.getClaimValidator();
    }

    @Override
    public void endReport() throws RatException {
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            int count = statistic.getCounter(counter);
            elements.statistics(counter.name(), count, validator.isValid(counter, count));
        }
    }
}
