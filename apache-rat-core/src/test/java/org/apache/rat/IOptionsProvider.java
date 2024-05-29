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
package org.apache.rat;

/**
 * A list of methods that an OptionsProvider in a test case must support.
 * Use of this interface ensures consistent testing across the UIs.  Each method
 * tests an Option from OptionCollection that must be implemented in the UI.
 * Each method in this interface tests an Optoin in OptionCollection.
 */
public interface IOptionsProvider {
    /**
     * @see OptionCollection#ADD_LICENSE
     */
    void addLicenseTest();

    /**
     * @see OptionCollection#ARCHIVE
     */
    void archiveTest();

    /**
     * @see OptionCollection#STANDARD
     */
    void standardTest();

    /**
     * @see OptionCollection#COPYRIGHT
     */
    void copyrightTest();

    /**
     * @see OptionCollection#DRY_RUN
     */
    void dryRunTest();

    /**
     * @see OptionCollection#EXCLUDE_CLI
     */
    void excludeCliTest();

    /**
     * @see OptionCollection#EXCLUDE_FILE_CLI
     */
    void excludeCliFileTest();

    /**
     * @see OptionCollection#FORCE
     */
    void forceTest();

    /**
     * @see OptionCollection#LICENSES
     */
    void licensesTest();

    /**
     * @see OptionCollection#LIST_LICENSES
     */
    void listLicensesTest();

    /**
     * @see OptionCollection#LIST_FAMILIES
     */
    void listFamiliesTest();

    /**
     * @see OptionCollection#NO_DEFAULTS
     */
    void noDefaultsTest();

    /**
     * @see OptionCollection#OUT
     */
    void outTest();

    /**
     * @see OptionCollection#SCAN_HIDDEN_DIRECTORIES
     */
    void scanHiddenDirectoriesTest();

    /**
     * @see OptionCollection#STYLESHEET_CLI
     */
    void styleSheetTest();

    /**
     * @see OptionCollection#XML
     */
    void xmlTest();
}
