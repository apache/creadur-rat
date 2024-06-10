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

import org.apache.rat.OptionCollectionTest;
import org.apache.rat.utils.DefaultLog;

import java.util.HashMap;
import java.util.Map;

/**
 * A list of methods that an OptionsProvider in a test case must support.
 * Use of this interface ensures consistent testing across the UIs.  Each method
 * tests an Option from OptionCollection that must be implemented in the UI.
 * Each method in this interface tests an Optoin in OptionCollection.
 */
public abstract class AbstractOptionsProvider {
    /** A map of tests Options to tests */
    protected final Map<String, OptionCollectionTest.OptionTest> testMap = new HashMap<>();

    protected AbstractOptionsProvider() {
        testMap.put("addLicense", this::addLicenseTest);
        testMap.put("archive", this::archiveTest);
        testMap.put("config", this::configTest);
        testMap.put("configuration-no-defaults", this::configurationNoDefaultsTest);
        testMap.put("copyright", this::copyrightTest);
        testMap.put("dir", () -> {
            DefaultLog.getInstance().info("--dir has no valid test");});
        testMap.put("dry-run", this::dryRunTest);
        testMap.put("edit-copyright", this::editCopyrightTest);
        testMap.put("edit-license", this::editLicensesTest);
        testMap.put("edit-overwrite", this::editOverwriteTest);
        testMap.put("exclude", this::excludeTest);
        testMap.put("exclude-file", this::excludeFileTest);
        testMap.put("force", this::forceTest);
        testMap.put("help", this::helpTest);
        testMap.put("input-exclude", this::inputExcludeTest);
        testMap.put("input-exclude-file", this::inputExcludeFileTest);
        testMap.put("license-families", this::licenseFamiliesTest);
        testMap.put("license-families-file", this::licenseFamiliesFileTest);
        testMap.put("licenses", this::licensesTest);
        testMap.put("licenses-approved", this::licensesApprovedTest);
        testMap.put("licenses-approved-file", this::licensesApprovedFileTest);
        testMap.put("licenses-remove-approved", this::licensesRemoveApprovedTest);
        testMap.put("licenses-remove-approved-file", this::licensesRemoveApprovedFileTest);
        testMap.put("licenses-remove-families", this::licensesRemoveFamiliesTest);
        testMap.put("licenses-remove-families-file", this::licensesRemoveFamiliesFileTest);
        testMap.put("list-families", this::listFamiliesTest);
        testMap.put("list-licenses", this::listLicensesTest);
        testMap.put("log-level", this::logLevelTest);
        testMap.put("no-default-licenses", this::noDefaultsTest);
        testMap.put("out", this::outTest);
        testMap.put("output-archive", this::outputArchiveTest);
        testMap.put("output-families", this::outputFamiliesTest);
        testMap.put("output-file", this::outputFileTest);
        testMap.put("output-licenses", this::outputLicensesTest);
        testMap.put("output-standard", this::outputStandardTest);
        testMap.put("output-style", this::outputStyleTest);
        testMap.put("scan-hidden-directories", this::scanHiddenDirectoriesTest);
        testMap.put("standard", this::standardTest);
        testMap.put("stylesheet", this::styleSheetTest);
        testMap.put("xml", this::xmlTest);
    }
    
    /* tests to be implemented */
    protected abstract void addLicenseTest();
    protected abstract void archiveTest();
    protected abstract void configTest();
    protected abstract void configurationNoDefaultsTest();
    protected abstract void copyrightTest();
    protected abstract void dryRunTest();
    protected abstract void editCopyrightTest();
    protected abstract void editLicensesTest();
    protected abstract void editOverwriteTest();
    protected abstract void excludeFileTest();
    protected abstract void excludeTest();
    protected abstract void forceTest();
    protected abstract void helpTest();
    protected abstract void inputExcludeFileTest();
    protected abstract void inputExcludeTest();
    protected abstract void licenseFamiliesFileTest();
    protected abstract void licenseFamiliesTest();
    protected abstract void licensesApprovedFileTest();
    protected abstract void licensesApprovedTest();
    protected abstract void licensesRemoveApprovedFileTest();
    protected abstract void licensesRemoveApprovedTest();
    protected abstract void licensesRemoveFamiliesFileTest();
    protected abstract void licensesRemoveFamiliesTest();
    protected abstract void licensesTest();
    protected abstract void listFamiliesTest();
    protected abstract void listLicensesTest();
    protected abstract void logLevelTest();
    protected abstract void noDefaultsTest();
    protected abstract void outTest();
    protected abstract void outputArchiveTest();
    protected abstract void outputFamiliesTest();
    protected abstract void outputFileTest();
    protected abstract void outputLicensesTest();
    protected abstract void outputStandardTest();
    protected abstract void outputStyleTest();
    protected abstract void scanHiddenDirectoriesTest();
    protected abstract void standardTest();
    protected abstract void styleSheetTest();
    protected abstract void xmlTest();
}
