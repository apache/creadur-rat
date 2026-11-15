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
package org.apache.rat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.rat.analysis.matchers.AndMatcher;
import org.apache.rat.analysis.matchers.SPDXMatcherFactory;
import org.apache.rat.analysis.matchers.SimpleTextMatcher;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.license.ILicense;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.testhelpers.BaseOption;
import org.apache.rat.testhelpers.BaseOptionCollection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.assertj.core.api.Assertions.assertThat;

/** End-to-end SPDX expression tests, including XML approval and report counters (RAT-583). */
class SPDXExpressionReportTest {
    @TempDir
    Path directory;

    private ReportConfiguration configuration(final String header) throws Exception {
        Path source = directory.resolve("Example.java");
        Files.writeString(source, header + "\nclass Example {}\n");
        OptionCollectionParser<BaseOption> parser = new OptionCollectionParser<>(new BaseOptionCollection());
        ArgumentContext context = parser.parseCommands(directory.toFile(), new String[]{directory.toString()});
        return context.getConfiguration();
    }

    private void assertReport(final Reporter.Output output, final boolean approved, final String... expectedIds) {
        assertThat(output.getStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED) == 0).isEqualTo(approved);
        NodeList nodes = output.getDocument().getElementsByTagName("resource");
        assertThat(nodes.getLength()).isEqualTo(1);
        NodeList licenses = ((Element) nodes.item(0)).getElementsByTagName("license");
        List<String> ids = new ArrayList<>();
        boolean allApproved = true;
        for (int i = 0; i < licenses.getLength(); i++) {
            Element license = (Element) licenses.item(i);
            ids.add(license.getAttribute("id"));
            allApproved &= Boolean.parseBoolean(license.getAttribute("approval"));
        }
        assertThat(ids).containsExactlyInAnyOrder(expectedIds);
        assertThat(allApproved).isEqualTo(approved);
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "Apache-2.0 OR GPL-3.0-only; true; AL2.0",
            "GPL-3.0-only OR Apache-2.0; true; AL2.0",
            "Apache-2.0 AND GPL-3.0-only; false; AL2.0,GPL3",
            "(Apache-2.0 OR MIT); true; AL2.0",
            "Apache-2.0 AND MIT; true; AL2.0,MIT",
            "Apache-2.0 OR MIT AND GPL-3.0-only; true; AL2.0",
            "(Apache-2.0 OR MIT) AND GPL-3.0-only; false; AL2.0,GPL3",
            "GPL-3.0-only OR LicenseRef-Private; false; GPL3,?????",
            "LicenseRef-Private OR Apache-2.0; true; AL2.0",
            "Apache-2.0 AND LicenseRef-Private; false; AL2.0,?????",
            "Apache-2.0 AND; false; ?????",
            "Apache-2.0 trailing prose; false; ?????",
            "(Apache-2.0 OR MIT; false; ?????",
            "Apache-2.0 WITH Unknown-exception; false; ?????"
    })
    void reportsExpressionVerdict(String expression, boolean approved, String ids) throws Exception {
        Reporter.Output output = new Reporter(configuration("// SPDX-License-Identifier: " + expression)).execute();
        assertReport(output, approved, ids.split(","));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "// SPDX-License-Identifier:  Apache-2.0",
            "// SPDX-License-Identifier:\t\tApache-2.0\t",
            "// SPDX-License-Identifier:Apache-2.0",
            "/* SPDX-License-Identifier:\u00a0(Apache-2.0\u2003OR MIT)\u00a0*/",
            "// SPDX-License-Identifier: ( GPL-3.0-only\tOR\tApache-2.0 )"
    })
    void reportsTagsWithFlexibleWhitespace(String header) throws Exception {
        assertReport(new Reporter(configuration(header)).execute(), true, "AL2.0");
    }

    @Test
    void separateTagsAreAdditionalRequirements() throws Exception {
        ReportConfiguration config = configuration("// SPDX-License-Identifier: Apache-2.0\r\n"
                + "// SPDX-License-Identifier: GPL-3.0-only\r\n");
        assertReport(new Reporter(config).execute(), false, "AL2.0", "GPL3");
    }

    @Test
    void malformedTagCannotBeHiddenByAnApprovedTag() throws Exception {
        ReportConfiguration config = configuration("// SPDX-License-Identifier: Apache-2.0\n"
                + "// SPDX-License-Identifier: MIT AND\n");
        assertReport(new Reporter(config).execute(), false, "AL2.0", "?????");
    }

    @Test
    void explicitExceptionMatcherCanBeApprovedIndependently() throws Exception {
        ReportConfiguration config = configuration(
                "// SPDX-License-Identifier: GPL-2.0-only\tWITH  Classpath-exception-2.0");
        config.addLicense(ILicense.builder().setId("GPLCP").setFamily("GPL")
                .setMatcher(SPDXMatcherFactory.newInstance().create("GPL-2.0-only WITH Classpath-exception-2.0")));
        config.addApprovedLicenseId("GPLCP");
        assertReport(new Reporter(config).execute(), true, "GPLCP");
    }

    @Test
    void approvedBaseLicenseDoesNotApproveAnUnconfiguredException() throws Exception {
        ReportConfiguration config = configuration("// SPDX-License-Identifier: GPL-2.0-only WITH Unknown-exception");
        config.addApprovedLicenseId("GPL2");
        assertReport(new Reporter(config).execute(), false, "?????");
    }

    @Test
    void doesNotHideIndependentUnapprovedLicenseText() throws Exception {
        ReportConfiguration config = configuration("// SPDX-License-Identifier: Apache-2.0 OR GPL-3.0-only\n"
                + "// This program is free software: you can redistribute it and/or modify it under the terms of the\n"
                + "// GNU General Public License as published by the Free Software Foundation, either version 3\n"
                + "// of the License, or (at your option) any later version.\n");
        assertReport(new Reporter(config).execute(), false, "AL2.0", "GPL3");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void retainsConfiguredMatcherConditions(boolean includeNotice) throws Exception {
        ReportConfiguration config = configuration("// SPDX-License-Identifier: LicenseRef-Project\n"
                + (includeNotice ? "// required notice\n" : ""));
        config.addLicense(ILicense.builder().setId("LOCAL").setFamily("AL")
                .setMatcher(new AndMatcher(List.of(
                        SPDXMatcherFactory.newInstance().create("LicenseRef-Project"),
                        new SimpleTextMatcher("required notice")), null)));
        assertReport(new Reporter(config).execute(), includeNotice, includeNotice ? "LOCAL" : "?????");
    }

    @Test
    void resetsExpressionMatchesBetweenFiles() throws Exception {
        ReportConfiguration config = configuration("// SPDX-License-Identifier: Apache-2.0 OR GPL-3.0-only");
        assertReport(new Reporter(config).execute(), true, "AL2.0");
        Files.writeString(directory.resolve("Example.java"), "// SPDX-License-Identifier: GPL-3.0-only\n");
        assertReport(new Reporter(config).execute(), false, "GPL3");
    }
}
