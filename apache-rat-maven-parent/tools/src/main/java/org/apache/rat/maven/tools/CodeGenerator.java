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
package org.apache.rat.maven.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.rat.DeprecationReporter;
import org.apache.rat.OptionCollection;
import org.apache.rat.maven.MavenOption;
import org.apache.rat.maven.MavenOptionCollection;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import static java.lang.String.format;

/**
 * Generates the ${code org.apache.rat.maven.AbstractMaven} source code.
 */
public final class CodeGenerator {
    /** The Syntax for this command */
    private static final String SYNTAX = String.format("java -cp ... %s [options]", CodeGenerator.class.getName());
    /** The package name for this AbstractMaven file */
    private final CasedString packageName = new CasedString(CasedString.StringCase.DOT, "org.apache.rat.maven");
    /** The base source directory */
    private final String baseDirectory;
    /** The template for the methods within {@code AbstractMaven}. */
    private final Template methodTemplate;
    /** The template for the {@code AbstractMaven.java} file */
    private final Template javaTemplate;
    /** The Maven options */
    private final MavenOptionCollection mavenOptionCollection;

    /**
     * private constructor.
     * @param baseDirectory The base source directory the org/apache/rat/maven directory will be created here if necessary.
     */
    CodeGenerator(final String baseDirectory) {
        this.baseDirectory = baseDirectory;
        this.mavenOptionCollection = new MavenOptionCollection();

        final VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();

        // retrieve the templates
        final String templatePath = new CasedString(CasedString.StringCase.DOT, CodeGenerator.class.getPackageName()).toCase(CasedString.StringCase.SLASH);
        methodTemplate = velocityEngine.getTemplate(templatePath + "/AbstractMavenFunc.vm");
        javaTemplate = velocityEngine.getTemplate(templatePath + "/AbstractMaven.vm");
    }

    /**
     * Gets the mavens options collection for testing.
     * @return the MavenOptionsCollections
     */
    MavenOptionCollection getMavenOptionCollection() {
        return mavenOptionCollection;
    }

    /**
     * Gets the options for the command line.
     * @return the command line options.
     */
    private static Options getOptions() {
        return new Options()
                .addOption(Option.builder("h").longOpt("help").desc("Print this message").build())
                .addOption(Option.builder("p").longOpt("path").required().hasArg().desc("The path to the base of the generated java code directory").build());
    }

    /**
     * Executable entry point.
     * @param args the arguments for the executable
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
        CodeGenerator codeGenerator = new CodeGenerator(commandLine.getOptionValue("p"));
        codeGenerator.execute();
    }

    /**
     * Executes the code generation.
     * @throws IOException on IO error
     */
    private void execute() throws IOException {
        final Map<MavenOption, String> methods = gatherMethods();
        final VelocityContext context = new VelocityContext();
        context.put("methods", String.join("\n\n", methods.values()));
        File javaFile = Path.of(baseDirectory).resolve(packageName.toCase(CasedString.StringCase.SLASH))
                .resolve("AbstractMaven.java").toFile();
        FileUtils.mkDir(javaFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(javaFile, StandardCharsets.UTF_8)) {
            javaTemplate.merge(context, fileWriter);
        }
    }

    /**
     * Creates the description for a method.
     * @param mavenOption the option generating the method.
     * @return the description for the method in {@code AbstractMaven.java}.
     */
    private String createDesc(final MavenOption mavenOption) {
        String desc = mavenOption.getDescription();
        if (desc == null) {
            if (!mavenOption.isDeprecated()) {
                throw new IllegalStateException(format("Description for %s may not be null", mavenOption.getName()));
            }
            desc = "";
        } else {
            if (!desc.contains(".")) {
                throw new IllegalStateException(format("First sentence of description for %s must end with a '.'", mavenOption.getName()));
            }
        }
        if (mavenOption.getArgType() != OptionCollection.ArgumentType.NONE) {
            desc = format("%s Argument%s should be %s%s. (See Argument Types for clarification)", desc, mavenOption.hasArgs() ? "s" : "",
                    mavenOption.hasArgs() ? "" : "a ", mavenOption.getArgName());
        }
        return desc;
    }

    /**
     * Gets the argument description for the method in {@code AbstractMaven.java}.
     * @param mavenOption the maven option generating the method.
     * @param desc the description of the argument.
     * @return the argument description for the method in {@code AbstractMaven.java}.
     */
    private String createArgDesc(final MavenOption mavenOption, final String desc) {
        return mavenOption.hasArg() ? desc.substring(desc.indexOf(" ") + 1, desc.indexOf(".") + 1) :
        "The state";
    }

    /**
     * Gets parameter annotation for the method in {@code AbstractMaven.java}.
     * @param mavenOption the maven option generating the method.
     * @param propertyName the name of the "rat.X" property for command line override.
     * @return the method name description for the method in {@code AbstractMaven.java}.
     */
    private String createParameterAnnotation(final MavenOption mavenOption, final String propertyName) {
        StringBuilder sb = new StringBuilder("@Parameter");
        String property = mavenOption.hasArgs() ? null : format("property = \"rat.%s\"", propertyName);
        String defaultValue = mavenOption.isDeprecated() ? null : mavenOption.getDefaultValue();
        if (property != null || defaultValue != null) {
            sb.append("(");
            if (property != null) {
                sb.append(property).append(defaultValue != null ? ", " : StringUtils.EMPTY);
            }
            if (defaultValue != null) {
                sb.append(format("defaultValue = \"%s\"", defaultValue));
            }
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Gathers all method definitions into a single string.
     * @return the definition of all the methods.
     */
    Map<MavenOption, String> gatherMethods() {
        final Map<MavenOption, String> methods = new TreeMap<>(Comparator.comparing(MavenOption::getName));
        final VelocityContext context = new VelocityContext();
        mavenOptionCollection.getMappedOptions().forEach(mavenOption -> {
            final StringWriter methodWriter = new StringWriter();
            context.put("esc", StringEscapeUtils.class);
            context.put("option", mavenOption);
            String desc = createDesc(mavenOption);
            context.put("desc", desc);
            context.put("argDesc", createArgDesc(mavenOption, desc));
            context.put("parameterAnnotation", createParameterAnnotation(mavenOption, mavenOption.getName()));
            context.put("args", (mavenOption.hasArg() ? "String" : "boolean") + (mavenOption.hasArgs() ? "[]" : ""));
            methodTemplate.merge(context, methodWriter);

            methods.put(mavenOption, methodWriter.toString());
        });
        return methods;
    }
}
