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

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.RatException;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.junit.jupiter.api.Test;

public class XMLConfigurationWriterTest {

    @Test
    public void roundTrip() throws RatException {
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(Defaults.builder().build());
        config.listFamilies(LicenseFilter.ALL);
        config.listLicenses(LicenseFilter.ALL);
        XMLConfigurationWriter underTest = new XMLConfigurationWriter(config);
        StringWriter writer = new StringWriter();
        underTest.writeFamilyInfo(writer);
        writer.flush();
        System.out.println(writer);
        XMLConfigurationReader reader = new XMLConfigurationReader();
        StringReader strReader = new StringReader(writer.toString());
        reader.read(strReader);
        reader.readLicenses();
    }
}
