/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.anttasks;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.test.AbstractOptionsProvider;
import org.apache.rat.OptionCollectionTest;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static java.lang.String.format;
import static org.apache.rat.commandline.Arg.HELP_LICENSES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests to ensure the option setting works correctly.
 */
public class ReportOptionTest  {
    @TempDir
    static Path testPath;

    static ReportConfiguration reportConfiguration;

    @AfterAll
    static void preserveData() {
        AbstractOptionsProvider.preserveData(testPath.toFile(), "optionTest");
    }

    @ParameterizedTest
    @ArgumentsSource(AntOptionsProvider.class)
    public void testOptionsUpdateConfig(String name, OptionCollectionTest.OptionTest test) {
        DefaultLog.getInstance().info("Running " + name);
        test.test();
    }

    public static class OptionTest extends Report {

        public OptionTest() {}

        @Override
        public void execute() {
            reportConfiguration = getConfiguration();
        }
    }

    final static class AntOptionsProvider extends AbstractOptionsProvider implements ArgumentsProvider {

        public AntOptionsProvider() {
            super(BaseAntTask.unsupportedArgs(), testPath.toFile());
        }

        protected ReportConfiguration generateConfig(final List<Pair<Option, String[]>> args) {
            BuildTask task = args.get(0).getKey() == null ? new BuildTask() : new BuildTask(args.get(0).getKey());
            task.setUp(args);
            Log log = DefaultLog.getInstance();
            Log.Level oldLevel = log.getLevel();
            log.setLevel(Log.Level.DEBUG);
            try {
                task.buildRule.executeTarget(task.name);
            } finally {
                log.setLevel(oldLevel);
                DefaultLog.setInstance(log);
            }
            return reportConfiguration;
        }

        @Override
        protected void helpTest() {
            fail("Should not be called");
        }

        @Override
        public void helpLicenses() {
            TestingLog testLog = new TestingLog();
            Log oldLog = DefaultLog.setInstance(testLog);
            try {
                ReportConfiguration config = generateConfig(ImmutablePair.of(HELP_LICENSES.option(), null));
                assertThat(config).isNotNull();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                DefaultLog.setInstance(oldLog);
            }
            testLog.assertContains("====== Licenses ======");
            testLog.assertContains("====== Defined Matchers ======");
            testLog.assertContains("====== Defined Families ======");
        }

        private class BuildTask extends AbstractRatAntTaskTest {
            final File antFile;
            final String name;

            BuildTask(Option option) {
                this(new AntOption(option).getName());
            }

            BuildTask() {
                this("anonymous");
            }
            BuildTask(String name) {
                this.name = name;
                antFile = new File(baseDir, name + ".xml");
            }

            public final void setUp(List<Pair<Option, String[]>> args) {
                List<String> childElements = new ArrayList<>();
                Map<String, String> attributes = new HashMap<>();
                if (args.get(0).getKey() != null) {
                    for (Pair<Option, String[]> pair : args) {
                        AntOption argOption = new AntOption(pair.getKey());
                        if (argOption.isAttribute()) {
                            String value = pair.getValue() == null ? "true" : pair.getValue()[0];
                            attributes.put(argOption.getName(), value);
                        } else {
                            AntOption.ExampleGenerator exampleGenerator = argOption.new ExampleGenerator();
                            for (String value : pair.getValue()) {
                                childElements.add(exampleGenerator.getChildElements(value, null));
                            }
                        }
                    }
                }
                String attrStr = attributes.entrySet().stream().map(e -> String.format("%s='%s'", e.getKey(), e.getValue()))
                        .collect(Collectors.joining(" "));
                String elements = String.join("\n", childElements);
                try (FileWriter writer = new FileWriter(antFile)) {
                    writer.append(format(ANT_FILE, this.name, attrStr, elements, antFile.getAbsolutePath(), OptionTest.class.getName()));
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                super.setUp();
            }

            protected File getAntFile() {
                return antFile;
            }
        }
    }

    /* $1 = target name
       $2 = attributes
       $3 = file name to read
       $4 = classname
     */
    final static String ANT_FILE = "<?xml version='1.0'?>\n" +
            "\n" +
            "<project\n" +
            "\txmlns:au=\"antlib:org.apache.ant.antunit\"\n" +
            "\txmlns:rat=\"antlib:org.apache.rat.anttasks\">\n" +
            "\n" +
            "\t<taskdef uri=\"antlib:org.apache.ant.antunit\"\n" +
            "\t\tresource=\"org/apache/rat/anttasks/antlib.xml\"\n" +
            "\t\tclasspath=\"${test.classpath}\" />\n" +
            "\n" +
            "\t<taskdef uri=\"antlib:org.apache.rat.anttasks\"\n" +
            "\t\tresource=\"org/apache/rat/anttasks/antlib.xml\"\n" +
            "\t\tclasspath=\"${test.classpath}\" />\n" +
            "\n" +
            "\t<taskdef \n" +
            "\t\tname=\"optionTest\"\n" +
            "\t\tclassname=\"%5$s\"\n" +
            "\t\tclasspath=\"${test.classpath}\" />\n" +
            "\n" +
            "\t<target name=\"%1$s\">\n" +
            "\t\t<optionTest %2$s>\n" +
            "%3$s" +
            "\t\t\t<file file=\"%4$s\" />\n" +
            "\t\t</optionTest>\n" +
            "\t</target>\n" +
            "\n" +
            "</project>";
}
