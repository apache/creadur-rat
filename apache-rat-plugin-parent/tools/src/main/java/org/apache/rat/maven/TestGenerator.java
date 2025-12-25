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
package org.apache.rat.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.DeprecationReporter;
import org.apache.rat.VersionInfo;
import org.apache.rat.testhelpers.FileUtils;
import org.apache.rat.testhelpers.data.ReportTestDataProvider;
import org.apache.rat.testhelpers.data.TestData;
import org.apache.rat.utils.CasedString;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Generates the Maven mojo tests.
 */
public final class TestGenerator {
    /** They syntax for this command */
    private static final String SYNTAX = String.format("java -cp ... %s [options]", TestGenerator.class.getName());
    /** The package name as a cased string */
    private final CasedString packageName;
    /** The resource directory where the test resources will be written */
    private final String resourceDirectory;
    /** The directory where the test classes will be written*/
    private final String testDirectory;
    /** The template for the pom.xml files */
    private final Template pomTemplate;
    /** The template for the test class file */
    private final Template javaTemplate;
    /** The template for the methods within the test class */
    private final Template methodTemplate;

    private TestGenerator(final String packageName, final String resourceDirectory, final String testDirectory) {
        this.packageName = new CasedString(CasedString.StringCase.DOT, packageName);
        this.resourceDirectory = resourceDirectory;
        this.testDirectory = testDirectory;

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();

        // retrieve the templates
        CasedString casedTemplateName = new CasedString(CasedString.StringCase.DOT, TestGenerator.class.getName());
        String[] nameParts = Arrays.copyOf(casedTemplateName.getSegments(), casedTemplateName.getSegments().length);
        pomTemplate = velocityEngine.getTemplate(casedTemplateName.toCase(CasedString.StringCase.SLASH) + ".vm");
        nameParts[nameParts.length - 1] = "TestJava.vm";
        javaTemplate = velocityEngine.getTemplate(CasedString.StringCase.SLASH.assemble(nameParts));
        nameParts[nameParts.length - 1] = "TestMethod.vm";
        methodTemplate = velocityEngine.getTemplate(CasedString.StringCase.SLASH.assemble(nameParts));
    }

    /**
     * Gets the resource path.
     * @return the resource path.
     */
    private Path resourcePath() {
        return Paths.get(resourceDirectory).resolve(packageName.toCase(CasedString.StringCase.SLASH));
    }

    /**
     * Gets the command line options for this executable.
     * @return the command line options.
     */
    private static Options getOptions() {
        return new Options()
                .addOption(Option.builder("r").longOpt("resources").required().hasArg().desc("The path to the base resource directory").build())
                .addOption(Option.builder("h").longOpt("help").desc("Print this message").build())
                .addOption(Option.builder("t").longOpt("tests").required().hasArg().desc("The path to the base of the generated test code directory").build())
                .addOption(Option.builder("p").longOpt("package").required().hasArg().desc("The package name to generate").build());
    }

    /**
     * The main code.
     *
     * @param args The arguments from the command line.
     * @throws IOException on IO error.
     */
    public static void main(final String[] args) throws IOException {

        CommandLine commandLine = null;
        try {
            commandLine = DefaultParser.builder().setDeprecatedHandler(DeprecationReporter.getLogReporter())
                    .build().parse(getOptions(), args);
        } catch (ParseException pe) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(SYNTAX, pe.getMessage(), getOptions(), "");
            System.exit(1);
        }

        if (commandLine.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(SYNTAX, getOptions());
            System.exit(0);
        }
        TestGenerator testGenerator = new TestGenerator(commandLine.getOptionValue("p"),
                commandLine.getOptionValue("r"), commandLine.getOptionValue("t"));

        testGenerator.execute();
    }

    /**
     * Execute the generation.
     * @throws IOException on IO error.
     */
    private void execute() throws IOException {
        VelocityContext context = new VelocityContext();
        context.put("packageName", packageName.toString());

        final VersionInfo versionInfo = new VersionInfo(TestGenerator.class);
        context.put("rat_version", versionInfo.getSpecVersion());
        context.put("plugin_version", versionInfo.getVersion());
        context.put("tests", writeTestPoms(context));
        writeTestFile(context);
    }

    /**
     * Write the {@code MavenTest.java} file
     * @param context
     * @throws IOException
     */
    private void writeTestFile(final VelocityContext context) throws IOException {
        File javaFile = Paths.get(testDirectory).resolve(packageName.toCase(CasedString.StringCase.SLASH))
                .resolve("MavenTest.java").toFile();
        FileUtils.mkDir(javaFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(javaFile)) {
            javaTemplate.merge(context, fileWriter);
        }
    }

    /**
     * Write the test poms for all the supported options.
     * Generates method definitions (one for each pom file) to be included in {@code MavenTest.java}.
     * @param context het velocity context.
     * @return a String comprising all the method definitions.
     * @throws IOException
     */
    private String writeTestPoms(final VelocityContext context) throws IOException {

        StringWriter funcCode = new StringWriter();

        for (final TestData testData : new ReportTestDataProvider().getOptionTests(MavenOption.UNSUPPORTED_SET)) {
            context.put("pomFile", testData.getTestName() + "/pom.xml");
            Path testPath = resourcePath().resolve(testData.getTestName());
            context.put("baseDir", testPath.toFile().getAbsolutePath());
            FileUtils.mkDir(testPath.toFile());
            CasedString testName = new CasedString(CasedString.StringCase.SLASH, testData.getTestName());

            context.put("artifactId", testName.toCase(CasedString.StringCase.KEBAB));
            context.put("testName", testData.getTestName());
            context.put("funcName", String.join("", testName.getSegments()));

            StringBuilder configuration = new StringBuilder();
            for (Pair<Option, String[]> pair : testData.getArgs()) {
                if (pair.getKey() != null) {
                    MavenOption option = new MavenOption(pair.getKey());
                    configuration.append(option.getExample(pair.getRight())).append(System.lineSeparator());
                }
            }
            context.put("rat_configuration", configuration.toString());

            File pomFile = testPath.resolve("pom.xml").toFile();
            try (FileWriter writer = new FileWriter(pomFile)) {
                pomTemplate.merge(context, writer);
            }

            methodTemplate.merge(context, funcCode);

        }
        return funcCode.toString();
    }
}
