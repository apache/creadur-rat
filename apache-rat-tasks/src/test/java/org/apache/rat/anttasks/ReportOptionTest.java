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

import org.apache.commons.cli.Option;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionTools;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.tools.CasedString;
import org.apache.rat.tools.Naming;
import org.apache.rat.utils.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class ReportOptionTest  {

    static File baseDir = new File("target/optionTest");
    static ReportConfiguration reportConfiguration;


    @BeforeAll
    public static void makeDirs() {
        baseDir.mkdirs();
    }


    public void testOptions() {
        exec("addLicense", "true", rc  -> assertTrue(rc.isAddingLicenses(), "addLicense"));
    }

    @ParameterizedTest
    @MethodSource("optionsProvider")
    public void exec(String name, String value, Consumer<ReportConfiguration> test) {
        BuildTask task = new BuildTask(name, value);
        task.setUp();
        task.buildRule.executeTarget(name);
        test.accept(reportConfiguration);
    }

    static Map<Option,Object[]> optionValues = new HashMap<>();
    static Map<Option, Function<ReportConfiguration, Object>> optionTest = new HashMap<>();

    static Stream<Arguments> optionsProvider() {

        List<Arguments> lst = new ArrayList<>();
        Predicate<ReportConfiguration> test;

        Predicate<Option> antFilter = Naming.optionFilter(Naming.antFilterList);
        for (Option option : OptionTools.buildOptions().getOptions()) {
            if (option.getLongOpt() != null && antFilter.test(option)) {
                CasedString opt = new CasedString(CasedString.StringCase.Kebab, option.getLongOpt());
                String name = WordUtils.uncapitalize(opt.toCase(CasedString.StringCase.Camel));
                for (Object arg : optionValues.get(option)) {
                    if (arg == null) {
                        fail("Option "+option+" is not defined in optionValues");
                    }
                    lst.add(Arguments.of(name, arg.toString(), getConsumer(option, arg)));
                }
            }
        }
        return lst.stream();
    }

    private static Consumer<ReportConfiguration> getConsumer(Option option, Object value ) {
        Function<ReportConfiguration, Object> func = optionTest.get(option);
        if (func == null) {
            fail("optionTest not specified for "+option);
        }
        return rc -> assertEquals(value, func.apply(rc));
    }

    static {
        optionValues.put(OptionTools.ADD_LICENSE, new Object[] {true, false});
        optionTest.put(OptionTools.ADD_LICENSE, ReportConfiguration::isAddingLicenses);
        optionValues.put(OptionTools.ARCHIVE, ReportConfiguration.Processing.values());
        optionTest.put(OptionTools.ARCHIVE, ReportConfiguration::getArchiveProcessing);
        optionValues.put(OptionTools.STANDARD, ReportConfiguration.Processing.values());
        optionTest.put(OptionTools.STANDARD, ReportConfiguration::getStandardProcessing);
        optionValues.put(OptionTools.COPYRIGHT, new Object[] {"MyCopyright"});
        optionTest.put(OptionTools.COPYRIGHT, ReportConfiguration::getCopyrightMessage);
        optionValues.put(OptionTools.DIR, new Object[] {true});
        optionTest.put(OptionTools.DIR, rc -> true);
        optionValues.put(OptionTools.DRY_RUN, new Object[] {true, false});
        optionTest.put(OptionTools.DRY_RUN, ReportConfiguration::isDryRun);
        optionValues.put(OptionTools.EXCLUDE_CLI, new Object[] {"*.junk"});
        optionTest.put(OptionTools.EXCLUDE_CLI, rc -> false);
        optionValues.put(OptionTools.EXCLUDE_FILE_CLI, new Object[] {baseDir});
        optionTest.put(OptionTools.EXCLUDE_FILE_CLI, rc -> false);
        optionValues.put(OptionTools.FORCE, new Object[] {true, false});
        optionTest.put(OptionTools.FORCE, rc -> rc.isAddingLicenses() && rc.isAddingLicensesForced());
        optionValues.put(OptionTools.LICENSES, new Object[] {baseDir.getPath()});
        optionTest.put(OptionTools.LICENSES, rc -> false);
        optionValues.put(OptionTools.LIST_LICENSES, LicenseSetFactory.LicenseFilter.values());
        optionTest.put(OptionTools.LIST_LICENSES, ReportConfiguration::listLicenses);
        optionValues.put(OptionTools.LIST_FAMILIES, LicenseSetFactory.LicenseFilter.values());
        optionTest.put(OptionTools.LIST_FAMILIES, ReportConfiguration::listFamilies);
        optionValues.put(OptionTools.LOG_LEVEL, Log.Level.values());
        optionTest.put(OptionTools.LOG_LEVEL, rc -> false);
        optionValues.put(OptionTools.NO_DEFAULTS, new Object[] {true, false});
        optionTest.put(OptionTools.NO_DEFAULTS, rc -> false);
        optionValues.put(OptionTools.OUT, new Object[] {baseDir.getPath()});
        optionTest.put(OptionTools.OUT, rc -> false);
        optionValues.put(OptionTools.SCAN_HIDDEN_DIRECTORIES, new Object[] {true, false});
        optionTest.put(OptionTools.SCAN_HIDDEN_DIRECTORIES, ReportConfiguration::getDirectoriesToIgnore);
        optionValues.put(OptionTools.STYLESHEET_CLI, new Object[] {baseDir});
        optionTest.put(OptionTools.STYLESHEET_CLI, rc -> false);
        optionValues.put(OptionTools.XML, new Object[] {true, false});
        optionTest.put(OptionTools.XML, rc -> !rc.isStyleReport());
    }


    private class BuildTask extends AbstractRatAntTaskTest {

        final File antFile;
        final String name;
        final String value;

        BuildTask(String name, String value) {
            this.name = name;
            this.value =value;
            antFile = new File(baseDir, name + ".xml");
        }

        public void setUp() {
            try (FileWriter writer = new FileWriter(antFile)) {
                writer.append(String.format(ANT_FILE, name, value, antFile.getAbsolutePath(), OptionTest.class.getName()));
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            super.setUp();
        }
        private String getAntFileName() {
            return getAntFile().getPath().replace('\\', '/');
        }

        protected File getAntFile() {
            return antFile;
        }

        private String logLine(String id) {
            return logLine(true, getAntFileName(), id);
        }

        private String logLine(String antFile, String id) {
            return logLine(true, antFile, id);
        }

        private String logLine(boolean approved, String antFile, String id) {
            return String.format("%sS \\Q%s\\E\\s+\\Q%s\\E ", approved ? " " : "!", antFile, id);
        }

        public void execute() {
            buildRule.executeTarget(name);
        }
    }

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
            "\t\tclassname=\"%4$s\"\n" +
            "\t\tclasspath=\"${test.classpath}\" />\n" +
            "\n" +
            "\t<target name=\"%1$s\">\n" +
            "\t\t<optionTest %1$s='%2$s'>\n" +
            "\t\t\t<file file=\"%3$s\" />\n" +
            "\t\t</optionTest>\n" +
            "\t</target>\n" +
            "\n" +
            "</project>";

    public static class OptionTest extends Report {

        public OptionTest() {}

        @Override
        public void execute() {
            reportConfiguration = validate(getConfiguration());
        }
    }
}
