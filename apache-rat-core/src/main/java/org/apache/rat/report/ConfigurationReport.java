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
package org.apache.rat.report;

import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.RatException;
import org.apache.rat.configuration.XMLConfigurationWriter;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.xml.writer.IXmlWriter;

/**
 * A report that dumps the ReportConfiguration into the XML output.
 */
public class ConfigurationReport implements RatReport {
    /** The report configuration to report on. */
    private final ReportConfiguration configuration;
    /** The XML writer to write the report with. */
    private final IXmlWriter writer;

    /**
     * Constructor.
     *
     * @param writer The writer to write the XML data to.
     * @param configuration the configuration to dump
     */
    public ConfigurationReport(final IXmlWriter writer, final ReportConfiguration configuration) {
        this.configuration = configuration;
        this.writer = writer;
    }

    @Override
    public void startReport() throws RatException {
        if (configuration.listFamilies() != LicenseFilter.NONE || configuration.listLicenses() != LicenseFilter.NONE) {
            new XMLConfigurationWriter(configuration).write(writer);
        }
    }
}
