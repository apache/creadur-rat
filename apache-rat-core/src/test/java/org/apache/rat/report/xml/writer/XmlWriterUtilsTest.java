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
package org.apache.rat.report.xml.writer;

import org.apache.rat.testhelpers.XmlUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlWriterUtilsTest {

    private StringWriter out;
    private IXmlWriter writer;

    @BeforeEach
    public void setUp() throws Exception {
        out = new StringWriter();
        writer = new XmlWriter(out);
        writer.openElement("alpha");
    }

    @Test
    public void writeTrue() throws Exception {
        XmlUtils.writeAttribute(writer, "name", true);
        assertEquals("<alpha name='true'", out.toString(), "Attribute written as True");
    }

    @Test
    public void writeFalse() throws Exception {
        XmlUtils.writeAttribute(writer, "name", false);
        assertEquals("<alpha name='false'", out.toString(), "Attribute written as False");
    }
}
