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
package org.apache.rat.documentation.options;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.commandline.Arg;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.CasedString;

import static java.lang.String.format;

/**
 * The collection of MavenOptions equivalent to the CLI options
 * with any unsupported options removed.
 */
public final class AntOptionCollection extends UIOptionCollection<AntOption> {
    /** mapping of standard name to non-conflicting name. */
    private static final Map<String, String> RENAME_MAP;

    /**
     * The format for an XML element
     */
    private static final String DEFAULT_XML = "<%1$s>%%s</%1$s>%n";

    /** Attributes that are required for example data. */
    private static final Map<String, Map<String, String>> REQUIRED_ATTRIBUTES = new HashMap<>();
    /** The list of data types that are specified as XML attributes in ant build.xml documents */
    private static final List<Class<?>> ATTRIBUTE_TYPES = new ArrayList<>();

    /** The map of option name conversioins */
    private final Map<Option, Option> conversionMap;

    static {
        RENAME_MAP = new HashMap<>();
        RENAME_MAP.put("addLicense", "add-license");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("editLicense", "true");
        REQUIRED_ATTRIBUTES.put("copyright", attributes);
        REQUIRED_ATTRIBUTES.put("editCopyright", attributes);
        REQUIRED_ATTRIBUTES.put("force", attributes);
        REQUIRED_ATTRIBUTES.put("editOverwrite", attributes);

        // types that are specified as XML attributes in Ant.
        ATTRIBUTE_TYPES.add(String.class);
        ATTRIBUTE_TYPES.add(String[].class);
        ATTRIBUTE_TYPES.add(Integer.class);
        ATTRIBUTE_TYPES.add(Long.class);
        ATTRIBUTE_TYPES.add(File.class);

    }

    public static Map<String, String> getRenameMap() {
        return new TreeMap<>(RENAME_MAP);
    }

    /** The instance of the MavenOptionCollection */
    public static final AntOptionCollection INSTANCE = new Builder().build();

    /**
     * Create an Instance.
     */
    private AntOptionCollection(final Builder builder) {
        super(builder);
        conversionMap = builder.conversions;
    }

    public Set<AntOption> convertedFrom(final AntOption antOption) {
        return conversionMap.entrySet().stream().filter(e -> e.getValue().equals(antOption.getOption()))
                .map(e -> getMappedOption(e.getKey()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    /**
     * If this option is converted to another option return that option otherwise
     * return this option.
     * @return the converted option.
     */
    public AntOption getActualAntOption(final AntOption antOption) {
        Option opt = conversionMap.get(antOption.getOption());
        return opt == null ? antOption : getMappedOption(opt).orElse(antOption);
    }

    public boolean isAttribute(final AntOption antOption) {
        Option opt = antOption.getOption();
        return (!opt.hasArg() || opt.getArgs() == 1) && convertedFrom(antOption).isEmpty() &&
                ATTRIBUTE_TYPES.contains(opt.getType());
    }

    public Map<String, String> getRequiredAttributes(final String name) {
        return REQUIRED_ATTRIBUTES.get(name);
    }

    BuildType buildType(final OptionCollection.ArgumentType type) {
        return switch (type) {
            case FILE, DIRORARCHIVE -> new BuildType("filename") {
                @Override
                protected String getMethodFormat(final AntOption antOption) {
                    return "  <fileset file='%s' />\n";
                }
            };
            case NONE -> new BuildType("");
            case COUNTERPATTERN -> new BuildType("cntr");
            case EXPRESSION -> new BuildType("expr");
            case STANDARDCOLLECTION -> new BuildType("std");
            case LICENSEID, FAMILYID -> new BuildType("lst");
            default -> new BuildType(type.getDisplayName()) {
                @Override
                protected String getMethodFormat(final AntOption antOption) {
                    return String.format(DEFAULT_XML, WordUtils.uncapitalize(antOption.getArgName()));
                }
            };
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Provides a new name for an option if it is renamed in the collection.
     * @param name the option name.
     * @return the collection name, may be the same as the option name.
     */
    static String rename(final String name) {
        return StringUtils.defaultIfEmpty(RENAME_MAP.get(name), name);
    }

    /**
     * Creates the name for the option based on rules for conversion of CLI option names.
     * @param option the standard option.
     * @return the new Option name as a CasedString.
     */
    static CasedString createName(final Option option) {
        List<String> pluralEndings = List.of("approved", "denied");
        String name = rename(ArgumentTracker.extractKey(option));
        CasedString casedName = new CasedString(CasedString.StringCase.KEBAB, name);
        String[] segments = casedName.getSegments();
        String lastSegment = segments[segments.length - 1];
        if (option.hasArgs() && !lastSegment.endsWith("s") && !pluralEndings.contains(lastSegment)) {
            segments[segments.length - 1] += "s";
            casedName = new CasedString(CasedString.StringCase.KEBAB, segments);
        }
        return casedName.as(CasedString.StringCase.PASCAL);
    }

    /**
     * The Builder for the MavenOptionCollection.
     */
    public static final class Builder extends UIOptionCollection.Builder<AntOption, Builder> {
        /** convert key to value type when generating code */
        private final Map<Option, Option> conversions = new HashMap<>();

        private Builder() {
            super(AntOption::new);
            Arg.getOptions().getOptions()
                    .stream().filter(o -> Objects.isNull(o.getLongOpt()))
                    .forEach(this::unsupported);
            unsupported(Arg.LOG_LEVEL)
                    .unsupported(Arg.DIR)
                    .unsupported(Arg.SOURCE)
                    .unsupported(Arg.HELP_LICENSES)
                    // conversions
                    .convert(Arg.LICENSES_APPROVED_FILE, Arg.LICENSES_APPROVED)
                    .convert(Arg.LICENSES_DENIED_FILE, Arg.LICENSES_DENIED)
                    .convert(Arg.FAMILIES_APPROVED_FILE, Arg.FAMILIES_APPROVED)
                    .convert(Arg.FAMILIES_DENIED_FILE, Arg.FAMILIES_DENIED)
                    .convert(Arg.INCLUDE_FILE, Arg.INCLUDE)
                    .convert(Arg.INCLUDE_STD, Arg.INCLUDE)
                    .convert(Arg.EXCLUDE_FILE, Arg.EXCLUDE)
                    .convert(Arg.EXCLUDE_STD, Arg.EXCLUDE);
        }

        @Override
        public AntOptionCollection build() {
            return new AntOptionCollection(this);
        }

        public Builder convert(final Arg from, final Arg to) {
            Option mapTo = to.option();
            for (Option option : from.group().getOptions()) {
                if (!option.equals(mapTo) && !option.isDeprecated()) {
                    conversions.put(option, mapTo);
                }
            }
            return self();
        }
    }

    /**
     * A mapping of data type of XML format.
     */
    public static class BuildType {
        /**
         * The configuration tag for this build type
         */
        private final String tag;
        /**
         * If True adds the tag as the test extension
         */
        private final boolean addExt;

        /**
         * The constructor for the build type.
         *
         * @param tag the XML tag for this data type.
         */
        BuildType(final String tag) {
            this.tag = tag;
            this.addExt = StringUtils.isNotEmpty(tag);
        }

        /**
         * Returns the format used when multiple arguments are expected by an Ant option.
         *
         * @param antOption the Ant option to check.
         * @return the format used for multiple arguments.
         */
        protected String getMultipleFormat(final AntOption antOption) {
            return String.format("<%1$s>%2$s</%1$s>%n", tag, antOption);
        }

        /**
         * Gets the method based on how many arguments an Ant option requires.
         *
         * @param antOption the Ant option to check.
         * @return the method format for the option.
         */
        protected String getMethodFormat(final AntOption antOption) {
            return antOption.hasArgs() ? getMultipleFormat(antOption) : String.format("<%1$s>%%s</%1$s>%n", tag);
        }

        /**
         * Gets a string comprising the Ant XML pattern for this data type and the number of arguments expected by the Ant option.
         *
         * @param delegateOption the Ant option that the call is delegated to.
         * @param antOption      the actual ant option.
         * @param data           the data for the actual ant option.
         * @return the Ant XML pattern for this data type.
         */
        public String getXml(final AntOption delegateOption, final AntOption antOption, final String data) {
            String fmt = getMethodFormat(antOption);
            String value = data == null ? WordUtils.uncapitalize(antOption.getArgName()) : data;
            String inner = format(fmt, value);
            return format("<%1$s>%2$s</%1$s>%n", delegateOption.getName(), inner);
        }

        public String getXml(final AntOption antOption, final String data) {
            AntOption delegateOption = antOption.getActualAntOption();
            if (delegateOption.isAttribute()) {
                return "";
            } else {
                return format(getMethodFormat(antOption), data);
            }
        }


        public String testName(final AntOption antOption) {
            return addExt ? format("%s_%s", antOption.getName(), antOption.getArgName()) : antOption.getName();
        }
    }
}
