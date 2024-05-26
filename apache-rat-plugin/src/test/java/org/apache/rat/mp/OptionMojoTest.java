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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.rat.IOptionsProvider;
import org.apache.rat.OptionCollection;
import org.apache.rat.OptionCollectionTest;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReportConfigurationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    public void x() throws Exception {
        new OptionsProvider().dryRunTest();
    }

    public static class OptionsProvider  implements ArgumentsProvider, IOptionsProvider {

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
            testMap.put(OptionCollection.LIST_LICENSES, this::listLicensesTest);
            testMap.put(OptionCollection.LIST_FAMILIES, this::listFamiliesTest);
            testMap.put(OptionCollection.NO_DEFAULTS, this::noDefaultsTest);
            testMap.put(OptionCollection.OUT, this::outTest);
            testMap.put(OptionCollection.SCAN_HIDDEN_DIRECTORIES, this::scanHiddenDirectoriesTest);
            testMap.put(OptionCollection.STYLESHEET_CLI, this::styleSheetTest);
            testMap.put(OptionCollection.XML, this::xmlTest);
        }


        private ReportConfiguration generateConfig(List<Pair<Option,Object>> args) {
            MavenOption mavenOption = new MavenOption(args.get(0).getKey());
            StringBuilder sb = new StringBuilder();
            args.stream().map(p -> new MavenOption(p.getKey()).xmlNode(p.getValue().toString())).forEach(sb::append);
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
                return mojo.getConfiguration();
            } catch (Exception e) {
                throw new RuntimeException("Unable to generate config", e);
            }
        }

        @Override
        public void addLicenseTest() {
            fail("not implemented");
        }

        @Override
        public void archiveTest() {
            fail("not implemented");
        }

        @Override
        public void standardTest() {
            fail("not implemented");
        }

        @Override
        public void copyrightTest() {
            fail("not implemented");
        }

        @Override
        public void dryRunTest() {
            List<Pair<Option,Object>> args = new ArrayList<>();
            ImmutablePair<Option,Object> pair= ImmutablePair.of(OptionCollection.DRY_RUN, false);
            args.add(pair);
            ReportConfiguration config = generateConfig(args);
            assertFalse(config.isDryRun());

            pair= ImmutablePair.of(OptionCollection.DRY_RUN, true);
            args.add(pair);
            config = generateConfig(args);
            assertTrue(config.isDryRun());
        }

        @Override
        public void excludeCliTest() {
            fail("not implemented");
        }

        @Override
        public void excludeCliFileTest() {
            fail("not implemented");
        }

        @Override
        public void forceTest() {
            fail("not implemented");
        }

        @Override
        public void licensesTest() {
            fail("not implemented");
        }

        @Override
        public void listLicensesTest() {
            fail("not implemented");
        }

        @Override
        public void listFamiliesTest() {
            fail("not implemented");
        }

        @Override
        public void noDefaultsTest() {
            fail("not implemented");
        }

        @Override
        public void outTest() {
            fail("not implemented");
        }

        @Override
        public void scanHiddenDirectoriesTest() {
            fail("not implemented");
        }

        @Override
        public void styleSheetTest() {
            fail("not implemented");
        }

        @Override
        public void xmlTest() {
            fail("not implemented");
        }

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.empty();
        }
    }

    public static class SimpleMojoTestcase extends BetterAbstractMojoTestCase {

        public RatCheckMojo getMojo(File pomFile) throws Exception {
            setUp();
            ProjectBuildingRequest buildingRequest = newMavenSession().getProjectBuildingRequest();
            ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);
            MavenProject project = projectBuilder.build(pomFile, buildingRequest).getProject();

            return (RatCheckMojo) lookupConfiguredMojo(project, "check");

        }
    }
}
