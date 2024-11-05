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
package org.apache.rat.report.claim;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.license.ILicense;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.xml.XmlElements;
import org.apache.rat.report.xml.writer.IXmlWriter;

/**
 * A claim reporter to write the XML document.
 */
public class SimpleXmlClaimReporter implements RatReport {


    /** the writer to write to */
    private final XmlElements xmlElements;

    /**
     * Constructor.
     * @param writer The writer to write the report to.
     */
    public SimpleXmlClaimReporter(final IXmlWriter writer) {
        this.xmlElements = new XmlElements(writer);
    }

    @Override
    public void report(final Document document) throws RatException {
        final MetaData metaData = document.getMetaData();
        xmlElements.document(document);
        for (Iterator<ILicense> iter = metaData.licenses().iterator(); iter.hasNext();) {
            final ILicense license = iter.next();
            xmlElements.license(license, metaData.isApproved(license));
        }
        final String sample = metaData.getSampleHeader();
        if (StringUtils.isNotBlank(sample)) {
            xmlElements.sample(sample);
        }
        xmlElements.closeElement();
    }
}
