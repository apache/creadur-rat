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


import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.rat.CLIOption;
import org.apache.rat.CLIOptionCollection;
import org.apache.rat.Defaults;
import org.apache.rat.OptionCollection;
import org.apache.rat.api.EnvVar;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.documentation.options.AntOptionCollection;
import org.apache.rat.documentation.options.MavenOption;
import org.apache.rat.documentation.options.MavenOptionCollection;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RatToolTest {
    final RatTool underTest = new RatTool();

    @Test
    void environmentVariables() {
        List<EnvVar> vars = underTest.environmentVariables();
        assertThat(vars.size()).isEqualTo(EnvVar.values().length);
        for (EnvVar envVar : EnvVar.values()) {
            vars.remove(envVar);
        }
        assertThat(vars).isEmpty();
    }

    @Test
    void antOptions() {
        Map<String, AntOption> options = underTest.antOptions();
        assertThat(options).isNotEmpty();
        assertThat(options.values()).containsExactlyInAnyOrderElementsOf(AntOptionCollection.INSTANCE.getMappedOptions().toList());
    }

    @Test
    void options() {
        List<Option> options = underTest.options();
        assertThat(options).isNotEmpty();
        assertThat(options).containsExactlyInAnyOrderElementsOf(CLIOptionCollection.INSTANCE.getMappedOptions()
                    .map(CLIOption::getOption).toList());
    }

    @Test
    void cliOptions() {
        Map<String, CLIOption> options = underTest.cliOptions();
        assertThat(options).isNotEmpty();
        assertThat(options.values()).containsExactlyInAnyOrderElementsOf(CLIOptionCollection.INSTANCE.getMappedOptions().toList());
    }

    @Test
    void mvnOptions() {
        Map<String, MavenOption> options = underTest.mvnOptions();
        assertThat(options).isNotEmpty();
        assertThat(options.values()).containsExactlyInAnyOrderElementsOf(MavenOptionCollection.INSTANCE.getMappedOptions().toList());
    }

    @Test
    void markdownEscape() {
        String chars = "\\`*_{}[]<>()#+-.!|";
        String result = underTest.markdownEscape(chars);
        assertThat(result).isEqualTo("\\\\\\`\\*\\_\\{\\}\\[\\]\\<\\>\\(\\)\\#\\+\\-\\.\\!\\|");
    }

    @Test
    void aptEscape() {
        String chars = "\\~=-+*[]<>{}";
        String result = underTest.aptEscape(chars);
        assertThat(result).isEqualTo("\\\\\\~\\=\\-\\+\\*\\[\\]\\<\\>\\{\\}");
    }

    @Test
    void argumentTypes() {
        List<OptionCollection.ArgumentType> argumentTypes = underTest.argumentTypes();
        for (OptionCollection.ArgumentType argumentType : OptionCollection.ArgumentType.values()) {
            if (argumentType != OptionCollection.ArgumentType.NONE) {
                assertThat(argumentTypes).contains(argumentType);
            }
        }
    }

    @Test
    void matchers() {   
        List<String> matchers = underTest.matchers().stream().map(Matcher::getName).toList();
        assertThat(matchers).containsExactlyInAnyOrder("all", "any", "copyright", "matcherRef", "not", "regex", "spdx", "text");
    }


    @Test
    void standardCollections() {
        List<StandardCollection> lst = underTest.standardCollections();
        assertThat(lst).containsExactlyInAnyOrder(StandardCollection.values());
    }

    @Test
    void styleSheets() {
        List<StyleSheets> styleSheets = underTest.styleSheets();
        assertThat(styleSheets).containsExactlyInAnyOrder(StyleSheets.values());
    }


    @Test
    void licenseProperties() {
        List<String> actual = underTest.licenseProperties().stream().map(Description::getCommonName).toList();
        assertThat(actual).containsExactlyInAnyOrder("family", "id", "matcher", "name", "note");
    }

    @Test
    void licenses() {
        List<String> actual = underTest.licenses().stream().map(License::name).toList();
        List<String> expected =
                Defaults.builder().build().getLicenseSetFactory().getLicenses(LicenseSetFactory.LicenseFilter.ALL)
                        .stream().map(ILicense::getName).toList();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }


}
