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
package org.apache.rat.documentation.velocity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.Defaults;
import org.apache.rat.OptionCollection;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.MatcherBuilderTracker;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.documentation.options.CLIOption;
import org.apache.rat.documentation.options.MavenOption;
import org.apache.rat.help.AbstractHelp;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

/**
 * The Velocity RAT plugin that provides access to the RAT data.
 */
@DefaultKey("rat")
@ValidScope({"application"})
public class RatTool {

    private static String[] charParser(final String charText) {
        char[] chars = charText.toCharArray();
        String[] result = new String[chars.length];
        for (int i = 0; i < chars.length; i++) {
            result[i] = String.valueOf(chars[i]);
        }
        return result;
    }

    /**
     * The characters to escape for markdown.
     */
    private static final String[] MARKDOWN_CHARS = charParser("\\`*_{}[]<>()#+-.!|");
    /**
     * The characters to escape for APT (Almost Plain Text)
     */
    private static final String[] APT_CHARS = charParser("\\~=-+*[]<>{}");

    /** The license factory this tool uses */
    private final LicenseSetFactory licenseSetFactory;;

    /**
     * Constructor.
     */
    public RatTool() {
        Defaults defaults = Defaults.builder().build();
        licenseSetFactory = defaults.getLicenseSetFactory();
    }

    /**
     * Gets the list of command line options.
     * @return the list of command line options.
     */
    public List<Option> options() {
        List<Option> lst = new ArrayList<>(OptionCollection.buildOptions().getOptions());
        lst.sort(Comparator.comparing(CLIOption::createName));
        return lst;
    }

    /**
     * Gets a map client option name to Ant Option.
     * @return a map client option name to Ant Option.
     */
    public Map<String, AntOption> antOptions() {
        Map<String, AntOption> result = new TreeMap<>();
        for (AntOption antOption : AntOption.getAntOptions()) {
            result.put(CLIOption.createName(antOption.getOption()), antOption);
        }
        return result;
    }

    /**
     * Gets a map client option name to CLI Option.
     * @return a map client option name to CLI Option.
     */
    public Map<String, CLIOption> cliOptions() {
        Map<String, CLIOption> result = new TreeMap<>();
        for (Option option : OptionCollection.buildOptions().getOptions()) {
            CLIOption cliOption = new CLIOption(option);
            result.put(cliOption.getName(), cliOption);
        }
        return result;
    }

    /**
     * Gets a map client option name to Maven Option.
     * @return a map client option name to Maven Option.
     */
    public Map<String, MavenOption> mvnOptions() {
        Map<String, MavenOption> result = new TreeMap<>();
        for (MavenOption mavenOption : MavenOption.getMavenOptions()) {
            result.put(CLIOption.createName(mavenOption.getOption()), mavenOption);
        }
        return result;
    }

    /**
     * Escapes a text string.
     * @param text the text to escape.
     * @param chars the characters to escape.
     * @return the escaped string.
     */
    private String escape(final String text, final String[] chars) {
        if (text == null) {
            return "";
        }
        String result = text;
        for (String c : chars) {
            result = result.replace(c, "\\" + c);
        }
        return result;
    }

    /**
     * Escapes a string for markdown.
     * @param text the text to escape.
     * @return the text with the markdown specific characters escaped.
     */
    public String markdownEscape(final String text) {
        return escape(text, MARKDOWN_CHARS);
    }

    /**
     * Escapes a string for APT (almost plain text).
     * @param text the text to escape.
     * @return the text with the APT specific characters escaped.
     */
    public String aptEscape(final String text) {
        return escape(text, APT_CHARS);
    }

    /**
     * Gets the list of argument types.
     * @return a list of argument types.
     */
    public List<OptionCollection.ArgumentType> argumentTypes() {
        return Arrays.stream(OptionCollection.ArgumentType.values()).filter(t -> t != OptionCollection.ArgumentType.NONE)
                .sorted((a, b) -> a.getDisplayName().compareTo(b.getDisplayName()))
                .collect(Collectors.toList());
    }

    /**
     * Gets the set of Matchers.
     * @return the set of Matchers.
     */
    public Set<Matcher> matchers() {
        MatcherBuilderTracker tracker = MatcherBuilderTracker.instance();
        Set<Matcher> documentationSet = new TreeSet<>((x, y) -> x.getName().compareTo(y.getName()));
        for (Class<?> clazz : tracker.getClasses()) {
            Description desc = DescriptionBuilder.buildMap(clazz);
            documentationSet.add(new Matcher(desc, null));
        }
        return documentationSet;
    }

    /**
     * Gets the list of standard collections.
     * @return the list of standard collections.
     */
    public List<StandardCollection> standardCollections() {
        return Arrays.stream(org.apache.rat.config.exclusion.StandardCollection.values())
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of stylesheets.
     * @return the list of stylesheets.
     */
    public List<StyleSheets> styleSheets() {
        return Arrays.stream(StyleSheets.values())
                .sorted(Comparator.comparing(StyleSheets::arg))
                .collect(Collectors.toList());
    }

    /**
     * Gets the {@link StringUtils} object.
     * @return the apache.commons.land3 StringUtils object.
     * @link StringUtils
     */
    public StringUtils stringUtils() {
        return new StringUtils();
    }

    /**
     * Gets a tab character.
     * @return the tab character.
     */
    public String tab() {
        return "\t";
    }

    /**
     * Gets two new line.
     * @return a string containing two new lines.
     */
    public String doubleLine() {
        return "\n\n";
    }

    /**
     * Gets a list of license property descriptions.
     * @return a list of license property descriptions.
     */
    public List<Description> licenseProperties() {
        SortedSet<ILicense> licenses = licenseSetFactory.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
        Description licenseDescription = DescriptionBuilder.build(licenses.first());
        List<Description> descriptions = new ArrayList<>(licenseDescription.filterChildren(d -> d.getType() == ComponentType.PARAMETER));
        descriptions.sort((a, b) -> a.getCommonName().compareTo(b.getCommonName()));
        return descriptions;
    }

    /**
     * Gets the list of defined licenses.
     * @return the list of defined licenses.
     */
    public List<License> licenses() {
        Set<ILicense> licenses = licenseSetFactory.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
        return licenses.stream().map(License::new).collect(Collectors.toList());
    }

    /**
     * Creates a string of spaces of the specified length.
     * @param length the lenght of the string.
     * @return a string of spaces of the specified length.
     */
    public String pad(final int length) {
        return AbstractHelp.createPadding(length);
    }
}
