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
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.document.DocumentName;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.utils.DefaultLog;
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
    @TempDir
    static Path tempDir;

    private static final Map<String, String> REQUIRED_ATTRIBUTES = new HashMap<>();
    private static final Map<String, String> REQUIRED_ELEMENTS = new HashMap<>();
    private static final Map<OptionCollection.ArgumentType, BuildType> ARG_TYPE_MAP = new HashMap<>();

    static {
        BuildType buildType = null;
        for (OptionCollection.ArgumentType argType : OptionCollection.ArgumentType.values()) {
            switch (argType) {
                case FILE:
                case DIRORARCHIVE:
                    buildType = new BuildType("") {
                        @Override
                        protected String getMethodFormat(final AntOption antOption) {
                            return "<fileset file='%s' />";
                        }
                    };
                    break;
                case NONE:
                    buildType = new BuildType("") {
                        @Override
                        protected String getMethodFormat(final AntOption antOption) {
                            return "";
                        }
                    };
                    break;
                case STANDARDCOLLECTION:
                    buildType = new BuildType("std");
                    break;
                case EXPRESSION:
                    buildType = new BuildType("expr");
                    break;
                case COUNTERPATTERN:
                    buildType = new BuildType("cntr");
                    break;
                case LICENSEID:
                case FAMILYID:
                    buildType = new BuildType("lst");
                    break;
                default:
                    buildType = new BuildType("") {
                        @Override
                        protected String getMethodFormat(final AntOption antOption) {
                            return format("<%1$s>%%s</%1$s>", tag);
                        }
                    };
            }
            ARG_TYPE_MAP.put(argType, buildType);
        }
    }

    /**
     * The prefix for the ant build.xml file.
     */
    private static final String BUILD_XML_PREFIX =
            "<project default=\"all\"\n" +
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
                "  <property name=\"ConfigFile\" location=\"configData.xml\" />\n\n";

    private static File writeFile(String name, String contents) throws IOException {
        final File testFile = new File(tempDir.toFile(), name);
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(testFile.toPath()))) {
            writer.write(contents);
        }
        return testFile;
    }

    @BeforeAll
    static void setupStatics() {
        REQUIRED_ATTRIBUTES.put("copyright", "editLicense='true'");
        REQUIRED_ATTRIBUTES.put("editCopyright", "editLicense='true'");
        REQUIRED_ATTRIBUTES.put("force", "editLicense='true'");
        REQUIRED_ATTRIBUTES.put("editOverwrite", "editLicense='true'");
        REQUIRED_ELEMENTS.put("configurationNoDefaults", configFile("noDefaultsConfig.xml"));
        REQUIRED_ELEMENTS.put("noDefaultLicenses", configFile("noDefaultLicensesConfig.xml"));
    }

    @BeforeEach
    public void setup() throws IOException {
        writeFile("test.file", "// test file");
    }

    private static String configFile(String fileName) {
        return format("<config><fileset file=\"%s\" /></config>", fileName);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("generatedData")
    void argumentTests(String name, String buildXml, AntOption option) throws IOException {
        DefaultLog.getInstance().debug("Running " + name);
        final File antFile = new File(tempDir.toFile(), targetName(option) + ".xml").getAbsoluteFile();
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(antFile.toPath()))) {
            writer.write(buildXml);
        }
        DocumentName documentName = DocumentName.builder(antFile).setBaseName(antFile.getParentFile()).build();
        System.setProperty(MagicNames.PROJECT_BASEDIR, documentName.getBaseName());
        StringBuilder fullLogBuffer = new StringBuilder();
        StringBuilder outputBuffer = new StringBuilder();
        StringBuilder errorBuffer = new StringBuilder();
        DefaultLog.setInstance(null);
        Project project = new Project();
        if (Boolean.getBoolean(MagicTestNames.TEST_BASEDIR_IGNORE)) {
            System.clearProperty(MagicNames.PROJECT_BASEDIR);
        }
        project.init();
        project.setProperty(MagicTestNames.TEST_PROCESS_ID, ProcessUtil.getProcessId("<Process>"));
        project.setProperty(MagicTestNames.TEST_THREAD_NAME, Thread.currentThread().getName());
        project.setUserProperty(MagicNames.ANT_FILE, antFile.getAbsolutePath());
        AntTestListener listener = new AntTestListener(option.getName(), fullLogBuffer, Project.MSG_DEBUG);
        project.addBuildListener(listener);
        ProjectHelper.configureProject(project, antFile);
        executeTarget(outputBuffer, errorBuffer, project, targetName(option));
        if (option.isDeprecated()) {
            assertThat(listener.logBuffer).contains(option.getDeprecated());
        }
    }

    private void executeTarget(StringBuilder outputBuffer, StringBuilder errorBuffer, Project project, String targetName) {
        PrintStream out = new PrintStream(new AntOutputStream(outputBuffer));
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
        AntOption actualOption = option.getActualAntOption();
        return actualOption.getName() + (actualOption.isAttribute() ? "Attribute" :"Element");
    }

    /**
     * Generate the data for the tests.
     * @return the arguments for the tests.
     */
    static Stream<Arguments> generatedData() {

        List<AntOption> options = Arg.getOptions().getOptions().stream()
                .filter(o -> !AntOption.getFilteredOptions().contains(o)).map(AntOption::new)
                .collect(Collectors.toList());

        List<Arguments> lst = new ArrayList<>();

        for (AntOption option : options) {
            lst.add(createTest(option));
            option.convertedFrom().forEach(o -> lst.add(createTest(new AntOption(o))));
        }
        for (Arguments arguments : lst) {
            Object[] objects = arguments.get();
            System.out.format("%s -> %s%n", objects[0], objects[1]);
        }
        return lst.stream();
    }

    private static Arguments createTest(AntOption option) {
        AntOption actualOption = option.getActualAntOption();
        BuildType buildType = ARG_TYPE_MAP.get(option.getArgType());
        String xml = buildXml(actualOption, option, buildType.getXml(option));
        return Arguments.of(buildType.testName(option), xml, option);
    }

    private static String buildXml(AntOption actualOption, AntOption option, String body) {
        StringBuilder xml = new StringBuilder(BUILD_XML_PREFIX);
        xml.append(format("<!-- %s -->%n", option.getName()));
        xml.append(format("  <target name='%s'>%n", targetName(option)));
        if (actualOption.isAttribute()) {
            xml.append(format("    <rat:report %s=\"%s\"", actualOption.getName(), getData(option)));
            String additionalAttributes = REQUIRED_ATTRIBUTES.get(actualOption.getName());
            if (additionalAttributes != null) {
                xml.append(format(" %s", additionalAttributes));
            }
            xml.append(format(" >%n"));
        } else {
            xml.append("    <rat:report");
            String additionalAttributes = REQUIRED_ATTRIBUTES.get(actualOption.getName());
            if (additionalAttributes != null) {
                xml.append(format(" %s", additionalAttributes));
            }
            xml.append(">\n");
            if (body == null) {
                xml.append(format("      <%1$s>%2$s</%1$s>%n", actualOption.getName(), getData(option)));
            } else {
//                if (actualOption.argCount() == 1) {
//                    xml.append(format("      <%s %s=\"%s\" />%n", actualOption.getName(), createAttribute(option), getData(option)));
//                } else {
                    xml.append(format("      <%1$s>%2$s</%1$s>%n", actualOption.getName(), body));
//                }
            }
        }

        String additionalElements = REQUIRED_ELEMENTS.get(option.getName());
        if (additionalElements != null) {
            xml.append(format("      %s%n", additionalElements));
        }
        xml.append(format("      <file file='test.file' />%n"));
        xml.append(format("    </rat:report>%n"));
        xml.append(format("  </target>%n%n</project>%n"));
        return xml.toString();
    }

    private static String getData(AntOption option) {
        String value = getData(option.getName());
        if (value == null) {
            if (!option.hasArg()) {
                return "true";
            } else {
                throw new IllegalStateException("Missing " + option.getName());
            }
        }
        return value;
    }
    private static String getData(String name) {
        try {
            switch (name) {
                case "copyright":
                case "editCopyright":
                    return "My Copyright info";
                case "config":
                    writeFile("configData.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rat-config/>");
                    return "${ConfigFile}";
                case "licenses":
                    return writeFile("licensesData.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rat-config/>").getName();
                case "licenseFamiliesApprovedFile":
                    return writeFile("licenseFamiliesApprovedFile.txt", getData("licenseFamiliesApproved")).getName();
                case "licenseFamiliesDeniedFile":
                    return writeFile("licenseFamiliesDeniedFile.txt", getData("licenseFamiliesDenied")).getName();
                case "licensesApproved":
                case "licenseFamiliesApproved":
                case "licenseFamiliesDenied":
                case "licensesDenied":
                    return "AL, CC";
                case "licensesApprovedFile":
                    return writeFile("licensesApprovedFile.txt", getData("licensesApproved")).getName();
                case "licensesDeniedFile":
                    return writeFile("licensesDeniedFile.txt", getData("licensesDenied")).getName();
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
                    return tempDir.resolve(name + ".txt").toString();
                case "xml":
                    return "true";
                case "stylesheet":
                case "outputStyle":
                    return StyleSheets.PLAIN.arg();
                case "inputInclude":
                case "inputExclude":
                case "exclude":
                case "include":
                    return "a/**file/stuff";
                case "excludeFile" :
                    return writeFile("excludeFile.txt", getData("exclude")).getAbsolutePath();
                case "includesFile" :
                    return writeFile("includeFile.txt", getData("include")).getAbsolutePath();
                case "inputIncludeFile" :
                    return writeFile("inputIncludeFile.txt", getData("inputInclude")).getName();
                case "inputExcludeFile" :
                    return writeFile("inputExcludeFile.txt", getData("inputExclude")).getName();
                case "inputExcludeSize":
                    return "500";
                case "inputIncludeStd":
                case "inputExcludeStd":
                    return "GIT";
                case "counterMin":
                case "counterMax":
                    return "BINARIES:3";
                case "inputExcludeParsedScm":
                    return "IDEA";
                case "outputLicenses":
                case "outputFamilies":
                case "listLicenses":
                case "listFamilies":
                    return LicenseSetFactory.LicenseFilter.ALL.name();
                case "outputArchive":
                case "outputStandard":
                    return ReportConfiguration.Processing.ABSENCE.name();
                default:
                    return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class AntTestListener implements BuildListener {
        private final int logLevel;
        private final StringBuilder logBuffer;
        private final StringBuilder fullLogBuffer;
        /**
         * Constructs a test listener which will ignore log events
         * above the given level.
         */
        public AntTestListener(String name, StringBuilder fullLogBuffer, int logLevel) {
            this.logBuffer = new StringBuilder();
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
         * be thrown if an error occurred during the build.
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
            if (event.getPriority() <= Project.MSG_INFO) {
                logBuffer.append(format("[%s] %s%n", Report.fromProjectLevel(event.getPriority()), event.getMessage()));
            }
            fullLogBuffer.append(format("[%s] %s%n", Report.fromProjectLevel(event.getPriority()), event.getMessage()));
        }
    }

    protected static class AntOutputStream extends OutputStream {
        private final StringBuilder buffer;

        public AntOutputStream(StringBuilder buffer) {
            this.buffer = buffer;
        }

        public void write(int b) {
            buffer.append((char) b);
        }
    }

    public static class BuildType {
        /** The configuration tag for this build type */
        protected final String tag;
        /** If True adds the tag as the test extension */
        private final boolean addExt;

        BuildType(final String tag) {
            this(tag, StringUtils.isNotEmpty(tag));
        }

        BuildType(final String tag, boolean addExt) {
            this.tag = tag;
            this.addExt = addExt;
        }

        protected String getMultipleFormat(final AntOption antOption) {
            return String.format("  <%1$s>%%s</%1$s>\n", tag);
        }

        protected String getMethodFormat(final AntOption antOption) {
            return antOption.hasArgs() ? getMultipleFormat(antOption) : String.format("  <%1$s>%%s</%1$s>\n", tag);
        }

        public String testName(final AntOption antOption) {
            return addExt ? format("%s_%s", antOption.getName(), antOption.getArgName()) : antOption.getName();
        }

        public String getXml(final AntOption antOption) {
            AntOption delegateOption = antOption.getActualAntOption();
            if (delegateOption.isAttribute()) {
                return "";
            } else {
                return format(getMethodFormat(antOption), getData(antOption));
            }
        }
    }
}
