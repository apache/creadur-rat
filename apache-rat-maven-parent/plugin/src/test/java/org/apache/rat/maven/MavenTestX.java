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

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.MojoExecutionException;
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
import static org.mockito.Mockito.when;

/**
 * Tests extracted from {@link ReportTestDataProvider}.
 * DO NOT EDIT - GENERATED FILE
 */
@MojoTest
public class MavenTestX {

    static ReportTestDataProvider reportTestDataProvider = new ReportTestDataProvider();

    final Map<String, TestData> testDataMap = reportTestDataProvider.getOptionTestMap(MavenOption.UNSUPPORTED_SET);

    @AfterAll
    @EnabledOnOs(OS.WINDOWS)
    static void cleanup() {
        System.gc(); // hacky workaround for windows bug.
    }

    
///**
// * Generated test for config/noDefaults
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/config/noDefaults")
//public void configNoDefaultsTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/config/noDefaults";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("config/noDefaults");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for config/withDefaults
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/config/withDefaults")
//public void configWithDefaultsTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/config/withDefaults";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("config/withDefaults");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for config_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/config_DefaultTest")
//public void config_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/config_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("config_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for configuration-no-defaults
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/configuration-no-defaults")
//public void configurationNoDefaultsTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/configuration-no-defaults";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("configuration-no-defaults");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for counter-max/Unapproved1
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/counter-max/Unapproved1")
//public void counterMaxUnapproved1Test(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/counter-max/Unapproved1";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("counter-max/Unapproved1");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for counter-min
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/counter-min")
//public void counterMinTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/counter-min";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("counter-min");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for counterMax_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/counterMax_DefaultTest")
//void counterMax_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/counterMax_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("counterMax_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for counterMin_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/counterMin_DefaultTest")
//public void counterMin_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/counterMin_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("counterMin_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for dry-run/stdRun
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/dry-run/stdRun")
//public void dryRunStdRunTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/dry-run/stdRun";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("dry-run/stdRun");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for dryRun_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/dryRun_DefaultTest")
//public void dryRun_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/dryRun_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("dryRun_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for edit-copyright/dryRun
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/edit-copyright/dryRun")
//public void editCopyrightDryRunTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/edit-copyright/dryRun";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("edit-copyright/dryRun");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for edit-copyright/editLicense
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editLicense")
//public void editCopyrightEditLicenseTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editLicense";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("edit-copyright/editLicense");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}

/**
 * Generated test for edit-copyright/editOverwrite
 */
@Test
@InjectMojo(goal = "check")
@Basedir("target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editOverwrite")
public void editCopyrightEditOverwriteTest(RatCheckMojo mojo) throws MojoExecutionException {
    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editOverwrite";
    when(mojo.project.getBuild()).thenReturn(mock());
    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
    TestData testData = testDataMap.get("edit-copyright/editOverwrite");
    testData.setupFiles(Paths.get(baseDir));
    if (testData.getExpectedException() != null) {
        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
    } else {
        try {
            mojo.execute();
            ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
        } finally {

            Reporter.Output.Builder builder = Reporter.Output.builder().configuration(new File("/home/claude/apache/creadur-rat/apache-rat-maven-parent/impl/target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editOverwrite/target/RAT/configuration.xml"))
                    .document(new File("/home/claude/apache/creadur-rat/apache-rat-maven-parent/impl/target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editOverwrite/target/RAT/rat.xml"))
                    .statistic(new File("/home/claude/apache/creadur-rat/apache-rat-maven-parent/impl/target/test-classes/org/apache/rat/maven/stubs/edit-copyright/editOverwrite/target/RAT/claimStatistic.xml"));


            ValidatorData validatorData2 = new ValidatorData(builder.build(), baseDir);
            testData.getValidator().accept(validatorData2);
        }
    }
}
//
///**
// * Generated test for edit-copyright/noEditLicense
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/edit-copyright/noEditLicense")
//public void editCopyrightNoEditLicenseTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/edit-copyright/noEditLicense";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("edit-copyright/noEditLicense");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for edit-license
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/edit-license")
//public void editLicenseTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/edit-license";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("edit-license");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for edit-overwrite
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/edit-overwrite")
//public void editOverwriteTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/edit-overwrite";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("edit-overwrite");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for edit-overwrite/noEditLicense
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/edit-overwrite/noEditLicense")
//public void editOverwriteNoEditLicenseTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/edit-overwrite/noEditLicense";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("edit-overwrite/noEditLicense");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for help-licenses/stdOut
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/help-licenses/stdOut")
//public void helpLicensesStdOutTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/help-licenses/stdOut";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("help-licenses/stdOut");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-exclude
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-exclude")
//public void inputExcludeTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-exclude";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-exclude");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-exclude-file
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-exclude-file")
//public void inputExcludeFileTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-exclude-file";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-exclude-file");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-exclude-parsed-scm/GIT
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-exclude-parsed-scm/GIT")
//public void inputExcludeParsedScmGITTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-exclude-parsed-scm/GIT";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-exclude-parsed-scm/GIT");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-exclude-size
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-exclude-size")
//public void inputExcludeSizeTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-exclude-size";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-exclude-size");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-exclude-std
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-exclude-std")
//public void inputExcludeStdTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-exclude-std";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-exclude-std");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-exclude/includeStdValidation
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-exclude/includeStdValidation")
//public void inputExcludeIncludeStdValidationTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-exclude/includeStdValidation";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-exclude/includeStdValidation");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-exclude/inputInclude
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-exclude/inputInclude")
//public void inputExcludeInputIncludeTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-exclude/inputInclude";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-exclude/inputInclude");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-exclude/inputIncludeFile
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-exclude/inputIncludeFile")
//public void inputExcludeInputIncludeFileTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-exclude/inputIncludeFile";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-exclude/inputIncludeFile");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-include
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-include")
//public void inputIncludeTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-include";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-include");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-include-file
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-include-file")
//public void inputIncludeFileTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-include-file";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-include-file");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-include-std/hidden_dir
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-include-std/hidden_dir")
//public void inputIncludeStdHidden_dirTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-include-std/hidden_dir";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-include-std/hidden_dir");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-include-std/hidden_file
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-include-std/hidden_file")
//public void inputIncludeStdHidden_fileTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-include-std/hidden_file";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-include-std/hidden_file");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-include-std/misc
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-include-std/misc")
//public void inputIncludeStdMiscTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-include-std/misc";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-include-std/misc");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for input-source
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/input-source")
//public void inputSourceTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/input-source";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("input-source");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for inputExcludeFile_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/inputExcludeFile_DefaultTest")
//public void inputExcludeFile_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/inputExcludeFile_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("inputExcludeFile_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for inputExcludeParsedScm_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/inputExcludeParsedScm_DefaultTest")
//public void inputExcludeParsedScm_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/inputExcludeParsedScm_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("inputExcludeParsedScm_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for inputExcludeSize_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/inputExcludeSize_DefaultTest")
//public void inputExcludeSize_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/inputExcludeSize_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("inputExcludeSize_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for inputExcludeStd_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/inputExcludeStd_DefaultTest")
//public void inputExcludeStd_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/inputExcludeStd_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("inputExcludeStd_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for inputExclude_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/inputExclude_DefaultTest")
//public void inputExclude_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/inputExclude_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("inputExclude_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for inputIncludeFile_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/inputIncludeFile_DefaultTest")
//public void inputIncludeFile_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/inputIncludeFile_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("inputIncludeFile_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for inputInclude_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/inputInclude_DefaultTest")
//public void inputInclude_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/inputInclude_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("inputInclude_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for inputSource_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/inputSource_DefaultTest")
//public void inputSource_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/inputSource_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("inputSource_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for license-families-approved-file/withLicenseDef
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/license-families-approved-file/withLicenseDef")
//public void licenseFamiliesApprovedFileWithLicenseDefTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/license-families-approved-file/withLicenseDef";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("license-families-approved-file/withLicenseDef");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for license-families-approved-file/withoutLicenseDef
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/license-families-approved-file/withoutLicenseDef")
//public void licenseFamiliesApprovedFileWithoutLicenseDefTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/license-families-approved-file/withoutLicenseDef";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("license-families-approved-file/withoutLicenseDef");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for license-families-approved/withLicenseDef
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/license-families-approved/withLicenseDef")
//public void licenseFamiliesApprovedWithLicenseDefTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/license-families-approved/withLicenseDef";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("license-families-approved/withLicenseDef");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for license-families-approved/withoutLicenseDef
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/license-families-approved/withoutLicenseDef")
//public void licenseFamiliesApprovedWithoutLicenseDefTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/license-families-approved/withoutLicenseDef";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("license-families-approved/withoutLicenseDef");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for license-families-denied
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/license-families-denied")
//public void licenseFamiliesDeniedTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/license-families-denied";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("license-families-denied");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for license-families-denied-file
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/license-families-denied-file")
//public void licenseFamiliesDeniedFileTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/license-families-denied-file";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("license-families-denied-file");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licenseFamiliesApprovedFile_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licenseFamiliesApprovedFile_DefaultTest")
//public void licenseFamiliesApprovedFile_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licenseFamiliesApprovedFile_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licenseFamiliesApprovedFile_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licenseFamiliesApproved_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licenseFamiliesApproved_DefaultTest")
//public void licenseFamiliesApproved_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licenseFamiliesApproved_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licenseFamiliesApproved_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licenseFamiliesDeniedFile_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licenseFamiliesDeniedFile_DefaultTest")
//public void licenseFamiliesDeniedFile_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licenseFamiliesDeniedFile_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licenseFamiliesDeniedFile_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licenseFamiliesDenied_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licenseFamiliesDenied_DefaultTest")
//public void licenseFamiliesDenied_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licenseFamiliesDenied_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licenseFamiliesDenied_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licenses-approved-file/withLicenseDef
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licenses-approved-file/withLicenseDef")
//public void licensesApprovedFileWithLicenseDefTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licenses-approved-file/withLicenseDef";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licenses-approved-file/withLicenseDef");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licenses-approved-file/withoutLicenseDef
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licenses-approved-file/withoutLicenseDef")
//public void licensesApprovedFileWithoutLicenseDefTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licenses-approved-file/withoutLicenseDef";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licenses-approved-file/withoutLicenseDef");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licenses-approved/withLicenseDef
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licenses-approved/withLicenseDef")
//public void licensesApprovedWithLicenseDefTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licenses-approved/withLicenseDef";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licenses-approved/withLicenseDef");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licenses-approved/withoutLicenseDef
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licenses-approved/withoutLicenseDef")
//public void licensesApprovedWithoutLicenseDefTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licenses-approved/withoutLicenseDef";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licenses-approved/withoutLicenseDef");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licenses-denied-file/ILLUMOS
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licenses-denied-file/ILLUMOS")
//public void licensesDeniedFileILLUMOSTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licenses-denied-file/ILLUMOS";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licenses-denied-file/ILLUMOS");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licenses-denied/ILLUMOS
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licenses-denied/ILLUMOS")
//public void licensesDeniedILLUMOSTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licenses-denied/ILLUMOS";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licenses-denied/ILLUMOS");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licensesApprovedFile_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licensesApprovedFile_DefaultTest")
//public void licensesApprovedFile_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licensesApprovedFile_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licensesApprovedFile_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for licensesApproved_DefaultTest
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/licensesApproved_DefaultTest")
//public void licensesApproved_DefaultTestTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/licensesApproved_DefaultTest";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("licensesApproved_DefaultTest");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-archive/absence
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-archive/absence")
//public void outputArchiveAbsenceTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-archive/absence";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-archive/absence");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-archive/notification
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-archive/notification")
//public void outputArchiveNotificationTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-archive/notification";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-archive/notification");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-archive/presence
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-archive/presence")
//public void outputArchivePresenceTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-archive/presence";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-archive/presence");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-families/all
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-families/all")
//public void outputFamiliesAllTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-families/all";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-families/all");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-families/approved
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-families/approved")
//public void outputFamiliesApprovedTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-families/approved";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-families/approved");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-families/none
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-families/none")
//public void outputFamiliesNoneTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-families/none";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-families/none");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-file
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-file")
//public void outputFileTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-file";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-file");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-licenses/ALL
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-licenses/ALL")
//public void outputLicensesALLTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-licenses/ALL";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-licenses/ALL");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-licenses/APPROVED
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-licenses/APPROVED")
//public void outputLicensesAPPROVEDTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-licenses/APPROVED";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-licenses/APPROVED");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-licenses/NONE
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-licenses/NONE")
//public void outputLicensesNONETest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-licenses/NONE";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-licenses/NONE");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-standard/absence
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-standard/absence")
//public void outputStandardAbsenceTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-standard/absence";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-standard/absence");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-standard/notification
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-standard/notification")
//public void outputStandardNotificationTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-standard/notification";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-standard/notification");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-standard/presence
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-standard/presence")
//public void outputStandardPresenceTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-standard/presence";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-standard/presence");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-style/fileStyleSheet
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-style/fileStyleSheet")
//public void outputStyleFileStyleSheetTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-style/fileStyleSheet";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-style/fileStyleSheet");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-style/missing_headers
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-style/missing_headers")
//public void outputStyleMissing_headersTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-style/missing_headers";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-style/missing_headers");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-style/plain
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-style/plain")
//public void outputStylePlainTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-style/plain";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-style/plain");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-style/unapproved_licenses
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-style/unapproved_licenses")
//public void outputStyleUnapproved_licensesTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-style/unapproved_licenses";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-style/unapproved_licenses");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-style/xhtml5
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-style/xhtml5")
//public void outputStyleXhtml5Test(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-style/xhtml5";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-style/xhtml5");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}
//
///**
// * Generated test for output-style/xml
// */
//@Test
//@InjectMojo(goal = "check")
//@Basedir("target/test-classes/org/apache/rat/maven/stubs/output-style/xml")
//public void outputStyleXmlTest(RatCheckMojo mojo) throws MojoExecutionException {
//    String baseDir = "target/test-classes/org/apache/rat/maven/stubs/output-style/xml";
//    when(mojo.project.getBuild()).thenReturn(mock());
//    when(mojo.project.getBuild().getDirectory()).thenReturn(baseDir + "/target");
//    TestData testData = testDataMap.get("output-style/xml");
//    testData.setupFiles(Paths.get(baseDir));
//    if (testData.getExpectedException() != null) {
//        assertThatThrownBy(mojo::execute).hasMessageContaining(testData.getExpectedException().getMessage());
//    } else {
//        mojo.execute();
//        ValidatorData validatorData = new ValidatorData(mojo.getOutput(), baseDir);
//        testData.getValidator().accept(validatorData);
//    }
//}

}
