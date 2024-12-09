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
import org.apache.rat.report.xml.writer.XmlWriter;
import org.apache.rat.testhelpers.XmlUtils;
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
}
