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
package org.apache.rat.testhelpers.data;


import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.rat.OptionCollectionParser;
import org.apache.rat.commandline.Arg;
import org.apache.rat.ui.AbstractOptionCollection;
import org.apache.rat.utils.DefaultLog;

/**
 * Generates a list of TestData for executing the Report.
 * Use of this interface ensures consistent testing across the UIs. Each method
 * tests an Option from {@link OptionCollectionParser} that must be implemented in the UI.
 * This differes from the {@link OptionTestDataProvider} in that tests from this set
 * expect that execptions will be thrown during execution, and tests the xml output.
 */
public abstract class AbstractTestDataProvider {

    /** The list of exclude args */
    static final String[] EXCLUDE_ARGS = {"*.foo", "%regex[[A-Z]\\.bar]", "justbaz"};
    /** the list of include args */
    static final String[] INCLUDE_ARGS = {"B.bar", "justbaz"};
    public final ImmutableList<ImmutablePair<Option, String[]>> NO_OPTIONS = ImmutableList.of(ImmutablePair.nullPair());

    /**
     * Generates a map of TestData indexed by the testName
     * @param optionCollection the collection of options for the UI under test.
     * @return the map of testName to Test Data.
     */
    public final Map<String, TestData> getOptionTestMap(final AbstractOptionCollection<?> optionCollection) {
        Map<String, TestData> map = new TreeMap<>();
        for (TestData test : getOptionTests(optionCollection)) {
            map.put(test.getTestName(), test);
        }
        return map;
    }

    /**
     * Generates a list of test data for Option testing.
     * This is different from UI testing as this is to test
     * that the command line is properly parsed into a configuration.
     * @param optionCollection the collection of options for the UI under test.
     * @return a set of TestData for the tests.
     */
    public final Set<TestData> getOptionTests(final AbstractOptionCollection<?> optionCollection) {
        // the optionCollection establishes any changes to the Arg values.
        Set<TestData> result = new TreeSet<>();
        for (Arg arg : Arg.values()) {
            if (!arg.isEmpty()) {
                switch (arg) {
                    case CONFIGURATION -> arg.group().getOptions().forEach(opt -> configTest(result, opt));
                    case CONFIGURATION_NO_DEFAULTS -> arg.group().getOptions().forEach(opt -> configurationNoDefaultsTest(result, opt));
                    case COUNTER_MIN -> arg.group().getOptions().forEach(opt -> counterMinTest(result, opt));
                    case COUNTER_MAX -> arg.group().getOptions().forEach(opt -> counterMaxTest(result, opt));
                    case DRY_RUN -> arg.group().getOptions().forEach(opt -> dryRunTest(result, opt));
                    case EDIT_COPYRIGHT -> arg.group().getOptions().forEach(opt -> editCopyrightTest(result, opt));
                    case EDIT_ADD -> arg.group().getOptions().forEach(opt -> editLicenseTest(result, opt));
                    case EDIT_OVERWRITE -> arg.group().getOptions().forEach(opt -> editOverwriteTest(result, opt));
                    case HELP_LICENSES -> arg.group().getOptions().forEach(opt -> helpLicenses(result, opt));
                    case EXCLUDE -> arg.group().getOptions().forEach(opt -> inputExcludeTest(result, opt));
                    case EXCLUDE_FILE -> arg.group().getOptions().forEach(opt -> inputExcludeFileTest(result, opt));
                    case EXCLUDE_PARSE_SCM -> arg.group().getOptions().forEach(opt -> inputExcludeParsedScmTest(result, opt));
                    case EXCLUDE_STD -> arg.group().getOptions().forEach(opt -> inputExcludeStdTest(result, opt));
                    case EXCLUDE_SIZE -> arg.group().getOptions().forEach(opt -> inputExcludeSizeTest(result, opt));
                    case INCLUDE -> arg.group().getOptions().forEach(opt -> inputIncludeTest(result, opt));
                    case INCLUDE_FILE -> arg.group().getOptions().forEach(opt -> inputIncludeFileTest(result, opt));
                    case INCLUDE_STD -> arg.group().getOptions().forEach(opt -> inputIncludeStdTest(result, opt));
                    case SOURCE -> arg.group().getOptions().forEach(opt -> inputSourceTest(result, opt));
                    case FAMILIES_APPROVED -> arg.group().getOptions().forEach(opt -> licenseFamiliesApprovedTest(result, opt));
                    case FAMILIES_APPROVED_FILE -> arg.group().getOptions().forEach(opt -> licenseFamiliesApprovedFileTest(result, opt));
                    case FAMILIES_DENIED -> arg.group().getOptions().forEach(opt -> licenseFamiliesDeniedTest(result, opt));
                    case FAMILIES_DENIED_FILE -> arg.group().getOptions().forEach(opt -> licenseFamiliesDeniedFileTest(result, opt));
                    case LICENSES_APPROVED -> arg.group().getOptions().forEach(opt -> licensesApprovedTest(result, opt));
                    case LICENSES_APPROVED_FILE -> arg.group().getOptions().forEach(opt -> licensesApprovedFileTest(result, opt));
                    case LICENSES_DENIED -> arg.group().getOptions().forEach(opt -> licensesDeniedTest(result, opt));
                    case LICENSES_DENIED_FILE -> arg.group().getOptions().forEach(opt -> licensesDeniedFileTest(result, opt));
                    case LOG_LEVEL -> arg.group().getOptions().forEach(opt -> logLevelTest(result, opt));
                    case OUTPUT_ARCHIVE -> arg.group().getOptions().forEach(opt -> outputArchiveTest(result, opt));
                    case OUTPUT_FAMILIES -> arg.group().getOptions().forEach(opt -> outputFamiliesTest(result, opt));
                    case OUTPUT_FILE -> arg.group().getOptions().forEach(opt -> outputFileTest(result, opt));
                    case OUTPUT_LICENSES -> arg.group().getOptions().forEach(opt -> outputLicensesTest(result, opt));
                    case OUTPUT_STANDARD -> arg.group().getOptions().forEach(opt -> outputStandardTest(result, opt));
                    case OUTPUT_STYLE -> arg.group().getOptions().forEach(opt -> outputStyleTest(result, opt));
                }
            }
        }
        validate(result);
        return result;
    }

    private void validate(Set<TestData> result) {
        Set<Option> options = new HashSet<>(Arg.getOptions().getOptions());
        result.forEach(testData -> options.remove(testData.getOption()));
        // TODO fix this once deprecated options are removed
        options.forEach(opt -> DefaultLog.getInstance().warn("Option " + opt.getKey() + " was not tested."));
        //assertThat(options).describedAs("All options are not accounted for.").isEmpty();
    }

    protected abstract void inputExcludeFileTest(final Set<TestData> result, final Option option);

    protected abstract void inputExcludeTest(final Set<TestData> result, final Option option);

    protected abstract void inputExcludeStdTest(final Set<TestData> result, final Option option);

    protected abstract void inputExcludeParsedScmTest(final Set<TestData> result, final Option option);

    protected abstract void inputExcludeSizeTest(final Set<TestData> result, final Option option);

    protected abstract void inputIncludeFileTest(final Set<TestData> result, final Option option);

    protected abstract void inputIncludeTest(final Set<TestData> result, final Option option);

    protected abstract void inputIncludeStdTest(final Set<TestData> result, final Option option);

    protected abstract void inputSourceTest(final Set<TestData> result, final Option option);

    protected abstract void helpLicenses(final Set<TestData> result, final Option option);

    protected abstract void licensesApprovedFileTest(final Set<TestData> result, final Option option);

    protected abstract void licensesApprovedTest(final Set<TestData> result, final Option option);

    protected abstract void licensesDeniedTest(final Set<TestData> result, final Option option);

    protected abstract void licensesDeniedFileTest(final Set<TestData> result, final Option option);

    protected abstract void licenseFamiliesApprovedFileTest(final Set<TestData> result, final Option option);

    protected abstract void licenseFamiliesApprovedTest(final Set<TestData> result, final Option option);

    protected abstract void licenseFamiliesDeniedFileTest(final Set<TestData> result, final Option option);

    protected abstract void licenseFamiliesDeniedTest(final Set<TestData> result, final Option option);

    protected abstract void counterMaxTest(final Set<TestData> result, final Option option);

    protected abstract void counterMinTest(final Set<TestData> result, final Option option);

    /**
     * Add results to the result list.
     * @param result the result list.
     * @param option configuration option we are testing.
     */
    protected abstract void configTest(final Set<TestData> result, final Option option);

    protected abstract void configurationNoDefaultsTest(final Set<TestData> result, final Option option);

    protected abstract void dryRunTest(final Set<TestData> result, final Option option);

    protected abstract  void editCopyrightTest(final Set<TestData> result, final Option option);

    protected abstract void editLicenseTest(final Set<TestData> result, final Option option);

    protected abstract void editOverwriteTest(final Set<TestData> result, final Option option);

    protected abstract void logLevelTest(final Set<TestData> result, final Option option);

    protected abstract void outputArchiveTest(final Set<TestData> result, final Option option);

    protected abstract void outputFamiliesTest(final Set<TestData> result, final Option option);

    protected abstract void outputFileTest(final Set<TestData> result, final Option option);

    protected abstract void outputLicensesTest(final Set<TestData> result, final Option option);

    protected abstract void outputStandardTest(final Set<TestData> result, final Option option);

    protected abstract void outputStyleTest(final Set<TestData> result, final Option option);
}
