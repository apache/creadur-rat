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

import java.io.IOException;
import java.io.StringWriter;

import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.RatException;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;

public class ConfigurationReportTest {

    
   @Test
   public void testAll() throws RatException, IOException {
       ReportConfiguration reportConfiguration = new ReportConfiguration(DefaultLog.INSTANCE);
       reportConfiguration.setFrom(Defaults.builder().build());
       reportConfiguration.listFamilies(LicenseFilter.all);
       reportConfiguration.listLicenses(LicenseFilter.all);
       StringWriter sw = new StringWriter();
       IXmlWriter writer = new XmlWriter(sw);
       
       ConfigurationReport report = new ConfigurationReport(writer, reportConfiguration);
       report.startReport();
       report.endReport();
       writer.closeDocument();
       String result = sw.toString();
      // AssertTrue( result.contains( ""));
   }
}
