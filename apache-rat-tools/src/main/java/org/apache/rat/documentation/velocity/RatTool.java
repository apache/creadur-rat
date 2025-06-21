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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.VersionInfo;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.documentation.options.CLIOption;
import org.apache.rat.documentation.options.MavenOption;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;


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

    public RatTool() {
        System.out.println("RatTool");
    }

    public List<Option> options() {
        List<Option> lst = new ArrayList<>(OptionCollection.buildOptions().getOptions());
        lst.sort((a, b) -> CLIOption.createName(a).compareTo(CLIOption.createName(b)));
        return lst;
    }

    public Map<String, AntOption> antOptions() {
        Map<String, AntOption> result = new TreeMap<>();
        for (Option option : OptionCollection.buildOptions().getOptions()) {
            result.put(CLIOption.createName(option), new AntOption(option));
        }
        return result;
    }

    public Map<String, CLIOption> cliOptions() {
        Map<String, CLIOption> result = new TreeMap<>();
        for (Option option : OptionCollection.buildOptions().getOptions()) {
            CLIOption cliOption = new CLIOption(option);
            result.put(cliOption.getName(), cliOption);
        }
        return result;
    }

    public Map<String, MavenOption> mavenOptions() {
        Map<String, MavenOption> result = new TreeMap<>();
        for (Option option : OptionCollection.buildOptions().getOptions()) {
            result.put(CLIOption.createName(option), new MavenOption(option));
        }
        return result;
    }

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
    public String markdownEscape(final String text) {
        return escape(text, MARKDOWN_CHARS);
    }

    public String aptEscape(final String text) {
        return escape(text, APT_CHARS);
    }

    public List<OptionCollection.ArgumentType> argumentTypes() {
        return Arrays.stream(OptionCollection.ArgumentType.values()).filter(t -> t != OptionCollection.ArgumentType.NONE)
                .sorted((a, b) -> a.getDisplayName().compareTo(b.getDisplayName()))
                .collect(Collectors.toList());
    }

    public Set<MatchersBuilder.Matcher> matchers() {
        return MatchersBuilder.build();
    }

    public List<StandardCollection> standardCollections() {
        return Arrays.stream(org.apache.rat.config.exclusion.StandardCollection.values())
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .collect(Collectors.toList());
    }

    public List<StyleSheets> styleSheets() {
        return Arrays.stream(StyleSheets.values())
                .sorted((a, b) -> a.arg().compareTo(b.arg()))
                .collect(Collectors.toList());
    }

    public StringUtils stringUtils() {
        return new StringUtils();
    }

    public VersionInfo version() {
        return new VersionInfo();
    }

    public String tab() {
        return "\t";
    }

    public String doubleLine() {
        return "\n\n";
    }
}
