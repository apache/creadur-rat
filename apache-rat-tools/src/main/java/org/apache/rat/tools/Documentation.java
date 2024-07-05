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
package org.apache.rat.tools;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.rat.ConfigurationException;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.MatcherBuilderTracker;
import org.apache.rat.configuration.builders.AbstractBuilder;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;

/**
 * Generates text based documentation for Licenses, LicenceFamilies, and Matchers.
 *
 * Utilizes the same command line as the CLI based Report client so that additional licenses, etc. can be added.
 *
 */
public final class Documentation {

    /** The indentation for the documentation */
    private static final String INDENT = "   ";

    /** The number of characters for a page width */
    private static final int PAGE_WIDTH = 120;

    private Documentation() {
        // Do not instantiate
    }

    /**
     * Output the ReportConfiguration to a writer.
     * @param cfg The configuration to write.
     * @param writer the writer to write to.
     * @throws IOException on error.
     */
    public static void output(final ReportConfiguration cfg, final Writer writer) throws IOException {
        writer.write(String.format("%n>>>> LICENSES <<<<%n%n"));
        for (ILicense l : cfg.getLicenses(LicenseFilter.ALL)) {
            printLicense(l, writer);
        }

        writer.write(String.format("%n>>>> MATCHERS (Datatype IHeaderMatcher) <<<<%n%n"));
        for (Class<? extends AbstractBuilder> mClazz : MatcherBuilderTracker.instance().getClasses()) {
            try {
                AbstractBuilder builder = mClazz.getConstructor().newInstance();
                printMatcher(DescriptionBuilder.buildMap(builder.builtClass()), writer);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                throw new ConfigurationException(
                        String.format("Can not instantiate matcher builder  %s ", mClazz.getName()), e);
            }
        }

        writer.write(String.format("%n>>>> FAMILIES (Datatype ILicenseFamily) <<<<%n%n"));
        for (ILicenseFamily family : cfg.getLicenseFamilies(LicenseFilter.ALL)) {
            printFamily(family, writer);
        }
    }

    private static void printFamily(final ILicenseFamily family, final Writer writer) throws IOException {
        writer.write(String.format("'%s' - %s%n", family.getFamilyCategory().trim(), family.getFamilyName()));
    }

    private static void printMatcher(final Description matcher, final Writer writer) throws IOException {
        writer.write(String.format("'%s' - %s%n", matcher.getCommonName(), matcher.getDescription()));
        printChildren(1, matcher.getChildren(), writer);

    }

    private static void printLicense(final ILicense license, final Writer writer) throws IOException {
        Description dLicense = license.getDescription();
        writer.write(String.format("'%s' - %s %n", dLicense.getCommonName(), dLicense.getDescription()));
        printChildren(1, dLicense.getChildren(), writer);
    }

    private static void writeIndent(final int indent, final Writer writer) throws IOException {
        for (int i = 0; i < indent; i++) {
            writer.write(INDENT);
        }
    }

    private static void printChildren(final int indent, final Map<String, Description> children, final Writer writer) throws IOException {
        for (Description d : children.values()) {
            writeIndent(indent, writer);
            switch (d.getType()) {
            case PARAMETER:
            case BUILD_PARAMETER:
                writer.write(String.format("'%s' %s (Datatype: %s%s)%n", d.getCommonName(), d.getDescription(),
                        d.isCollection() ? "Collection of " : "", d.getChildType().getSimpleName()));
                break;
            case LICENSE:
                // do nothing
                break;
            case MATCHER:
                writeIndent(indent + 1, writer);
                writer.write(String.format("%s %s %n", d.isCollection() ? "Collection of " : "A ",
                        d.getChildType().getSimpleName()));
                printChildren(indent + 2, d.getChildren(), writer);
                break;
            }
        }
    }

    /**
     * Creates the documentation.  Writes to the output specified by the -o or --out option.  Defaults to System.out.
     * @param args the arguments.  Try --help for help.
     * @throws IOException on error
     */
    public static void main(final String[] args) throws IOException {
        ReportConfiguration config = OptionCollection.parseCommands(args, Documentation::printUsage, true);
        if (config != null) {
            try (Writer writer = config.getWriter().get()) {
                Documentation.output(config, writer);
            }
        }
    }

    private static void printUsage(final Options opts) {
        HelpFormatter f = new HelpFormatter();
        f.setOptionComparator(new OptionCollection.OptionComparator());
        f.setWidth(PAGE_WIDTH);
        String header = "\nAvailable options";
        String footer = "";
        String cmdLine = String.format("java -jar apache-rat/target/apache-rat-CURRENT-VERSION.jar %s [options]",
                Documentation.class.getName());
        f.printHelp(cmdLine, header, opts, footer, false);
        System.exit(0);
    }
}
