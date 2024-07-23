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
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.MatcherBuilderTracker;
import org.apache.rat.configuration.XMLConfig;
import org.apache.rat.configuration.builders.AbstractBuilder;
import org.apache.rat.help.Licenses;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * Generates text based documentation for Licenses, LicenceFamilies, and Matchers.
 * Utilizes the same command line as the CLI based Report client so that additional licenses, etc. can be added.
 */
public final class Documentation {

    /** The width of the display in characters */
    private static final int WIDTH = 120;

    private static final int INDENT_SIZE = 4;

    ReportConfiguration config;
    SortedSet<ILicense> licenses;
    Map<String, IHeaderMatcher> matchers;
    HelpFormatter formatter;
    PrintWriter printWriter;

    Documentation(ReportConfiguration config, Writer writer) {
        this.config = config;
        this.licenses = config.getLicenses(LicenseFilter.ALL);
        matchers = new TreeMap<>();
        for (ILicense l : licenses) {
            matchers.put(l.getMatcher().getId(), l.getMatcher());
        }
        printWriter = new PrintWriter(writer);
        formatter = HelpFormatter.builder().setShowDeprecated(false).setPrintWriter(printWriter).get();
    }

    void print(int indent, String text) {
        StringBuilder sb = new StringBuilder();
        int leftMargin = indent * INDENT_SIZE;
        for (int i = 0; i < leftMargin; i++) {
            sb.append(' ');
        }
        sb.append(text);
        int tabStop = leftMargin + (INDENT_SIZE / 2);
        formatter.printWrapped( printWriter, WIDTH, tabStop, sb.toString());
    }

    /**
     * Output the ReportConfiguration to a writer.
     * @throws IOException on error.
     */
    public void output() throws IOException {

        print(0, format("%n>>>> LICENSES <<<<%n%n"));

        Description licenseDescription = DescriptionBuilder.build(licenses.first());
        Collection<Description> licenseParams = licenseDescription.filterChildren(d -> d.getType() == ComponentType.PARAMETER);

        print(0, format("Licenses have the following properties:%n"));
        for (Description param : licenseParams) {
            print(1, format("%s: %s%n", param.getCommonName(), param.getDescription()));
        }

        print(0, format("%nThe defined licenses are:%n"));
        for (ILicense l : licenses) {
            printObject(0, l);
        }

        print(0, format("%n>>>> MATCHERS (Datatype IHeaderMatcher) <<<<%n%n"));
        for (Class<? extends AbstractBuilder> mClazz : MatcherBuilderTracker.instance().getClasses()) {
            try {
                AbstractBuilder builder = mClazz.getConstructor().newInstance();
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                throw new ConfigurationException(
                        format("Can not instantiate matcher builder  %s ", mClazz.getName()), e);
            }
        }

        print(0, format("%n>>>> FAMILIES (Datatype ILicenseFamily) <<<<%n%n"));
        for (ILicenseFamily family : config.getLicenseFamilies(LicenseFilter.ALL)) {
            print(1, format("'%s' - %s%n", family.getFamilyCategory().trim(), family.getFamilyName()));
        }
    }

    private void printObject(int indent, final Object object) throws IOException {
        if (object == null) {
            return;
        }
        Description description = DescriptionBuilder.build(object);
        print(indent, format("%s (%s)%n", description.getCommonName(), description.getDescription()));
        printChildren(indent+1, object, description.getChildren());
    }

    private boolean isUUID(String s) {
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException expected) {
           return false;
        }
    }

    private void printChildren(final int indent, final Object parent, final Map<String, Description> children) throws IOException {
        for (Description d : children.values()) {
            switch (d.getType()) {
                case PARAMETER:
                    if (d.isCollection()) {
                        print(indent, format("%s: %n", d.getCommonName()));
                        try {
                            Collection<?> result = (Collection<?>) d.getter(parent.getClass()).invoke(parent);
                            for (Object o : result) {
                                printObject(indent+1, o);
                            }
                            return;
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (IHeaderMatcher.class.isAssignableFrom(d.getChildType())) {
                        print(indent, format("%s: %n", d.getCommonName()));
                        // is a matcher.
                        try {
                            Object matcher = d.getter(parent.getClass()).invoke(parent);
                            printObject(indent + 1, matcher);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
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

    /**
     * Creates the documentation. Writes to the output specified by the -o or --out option. Defaults to System.out.
     * @param args the arguments. Try --help for help.
     * @throws IOException on error
     */
    public static void main(final String[] args) throws IOException {
        ReportConfiguration config = OptionCollection.parseCommands(args, Documentation::printUsage, true);
        if (config != null) {
            try (Writer writer = config.getWriter().get()) {
                new Licenses(config, writer).output();
            }
        }
    }

    private static void printUsage(final Options opts) {
        HelpFormatter f = new HelpFormatter();
        f.setOptionComparator(OptionCollection.optionComparator);
        f.setWidth(WIDTH);
        String header = "\nAvailable options";
        String footer = "";
        String cmdLine = format("java -jar apache-rat/target/apache-rat-CURRENT-VERSION.jar %s [options]",
                Documentation.class.getName());
        f.printHelp(cmdLine, header, opts, footer, false);
        System.exit(0);
    }
}
