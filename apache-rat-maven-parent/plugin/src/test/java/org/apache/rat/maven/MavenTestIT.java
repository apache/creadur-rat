/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Map;
import org.apache.maven.cli.MavenCli;
import org.apache.rat.Reporter;
import org.apache.rat.testhelpers.data.ReportTestDataProvider;
import org.apache.rat.testhelpers.data.TestData;
import org.apache.rat.testhelpers.data.ValidatorData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Tests extracted from {@link ReportTestDataProvider}.
 * DO NOT EDIT - GENERATED FILE
 */

public class MavenTestIT {

    static ReportTestDataProvider reportTestDataProvider = new ReportTestDataProvider();

    final Map<String, TestData> testDataMap = reportTestDataProvider.getOptionTestMap(MavenOption.UNSUPPORTED_SET);

    @AfterAll
    @EnabledOnOs(OS.WINDOWS)
    static void cleanup() {
        System.gc(); // hacky workaround for windows bug.
    }

    

/**
 * Generated test for edit-copyright/editOverwrite
 */
@Test
public void editCopyrightEditOverwriteTest() {
    final String MVN_HOME = "maven.multiModuleProjectDirectory";

    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editOverwrite";
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
    MavenCli cli = new MavenCli();
    System.setProperty(MVN_HOME, baseDir);

    try (PrintStream stdOutStream = new PrintStream(stdOut);
         PrintStream stdErrStream = new PrintStream(stdErr)) {
        cli.doMain(new String[]{"apache-rat-plugin:check"}, baseDir, stdOutStream, stdErrStream);
    }

    Reporter.Output.Builder builder = Reporter.Output.builder().configuration(new File("/home/claude/apache/creadur-rat/apache-rat-maven-parent/impl/target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editOverwrite/target/RAT/configuration.xml"))
            .document(new File("/home/claude/apache/creadur-rat/apache-rat-maven-parent/impl/target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editOverwrite/target/RAT/rat.xml"))
            .statistic(new File("/home/claude/apache/creadur-rat/apache-rat-maven-parent/impl/target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editOverwrite/target/RAT/claimStatistic.xml"));


    ValidatorData validatorData = new ValidatorData(builder.build(), baseDir);
    System.out.println(validatorData);
    //testData.getValidator().accept(validatorData2);
}


}
