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
package org.apache.rat.ui;

import java.io.IOException;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.text.WordUtils;
import org.apache.rat.DeprecationReporter;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import static java.lang.String.format;
import static org.apache.rat.OptionCollectionParser.ArgumentType.NONE;

/**
 * Generates the ${code org.apache.rat.maven.AbstractMaven} source code.
 * @param <T> The concrete implementation of the AbstractOption.
 */
public abstract class AbstractCodeGenerator<T extends AbstractOption<?>> {
    /** The base source directory */
    protected final String baseDirectory;
    /** the velocity engine to generate files with */
    protected final VelocityEngine velocityEngine;
    /**
     * private constructor.
     * @param baseDirectory The base source directory.
     */
    protected AbstractCodeGenerator(final String baseDirectory) {
        this.baseDirectory = baseDirectory;
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();
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
    protected static void processArgs(final String syntax,
                                   final Function<String, AbstractCodeGenerator<?>> instance,
                                   final String[] args)
            throws IOException {
        CommandLine commandLine = null;
        try {
            commandLine = DefaultParser.builder().setDeprecatedHandler(DeprecationReporter.getLogReporter())
                    .build().parse(getOptions(), args);
        } catch (ParseException pe) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(syntax, pe.getMessage(), getOptions(), "");
            System.exit(1);
        }

        if (commandLine.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(syntax, getOptions());
            System.exit(0);
        }
        AbstractCodeGenerator<?> codeGenerator = instance.apply(commandLine.getOptionValue("p"));
        codeGenerator.execute();
    }

    /**
     * Executes the code generation.
     * @throws IOException on IO error
     */
    protected abstract void execute() throws IOException;

    /**
     * Creates the description for a method.
     * @param abstractOption the option generating the method.
     * @return the description for the method in {@code AbstractMaven.java}.
     */
    protected final String createDesc(final T abstractOption) {
        String desc = abstractOption.getDescription();
        if (desc == null) {
            throw new IllegalStateException(format("Description for %s may not be null", abstractOption.getName()));
        }
        if (!desc.contains(".")) {
            throw new IllegalStateException(format("First sentence of description for %s must end with a '.'", abstractOption.getName()));
        }
        if (abstractOption.getArgType() != NONE) {
            desc = format("%s Argument%s should be %s%s. (See Argument Types for clarification)", desc, abstractOption.hasArgs() ? "s" : "",
                    abstractOption.hasArgs() ? "" : "a ", abstractOption.getArgName());
        }
        return desc;
    }

    /**
     * Gets the argument description for the method returned from ${link createMethodName}.
     * @param abstractOption the maven option generating the method.
     * @param desc the description of the argument.
     * @return the argument description for the method in {@code AbstractMaven.java}.
     */
    protected String createArgDesc(final T abstractOption, final String desc) {
        if (abstractOption.hasArg()) {
            String argDesc = desc.substring(desc.indexOf(" ") + 1, desc.indexOf(".") + 1);
            return WordUtils.capitalize(argDesc.substring(0, 1)) + argDesc.substring(1);
        } else {
            return "The state";
        }
    }

    /**
     * Gets method name for the option.
     * @param abstractOption the maven option generating the method.
     * @return the method name description for the method in {@code AbstractMaven.java}.
     */
    protected abstract String createMethodName(T abstractOption);

    /**
     * Gathers all method definitions into a single string.
     * @return the definition of all the methods.
     */
    protected abstract String gatherMethods();
}
