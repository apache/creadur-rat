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
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.rat.commandline.Arg;
import org.apache.rat.mp.util.ignore.IgnoringDirectoryScanner;
import org.apache.rat.test.AbstractOptionsProvider;
import org.apache.rat.OptionCollectionTest;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.plugin.BaseRatMojo;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
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

    @ParameterizedTest
    @ArgumentsSource(OptionsProvider.class)
    public void testOptionsUpdateConfig(String name, OptionCollectionTest.OptionTest test) throws Exception {
        test.test();
    }

    public static class OptionsProvider extends AbstractOptionsProvider implements ArgumentsProvider  {

        private RatCheckMojo mojo = null;
        public OptionsProvider() {
            super(BaseRatMojo.unsupportedArgs());
        }

       private RatCheckMojo generateMojo(Pair<Option,String[]>... args) throws IOException {
           MavenOption keyOption = new MavenOption(args[0].getKey() == null ? Option.builder().longOpt("no-option").build() : args[0].getKey());
           List<String> mavenOptions = new ArrayList<>();
           for (Pair<Option, String[]> pair : args) {
               if (pair.getKey() != null) {
                   String[] values = pair.getValue();
                   if (values != null) {
                       for (String value : values) {
                           mavenOptions.add(new MavenOption(pair.getKey()).xmlNode(value));
                       }
                   } else {
                       MavenOption mavenOption = new MavenOption(pair.getKey());
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
               writer.append(format(POM_FMT, keyOption.name, sb.toString()));
               writer.flush();
           }
           try {
               final RatCheckMojo mojo = new SimpleMojoTestcase().getMojo(pomFile);
               Assertions.assertNotNull(mojo);
               return mojo;
           } catch (IOException e) {
               throw e;
           } catch (Exception e) {
               throw new IOException(format("Unable to generate mojo for %s (%s)", keyOption.name, keyOption), e);
           }
       }

        @Override
        protected ReportConfiguration generateConfig(Pair<Option, String[]>... args) throws IOException {
            try {
                this.mojo = generateMojo(args);
                return mojo.getConfiguration();
            } catch (MojoExecutionException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        @Override
        protected void helpTest() {
            fail("Should not call help");
        }


        private void execExcludeTest(Option option, String[] args) {

            try {
                ReportConfiguration config = generateConfig(ImmutablePair.of(option, args));
                File workingDir = mojo.getProject().getBasedir();
                for (String fn : new String[] {"some.foo", "B.bar", "justbaz", "notbaz"}) {
                    try (FileOutputStream fos = new FileOutputStream(new File(workingDir, fn))) {
                        fos.write("Hello world".getBytes());
                    }
                }
                IOFileFilter filter = config.getFilesToIgnore();
                assertThat(filter).isExactlyInstanceOf(FalseFileFilter.class);
                TestingDirectoryScanner ds = new TestingDirectoryScanner();
                ds.setBasedir(workingDir);
                mojo.setExcludes(ds);
                mojo.setIncludes(ds);
                ds.scan();
                assertThat(ds.getExcludedList()).contains("some.foo");
                assertThat(ds.getExcludedList()).contains("B.bar");
                assertThat(ds.getExcludedList()).contains("justbaz");
                assertThat(ds.getIncludedList()).contains("notbaz");
            } catch (IOException | MojoExecutionException e) {
                fail(e.getMessage());
            }
        }

        @Override
        protected void excludeTest() {
            String[] args = { "*.foo", "*.bar", "justbaz"};
            execExcludeTest(Arg.EXCLUDE.find("exclude"), args);
        }
        @Override
        protected void inputExcludeTest() {
            String[] args = { "*.foo", "*.bar", "justbaz"};
            execExcludeTest(Arg.EXCLUDE.find("input-exclude"), args);
        }

        private void excludeFileTest(Option option) {
            File outputFile = new File(baseDir, "exclude.txt");
            try (FileWriter fw = new FileWriter(outputFile)) {
                fw.write("*.foo");
                fw.write(System.lineSeparator());
                fw.write("*.bar");
                fw.write(System.lineSeparator());
                fw.write("justbaz");
                fw.write(System.lineSeparator());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            execExcludeTest(option, new String[] {outputFile.getPath()});
        }

        protected void excludeFileTest() {
            excludeFileTest(Arg.EXCLUDE_FILE.find("exclude-file"));
        }


        protected void inputExcludeFileTest() {
            excludeFileTest(Arg.EXCLUDE_FILE.find("input-exclude-file"));
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

    private static class TestingDirectoryScanner extends IgnoringDirectoryScanner {

        TestingDirectoryScanner() {

        }

        List<String> getExcludedList() {
            return this.filesExcluded;
        }

        List<String> getIncludedList() {
            return this.filesIncluded;
        }
    }
}
