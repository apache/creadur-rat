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

package org.apache.rat.ant;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;

/**
* Generated class to provide Ant support for standard RAT command line options.
* DO NOT EDIT - GENERATED FILE
*/
public final class AntReport extends AbstractAntReport {

    /**
     * Constructor.
     */
    public AntReport() {
        super();
    }

    /*  GENERATED METHODS */


    /**
     * The copyright message to use in the license headers. Argument should be a Arg. (See Argument Types for clarification)
     * @param copyright Copyright message to use in the license headers.
     * @deprecated Deprecated for removal since 0.17: Use editCopyright attribute instead.
     */
    @Deprecated
    public void setCopyright(final String copyright) {
        argumentTracker.setArg("copyright", copyright);
    }

    /**
     * The copyright message to use in the license headers. Usually in the form of "Copyright 2008 Foo".  Only valid with editLicense attribute Argument should be a Arg. (See Argument Types for clarification)
     * @param editCopyright Copyright message to use in the license headers.
     */
    public void setEditCopyright(final String editCopyright) {
        argumentTracker.setArg("edit-copyright", editCopyright);
    }

    /**
     * Forces any changes in files to be written directly to the source files so that new files are not created.
     * @param force The state
     * @deprecated Deprecated for removal since 0.17: Use editOverwrite attribute instead.
     */
    @Deprecated
    public void setForce(final boolean force) {
        if (force) {
            argumentTracker.setArg("force", null);
        } else {
            argumentTracker.removeArg("force");
        }
    }

    /**
     * Forces any changes in files to be written directly to the source files so that new files are not created. Only valid with editLicense attribute.
     * @param editOverwrite The state
     */
    public void setEditOverwrite(final boolean editOverwrite) {
        if (editOverwrite) {
            argumentTracker.setArg("edit-overwrite", null);
        } else {
            argumentTracker.removeArg("edit-overwrite");
        }
    }

    /**
     * Add the Apache-2.0 license header to any file with an unknown license that is not in the exclusion list.
     * @param addLicense The state
     * @deprecated Deprecated for removal since 0.17: Use editLicense attribute instead.
     */
    @Deprecated
    public void setAddLicense(final boolean addLicense) {
        if (addLicense) {
            argumentTracker.setArg("addLicense", null);
        } else {
            argumentTracker.removeArg("addLicense");
        }
    }

    /**
     * Add the Apache-2.0 license header to any file with an unknown license that is not in the exclusion list. By default new files will be created with the license header, to force the modification of existing files use the editOverwrite attribute option.
     * @param editLicense The state
     */
    public void setEditLicense(final boolean editLicense) {
        if (editLicense) {
            argumentTracker.setArg("edit-license", null);
        } else {
            argumentTracker.removeArg("edit-license");
        }
    }

    /**
     * File names for system configuration. Arguments should be File. (See Argument Types for clarification)
     */
    public Config createConfig() {
        return new Config();
    }

    /**
     * Handles Config processing.
     */
    public class Config {
        Config() {}
        /**
         * Adds a configured FileSet to the config.
         * @param fileSet The fileSet to add to config.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("config", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * File names for system configuration. Arguments should be File. (See Argument Types for clarification)
     * @deprecated Deprecated for removal since 0.17: Use <config> instead.
     */
    @Deprecated
    public Licenses createLicenses() {
        return new Licenses();
    }

    /**
     * Handles Licenses processing.
     */
    public class Licenses {
        Licenses() {}
        /**
         * Adds a configured FileSet to the licenses.
         * @param fileSet The fileSet to add to licenses.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("licenses", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * Ignore default configuration.
     * @param configurationNoDefaults The state
     */
    public void setConfigurationNoDefaults(final boolean configurationNoDefaults) {
        if (configurationNoDefaults) {
            argumentTracker.setArg("configuration-no-defaults", null);
        } else {
            argumentTracker.removeArg("configuration-no-defaults");
        }
    }

    /**
     * Ignore default configuration.
     * @param noDefaultLicenses The state
     * @deprecated Deprecated for removal since 0.17: Use configurationNoDefaults attribute instead.
     */
    @Deprecated
    public void setNoDefaultLicenses(final boolean noDefaultLicenses) {
        if (noDefaultLicenses) {
            argumentTracker.setArg("no-default-licenses", null);
        } else {
            argumentTracker.removeArg("no-default-licenses");
        }
    }

    /**
     * A comma separated list of approved License IDs. These licenses will be added to the list of approved licenses. Argument should be a LicenseID. (See Argument Types for clarification)
     */
    public LicensesApproved createLicensesApproved() {
        return new LicensesApproved();
    }

    /**
     * Handles LicensesApproved processing.
     */
    public class LicensesApproved {
        LicensesApproved() {}
        /**
         * Adds the licenseID {@link AbstractAntReport.Lst} to licenses-approved.
         * @param licenseID The Lst to add to licenses-approved.
         */
        public void addConfiguredLst(Lst licenseID) {
            argumentTracker.addArg("licenses-approved", licenseID.toString());
        }
        /**
         * Adds a configured FileSet to the licenses-approved.
         * @param fileSet The fileSet to add to licenses-approved.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("licenses-approved", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * Handles LicensesApprovedFile processing.
     */
    public class LicensesApprovedFile {
        LicensesApprovedFile() {}
        /**
         * Adds a configured FileSet to the licenses-approved.
         * @param fileSet The fileSet to add to licenses-approved.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("licenses-approved", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * A comma separated list of approved license family IDs. These license families will be added to the list of approved license families. Argument should be a FamilyID. (See Argument Types for clarification)
     */
    public LicenseFamiliesApproved createLicenseFamiliesApproved() {
        return new LicenseFamiliesApproved();
    }

    /**
     * Handles LicenseFamiliesApproved processing.
     */
    public class LicenseFamiliesApproved {
        LicenseFamiliesApproved() {}
        /**
         * Adds the familyID {@link AbstractAntReport.Lst} to license-families-approved.
         * @param familyID The Lst to add to license-families-approved.
         */
        public void addConfiguredLst(Lst familyID) {
            argumentTracker.addArg("license-families-approved", familyID.toString());
        }
        /**
         * Adds a configured FileSet to the license-families-approved.
         * @param fileSet The fileSet to add to license-families-approved.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("license-families-approved", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * Handles LicenseFamiliesApprovedFile processing.
     */
    public class LicenseFamiliesApprovedFile {
        LicenseFamiliesApprovedFile() {}
        /**
         * Adds a configured FileSet to the license-families-approved.
         * @param fileSet The fileSet to add to license-families-approved.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("license-families-approved", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * A comma separated list of denied License IDs. These licenses will be removed from the list of approved licenses. Once licenses are removed they can not be added back. Argument should be a LicenseID. (See Argument Types for clarification)
     */
    public LicensesDenied createLicensesDenied() {
        return new LicensesDenied();
    }

    /**
     * Handles LicensesDenied processing.
     */
    public class LicensesDenied {
        LicensesDenied() {}
        /**
         * Adds a configured FileSet to the licenses-denied.
         * @param fileSet The fileSet to add to licenses-denied.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("licenses-denied", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
        /**
         * Adds the licenseID {@link AbstractAntReport.Lst} to licenses-denied.
         * @param licenseID The Lst to add to licenses-denied.
         */
        public void addConfiguredLst(Lst licenseID) {
            argumentTracker.addArg("licenses-denied", licenseID.toString());
        }
    }

    /**
     * Handles LicensesDeniedFile processing.
     */
    public class LicensesDeniedFile {
        LicensesDeniedFile() {}
        /**
         * Adds a configured FileSet to the licenses-denied.
         * @param fileSet The fileSet to add to licenses-denied.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("licenses-denied", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * A comma separated list of denied License family IDs. These license families will be removed from the list of approved licenses. Once license families are removed they can not be added back. Argument should be a FamilyID. (See Argument Types for clarification)
     */
    public LicenseFamiliesDenied createLicenseFamiliesDenied() {
        return new LicenseFamiliesDenied();
    }

    /**
     * Handles LicenseFamiliesDenied processing.
     */
    public class LicenseFamiliesDenied {
        LicenseFamiliesDenied() {}
        /**
         * Adds the familyID {@link AbstractAntReport.Lst} to license-families-denied.
         * @param familyID The Lst to add to license-families-denied.
         */
        public void addConfiguredLst(Lst familyID) {
            argumentTracker.addArg("license-families-denied", familyID.toString());
        }
        /**
         * Adds a configured FileSet to the license-families-denied.
         * @param fileSet The fileSet to add to license-families-denied.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("license-families-denied", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * Handles LicenseFamiliesDeniedFile processing.
     */
    public class LicenseFamiliesDeniedFile {
        LicenseFamiliesDeniedFile() {}
        /**
         * Adds a configured FileSet to the license-families-denied.
         * @param fileSet The fileSet to add to license-families-denied.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("license-families-denied", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * The acceptable maximum number for the specified counter. A value of '-1' specifies an unlimited number. Arguments should be CounterPattern. (See Argument Types for clarification)
     */
    public CounterMax createCounterMax() {
        return new CounterMax();
    }

    /**
     * Handles CounterMax processing.
     */
    public class CounterMax {
        CounterMax() {}
        /**
         * Adds the counterPattern {@link AbstractAntReport.Cntr} to counter-max.
         * @param counterPattern The Cntr to add to counter-max.
         */
        public void addConfiguredCntr(Cntr counterPattern) {
            argumentTracker.addArg("counter-max", counterPattern.toString());
        }
    }

    /**
     * The minimum number for the specified counter. Arguments should be CounterPattern. (See Argument Types for clarification)
     */
    public CounterMin createCounterMin() {
        return new CounterMin();
    }

    /**
     * Handles CounterMin processing.
     */
    public class CounterMin {
        CounterMin() {}
        /**
         * Adds the counterPattern {@link AbstractAntReport.Cntr} to counter-min.
         * @param counterPattern The Cntr to add to counter-min.
         */
        public void addConfiguredCntr(Cntr counterPattern) {
            argumentTracker.addArg("counter-min", counterPattern.toString());
        }
    }

    /**
     * Excludes files matching <Expression>. Arguments should be Expression. (See Argument Types for clarification)
     * @deprecated Deprecated for removal since 0.17: Use <inputExclude> instead.
     */
    @Deprecated
    public Exclude createExclude() {
        return new Exclude();
    }

    /**
     * Handles Exclude processing.
     */
    public class Exclude {
        Exclude() {}
        /**
         * Adds the expression {@link AbstractAntReport.Expr} to exclude.
         * @param expression The Expr to add to exclude.
         */
        public void addConfiguredExpr(Expr expression) {
            argumentTracker.addArg("exclude", expression.toString());
        }
    }

    /**
     * Excludes files matching <Expression>. Arguments should be Expression. (See Argument Types for clarification)
     */
    public InputExclude createInputExclude() {
        return new InputExclude();
    }

    /**
     * Handles InputExclude processing.
     */
    public class InputExclude {
        InputExclude() {}
        /**
         * Adds a configured FileSet to the input-exclude.
         * @param fileSet The fileSet to add to input-exclude.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("input-exclude", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
        /**
         * Adds the expression {@link AbstractAntReport.Expr} to input-exclude.
         * @param expression The Expr to add to input-exclude.
         */
        public void addConfiguredExpr(Expr expression) {
            argumentTracker.addArg("input-exclude", expression.toString());
        }
        /**
         * Adds the standardCollection {@link AbstractAntReport.Std} to input-exclude.
         * @param standardCollection The Std to add to input-exclude.
         */
        public void addConfiguredStd(Std standardCollection) {
            argumentTracker.addArg("input-exclude", standardCollection.toString());
        }
    }

    /**
     * Handles InputExcludeStd processing.
     */
    public class InputExcludeStd {
        InputExcludeStd() {}
        /**
         * Adds the standardCollection {@link AbstractAntReport.Std} to input-exclude.
         * @param standardCollection The Std to add to input-exclude.
         */
        public void addConfiguredStd(Std standardCollection) {
            argumentTracker.addArg("input-exclude", standardCollection.toString());
        }
    }

    /**
     * Handles InputExcludeFile processing.
     */
    public class InputExcludeFile {
        InputExcludeFile() {}
        /**
         * Adds a configured FileSet to the input-exclude.
         * @param fileSet The fileSet to add to input-exclude.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("input-exclude", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * Reads <Expression> entries from a file. Entries will be excluded from processing. Argument should be a File. (See Argument Types for clarification)
     * @param excludeFile <Expression> entries from a file.
     * @deprecated Deprecated for removal since 0.17: Use inputExcludeFile attribute instead.
     */
    @Deprecated
    public void setExcludeFile(final String excludeFile) {
        argumentTracker.setArg("exclude-file", excludeFile);
    }

    /**
     * Excludes files with sizes less than the number of bytes specified. Argument should be a Integer. (See Argument Types for clarification)
     * @param inputExcludeSize Files with sizes less than the number of bytes specified.
     */
    public void setInputExcludeSize(final String inputExcludeSize) {
        argumentTracker.setArg("input-exclude-size", inputExcludeSize);
    }

    /**
     * Includes files matching <Expression>. Will override excluded files. Arguments should be Expression. (See Argument Types for clarification)
     */
    public InputInclude createInputInclude() {
        return new InputInclude();
    }

    /**
     * Handles InputInclude processing.
     */
    public class InputInclude {
        InputInclude() {}
        /**
         * Adds a configured FileSet to the input-include.
         * @param fileSet The fileSet to add to input-include.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("input-include", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
        /**
         * Adds the expression {@link AbstractAntReport.Expr} to input-include.
         * @param expression The Expr to add to input-include.
         */
        public void addConfiguredExpr(Expr expression) {
            argumentTracker.addArg("input-include", expression.toString());
        }
        /**
         * Adds the standardCollection {@link AbstractAntReport.Std} to input-include.
         * @param standardCollection The Std to add to input-include.
         */
        public void addConfiguredStd(Std standardCollection) {
            argumentTracker.addArg("input-include", standardCollection.toString());
        }
    }

    /**
     * Handles InputIncludeFile processing.
     */
    public class InputIncludeFile {
        InputIncludeFile() {}
        /**
         * Adds a configured FileSet to the input-include.
         * @param fileSet The fileSet to add to input-include.
         */
        public void addConfiguredFileSet(FileSet fileSet) {
            for (Resource resource : fileSet) {
                if (resource.isFilesystemOnly()) {
                    argumentTracker.addArg("input-include", ((FileResource) resource).getFile().getAbsolutePath());
                }
            }
        }
    }

    /**
     * Handles InputIncludeStd processing.
     */
    public class InputIncludeStd {
        InputIncludeStd() {}
        /**
         * Adds the standardCollection {@link AbstractAntReport.Std} to input-include.
         * @param standardCollection The Std to add to input-include.
         */
        public void addConfiguredStd(Std standardCollection) {
            argumentTracker.addArg("input-include", standardCollection.toString());
        }
    }

    /**
     * Includes files matching <Expression>. Will override excluded files. Arguments should be Expression. (See Argument Types for clarification)
     * @deprecated Deprecated for removal since 0.17: Use <inputInclude> instead.
     */
    @Deprecated
    public Include createInclude() {
        return new Include();
    }

    /**
     * Handles Include processing.
     */
    public class Include {
        Include() {}
        /**
         * Adds the expression {@link AbstractAntReport.Expr} to include.
         * @param expression The Expr to add to include.
         */
        public void addConfiguredExpr(Expr expression) {
            argumentTracker.addArg("include", expression.toString());
        }
    }

    /**
     * Reads <Expression> entries from a file. Entries will be excluded from processing. Argument should be a File. (See Argument Types for clarification)
     * @param includesFile <Expression> entries from a file.
     * @deprecated Deprecated for removal since 0.17: Use inputIncludeFile attribute instead.
     */
    @Deprecated
    public void setIncludesFile(final String includesFile) {
        argumentTracker.setArg("includes-file", includesFile);
    }

    /**
     * Scans hidden directories.
     * @param scanHiddenDirectories The state
     * @deprecated Deprecated for removal since 0.17: Use <inputIncludeStd> with 'HIDDEN_DIR' argument instead.
     */
    @Deprecated
    public void setScanHiddenDirectories(final boolean scanHiddenDirectories) {
        if (scanHiddenDirectories) {
            argumentTracker.setArg("scan-hidden-directories", null);
        } else {
            argumentTracker.removeArg("scan-hidden-directories");
        }
    }

    /**
     * Parse SCM based exclusion files to exclude specified files and directories. This action can apply to any standard collection that implements a file processor. Arguments should be StandardCollection. (See Argument Types for clarification)
     */
    public InputExcludeParsedScm createInputExcludeParsedScm() {
        return new InputExcludeParsedScm();
    }

    /**
     * Handles InputExcludeParsedScm processing.
     */
    public class InputExcludeParsedScm {
        InputExcludeParsedScm() {}
        /**
         * Adds the standardCollection {@link AbstractAntReport.Std} to input-exclude-parsed-scm.
         * @param standardCollection The Std to add to input-exclude-parsed-scm.
         */
        public void addConfiguredStd(Std standardCollection) {
            argumentTracker.addArg("input-exclude-parsed-scm", standardCollection.toString());
        }
    }

    /**
     * XSLT stylesheet to use when creating the report. Either an external xsl file may be specified or one of the internal named sheets. Argument should be a StyleSheet. (See Argument Types for clarification)
     * @param outputStyle Stylesheet to use when creating the report.
     */
    public void setOutputStyle(final String outputStyle) {
        argumentTracker.setArg("output-style", outputStyle);
    }

    /**
     * XSLT stylesheet to use when creating the report. Argument should be a StyleSheet. (See Argument Types for clarification)
     * @param stylesheet Stylesheet to use when creating the report.
     * @deprecated Deprecated for removal since 0.17: Use outputStyle attribute instead.
     */
    @Deprecated
    public void setStylesheet(final String stylesheet) {
        argumentTracker.setArg("stylesheet", stylesheet);
    }

    /**
     * forces XML output rather than the textual report.
     * @param xml The state
     * @deprecated Deprecated for removal since 0.17: Use outputStyle attribute with the 'xml' argument instead.
     */
    @Deprecated
    public void setXml(final boolean xml) {
        if (xml) {
            argumentTracker.setArg("xml", null);
        } else {
            argumentTracker.removeArg("xml");
        }
    }

    /**
     * List the defined licenses. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param outputLicenses The defined licenses.
     */
    public void setOutputLicenses(final String outputLicenses) {
        argumentTracker.setArg("output-licenses", outputLicenses);
    }

    /**
     * List the defined licenses. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param listLicenses The defined licenses.
     * @deprecated Deprecated for removal since 0.17: Use outputLicenses attribute instead.
     */
    @Deprecated
    public void setListLicenses(final String listLicenses) {
        argumentTracker.setArg("list-licenses", listLicenses);
    }

    /**
     * List the defined license families. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param outputFamilies The defined license families.
     */
    public void setOutputFamilies(final String outputFamilies) {
        argumentTracker.setArg("output-families", outputFamilies);
    }

    /**
     * List the defined license families. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param listFamilies The defined license families.
     * @deprecated Deprecated for removal since 0.17: Use outputFamilies attribute instead.
     */
    @Deprecated
    public void setListFamilies(final String listFamilies) {
        argumentTracker.setArg("list-families", listFamilies);
    }

    /**
     * If set do not update the files but generate the reports.
     * @param dryRun The state
     */
    public void setDryRun(final boolean dryRun) {
        if (dryRun) {
            argumentTracker.setArg("dry-run", null);
        } else {
            argumentTracker.removeArg("dry-run");
        }
    }

    /**
     * Define the output file where to write a report to. Argument should be a File. (See Argument Types for clarification)
     * @param out The output file where to write a report to.
     * @deprecated Deprecated for removal since 0.17: Use outputFile attribute instead.
     */
    @Deprecated
    public void setOut(final String out) {
        argumentTracker.setArg("out", out);
    }

    /**
     * Define the output file where to write a report to. Argument should be a File. (See Argument Types for clarification)
     * @param outputFile The output file where to write a report to.
     */
    public void setOutputFile(final String outputFile) {
        argumentTracker.setArg("output-file", outputFile);
    }

    /**
     * Specifies the level of detail in ARCHIVE file reporting. Argument should be a ProcessingType. (See Argument Types for clarification)
     * @param outputArchive The level of detail in ARCHIVE file reporting.
     */
    public void setOutputArchive(final String outputArchive) {
        argumentTracker.setArg("output-archive", outputArchive);
    }

    /**
     * Specifies the level of detail in STANDARD file reporting. Argument should be a ProcessingType. (See Argument Types for clarification)
     * @param outputStandard The level of detail in STANDARD file reporting.
     */
    public void setOutputStandard(final String outputStandard) {
        argumentTracker.setArg("output-standard", outputStandard);
    }


}
