/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.RatException;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLConfigurationWriterTest {

    @Test
    public void roundTrip() throws RatException {
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(Defaults.builder().build());
        config.listFamilies(LicenseFilter.ALL);
        config.listLicenses(LicenseFilter.ALL);
        XMLConfigurationWriter underTest = new XMLConfigurationWriter(config);
        StringWriter writer = new StringWriter();
        underTest.write(writer);
        writer.flush();
        System.out.println(writer);
        XMLConfigurationReader reader = new XMLConfigurationReader();
        StringReader strReader = new StringReader(writer.toString());
        reader.read(strReader);
        reader.readLicenses();
    }
    
    @Test
    public void testGen() throws Exception {
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(Defaults.builder().build());
        config.listFamilies(LicenseFilter.ALL);
        config.listLicenses(LicenseFilter.ALL);
        XMLConfigurationWriter underTest = new XMLConfigurationWriter(config);
        Optional<ILicense> opt = config.getLicenses(LicenseFilter.APPROVED).stream()
                .filter(l -> "GEN".equals(l.getId())).findAny();
        assertTrue(opt.isPresent());
        Description description = opt.get().getDescription();
        StringWriter sw = new StringWriter();
        XmlWriter writer = new XmlWriter(sw);
        underTest.writeDescription(writer, description, opt.get());
        writer.closeDocument();
        String result = sw.toString();

        XPath xPath = XPathFactory.newInstance().newXPath();
        Document doc = XmlUtils.toDom(new ByteArrayInputStream(result.getBytes()));

        Node any = (Node) xPath.compile("/license[@id='GEN']/any").evaluate(doc, XPathConstants.NODE);
        assertNotNull(any, "GEN/any node missing");
        assertEquals(0, any.getChildNodes().getLength());
        assertNotNull(any.getAttributes().getNamedItem("resource"), "'resource' attribute missing");
    }
}
