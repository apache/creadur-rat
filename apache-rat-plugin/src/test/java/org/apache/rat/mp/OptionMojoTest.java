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
package org.apache.rat.mp;

import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.rat.test.AbstractOptionsProvider;
import org.apache.rat.OptionCollection;
import org.apache.rat.OptionCollectionTest;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReportTest;
import org.apache.rat.commandline.OutputArgs;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.plugin.BaseRatMojo;
import org.apache.rat.tools.AntGenerator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class OptionMojoTest   {

    static Path testPath = FileSystems.getDefault().getPath("target", "optionTest");
    static String POM_FMT;


    @BeforeAll
    public static void makeDirs() throws IOException {
        testPath.toFile().mkdirs();
        POM_FMT = IOUtils.resourceToString("/optionTest/pom.tpl", StandardCharsets.UTF_8);
    }

    @ParameterizedTest
    @ArgumentsSource(OptionsProvider.class)
    public void testOptionsUpdateConfig(String name, OptionCollectionTest.OptionTest test) throws Exception {
        test.test();
    }

    public static class OptionsProvider  implements ArgumentsProvider, AbstractOptionsProvider {

        final AtomicBoolean helpCalled = new AtomicBoolean(false);

        final Map<Option, OptionCollectionTest.OptionTest> testMap = new HashMap<>();

        public OptionsProvider() {
            testMap.put(OptionCollection.ADD_LICENSE, this::addLicenseTest);
            testMap.put(OptionCollection.ARCHIVE, this::archiveTest);
            testMap.put(OptionCollection.STANDARD, this::standardTest);
            testMap.put(OptionCollection.COPYRIGHT, this::copyrightTest);
            testMap.put(OptionCollection.DRY_RUN, this::dryRunTest);
            testMap.put(OptionCollection.EXCLUDE_CLI, this::excludeCliTest);
            testMap.put(OptionCollection.EXCLUDE_FILE_CLI, this::excludeCliFileTest);
            testMap.put(OptionCollection.FORCE, this::forceTest);
            testMap.put(OptionCollection.LICENSES, this::licensesTest);
            testMap.put(OutputArgs.LIST_LICENSES, this::listLicensesTest);
            testMap.put(OutputArgs.OUTPUT_FAMILIES, this::listFamiliesTest);
            testMap.put(OptionCollection.NO_DEFAULTS, this::noDefaultsTest);
            testMap.put(OutputArgs.OUT, this::outTest);
            testMap.put(OptionCollection.SCAN_HIDDEN_DIRECTORIES, this::scanHiddenDirectoriesTest);
            testMap.put(OutputArgs.STYLESHEET_CLI, this::styleSheetTest);
            testMap.put(OutputArgs.XML, this::xmlTest);
        }

       private RatCheckMojo generateMojo(Pair<Option,Object>... args) {
            MavenOption mavenOption = new MavenOption(args[0].getKey());
            StringBuilder sb = new StringBuilder();
            Arrays.stream(args).map(p -> new MavenOption(p.getKey()).xmlNode(p.getValue().toString())).forEach(sb::append);
            Path pomPath = testPath.resolve(mavenOption.name).resolve("pom.xml");
            File pomFile = pomPath.toFile();
            pomFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(pomFile)) {
                writer.append(format(POM_FMT, mavenOption.name, sb.toString()));
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                final RatCheckMojo mojo = new SimpleMojoTestcase().getMojo(pomFile);
                Assertions.assertNotNull(mojo);
                return mojo;
            } catch (Exception e) {
                throw new RuntimeException(format("Unable to generate mojo for %s (%s)", mavenOption.name, mavenOption.keyValue()), e);
            }
        }

        private ReportConfiguration generateConfig(Pair<Option,Object>... args) {
            try {
                return generateMojo(args).getConfiguration();
            } catch (Exception e) {
                MavenOption mavenOption = new MavenOption(args[0].getKey());
                throw new RuntimeException(format("Unable to generate mojo for %s (%s)", mavenOption.name, mavenOption.keyValue()), e);
            }
        }

        @Override
        public void addLicenseTest() {
            ReportConfiguration config = generateConfig(ImmutablePair.of(OptionCollection.ADD_LICENSE, true));
            assertTrue(config.isAddingLicenses());
            config = generateConfig(ImmutablePair.of(OptionCollection.ADD_LICENSE, false));
            assertFalse(config.isAddingLicenses());
        }

        @Override
        public void archiveTest() {
            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                ReportConfiguration config = generateConfig(ImmutablePair.of(OptionCollection.ARCHIVE, proc.name()));
                assertEquals(proc, config.getArchiveProcessing());
            }
        }

        @Override
        public void standardTest() {
            for (ReportConfiguration.Processing proc : ReportConfiguration.Processing.values()) {
                ReportConfiguration config = generateConfig(ImmutablePair.of(OptionCollection.STANDARD, proc.name()));
                assertEquals(proc, config.getStandardProcessing());
            }
        }

        @Override
        public void copyrightTest() {
            ReportConfiguration config = generateConfig(ImmutablePair.of(OptionCollection.COPYRIGHT, "MyCopyright"));
            assertNull(config.getCopyrightMessage(), "Copyright without ADD_LICENCE should not work");
            config = generateConfig(ImmutablePair.of(OptionCollection.COPYRIGHT, "MyCopyright"),
                    ImmutablePair.of(OptionCollection.ADD_LICENSE, true));
            assertEquals("MyCopyright", config.getCopyrightMessage());
        }

        @Override
        public void dryRunTest() {
            ReportConfiguration config = generateConfig(ImmutablePair.of(OptionCollection.DRY_RUN, false));
            assertFalse(config.isDryRun());

            config = generateConfig(ImmutablePair.of(OptionCollection.DRY_RUN, true));
            assertTrue(config.isDryRun());
        }

        @Override
        public void excludeCliTest() {
            // AbstractRatMojo handles exclusion by removing them from the initial producer so check that the
            // configuration was properly set for the generation.
            RatCheckMojo mojo = generateMojo(ImmutablePair.of(OptionCollection.EXCLUDE_CLI, "*.foo"),
                    ImmutablePair.of(OptionCollection.EXCLUDE_CLI, "[A-Z]\\.bar"),
                    ImmutablePair.of(OptionCollection.EXCLUDE_CLI, "justbaz"));
            List<String> lst = mojo.getExcludes();
            assertThat(lst).contains( "*.foo", "[A-Z]\\.bar", "justbaz");
            assertEquals(3, lst.size());
        }

        @Override
        public void excludeCliFileTest() {
            File outputFile = new File(testPath.toFile(), "exclude.txt");
            try (FileWriter fw = new FileWriter(outputFile)) {
                fw.write("*.foo");
                fw.write(System.lineSeparator());
                fw.write("[A-Z]\\.bar");
                fw.write(System.lineSeparator());
                fw.write("justbaz");
                fw.write(System.lineSeparator());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            RatCheckMojo mojo = generateMojo(ImmutablePair.of(OptionCollection.EXCLUDE_FILE_CLI, outputFile.getPath()));
            List<String> lst = mojo.getExcludesFile();
            assertThat(lst).contains(outputFile.getPath());
            assertEquals(1, lst.size());
        }

        @Override
        public void forceTest() {
            ReportConfiguration config = generateConfig(ImmutablePair.of(OptionCollection.FORCE, true));
            assertFalse(config.isAddingLicensesForced());
            config = generateConfig(ImmutablePair.of(OptionCollection.FORCE, true),
                    ImmutablePair.of(OptionCollection.ADD_LICENSE, true));
            assertTrue(config.isAddingLicensesForced());
        }

        @Override
        public void licensesTest() {
            ReportConfiguration config = generateConfig(ImmutablePair.of(OptionCollection.LICENSES, "src/test/resources/OptionTools/One.xml"),
                    ImmutablePair.of(OptionCollection.LICENSES, "src/test/resources/OptionTools/Two.xml"));

            SortedSet<ILicense> set = config.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
            assertTrue(set.size() > 2);
            assertTrue(LicenseSetFactory.search("ONE", "ONE", set).isPresent());
            assertTrue(LicenseSetFactory.search("TWO", "TWO", set).isPresent());

            config = generateConfig(ImmutablePair.of(OptionCollection.LICENSES, "src/test/resources/OptionTools/One.xml"),
                    ImmutablePair.of(OptionCollection.LICENSES, "src/test/resources/OptionTools/Two.xml"),
                    ImmutablePair.of(OptionCollection.NO_DEFAULTS, true));

            set = config.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
            assertEquals(2, set.size());
            assertTrue(LicenseSetFactory.search("ONE", "ONE", set).isPresent());
            assertTrue(LicenseSetFactory.search("TWO", "TWO", set).isPresent());
        }

        @Override
        public void listLicensesTest() {
            for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
                ReportConfiguration config = generateConfig(ImmutablePair.of(OutputArgs.LIST_LICENSES, filter.name()));
                assertEquals(filter, config.listLicenses());
            }
        }

        @Override
        public void listFamiliesTest() {
            for (LicenseSetFactory.LicenseFilter filter : LicenseSetFactory.LicenseFilter.values()) {
                ReportConfiguration config = generateConfig(ImmutablePair.of(OutputArgs.OUTPUT_FAMILIES, filter.name()));
                assertEquals(filter, config.listFamilies());
            }
        }

        @Override
        public void noDefaultsTest() {
            ReportConfiguration config = generateConfig(ImmutablePair.of(OptionCollection.NO_DEFAULTS, true));
            assertThat(config.getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isEmpty();
            config = generateConfig(ImmutablePair.of(OptionCollection.NO_DEFAULTS, false));
            assertThat(config.getLicenses(LicenseSetFactory.LicenseFilter.ALL)).isNotEmpty();
        }

        @Override
        public void outTest() {
            File outFile = new File( testPath.toFile(), "outexample");
            ReportConfiguration config = generateConfig(ImmutablePair.of(OutputArgs.OUT, outFile.getAbsolutePath()));
            try (OutputStream os = config.getOutput().get()) {
                os.write("Hello world".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(outFile.toPath())))) {
                assertEquals("Hello world",reader.readLine());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void scanHiddenDirectoriesTest() {
            ReportConfiguration config = generateConfig(ImmutablePair.of(OptionCollection.SCAN_HIDDEN_DIRECTORIES, true));
            assertThat(config.getDirectoriesToIgnore()).isExactlyInstanceOf(FalseFileFilter.class);
        }

        @Override
        public void styleSheetTest() {
            URL url = ReportTest.class.getResource("MatcherContainerResource.txt");
            if (url == null) {
                fail("Could not locate 'MatcherContainerResource.txt'");
            }
            for (String sheet : new String[]{"target/optionTools/stylesheet.xlt", "plain-rat", "missing-headers", "unapproved-licenses", url.getFile()}) {
                ReportConfiguration config = generateConfig(ImmutablePair.of(OutputArgs.STYLESHEET_CLI, sheet));
                assertTrue(config.isStyleReport());
            }
        }

        @Override
        public void xmlTest() {
            ReportConfiguration config = generateConfig(ImmutablePair.of(OutputArgs.XML, true ));
            assertFalse(config.isStyleReport());
        }

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            List<Arguments> lst = new ArrayList<>();

            List<Option> opt =  OptionCollection.buildOptions().getOptions().stream().filter(AntGenerator.getFilter()).collect(Collectors.toList());
            for (Option option : opt) {
                if (option.getLongOpt() != null) {
                    String name = BaseRatMojo.createName(option.getLongOpt());
                    OptionCollectionTest.OptionTest test = testMap.get(option);
                    if (test == null) {
                        fail("Option "+name+" is not defined in testMap");
                    }
                    lst.add(Arguments.of(name, test));
                }
            }
            return lst.stream();
        }
    }

    public static class SimpleMojoTestcase extends BetterAbstractMojoTestCase {

        public RatCheckMojo getMojo(File pomFile) throws Exception {
            setUp();
            ProjectBuildingRequest buildingRequest = newMavenSession().getProjectBuildingRequest();
            ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);
            MavenProject project = projectBuilder.build(pomFile, buildingRequest).getProject();
            try {
                return (RatCheckMojo) lookupConfiguredMojo(project, "check");
            } catch (ComponentConfigurationException e) {
                for (Method m : RatCheckMojo.class.getMethods()) {
                    System.out.println( m );
                }
                throw e;
            }
        }
    }
}
