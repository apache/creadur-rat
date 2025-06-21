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
package org.apache.rat.help;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.cli.Options;
import org.apache.rat.OptionCollection;
import org.apache.rat.config.exclusion.StandardCollection;

import static java.lang.String.format;

/**
 * The help output for the command line client.
 */
public class Help extends AbstractHelp {

    /**
     * An array of notes to go at the bottom of the help output
     */
    protected static final String[] NOTES = {
            "RAT highlights possible issues.",
            "RAT reports require interpretation.",
            "RAT often requires some tuning before it runs well against a project.",
            "RAT relies on heuristics: it may miss issues"
    };

    /** The writer this instance writes to */
    protected final PrintWriter writer;

    /**
     * Creates a Help instance to write to the specified writer.
     * @param writer the writer to write to.
     */
    public Help(final Writer writer) {
        super();
        this.writer = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter(writer);
    }

    /**
     * Creates a Help instance to print to the specified stream.
     * @param stream the PrintStream to write to.
     */
    public Help(final PrintStream stream) {
        this(new PrintWriter(stream));
    }

    /**
     * Print the usage to the specific PrintWriter.
     * @param opts The defined options.
     */
    public void printUsage(final Options opts) {
        String syntax = format("java -jar apache-rat/target/apache-rat-%s.jar [options] [DIR|ARCHIVE]", versionInfo.getVersion());
        helpFormatter.printHelp(writer, syntax, header("Available options"), opts, header("Argument Types"));

        String argumentPadding = printArgumentTypes();

        writer.println(header("Standard Collections"));
        for (StandardCollection sc : StandardCollection.values()) {
            writer.format("%n<%s>%n", sc.name());
            helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
                    argumentPadding + sc.desc());
            helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
                    argumentPadding + "File patterns: " + (sc.patterns().isEmpty() ? "<none>" : String.join(", ", sc.patterns())));
            helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
                    argumentPadding + "Provides a path matcher: " + sc.hasStaticDocumentNameMatcher());
            helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
                    argumentPadding + "Provides a file processor: " + sc.fileProcessorBuilder().hasNext());
        }
        writer.println("\nA path matcher will match specific information about the file.");
        writer.println("\nA file processor will process the associated \"ignore\" file for include and exclude directives");

        writer.println(header("Notes"));
        int idx = 1;
        for (String note : NOTES) {
            writer.format("%d. %s%n", idx++, note);
        }

        writer.flush();
    }

    /**
     * Prints the list of argument types to the writer.
     * @return returns the padding for the arguments.
     */
    public String printArgumentTypes() {
        String argumentPadding = createPadding(helpFormatter.getLeftPadding() + HELP_PADDING);
        for (OptionCollection.ArgumentType argType : OptionCollection.ArgumentType.values()) {
            if (argType != OptionCollection.ArgumentType.NONE) {
                writer.format("%n<%s>%n", argType.getDisplayName());
                helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
                        argumentPadding + argType.description().get());
            }
        }
        return argumentPadding;
    }
}
