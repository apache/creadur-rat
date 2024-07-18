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
package org.apache.rat.help;

import static java.lang.String.format;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.MatcherBuilderTracker;
import org.apache.rat.configuration.builders.AbstractBuilder;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;

/**
 * Generates text based documentation for Licenses, LicenceFamilies, and Matchers.
 * Utilizes the same command line as the CLI based Report client so that additional licenses, etc. can be added.
 */
public final class Licenses extends AbstractHelp {
    /** The report configuration to extract licenses from */
    private final ReportConfiguration config;
    /** The licenses in the report config */
    private final SortedSet<ILicense> licenses;
    /** The formatter */
    private final HelpFormatter formatter;
    /** The writer to write to */
    private final PrintWriter printWriter;

    /**
     * Constructor
     * @param config The configuration that contains the license information.
     * @param writer the writer to write the report to.
     */
    public Licenses(final ReportConfiguration config, final Writer writer) {
        this.config = config;
        this.licenses = config.getLicenses(LicenseFilter.ALL);
        printWriter = new PrintWriter(writer);
        formatter = HelpFormatter.builder().setShowDeprecated(false).setPrintWriter(printWriter).get();
    }

    /**
     * Prints the text indented and wrapped
     * @param indent the number of spaces to indent.
     * @param text the text to write.
     */
    void print(final int indent, final String text) {
        int leftMargin = indent * HELP_PADDING;
        int tabStop = leftMargin + (HELP_PADDING / 2);
        formatter.printWrapped(printWriter, HELP_WIDTH, tabStop, createPadding(leftMargin) + text);
    }

    /**
     * print the help text with the version information.
     * @throws IOException on output error.
     */
    public void printHelp() throws IOException {
        print(0, format("Listing of licenses for %s", versionInfo));
        output();
    }

    /**
     * Output the License information without the version information.
     * @throws IOException on error.
     */
    public void output() throws IOException {

        print(0, header("LICENSES"));

        if (licenses.isEmpty()) {
            print(0, "No licenses defined");
        } else {
            Description licenseDescription = DescriptionBuilder.build(licenses.first());
            Collection<Description> licenseParams = licenseDescription.filterChildren(d -> d.getType() == ComponentType.PARAMETER);

            print(0, format("Licenses have the following properties:%n"));

            for (Description param : licenseParams) {
                print(1, format("%s: %s%n", param.getCommonName(), param.getDescription()));
            }

            print(0, format("%nThe defined licenses are:%n"));
            for (ILicense l : licenses) {
                print(0, System.lineSeparator());
                printObject(0, l);
            }
        }
        print(0, header("DEFINED MATCHERS"));
        SortedSet<Description> matchers = new TreeSet<>((d1, d2) -> d1.getCommonName().compareTo(d2.getCommonName()));
        for (Class<? extends AbstractBuilder> mClazz : MatcherBuilderTracker.instance().getClasses()) {
            try {
                AbstractBuilder builder = mClazz.getConstructor().newInstance();
                matchers.add(builder.getDescription());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                     | IllegalArgumentException | InvocationTargetException e) {
                throw new ConfigurationException(
                        format("Can not instantiate matcher builder  %s ", mClazz.getName()), e);
            }
        }
        for (Description description : matchers) {
            print(0, format("%n%s: %s%n", description.getCommonName(), description.getDescription()));
            for (Description child : description.getChildren().values()) {
                if (IHeaderMatcher.class.isAssignableFrom(child.getChildType())) {
                    if (child.isCollection()) {
                        print(2, "Encloses multiple matchers");
                    } else {
                        print(2, "Wraps a single matcher");
                    }
                } else {
                    print(1, format("%s: %s%s", child.getCommonName(), child.isRequired() ? "(required) " : "", child.getDescription()));
                }
            }
        }

        print(0, header("DEFINED FAMILIES"));
        for (ILicenseFamily family : config.getLicenseFamilies(LicenseFilter.ALL)) {
            print(1, format("%s - %s%n", family.getFamilyCategory(), family.getFamilyName()));
        }
        printWriter.flush();
    }

    /**
     * Print the description of an object.
     * @param indent the number of spaces to indent the pring.
     * @param object the object to print.
     * @throws IOException on output error.
     */
    private void printObject(final int indent, final Object object) throws IOException {
        if (object == null) {
            return;
        }
        Description description = DescriptionBuilder.build(object);
        if (description == null) {
            print(indent, format("Unknown Object of class: %s%n", object.getClass().getName()));
        } else {
            print(indent, format("%s (%s)%n", description.getCommonName(), description.getDescription()));
            printChildren(indent + 1, object, description.getChildren());
        }
    }

    /**
     * Returns {@code true} if the string is a UUID.
     * @param s the string to check.
     * @return {@code true} if the string is a UUID.
     */
    private boolean isUUID(final String s) {
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException expected) {
           return false;
        }
    }

    /**
     * Print the information for the children.
     * @param indent the number of spaces to indent.
     * @param parent the parent object.
     * @param children the children of the parent.
     * @throws IOException on write error.
     */
    private void printChildren(final int indent, final Object parent, final Map<String, Description> children) throws IOException {
        for (Description d : children.values()) {
            switch (d.getType()) {
                case PARAMETER:
                    if (d.isCollection()) {
                        print(indent, format("%s: %n", d.getCommonName()));
                        try {
                            Collection<?> result = (Collection<?>) d.getter(parent.getClass()).invoke(parent);
                            for (Object o : result) {
                                printObject(indent + 1, o);
                            }
                            return;
                        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (IHeaderMatcher.class.isAssignableFrom(d.getChildType())) {
                        print(indent, format("%s: %n", d.getCommonName()));
                        // is a matcher.
                        try {
                            Object matcher = d.getter(parent.getClass()).invoke(parent);
                            printObject(indent + 1, matcher);
                        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        String txt = StringUtils.defaultIfBlank(d.getParamValue(parent), "").replaceAll("\\s{2,}", " ");
                        if (!txt.isEmpty() && !(d.getCommonName().equals("id") && isUUID(txt))) {
                               print(indent, format("%s: %s%n", d.getCommonName(), txt.replaceAll("\\s{2,}", " ")));
                        }
                    }
                    break;
                case BUILD_PARAMETER:
                case LICENSE:
                    // do nothing
                    break;
                case MATCHER:
                    printChildren(indent + 1, parent, d.getChildren());
                    break;
            }
        }
    }
}
