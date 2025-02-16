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
package org.apache.rat.anttasks;

import java.io.File;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.text.WordUtils;
import org.apache.rat.commandline.Arg;
import org.apache.rat.document.DocumentName;
import org.apache.rat.tools.AntGenerator;
import org.apache.rat.tools.AntOption;
import org.apache.rat.utils.CasedString;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.MagicTestNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.util.ProcessUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;


public class GeneratedReportTest  {

    private StringBuilder logBuffer;
    private StringBuilder fullLogBuffer;
    private StringBuilder outputBuffer;
    private StringBuilder errorBuffer;
    private Project project;

    @TempDir
    static Path tempDir;

    static Map<String, String> requiredAttributes = new HashMap<>();
    static Map<String, String> requiredElements = new HashMap<>();
    static Map<String, String> dataValues = new HashMap<>();


    private static void writeFile(String name, String contents) throws IOException {
        final File testFile = new File(tempDir.toFile(), name);
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(testFile.toPath()))) {
            writer.write(contents);
        }
    }

    @BeforeAll
    static void setupStatics() {
        requiredAttributes.put("copyright", "editLicense='true'");
        requiredAttributes.put("editCopyright", "editLicense='true'");
        requiredAttributes.put("force", "editLicense='true'");
        requiredAttributes.put("editOverwrite", "editLicense='true'");
        requiredElements.put("configurationNoDefaults", "<config file='noDefaultsConfig.xml'/>");
        requiredElements.put("noDefaultLicenses", "<config file='noDefaultLicensesConfig.xml'/>");
    }

    @BeforeEach
    public void setup() throws IOException {
        writeFile("test.file", "// test file");
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("generatedData")
    void argumentTests(String name, String buildXml, AntOption option) throws IOException {
        final File antFile = new File(tempDir.toFile(), targetName(option) + ".xml").getAbsoluteFile();
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(antFile.toPath()))) {
            writer.write(buildXml);
        }
        DocumentName documentName = DocumentName.builder(antFile).setBaseName(antFile.getParentFile()).build();
        System.setProperty(MagicNames.PROJECT_BASEDIR, documentName.getBaseName());
        logBuffer = new StringBuilder();
        fullLogBuffer = new StringBuilder();

        project = new Project();
        if (Boolean.getBoolean(MagicTestNames.TEST_BASEDIR_IGNORE)) {
            System.clearProperty(MagicNames.PROJECT_BASEDIR);
        }
        project.init();

        project.setProperty(MagicTestNames.TEST_PROCESS_ID, ProcessUtil.getProcessId("<Process>"));
        project.setProperty(MagicTestNames.TEST_THREAD_NAME, Thread.currentThread().getName());
        project.setUserProperty(MagicNames.ANT_FILE, antFile.getAbsolutePath());
        project.addBuildListener(new AntTestListener(logBuffer, fullLogBuffer, Project.MSG_DEBUG));
        ProjectHelper.configureProject(project, antFile);

        executeTarget(targetName(option));
        if (option.isDeprecated()) {
            assertThat(logBuffer).contains(option.getDeprecated());
        }
    }

    public void executeTarget(String targetName) {
        outputBuffer = new StringBuilder();
        PrintStream out = new PrintStream(new AntOutputStream(outputBuffer));
        errorBuffer = new StringBuilder();
        PrintStream err = new PrintStream(new AntOutputStream(errorBuffer));

        /* we synchronize to protect our custom output streams from being overridden
         * by other tests executing targets concurrently. Ultimately this would only
         * happen if we ran a multi-threaded test executing multiple targets at once, and
         * this protection doesn't prevent a target from internally modifying the output
         * stream during a test - but at least this scenario is fairly deterministic so
         * easier to troubleshoot.
         */
        synchronized (System.out) {
            PrintStream sysOut = System.out;
            PrintStream sysErr = System.err;
            sysOut.flush();
            sysErr.flush();
            try {
                System.setOut(out);
                System.setErr(err);
                project.executeTarget(targetName);
            } finally {
                System.setOut(sysOut);
                System.setErr(sysErr);
            }
        }
    }

    static String targetName(AntOption option) {
        return option.getName() + (option.isAttribute() ? "Attribute" :"Element");
    }

    static Stream<Arguments> generatedData() throws IOException {
        List<org.apache.rat.tools.AntOption> options = Arg.getOptions().getOptions().stream()
                .filter(AntGenerator.getFilter()).map(AntOption::new)
                .collect(Collectors.toList());

        List<Arguments> lst = new ArrayList<>();

        for (AntOption option : options) {
            StringBuilder xml = new StringBuilder(prefix());
            xml.append(format("  <target name='%s'>%n", targetName(option)));
            if (option.isAttribute()) {
                xml.append(format("    <rat:report %s=\"%s\"", option.getName(), getData(option)));
                String additionalAttributes = requiredAttributes.get(option.getName());
                if (additionalAttributes != null) {
                    xml.append(format(" %s", additionalAttributes));
                }
                xml.append(format(" >%n"));
            }

            if (option.isElement()) {
                xml.append(format("    <rat:report>%n"));
                if (option.argCount() == 1) {
                    xml.append(format("      <%s %s=\"%s\" />%n", option.getName(), createAttribute(option), getData(option)));
                } else {
                    xml.append(format("      <%1s>%n", option.getName()));
                    if (option.getType() == String.class) {
                        xml.append(format("       %s%n", getData(option)));
                    } else {
                        xml.append(format("        <%1$s>%2$s</%1$s>%n", createAttribute(option), getData(option)));
                    }
                    xml.append(format("      </%s>%n", option.getName()));
                }
            }

            String additionalElements = requiredElements.get(option.getName());
            if (additionalElements != null) {
                xml.append(format("      %s%n", additionalElements));
            }
            xml.append(format("      <file file='test.file' />%n"));
            xml.append(format("    </rat:report>%n"));
            xml.append(format("  </target>%n%n</project>%n"));

            lst.add(Arguments.of(option.getName(), xml.toString(), option));
        }
        return lst.stream();
    }

    private static String createAttribute(final AntOption option) {
        return WordUtils.capitalize(option.getArgName().substring(0, 1)) + option.getArgName().substring(1);
    }

    private static String getData(final AntOption option) throws IOException {

        switch (option.getName()) {
            case "config":
                writeFile("configData.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rat-config/>");
                return "configData.xml";
            case "licenses":
                writeFile("licensesData.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rat-config/>");
                return "licensesData.xml";
            case "licensesApproved" :
                return "AL, CC";
            case "configurationNoDefaults":
                writeFile("noDefaultsConfig.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<rat-config>\n" +
                        "\t<families>\n" +
                        "\t\t<family id=\"DUMMY\" name=\"A Dummy license\" />\n" +
                        "\t</families>\n" +
                        "\t<licenses>\n" +
                        "\t\t<license family=\"DUMMY\">\n" +
                        "\t\t\t<text>Any old text</text>\n" +
                        "\t\t</license>\n" +
                        "\t</licenses>\n" +
                        "\t<approved>\n" +
                        "\t\t<family license_ref='DUMMY' />\n" +
                        "\t</approved>\n" +
                        "\t<matchers>\n" +
                        "\t\t<matcher class=\"org.apache.rat.configuration.builders.AllBuilder\" />\n" +
                        "\t\t<matcher class=\"org.apache.rat.configuration.builders.AnyBuilder\" />\n" +
                        "\t\t<matcher class=\"org.apache.rat.configuration.builders.CopyrightBuilder\" />\n" +
                        "\t\t<matcher class=\"org.apache.rat.configuration.builders.MatcherRefBuilder\" />\n" +
                        "\t\t<matcher class=\"org.apache.rat.configuration.builders.NotBuilder\" />\n" +
                        "\t\t<matcher class=\"org.apache.rat.configuration.builders.RegexBuilder\" />\n" +
                        "\t\t<matcher class=\"org.apache.rat.configuration.builders.SpdxBuilder\" />\n" +
                        "\t\t<matcher class=\"org.apache.rat.configuration.builders.TextBuilder\" />\n" +
                        "\t</matchers>\n" +
                        "</rat-config>\n");
                return "true";
            case "noDefaultLicenses":
                writeFile("noDefaultLicensesConfig.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<rat-config>\n" +
                        "\t<families>\n" +
                        "\t\t<family id=\"DUMMY\" name=\"A Dummy license\" />\n" +
                        "\t</families>\n" +
                        "\t<licenses>\n" +
                        "\t\t<license family=\"DUMMY\">\n" +
                        "\t\t\t<text>Any old text</text>\n" +
                        "\t\t</license>\n" +
                        "\t</licenses>\n" +
                        "</rat-config>\n");
                return "true";
            case "out":
            case "outputFile":
                return option.getName() + ".txt";
            default:
                if (!option.hasArg()) {
                    return "true";
                } else {
                    return format("${%s}", option.getType().getSimpleName());
                }
        }
    }

    private static String prefix() {
        return "<project default=\"all\"\n" +
                "  xmlns:au=\"antlib:org.apache.ant.antunit\"\n" +
                "  xmlns:rat=\"antlib:org.apache.rat.anttasks\">\n" +
                "\n" +
                "  <taskdef uri=\"antlib:org.apache.ant.antunit\"\n" +
                "  \tresource=\"org/apache/ant/antunit/antlib.xml\"\n" +
                "  \tclasspath=\"${test.classpath}\" />\n" +
                "\n" +
                "  <taskdef uri=\"antlib:org.apache.rat.anttasks\"\n" +
                "  \tresource=\"org/apache/rat/anttasks/antlib.xml\"\n" +
                "  \tclasspath=\"${test.classpath}\" />\n" +
                "\n" +
                "  <property name=\"File\" value='test.file' />\n" +
                "  <property name=\"Integer\" value=\"5\" />\n" +
                "  <property name=\"String\" value=\"hello\" />\n" +
                "  <property name=\"StandardCollection\" value=\"GIT\" />\n\n";
    }


    private static class AntTestListener implements BuildListener {
        private int logLevel;
        private StringBuilder logBuffer;
        private StringBuilder fullLogBuffer;
        /**
         * Constructs a test listener which will ignore log events
         * above the given level.
         */
        public AntTestListener(StringBuilder logBuffer, StringBuilder fullLogBuffer, int logLevel) {
            this.logBuffer = logBuffer;
            this.fullLogBuffer = fullLogBuffer;
            this.logLevel = logLevel;
        }

        /**
         * Fired before any targets are started.
         */
        public void buildStarted(BuildEvent event) {
        }

        /**
         * Fired after the last target has finished. This event
         * will still be thrown if an error occurred during the build.
         *
         * @see BuildEvent#getException()
         */
        public void buildFinished(BuildEvent event) {
        }

        /**
         * Fired when a target is started.
         *
         * @see BuildEvent#getTarget()
         */
        public void targetStarted(BuildEvent event) {
        }

        /**
         * Fired when a target has finished. This event will
         * still be thrown if an error occurred during the build.
         *
         * @see BuildEvent#getException()
         */
        public void targetFinished(BuildEvent event) {
        }

        /**
         * Fired when a task is started.
         *
         * @see BuildEvent#getTask()
         */
        public void taskStarted(BuildEvent event) {
        }

        /**
         * Fired when a task has finished. This event will still
         * be throw if an error occurred during the build.
         *
         * @see BuildEvent#getException()
         */
        public void taskFinished(BuildEvent event) {
        }

        /**
         * Fired whenever a message is logged.
         *
         * @see BuildEvent#getMessage()
         * @see BuildEvent#getPriority()
         */
        public void messageLogged(BuildEvent event) {
            if (event.getPriority() > logLevel) {
                // ignore event
                return;
            }

            if (event.getPriority() == Project.MSG_INFO
                    || event.getPriority() == Project.MSG_WARN
                    || event.getPriority() == Project.MSG_ERR) {
                logBuffer.append(event.getMessage());
            }
            fullLogBuffer.append(format("[%s] %s%n", Report.fromProjectLevel(event.getPriority()), event.getMessage()));
        }
    }

    protected static class AntOutputStream extends OutputStream {
        private StringBuilder buffer;

        public AntOutputStream(StringBuilder buffer) {
            this.buffer = buffer;
        }

        public void write(int b) {
            buffer.append((char) b);
        }

    }
}
