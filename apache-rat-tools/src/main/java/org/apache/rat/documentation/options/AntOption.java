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
package org.apache.rat.documentation.options;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.commandline.Arg;
import org.apache.rat.utils.CasedString;

import static java.lang.String.format;

/**
 * A class that wraps the CLI option and provides Ant specific values.
 */
public class AntOption extends AbstractOption {


    /**
     * the filter to filter out CLI options that Ant does not support.
     */
    private static final Predicate<Option> ANT_FILTER;


    /**
     * The list of Options that are not supported by Ant.
     */
    private static final List<Option> UNSUPPORTED_LIST = new ArrayList<>();

    /**
     * The Mapping of option to the implementation option in Ant.
     */
    private static final Map<Option, Option> ANT_CONVERSION_MAP = new HashMap<>();

    /**
     * The list of example patterns for various classes
     */
    private static final Map<OptionCollection.ArgumentType, BuildType> BUILD_TYPE_MAP = new HashMap<>();

    /** The list of attribute Types */
    private static final List<Class<?>> ATTRIBUTE_TYPES = new ArrayList<>();
    /** A mapping of external name to internal name if not standard. */
    private static final Map<String, String> RENAME_MAP = new HashMap<>();


    /** Attributes that are required for example data */
    private static final Map<String, Map<String, String>> REQUIRED_ATTRIBUTES = new HashMap<>();


    /**
     * Adds a mapping from the specified Arg to the Arg to be used for generation.
     * @param arg the arg to map.
     * @param actualArg the Arg to map {@code arg} to.
     */
    private static void updateConversionMap(final Arg arg, final Arg actualArg) {
        Option mapTo = actualArg.option();
        for (Option option : arg.group().getOptions()) {
            if (!option.equals(mapTo) && !option.isDeprecated()) {
                ANT_CONVERSION_MAP.put(option, mapTo);
            }
        }
    }

    static {
        RENAME_MAP.put("addLicense", "add-license");
        ATTRIBUTE_TYPES.add(String.class);
        ATTRIBUTE_TYPES.add(String[].class);
        ATTRIBUTE_TYPES.add(Integer.class);
        ATTRIBUTE_TYPES.add(Long.class);
        ATTRIBUTE_TYPES.add(File.class);
        Arg.getOptions().getOptions().stream().filter(o -> Objects.isNull(o.getLongOpt())).forEach(UNSUPPORTED_LIST::add);
        UNSUPPORTED_LIST.addAll(Arg.LOG_LEVEL.group().getOptions());
        UNSUPPORTED_LIST.addAll(Arg.DIR.group().getOptions());
        UNSUPPORTED_LIST.add(OptionCollection.HELP);
        UNSUPPORTED_LIST.addAll(Arg.SOURCE.group().getOptions());
        updateConversionMap(Arg.LICENSES_APPROVED_FILE, Arg.LICENSES_APPROVED);
        updateConversionMap(Arg.LICENSES_DENIED_FILE, Arg.LICENSES_DENIED);
        updateConversionMap(Arg.FAMILIES_APPROVED_FILE, Arg.FAMILIES_APPROVED);
        updateConversionMap(Arg.FAMILIES_DENIED_FILE, Arg.FAMILIES_DENIED);
        updateConversionMap(Arg.INCLUDE_FILE, Arg.INCLUDE);
        updateConversionMap(Arg.INCLUDE_STD, Arg.INCLUDE);
        updateConversionMap(Arg.EXCLUDE_FILE, Arg.EXCLUDE);
        updateConversionMap(Arg.EXCLUDE_STD, Arg.EXCLUDE);

        /*
         * Create the BuildTypes for the Argument types.
         */
        BuildType buildType;
        for (OptionCollection.ArgumentType type : OptionCollection.ArgumentType.values()) {
            switch (type) {
                case FILE:
                case DIRORARCHIVE:
                    buildType = new BuildType(type, "filename") {
                        @Override
                        protected String getMultipleFormat(final AntOption antOption) {
                            return "  <fileset file='%s' />\n";
                        }
                    };
                    BUILD_TYPE_MAP.put(type, buildType);
                    break;
                case NONE:
                    buildType = new BuildType(type, "");
                    BUILD_TYPE_MAP.put(type, buildType);
                    break;
                case COUNTERPATTERN:
                    buildType = new BuildType(type, "cntr");
                    BUILD_TYPE_MAP.put(type, buildType);
                    break;
                case EXPRESSION:
                    buildType = new BuildType(type, "expr");
                    BUILD_TYPE_MAP.put(type, buildType);
                    break;
                case STANDARDCOLLECTION:
                    buildType = new BuildType(type, "std");
                    BUILD_TYPE_MAP.put(type, buildType);
                    break;
                case LICENSEID:
                case FAMILYID:
                    buildType = new BuildType(type, "lst");
                    BUILD_TYPE_MAP.put(type, buildType);
                    break;
                default:
                    buildType = new BuildType(type, type.getDisplayName()) {
                        protected String getMethodFormat(final AntOption antOption) {
                            return String.format("<%1$s>%%s</%1$s>\n", WordUtils.uncapitalize(antOption.getArgName()));
                        }
                    };
                    BUILD_TYPE_MAP.put(type, buildType);
            }
        }
        Set<Option> filteredOptions = getFilteredOptions();
        ANT_FILTER = option -> !(filteredOptions.contains(option) || option.getLongOpt() == null);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("editLicense", "true");
        REQUIRED_ATTRIBUTES.put("copyright", attributes);
        REQUIRED_ATTRIBUTES.put("editCopyright", attributes);
        REQUIRED_ATTRIBUTES.put("force", attributes);
        REQUIRED_ATTRIBUTES.put("editOverwrite", attributes);

    }

    public static List<AntOption> getAntOptions() {
        return Arg.getOptions().getOptions().stream().filter(ANT_FILTER).map(AntOption::new)
                .collect(Collectors.toList());
    }
    /**
     * Gets the list of unsupported options.
     * @return The list of unsupported options.
     */
    public static List<Option> getUnsupportedOptions() {
        return Collections.unmodifiableList(UNSUPPORTED_LIST);
    }

    /**
     * Gets the set of options that are not supported by Ant either by not being supported or
     * by being converted to another argument.
     * @return The set of options that are not supported by Ant.
     */
    public static Set<Option> getFilteredOptions() {
        HashSet<Option> filteredOptions = new HashSet<>(UNSUPPORTED_LIST);
        filteredOptions.addAll(ANT_CONVERSION_MAP.keySet());
        return filteredOptions;
    }

    public static Map<String, String> getRenameMap() {
        return Collections.unmodifiableMap(RENAME_MAP);
    }

    /**
     * Constructor.
     *
     * @param option the option to wrap.
     */
    public AntOption(final Option option) {
        super(option, createName(option));
    }

    public static String createName(final Option option) {
        String name = option.getLongOpt();
        name = StringUtils.defaultIfEmpty(RENAME_MAP.get(name), name).toLowerCase(Locale.ROOT);
        return new CasedString(CasedString.StringCase.KEBAB, name).toCase(CasedString.StringCase.CAMEL);
    }

    /**
     * Returns {@code true} if the option should be an attribute of the &lt;rat:report&gt; element.
     *
     * @return {@code true} if the option should be an attribute of the &lt;rat:report&gt; element.
     */
    public boolean isAttribute() {
        return (!option.hasArg() || option.getArgs() == 1) && ATTRIBUTE_TYPES.contains(option.getType())
                && convertedFrom().isEmpty();

    }

    /**
     * Returns {@code true} if the option should be a child element of the &lt;rat:report&gt; element.
     *
     * @return {@code true} if the option should be a child element of the &lt;rat:report&gt; element.
     */
    public boolean isElement() {
        return !isAttribute();
    }

    /**
     * If this option is converted to another option return that option otherwise
     * return this option.
     * @return the converted option.
     */
    public AntOption getActualAntOption() {
        Option opt = ANT_CONVERSION_MAP.get(this.option);
        return opt == null ? this : new AntOption(opt);
    }

    /**
     * Gets the set of options that are mapped to this option.
     * @return the set of options that are mapped to this option.
     */
    public Set<Option> convertedFrom() {
        return ANT_CONVERSION_MAP.entrySet().stream().filter(e -> e.getValue().equals(option))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public String getText() {
        return cleanupName(option);
    }

    protected String cleanupName(final Option option) {
        AntOption antOption = new AntOption(option);
        String fmt = antOption.isAttribute() ? "%s attribute" : "<%s>";
        return format(fmt, createName(option));
    }

    /**
     * Get the method comment for this option.
     *
     * @param addParam if {@code true} the param annotation is added.
     * @return the Comment block for the function.
     */
    public String getComment(final boolean addParam) {
        StringBuilder sb = new StringBuilder();
        String desc = getDescription();
        if (desc == null) {
            throw new IllegalStateException(format("Description for %s may not be null", getName()));
        }
        if (!desc.contains(".")) {
            throw new IllegalStateException(format("First sentence of description for %s must end with a '.'", getName()));
        }
        if (addParam) {
            String arg;
            if (option.hasArg()) {
                arg = desc.substring(desc.indexOf(" ") + 1, desc.indexOf(".") + 1);
                arg = WordUtils.capitalize(arg.substring(0, 1)) + arg.substring(1);
            } else {
                arg = "The state";
            }
            if (option.getArgName() != null) {
                Supplier<String> sup = OptionCollection.getArgumentTypes().get(option.getArgName());
                if (sup == null) {
                    throw new IllegalStateException(format("Argument type %s must be in OptionCollection.ARGUMENT_TYPES", option.getArgName()));
                }
                desc = format("%s Argument%s should be %s%s. (See Argument Types for clarification)", desc, option.hasArgs() ? "s" : "",
                        option.hasArgs() ? "" : "a ", option.getArgName());
            }
            sb.append(format("    /**%n     * %s%n     * @param %s %s%n", StringEscapeUtils.escapeHtml4(desc), getName(),
                    StringEscapeUtils.escapeHtml4(arg)));
        } else {
            sb.append(format("    /**%n     * %s%n", StringEscapeUtils.escapeHtml4(desc)));
        }
        if (option.isDeprecated()) {
            sb.append(format("     * @deprecated %s%n", StringEscapeUtils.escapeHtml4(getDeprecated())));
        }
        return sb.append(format("     */%n")).toString();
    }


    /**
     * Get the signature of the attribute function.
     *
     * @return the signature of the attribute function.
     */
    public String getAttributeFunctionName() {
        return "set" +
                WordUtils.capitalize(name) +
                (option.hasArg() ? "(String " : "(boolean ") +
                name +
                ")";
    }

    @Override
    public String getExample() {
        return new ExampleGenerator().getExample();
    }

    /**
     * A mapping of data type of XML format.
     */
    private static class BuildType {
        /** The argument type associated with their build type */
        private final OptionCollection.ArgumentType type;
        /** The configuration tag for this build type */
        private final String tag;

        /**
         * The constructor for the build type.
         * @param type the ArgumentType as specified in the OptionCollection.
         * @param tag the XML tag for this data type.
         */
        BuildType(final OptionCollection.ArgumentType type, final String tag) {
            this.type = type;
            this.tag = tag;
        }

        /**
         * Returns the format used when multiple arguments are expected by an Ant option.
         * @param antOption the Ant option to check.
         * @return the format used for multiple arguments.
         */
        protected String getMultipleFormat(final AntOption antOption) {
            return String.format("<%1$s>%%s</%1$s>\n", tag);
        }

        /**
         * Gets the method based on how many arguments an Ant option requires.
         * @param antOption the Ant option to check.
         * @return the method format for the option.
         */
        protected String getMethodFormat(final AntOption antOption) {
            return antOption.hasArgs() ? getMultipleFormat(antOption) : String.format("<%1$s>%%s</%1$s>\n", tag);
        }

        /**
         * Gets a string comprising the Ant XML pattern for this data type and the number of arguments expected by the Ant option.
         * @param delegateOption the Ant option that the call is delegated to.
         * @param antOption the actual ant option.
         * @param data the data for the actual ant option.
         * @return the Ant XML pattern for this data type.
         */
        public String getPattern(final AntOption delegateOption, final AntOption antOption, final String data) {
            String fmt = getMethodFormat(antOption);
            String value = data == null ? WordUtils.uncapitalize(antOption.getArgName()) : data;
            String inner = format(fmt, value);
            return format("<%1$s>%2$s</%1$s>%n", delegateOption.getName(), inner);
        }
    }

    /**
     * An example code generator for this AntOption.
     */
    public class ExampleGenerator {

        /**
         * The constructor.
         */
        public ExampleGenerator() {
        }

        /**
         * Gets an example Ant XML report call using ant option.
         * @return the example of this ant option.
         */
        String getExample() {
                return getExample("data", REQUIRED_ATTRIBUTES.get(getName()), null);
        }

        /**
         * Gets an example Ant XML report call using ant option with the specified attributes and child elements.
         * @param data The data value for this option.
         * @param attributes A map of attribute keys and values.
         * @param childElements a list of child elements for the example
         * @return
         */
        public String getExample(final String data, final Map<String, String> attributes, final List<String> childElements) {
            if (UNSUPPORTED_LIST.contains(option)) {
                return "-- not supported --";
            }
            return "<rat:report" +
                    getExampleAttributes(data, attributes) +
                    "> \n" +
                    getChildElements(data, childElements) +
                    "</rat:report>\n";
        }

        /**
         * Creates a string comprising the attributes for the Ant XML report call.
         * @param data The data value for this option.
         * @param attributes A map of attribute keys and values.
         * @return a string comprising all the attribute keys and values for the Ant XML report element.
         */
        public String getExampleAttributes(final String data, final Map<String, String> attributes) {
            AntOption actualOption = getActualAntOption();
            StringBuilder result = new StringBuilder();
            if (attributes != null) {
                attributes.forEach((k, v) -> result.append(format(" %s=\"%s\"", k, v)));
            }
            if (actualOption.isAttribute()) {
                result.append(format(" %s=\"%s\"", actualOption.getName(), actualOption.hasArg() ? data : "true"));
            }
            return result.toString();
        }

        /**
         * Creates a string comprising the child elements for the Ant XML report call.
         * @param data the data for this option.
         * @param childElements additional child elements.
         * @return A string comprising the child elements for the Ant XML report call.
         */
        public String getChildElements(final String data, final List<String> childElements) {
            AntOption baseOption = AntOption.this;
            AntOption actualOption = getActualAntOption();
            StringBuilder result = new StringBuilder();
            if (!actualOption.isAttribute()) {
                String inner = BUILD_TYPE_MAP.get(getArgType()).getPattern(actualOption, baseOption, data);
                result.append(inner);
            }
            if (childElements != null) {
                childElements.forEach(x -> result.append(x).append("\n"));
            }
            return result.toString();
        }
    }
}
