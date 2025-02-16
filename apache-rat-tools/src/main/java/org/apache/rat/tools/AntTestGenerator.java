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
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.commandline.Arg;

import static java.lang.String.format;

/**
 * A simple tool to generate example Ant XML file from options.
 */
public final class AntTestGenerator {

    /**
     * The list of Options that are not supported by Ant.
     */
    private static final List<Option> ANT_FILTER_LIST = new ArrayList<>();

    static {
        ANT_FILTER_LIST.addAll(Arg.LOG_LEVEL.group().getOptions());
        ANT_FILTER_LIST.addAll(Arg.DIR.group().getOptions());
        ANT_FILTER_LIST.add(OptionCollection.HELP);
    }

    /**
     * the filter to filter out CLI options that Ant does not support.
     */
    private static final Predicate<Option> ANT_FILTER = option -> !(ANT_FILTER_LIST.contains(option) || option.getLongOpt() == null);

    /** A mapping of external name to internal name if not standard */
    private static final Map<String, String> RENAME_MAP = new HashMap<>();

    static {
        RENAME_MAP.put("addLicense", "add-license");
    }

    private AntTestGenerator() { }

    /**
     * Gets the Option predicate that removes unsupported CLI options.
     * @return The Option predicate that removes unsupported CLI options.
     */
    public static Predicate<Option> getFilter() {
        return ANT_FILTER;
    }

    private static String argsKey(final Option option) {
        return StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt());
    }

    /**
     * Creates a base class for an Ant task.
     * Requires 3 arguments:
     * <ol>
     *     <li>the package name for the class</li>
     *     <li>the simple class name</li>
     *     <li>the directory in which to write the class file.</li>
     * </ol>
     * @param args the arguments.
     * @throws IOException on error.
     */
    public static void main(final String[] args) throws IOException {

        List<AntOption> options = Arg.getOptions().getOptions().stream().filter(ANT_FILTER).map(AntOption::new)
                .collect(Collectors.toList());

        try (StringWriter writer = new StringWriter()) {
            writeXMLMethods(writer, options);
            writeTestMethods(writer, options);
            System.out.println(writer);
        }
    }

    private static void writeXMLMethods(final Writer writer, final List<AntOption> options) throws IOException {

        for (AntOption option : options) {
            if (option.isAttribute()) {
                writer.write(format("  <target name='%sAttribute'>%n", option.getName()));
                writer.write(format("    <rat:report %s='%s' />%n", option.getName(), getData(option)));
                writer.write(format("  </target>%n%n"));
            }

            if (option.isElement()) {
                writer.write(format("  <target name='%sElement'>%n", option.getName()));
                writer.write(format("    <rat:report>%n"));
                writer.write(format("      <%1$s>%2$s</%1$s>%n", option.getName(), getData(option)));
                writer.write(format("    </rat:report>%n"));
                writer.write(format("  </target>%n%n"));
            }
        }
    }

    private static String getData(final AntOption option) {
        if (!option.hasArg()) {
            return "true";
        }
        return format("${%s}", option.getType().getSimpleName());
    }



    private static void writeTestMethods(final Writer writer, final List<AntOption> options) throws IOException {
/*
   @Test
    public void testAddLicenseHeaders() throws Exception {
        buildRule.executeTarget("testAddLicenseHeaders");

        final File origFile = new File("target/anttasks/it-sources/index.apt");
        final String origFirstLine = getFirstLine(origFile);
        assertTrue(origFirstLine.contains("--"));
        assertFalse(origFirstLine.contains("~~"));
        final File modifiedFile = new File("target/anttasks/it-sources/index.apt.new");
        final String modifiedFirstLine = getFirstLine(modifiedFile);
        assertFalse(modifiedFirstLine.contains("--"));
        assertTrue(modifiedFirstLine.contains("~~"));
    }
 */
        for (AntOption option : options) {

            if (option.isAttribute()) {
                writer.write(format("  <target name='%sAttribute'>%n", option.getName()));
                writer.write(format("    <rat:report %s='%s' />%n", option.getName(), getData(option)));
                writer.write(format("  </target>%n%n"));
            }

            if (option.isElement()) {
                writer.write(format("  <target name='%sElement'>%n", option.getName()));
                writer.write(format("    <rat:report>%n"));
                writer.write(format("      <%1$s>%2$s</%1$s>%n", option.getName(), getData(option)));
                writer.write(format("    </rat:report>%n"));
                writer.write(format("  </target>%n%n"));
            }
        }
    }

    private static String targetName(final AntOption option) {
        if (option.isAttribute()) {
            return format("%sAttribute", option.getName());
        } else {
            return format("%sElement", option.getName());
        }
    }

//    private static String getAttributeBody(final AntOption option) {
//        return option.hasArg() ? format("        setArg(%s, %s);%n", option.keyValue(), option.getName())
//            : format("        if (%1$s) { setArg(%2$s, null); } else { removeArg(%2$s); }", option.getName(), option.keyValue());
//    }
//
//    private static String getElementClass(final AntOption option) {
//        return format("    public class %1$s extends Child { %1$s() {super(%2$s);}}%n%n", WordUtils.capitalize(option.getName()),
//                option.keyValue());
//    }
//
//    static String createName(final Option option) {
//        String name = option.getLongOpt();
//        name = StringUtils.defaultIfEmpty(RENAME_MAP.get(name), name).toLowerCase(Locale.ROOT);
//        return new CasedString(StringCase.KEBAB, name).toCase(StringCase.CAMEL);
//    }
}
