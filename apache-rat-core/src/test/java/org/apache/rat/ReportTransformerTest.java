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
package org.apache.rat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TextUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ReportTransformerTest {

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("transformers")
    public void testTransform(String xslt, String[] expected) throws Exception {
        URL url = this.getClass().getClassLoader().getResource("XmlOutputExamples/elements.xml");
        StringWriter writer = new StringWriter();

        ReportTransformer transformer = new ReportTransformer(writer,
                new BufferedReader(new FileReader(Resources.getMainResourceFile("/org/apache/rat/" + xslt))),
                new InputStreamReader(url.openStream()));
        transformer.transform();
        String text = writer.getBuffer().toString();
        for (String pattern : expected) {
            TextUtils.assertPatternInOutput(pattern, text);
        }
    }

    public static Stream<Arguments> transformers() {
        List<Arguments> lst = new ArrayList<>();

        lst.add(Arguments.of("plain-rat.xsl", new String[] { "Notes: 2$", "Binaries: 2$", "Archives: 1$",
                "Standards: 8$", "Apache Licensed: 5$", "Generated Documents: 1$", "2 Unknown Licenses" }));
        lst.add(Arguments.of("unapproved-licenses.xsl", new String[] { "Files with unapproved licenses",
                "src/test/resources/elements/Source.java", "src/test/resources/elements/sub/Empty.txt" }));
        lst.add(Arguments.of("missing-headers.xsl", new String[] { "Files with missing headers",
                "src/test/resources/elements/Source.java", "src/test/resources/elements/sub/Empty.txt" }));
        return lst.stream();
    }
}
