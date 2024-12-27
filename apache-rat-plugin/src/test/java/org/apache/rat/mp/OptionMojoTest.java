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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.rat.test.AbstractOptionsProvider;
import org.apache.rat.OptionCollectionTest;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.plugin.BaseRatMojo;
import org.apache.rat.utils.DefaultLog;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.fail;


public class OptionMojoTest {

    @TempDir
    static Path testPath;

    static String POM_FMT;

    @BeforeAll
    public static void makeDirs() throws IOException {
        POM_FMT = IOUtils.resourceToString("/optionTest/pom.tpl", StandardCharsets.UTF_8);
    }

    @AfterAll
    static void preserveData() {
         AbstractOptionsProvider.preserveData(testPath.toFile(), "optionTest");
    }

    /**
     * This method is a known workaround for
     * {@link <a href="https://github.com/junit-team/junit5/issues/2811">junit 5 issue #2811</a> }.
     */
    @AfterEach
    @EnabledOnOs(OS.WINDOWS)
    void cleanUp() {
        System.gc();
    }

    @ParameterizedTest
    @ArgumentsSource(MojoOptionsProvider.class)
    void testOptionsUpdateConfig(String name, OptionCollectionTest.OptionTest test) {
        DefaultLog.getInstance().info("Running " + name);
        test.test();
    }

    static class MojoOptionsProvider extends AbstractOptionsProvider implements ArgumentsProvider  {

        private RatCheckMojo mojo = null;

        public MojoOptionsProvider() {
            super(BaseRatMojo.unsupportedArgs(), testPath.toFile());
        }

       private RatCheckMojo generateMojo(List<Pair<Option, String[]>> args) throws IOException {
           MavenOption keyOption = new MavenOption(args.get(0).getKey() == null ?
                   Option.builder().longOpt("no-option").build() :
                   args.get(0).getKey());
           List<String> mavenOptions = new ArrayList<>();
           for (Pair<Option, String[]> pair : args) {
               if (pair.getKey() != null) {
                   String[] values = pair.getValue();
                   if (values != null) {
                       for (String value : values) {
                           mavenOptions.add(new MavenOption(pair.getKey()).xmlNode(value));
                       }
                   } else {
                       mavenOptions.add(new MavenOption(pair.getKey()).xmlNode("true"));
                   }
               }
           }
           // StringBuilder develops the core pom commands.
           StringBuilder sb = new StringBuilder();
           mavenOptions.forEach(sb::append);
           Path pomPath = testPath.resolve(keyOption.name).resolve("pom.xml");
           File pomFile = pomPath.toFile();
           pomFile.getParentFile().mkdirs();
           try (FileWriter writer = new FileWriter(pomFile)) {
               writer.append(format(POM_FMT, keyOption.name, sb));
               writer.flush();
           }
           try {
               final RatCheckMojo mojo = new SimpleMojoTestcase(){}.getMojo(pomFile);
               Assertions.assertNotNull(mojo);
               return mojo;
           } catch (IOException e) {
               throw e;
           } catch (Exception e) {
               throw new IOException(format("Unable to generate mojo for %s (%s)", keyOption.name, keyOption), e);
           }
       }

        @Override
        protected final ReportConfiguration generateConfig(List<Pair<Option, String[]>> args) throws IOException {
            try {
                this.mojo = generateMojo(args);
                AbstractOptionsProvider.setup(this.mojo.getProject().getBasedir());
                return mojo.getConfiguration();
            } catch (MojoExecutionException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        @Override
        protected void helpTest() {
            fail("Should not call help");
        }
    }

    public abstract static class SimpleMojoTestcase extends BetterAbstractMojoTestCase {
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
