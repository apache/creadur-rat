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
package org.apache.rat.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.OptionCollectionTest;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReporterTest;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.DocumentNameMatcherTest;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log.Level;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import static org.apache.rat.commandline.Arg.HELP_LICENSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * A list of methods that an OptionsProvider in a test case must support.
 * Use of this interface ensures consistent testing across the UIs. Each method
 * tests an Option from OptionCollection that must be implemented in the UI.
 * Each method in this interface tests an Option in {@link org.apache.rat.OptionCollection}.
 */
public abstract class AbstractOptionsProvider implements ArgumentsProvider {
    /**
     * A map of test Options to tests.
     */
    protected final Map<String, OptionCollectionTest.OptionTest> testMap = new TreeMap<>();
    /** The list of exclude args */
    protected static final String[] EXCLUDE_ARGS = {"*.foo", "%regex[[A-Z]\\.bar]", "justbaz"};
    /** the list of include args */
    protected static final String[] INCLUDE_ARGS = {"B.bar", "justbaz"};
    /**
     * The directory to place test data in.
     */
    protected final File baseDir;

    /**
     * Copy the runtime data to the "target" directory.
     * @param baseDir the base directory to copy to.
     * @param targetDir the directory relative to the base directory to copy to.
     */
    public static void preserveData(File baseDir, String targetDir) {
        final Path recordPath = FileSystems.getDefault().getPath("target", targetDir);
        recordPath.toFile().mkdirs();
        try {
            FileUtils.copyDirectory(baseDir, recordPath.toFile());
        } catch (IOException e) {
            System.err.format("Unable to copy data from %s to %s%n", baseDir, recordPath);
        }
    }

    /**
     * Gets the document name based on the baseDir.
     * @return The document name based on the baseDir.
     */
    protected DocumentName baseName() {
        return DocumentName.builder(baseDir).build();
    }

    protected AbstractOptionsProvider(final File baseDir) {
        this.baseDir = baseDir;
    }

    protected void validate(final Collection<String> unsupportedArgs) {
        unsupportedArgs.forEach(testMap::remove);
        verifyAllMethodsDefinedAndNeeded(unsupportedArgs);
    }

    private void verifyAllMethodsDefinedAndNeeded(final Collection<String> unsupportedArgs) {
        // verify all options have functions.
        final List<String> argNames = new ArrayList<>();
        Arg.getOptions().getOptions().forEach(o -> {
            if (o.getLongOpt() != null) {
                argNames.add(o.getLongOpt());
            }
        });
        argNames.removeAll(unsupportedArgs);
        argNames.removeAll(testMap.keySet());
        if (!argNames.isEmpty()) {
            fail("Missing methods for: " + String.join(", ", argNames));
        }

        // verify all functions have options.
        argNames.clear();
        argNames.addAll(testMap.keySet());
        argNames.remove("help");
        Arg.getOptions().getOptions().forEach(o -> {
            if (o.getLongOpt() != null) {
                argNames.remove(o.getLongOpt());
            }
        });
        if (!argNames.isEmpty()) {
            fail("Extra methods defined: " + String.join(", ", argNames));
        }
        unsupportedArgs.forEach(testMap::remove);
    }

    @SafeVarargs
    protected final ReportConfiguration generateConfig(final Pair<Option, String[]>... args) throws IOException {
        List<Pair<Option, String[]>> options = Arrays.asList(args);
        return generateConfig(options);
    }

    /**
     * Create the report configuration from the argument pairs.
     * There must be at least one arg. It may be {@code ImmutablePair.nullPair()}.
     *
     * @param args Pairs comprising the argument option and the values for the option.
     * @return The generated ReportConfiguration.
     * @throws IOException on error.
     */
    protected abstract ReportConfiguration generateConfig(final List<Pair<Option, String[]>> args) throws IOException;

    protected File writeFile(final String name, final Iterable<String> lines) {
        return writeFile(baseDir, name, lines);
    }

    final protected File writeFile(File baseDir, final String name, final Iterable<String> lines) {
        File file = new File(baseDir, name);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            lines.forEach(writer::println);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return file;
    }


    final protected DocumentName mkDocName(final String name) {
        return DocumentName.builder(new File(baseDir, name)).build();
    }

    /** Help test */
    protected abstract void helpTest();

    /** Display the option and value under test */
    final protected String displayArgAndName(final Option option, final String fname) {
        return String.format("%s %s", option.getLongOpt(), fname);
    }

    final protected String dump(final DocumentNameMatcher nameMatcher, final DocumentName name) {
        StringBuilder sb = new StringBuilder();
        nameMatcher.decompose(name).forEach(s -> sb.append(s).append("\n"));
        return sb.toString();
    }

    final protected String dump(final Option option, final String fname, final DocumentNameMatcher matcher, final DocumentName name) {
        return String.format("Argument and Name: %s%nMatcher decomposition:%n%s", displayArgAndName(option, fname),
                DocumentNameMatcherTest.processDecompose(matcher, name));
    }

    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
        List<Arguments> lst = new ArrayList<>();
        List<String> missingTests = new ArrayList<>();

        for (String key : OptionsList.getKeys()) {
            OptionCollectionTest.OptionTest test = testMap.get(key);
            if (test == null) {
                missingTests.add(key);
            } else {
                lst.add(Arguments.of(key, test));
            }
        }
        if (!missingTests.isEmpty()) {
            System.out.println("The following tests are excluded: '" + String.join("', '", missingTests) + "'");
        }
        return lst.stream();
    }
}
