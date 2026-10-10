/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.maven;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.rat.Reporter;
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.document.DocumentName;
import org.apache.rat.maven.MavenOptionCollection;
import org.apache.rat.maven.RecordingOutputHandler;
import org.apache.rat.maven.tools.TestGenerator;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.testhelpers.data.ReportTestDataProvider;
import org.apache.rat.testhelpers.data.TestData;
import org.apache.rat.testhelpers.data.ValidatorData;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executes the dynamic tests provided by the ReportTestDataProvider.
 */
public class MavenTestDynIT {

    private final TestGenerator testGenerator;
    private final Map<String, Consumer<ValidatorData>> validatorEditors;

    /**
     * Constructor.
     * @throws IOException on IO Exception,
     */
    public MavenTestDynIT() throws IOException {
        testGenerator = new TestGenerator();
        validatorEditors = new HashMap<>();
        populateValidatorEditors();
    }

    /**
     * Maven testing adds
     * <ul>
     *     <li>a pom.xml with Apache 2 license to the root directory</li>
     *     <li>a .rat directory in the root directory</li>
     *     <li>one or more files in the .rat directory.</li>
     *     <li>a target directory</li>
     *     <li>a rat.txt file to the target directory</li>
     *     <li>a RAT directory to the target directory</li>
     *     <li>3 xml files in the RAT directory.</li>
     * </ul>
     * These have to be accounted for in the counters.
     */
    private void populateValidatorEditors() {
        /* In the changes below there are overcounts verses the expected
        counts for commandline.  The changes are to decrements the counted
        files so that they match what is expected for the command line.
        The comment before each block of changes describes why they are changed.
         */

        // Decrements standards and approved by 1
        Consumer<ValidatorData> stdApproved = data -> {
            data.getStatistic().incCounter(ClaimStatistic.Counter.STANDARDS, -1);
            data.getStatistic().incCounter(ClaimStatistic.Counter.APPROVED, -1);
        };

        // Decrements standards and ignored by 1
        Consumer<ValidatorData> stdIgnored = data -> {
            data.getStatistic().incCounter(ClaimStatistic.Counter.STANDARDS, -1);
            data.getStatistic().incCounter(ClaimStatistic.Counter.IGNORED, -1);
        };

        /* POM is a standard file not found in the non-Maven test case
         so we have to modify the STANDARD count in these cases to
         account for it.  The POM also contains the tag for ONE so
         there is an APPROVED is also modified.
         */
        validatorEditors.put("config/noDefaults", stdApproved);
        validatorEditors.put("config/withDefaults", data -> {
            data.getStatistic().incCounter(ClaimStatistic.Counter.STANDARDS, -1);
            // with defaults POM has 2 approved licenses
            data.getStatistic().incCounter(ClaimStatistic.Counter.APPROVED, -2);
        });
        validatorEditors.put("config_DefaultTest", stdApproved);

        validatorEditors.put("exclude", stdIgnored);
        validatorEditors.put("exclude-file", stdIgnored);
        validatorEditors.put("excludeFile_DefaultTest", stdIgnored);
        validatorEditors.put("exclude_DefaultTest", stdIgnored);
        validatorEditors.put("include", stdIgnored);
        validatorEditors.put("include_DefaultTest", stdIgnored);
        validatorEditors.put("includes-file", stdIgnored);
        validatorEditors.put("includesFile_DefaultTest", stdIgnored);
        validatorEditors.put("input-exclude", stdIgnored);
        validatorEditors.put("input-exclude-file", stdIgnored);
        validatorEditors.put("input-exclude-parsed-scm/GIT", stdIgnored);
        validatorEditors.put("input-exclude-size", stdIgnored);
        validatorEditors.put("input-exclude-std", stdIgnored);
        validatorEditors.put("input-exclude/include", stdIgnored);
        validatorEditors.put("input-exclude/includeStdValidation", stdIgnored);
        validatorEditors.put("input-exclude/includesFile", stdIgnored);
        validatorEditors.put("input-exclude/inputInclude", stdIgnored);
        validatorEditors.put("input-exclude/inputIncludeFile", stdIgnored);
        validatorEditors.put("input-include", stdIgnored);
        validatorEditors.put("input-include-file", stdIgnored);
        validatorEditors.put("input-include-std/hidden_dir", stdIgnored);
        validatorEditors.put("input-include-std/hidden_file", stdIgnored);
        validatorEditors.put("input-include-std/misc", stdIgnored);
        validatorEditors.put("inputExcludeFile_DefaultTest", stdIgnored);
        validatorEditors.put("inputExcludeParsedScm_DefaultTest", stdIgnored);
        validatorEditors.put("inputExcludeSize_DefaultTest", stdIgnored);
        // maven automatically ignores target and build.log
        validatorEditors.put("inputExcludeStd_DefaultTest", data -> {
            data.getStatistic().incCounter(ClaimStatistic.Counter.IGNORED, -2);
        });
        validatorEditors.put("inputExclude_DefaultTest", stdIgnored);
        validatorEditors.put("inputInclude_DefaultTest", stdIgnored);
        validatorEditors.put("inputIncludeFile_DefaultTest", stdIgnored);
        validatorEditors.put("inputSource_DefaultTest", stdIgnored);

        validatorEditors.put("license-families-approved-file/withLicenseDef", stdApproved);
        validatorEditors.put("license-families-approved-file/withoutLicenseDef", stdApproved);
        validatorEditors.put("license-families-approved/withLicenseDef", stdApproved);
        validatorEditors.put("license-families-approved/withoutLicenseDef", stdApproved);

        validatorEditors.put("license-families-denied", stdApproved);
        validatorEditors.put("license-families-denied-file", stdApproved);
        validatorEditors.put("licenseFamiliesApprovedFile_DefaultTest", stdApproved);
        validatorEditors.put("licenseFamiliesApproved_DefaultTest", stdApproved);
        validatorEditors.put("licenseFamiliesDeniedFile_DefaultTest", stdApproved);
        validatorEditors.put("licenseFamiliesDenied_DefaultTest", stdApproved);

        validatorEditors.put("licenses-approved-file/withLicenseDef", stdApproved);
        validatorEditors.put("licenses-approved-file/withoutLicenseDef", stdApproved);
        validatorEditors.put("licenses-approved/withLicenseDef", stdApproved);
        validatorEditors.put("licenses-approved/withoutLicenseDef", stdApproved);

        validatorEditors.put("licenses/noDefaults", stdApproved);
        validatorEditors.put("licenses/withDefaults", data -> {
                    data.getStatistic().incCounter(ClaimStatistic.Counter.STANDARDS, -1);
                    // with defaults POM has 2 approved licenses
                    data.getStatistic().incCounter(ClaimStatistic.Counter.APPROVED, -2);
                });
        validatorEditors.put("licensesApprovedFile_DefaultTest", stdApproved);
        validatorEditors.put("licensesApproved_DefaultTest", stdApproved);
        validatorEditors.put("licenses_DefaultTest", stdApproved);

        validatorEditors.put("out", stdApproved);
        validatorEditors.put("output-file", stdApproved);


        validatorEditors.put("scan-hidden-directories", stdIgnored);


    }

    /**
     * Runs the test for the specified TestData.
     * @param testData the test to run.
     * @param testPath the path to create file etc. in .
     * @throws IOException on IO Error.
     * @throws MavenInvocationException on Maven invocation error.
     * @throws RatException on RAT error.
     */
    @ParameterizedTest
    @MethodSource("testData")
    void optionTest(TestData testData, @TempDir Path testPath) throws IOException, MavenInvocationException, RatException {

        Path baseDir = testPath.resolve(testData.getTestName());
        String pomText = testGenerator.buildPom(testData);
        System.out.println(pomText);
        testData.setupFiles(baseDir);
        File pomFile = baseDir.resolve("pom.xml").toFile();
        try (FileOutputStream fos = new FileOutputStream(pomFile)) {
            fos.write(pomText.getBytes(StandardCharsets.UTF_8));
        }
        RecordingOutputHandler[] recordingOutputHandler = {null};

        InvocationRequest request = new DefaultInvocationRequest() {
            @Override
            public InvocationOutputHandler getOutputHandler(InvocationOutputHandler defaultHandler) {
                recordingOutputHandler[0] = new RecordingOutputHandler(defaultHandler);
                return super.getOutputHandler(recordingOutputHandler[0]);
            }
        };
        request.setDebug(true);
        request.setBatchMode(true);
        request.setBaseDirectory(baseDir.toFile());
        request.setPomFile(pomFile);
        request.addArg("apache-rat:check");

        Invoker invoker = new DefaultInvoker();
        InvocationResult result = invoker.execute(request);
        if (testData.expectingException()) {
            assertThat(result.getExitCode()).as("Result of Maven execution").isEqualTo(1);
            List<String> entries = recordingOutputHandler[0].getLines().filter(s -> s.contains(testData.getExpectedException().getMessage()))
                    .filter(s -> s.contains("[ERROR]")).toList();
            assertThat(entries).hasSize(1);
        } else {
            DocumentName baseName = DocumentName.builder(baseDir.toFile()).build();
            Reporter.Output output = Reporter.Output.builder()
                    .configuration("target/RAT/configuration.xml", baseName)
                    .document("target/RAT/rat.xml", baseName)
                    .statistic("target/RAT/claimStatistic.xml", baseName)
                    .build();

            ValidatorData validatorData = new ValidatorData(output, baseDir.toString());
            Consumer<ValidatorData> editor = validatorEditors.get(testData.toString());
            if (editor != null) {
                editor.accept(validatorData);
            }
            output.format(StyleSheets.PLAIN.getStyleSheet().ioSupplier(), output.getConfiguration().getOutput() );
            testData.getValidator().accept(validatorData);
        }
    }

    /**
     * The static class to create retrieve the tests for the MavenOptions.
     * @return the collection of Tests for the supported options.
     */
    static Collection<TestData> testData() {
        return new ReportTestDataProvider().getOptionTests(new MavenOptionCollection());
    }
}
