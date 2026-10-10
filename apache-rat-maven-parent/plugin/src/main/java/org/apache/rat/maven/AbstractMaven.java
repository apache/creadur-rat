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

package org.apache.rat.maven;

import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.rat.commandline.Arg;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.ui.ArgumentTracker;

/**
 * Generated class to provide Maven support for standard RAT command line options
 * DO NOT EDIT - GENERATED FILE
 */
@SuppressWarnings("checkstyle:LineLength")
public abstract class AbstractMaven extends AbstractMojo {
    /** The arguments that have been set */
    protected final ArgumentTracker argumentTracker;
    /** The MavenOptionCollection */
    protected final MavenOptionCollection mavenOptionCollection;

    protected AbstractMaven() {
        mavenOptionCollection = new MavenOptionCollection();
        argumentTracker = new ArgumentTracker(mavenOptionCollection);
        Optional<MavenOption> mavenOption = mavenOptionCollection.getMappedOption(Arg.EXCLUDE_STD.option());
        argumentTracker.addArg(mavenOption.orElseThrow(() -> new IllegalStateException(Arg.EXCLUDE_STD.option() + " not found in Maven option collection")), StandardCollection.MAVEN.name());
    }

    /*  GENERATED METHODS */


    /**
     * Add the Apache-2.0 license header to any file with an unknown license that is not in the exclusion list.
     * @param addLicense The state
     * @deprecated Deprecated for removal since 0.17: Use <editLicense> instead.
     */
    @Deprecated
    @Parameter(property = "rat.addLicense")
    public void setAddLicense(final boolean addLicense) {
        if (addLicense) {
            argumentTracker.setArg("addLicense", null);
        } else {
            argumentTracker.removeArg("addLicense");
        }
    }



    /**
     * File names for system configuration. Arguments should be File. (See Argument Types for clarification)
     * @param configs names for system configuration.
     */
    @Parameter
    public void setConfigs(final String[] configs) {
        argumentTracker.addArg("config", configs);
    }



    /**
     * Ignore default configuration.
     * @param configurationNoDefaults The state
     */
    @Parameter(property = "rat.configurationNoDefaults")
    public void setConfigurationNoDefaults(final boolean configurationNoDefaults) {
        if (configurationNoDefaults) {
            argumentTracker.setArg("configuration-no-defaults", null);
        } else {
            argumentTracker.removeArg("configuration-no-defaults");
        }
    }



    /**
     * The copyright message to use in the license headers. Argument should be a Arg. (See Argument Types for clarification)
     * @param copyright copyright message to use in the license headers.
     * @deprecated Deprecated for removal since 0.17: Use <editCopyright> instead.
     */
    @Deprecated
    @Parameter(property = "rat.copyright")
    public void setCopyright(final String copyright) {
        argumentTracker.setArg("copyright", copyright);
    }



    /**
     * The acceptable maximum number for the specified counter. A value of '-1' specifies an unlimited number. Arguments should be CounterPattern. (See Argument Types for clarification)
     * @param counterMaxs acceptable maximum number for the specified counter.
     */
    @Parameter
    public void setCounterMaxs(final String[] counterMaxs) {
        argumentTracker.addArg("counter-max", counterMaxs);
    }



    /**
     * The minimum number for the specified counter. Arguments should be CounterPattern. (See Argument Types for clarification)
     * @param counterMins minimum number for the specified counter.
     */
    @Parameter
    public void setCounterMins(final String[] counterMins) {
        argumentTracker.addArg("counter-min", counterMins);
    }



    /**
     * If set do not update the files but generate the reports.
     * @param dryRun The state
     */
    @Parameter(property = "rat.dryRun")
    public void setDryRun(final boolean dryRun) {
        if (dryRun) {
            argumentTracker.setArg("dry-run", null);
        } else {
            argumentTracker.removeArg("dry-run");
        }
    }



    /**
     * The copyright message to use in the license headers. Usually in the form of "Copyright 2008 Foo".  Only valid with <editLicense> Argument should be a Arg. (See Argument Types for clarification)
     * @param editCopyright copyright message to use in the license headers.
     */
    @Parameter(property = "rat.editCopyright")
    public void setEditCopyright(final String editCopyright) {
        argumentTracker.setArg("edit-copyright", editCopyright);
    }



    /**
     * Add the Apache-2.0 license header to any file with an unknown license that is not in the exclusion list. By default new files will be created with the license header, to force the modification of existing files use the <editOverwrite> option.
     * @param editLicense The state
     */
    @Parameter(property = "rat.editLicense")
    public void setEditLicense(final boolean editLicense) {
        if (editLicense) {
            argumentTracker.setArg("edit-license", null);
        } else {
            argumentTracker.removeArg("edit-license");
        }
    }



    /**
     * Forces any changes in files to be written directly to the source files so that new files are not created. Only valid with <editLicense>.
     * @param editOverwrite The state
     */
    @Parameter(property = "rat.editOverwrite")
    public void setEditOverwrite(final boolean editOverwrite) {
        if (editOverwrite) {
            argumentTracker.setArg("edit-overwrite", null);
        } else {
            argumentTracker.removeArg("edit-overwrite");
        }
    }



    /**
     * Reads <Expression> entries from a file. Entries will be excluded from processing. Argument should be a File. (See Argument Types for clarification)
     * @param excludeFile <Expression> entries from a file.
     * @deprecated Deprecated for removal since 0.17: Use <inputExcludeFile> instead.
     */
    @Deprecated
    @Parameter(property = "rat.excludeFile")
    public void setExcludeFile(final String excludeFile) {
        argumentTracker.setArg("exclude-file", excludeFile);
    }



    /**
     * Excludes files matching <Expression>. Arguments should be Expression. (See Argument Types for clarification)
     * @param excludes files matching <Expression>.
     * @deprecated Deprecated for removal since 0.17: Use <inputExcludes> instead.
     */
    @Deprecated
    @Parameter
    public void setExcludes(final String[] excludes) {
        argumentTracker.addArg("exclude", excludes);
    }



    /**
     * Forces any changes in files to be written directly to the source files so that new files are not created.
     * @param force The state
     * @deprecated Deprecated for removal since 0.17: Use <editOverwrite> instead.
     */
    @Deprecated
    @Parameter(property = "rat.force")
    public void setForce(final boolean force) {
        if (force) {
            argumentTracker.setArg("force", null);
        } else {
            argumentTracker.removeArg("force");
        }
    }



    /**
     * Print information about registered licenses.
     * @param helpLicenses The state
     */
    @Parameter(property = "rat.helpLicenses")
    public void setHelpLicenses(final boolean helpLicenses) {
        if (helpLicenses) {
            argumentTracker.setArg("help-licenses", null);
        } else {
            argumentTracker.removeArg("help-licenses");
        }
    }



    /**
     * Includes files matching <Expression>. Will override excluded files. Arguments should be Expression. (See Argument Types for clarification)
     * @param includes files matching <Expression>.
     * @deprecated Deprecated for removal since 0.17: Use <inputIncludes> instead.
     */
    @Deprecated
    @Parameter
    public void setIncludes(final String[] includes) {
        argumentTracker.addArg("include", includes);
    }



    /**
     * Reads <Expression> entries from a file. Entries will override excluded files. Argument should be a File. (See Argument Types for clarification)
     * @param includesFile <Expression> entries from a file.
     * @deprecated Deprecated for removal since 0.17: Use <inputIncludeFile> instead.
     */
    @Deprecated
    @Parameter(property = "rat.includesFile")
    public void setIncludesFile(final String includesFile) {
        argumentTracker.setArg("includes-file", includesFile);
    }



    /**
     * Reads <Expression> entries from a file. Entries will be excluded from processing. Argument should be a File. (See Argument Types for clarification)
     * @param inputExcludeFile <Expression> entries from a file.
     */
    @Parameter(property = "rat.inputExcludeFile")
    public void setInputExcludeFile(final String inputExcludeFile) {
        argumentTracker.setArg("input-exclude-file", inputExcludeFile);
    }



    /**
     * Parse SCM based exclusion files to exclude specified files and directories. This action can apply to any standard collection that implements a file processor. Arguments should be StandardCollection. (See Argument Types for clarification)
     * @param inputExcludeParsedScms SCM based exclusion files to exclude specified files and directories.
     */
    @Parameter
    public void setInputExcludeParsedScms(final String[] inputExcludeParsedScms) {
        argumentTracker.addArg("input-exclude-parsed-scm", inputExcludeParsedScms);
    }



    /**
     * Excludes files with sizes less than the number of bytes specified. Argument should be a Integer. (See Argument Types for clarification)
     * @param inputExcludeSize files with sizes less than the number of bytes specified.
     */
    @Parameter(property = "rat.inputExcludeSize")
    public void setInputExcludeSize(final String inputExcludeSize) {
        argumentTracker.setArg("input-exclude-size", inputExcludeSize);
    }



    /**
     * Excludes files defined in standard collections based on commonly occurring groups. Excludes any path matcher actions but DOES NOT exclude any file processor actions. Arguments should be StandardCollection. (See Argument Types for clarification)
     * @param inputExcludeStds files defined in standard collections based on commonly occurring groups.
     */
    @Parameter
    public void setInputExcludeStds(final String[] inputExcludeStds) {
        argumentTracker.addArg("input-exclude-std", inputExcludeStds);
    }



    /**
     * Excludes files matching <Expression>. Arguments should be Expression. (See Argument Types for clarification)
     * @param inputExcludes files matching <Expression>.
     */
    @Parameter
    public void setInputExcludes(final String[] inputExcludes) {
        argumentTracker.addArg("input-exclude", inputExcludes);
    }



    /**
     * Reads <Expression> entries from a file. Entries will override excluded files. Argument should be a File. (See Argument Types for clarification)
     * @param inputIncludeFile <Expression> entries from a file.
     */
    @Parameter(property = "rat.inputIncludeFile")
    public void setInputIncludeFile(final String inputIncludeFile) {
        argumentTracker.setArg("input-include-file", inputIncludeFile);
    }



    /**
     * Includes files defined in standard collections based on commonly occurring groups. Includes any path matcher actions but DOES NOT include any file processor actions. Arguments should be StandardCollection. (See Argument Types for clarification)
     * @param inputIncludeStds files defined in standard collections based on commonly occurring groups.
     */
    @Parameter
    public void setInputIncludeStds(final String[] inputIncludeStds) {
        argumentTracker.addArg("input-include-std", inputIncludeStds);
    }



    /**
     * Includes files matching <Expression>. Will override excluded files. Arguments should be Expression. (See Argument Types for clarification)
     * @param inputIncludes files matching <Expression>.
     */
    @Parameter
    public void setInputIncludes(final String[] inputIncludes) {
        argumentTracker.addArg("input-include", inputIncludes);
    }



    /**
     * A file containing file names to process. File names must use linux directory separator ('/') or none at all. File names that do not start with '/' are relative to the directory where the argument is located. Arguments should be File. (See Argument Types for clarification)
     * @param inputSources file containing file names to process.
     */
    @Parameter
    public void setInputSources(final String[] inputSources) {
        argumentTracker.addArg("input-source", inputSources);
    }



    /**
     * A comma separated list of approved license family IDs. These license families will be added to the list of approved license families. Argument should be a FamilyID. (See Argument Types for clarification)
     * @param licenseFamiliesApproved comma separated list of approved license family IDs.
     */
    @Parameter(property = "rat.licenseFamiliesApproved")
    public void setLicenseFamiliesApproved(final String licenseFamiliesApproved) {
        argumentTracker.setArg("license-families-approved", licenseFamiliesApproved);
    }



    /**
     * Name of file containing comma separated lists of approved family IDs. Argument should be a File. (See Argument Types for clarification)
     * @param licenseFamiliesApprovedFile of file containing comma separated lists of approved family IDs.
     */
    @Parameter(property = "rat.licenseFamiliesApprovedFile")
    public void setLicenseFamiliesApprovedFile(final String licenseFamiliesApprovedFile) {
        argumentTracker.setArg("license-families-approved-file", licenseFamiliesApprovedFile);
    }



    /**
     * A comma separated list of denied License family IDs. These license families will be removed from the list of approved licenses. Once license families are removed they can not be added back. Argument should be a FamilyID. (See Argument Types for clarification)
     * @param licenseFamiliesDenied comma separated list of denied License family IDs.
     */
    @Parameter(property = "rat.licenseFamiliesDenied")
    public void setLicenseFamiliesDenied(final String licenseFamiliesDenied) {
        argumentTracker.setArg("license-families-denied", licenseFamiliesDenied);
    }



    /**
     * Name of file containing comma separated lists of denied license IDs. These license families will be removed from the list of approved licenses. Once license families are removed they can not be added back. Argument should be a File. (See Argument Types for clarification)
     * @param licenseFamiliesDeniedFile of file containing comma separated lists of denied license IDs.
     */
    @Parameter(property = "rat.licenseFamiliesDeniedFile")
    public void setLicenseFamiliesDeniedFile(final String licenseFamiliesDeniedFile) {
        argumentTracker.setArg("license-families-denied-file", licenseFamiliesDeniedFile);
    }



    /**
     * File names for system configuration. Arguments should be File. (See Argument Types for clarification)
     * @param licenses names for system configuration.
     * @deprecated Deprecated for removal since 0.17: Use <configs> instead.
     */
    @Deprecated
    @Parameter
    public void setLicenses(final String[] licenses) {
        argumentTracker.addArg("licenses", licenses);
    }



    /**
     * A comma separated list of approved License IDs. These licenses will be added to the list of approved licenses. Argument should be a LicenseID. (See Argument Types for clarification)
     * @param licensesApproved comma separated list of approved License IDs.
     */
    @Parameter(property = "rat.licensesApproved")
    public void setLicensesApproved(final String licensesApproved) {
        argumentTracker.setArg("licenses-approved", licensesApproved);
    }



    /**
     * Name of file containing comma separated lists of approved License IDs. Argument should be a File. (See Argument Types for clarification)
     * @param licensesApprovedFile of file containing comma separated lists of approved License IDs.
     */
    @Parameter(property = "rat.licensesApprovedFile")
    public void setLicensesApprovedFile(final String licensesApprovedFile) {
        argumentTracker.setArg("licenses-approved-file", licensesApprovedFile);
    }



    /**
     * A comma separated list of denied License IDs. These licenses will be removed from the list of approved licenses. Once licenses are removed they can not be added back. Argument should be a LicenseID. (See Argument Types for clarification)
     * @param licensesDenied comma separated list of denied License IDs.
     */
    @Parameter(property = "rat.licensesDenied")
    public void setLicensesDenied(final String licensesDenied) {
        argumentTracker.setArg("licenses-denied", licensesDenied);
    }



    /**
     * Name of file containing comma separated lists of the denied license IDs. These licenses will be removed from the list of approved licenses. Once licenses are removed they can not be added back. Argument should be a File. (See Argument Types for clarification)
     * @param licensesDeniedFile of file containing comma separated lists of the denied license IDs.
     */
    @Parameter(property = "rat.licensesDeniedFile")
    public void setLicensesDeniedFile(final String licensesDeniedFile) {
        argumentTracker.setArg("licenses-denied-file", licensesDeniedFile);
    }



    /**
     * List the defined license families. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param listFamilies the defined license families.
     * @deprecated Deprecated for removal since 0.17: Use <outputFamilies> instead.
     */
    @Deprecated
    @Parameter(property = "rat.listFamilies")
    public void setListFamilies(final String listFamilies) {
        argumentTracker.setArg("list-families", listFamilies);
    }



    /**
     * List the defined licenses. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param listLicenses the defined licenses.
     * @deprecated Deprecated for removal since 0.17: Use <outputLicenses> instead.
     */
    @Deprecated
    @Parameter(property = "rat.listLicenses")
    public void setListLicenses(final String listLicenses) {
        argumentTracker.setArg("list-licenses", listLicenses);
    }



    /**
     * Ignore default configuration.
     * @param noDefaultLicenses The state
     * @deprecated Deprecated for removal since 0.17: Use <configurationNoDefaults> instead.
     */
    @Deprecated
    @Parameter(property = "rat.noDefaultLicenses")
    public void setNoDefaultLicenses(final boolean noDefaultLicenses) {
        if (noDefaultLicenses) {
            argumentTracker.setArg("no-default-licenses", null);
        } else {
            argumentTracker.removeArg("no-default-licenses");
        }
    }



    /**
     * Define the output file where to write a report to. Argument should be a File. (See Argument Types for clarification)
     * @param out the output file where to write a report to.
     * @deprecated Deprecated for removal since 0.17: Use <outputFile> instead.
     */
    @Deprecated
    @Parameter(property = "rat.out")
    public void setOut(final String out) {
        argumentTracker.setArg("out", out);
    }



    /**
     * Specifies the level of detail in ARCHIVE file reporting. Argument should be a ProcessingType. (See Argument Types for clarification)
     * @param outputArchive the level of detail in ARCHIVE file reporting.
     */
    @Parameter(property = "rat.outputArchive", defaultValue = "NOTIFICATION")
    public void setOutputArchive(final String outputArchive) {
        argumentTracker.setArg("output-archive", outputArchive);
    }



    /**
     * List the defined license families. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param outputFamilies the defined license families.
     */
    @Parameter(property = "rat.outputFamilies", defaultValue = "NONE")
    public void setOutputFamilies(final String outputFamilies) {
        argumentTracker.setArg("output-families", outputFamilies);
    }



    /**
     * Define the output file where to write a report to. Argument should be a File. (See Argument Types for clarification)
     * @param outputFile the output file where to write a report to.
     */
    @Parameter(property = "rat.outputFile")
    public void setOutputFile(final String outputFile) {
        argumentTracker.setArg("output-file", outputFile);
    }



    /**
     * List the defined licenses. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param outputLicenses the defined licenses.
     */
    @Parameter(property = "rat.outputLicenses", defaultValue = "NONE")
    public void setOutputLicenses(final String outputLicenses) {
        argumentTracker.setArg("output-licenses", outputLicenses);
    }



    /**
     * Specifies the level of detail in STANDARD file reporting. Argument should be a ProcessingType. (See Argument Types for clarification)
     * @param outputStandard the level of detail in STANDARD file reporting.
     */
    @Parameter(property = "rat.outputStandard", defaultValue = "ABSENCE")
    public void setOutputStandard(final String outputStandard) {
        argumentTracker.setArg("output-standard", outputStandard);
    }



    /**
     * XSLT stylesheet to use when creating the report. Either an external xsl file may be specified or one of the internal named sheets. Argument should be a StyleSheet. (See Argument Types for clarification)
     * @param outputStyle stylesheet to use when creating the report.
     */
    @Parameter(property = "rat.outputStyle")
    public void setOutputStyle(final String outputStyle) {
        argumentTracker.setArg("output-style", outputStyle);
    }



    /**
     * Scans hidden directories.
     * @param scanHiddenDirectories The state
     * @deprecated Deprecated for removal since 0.17: Use <inputIncludeStds> with 'HIDDEN_DIR' argument instead.
     */
    @Deprecated
    @Parameter(property = "rat.scanHiddenDirectories")
    public void setScanHiddenDirectories(final boolean scanHiddenDirectories) {
        if (scanHiddenDirectories) {
            argumentTracker.setArg("scan-hidden-directories", null);
        } else {
            argumentTracker.removeArg("scan-hidden-directories");
        }
    }



    /**
     * XSLT stylesheet to use when creating the report. Argument should be a StyleSheet. (See Argument Types for clarification)
     * @param stylesheet stylesheet to use when creating the report.
     * @deprecated Deprecated for removal since 0.17: Use <outputStyle> instead.
     */
    @Deprecated
    @Parameter(property = "rat.stylesheet")
    public void setStylesheet(final String stylesheet) {
        argumentTracker.setArg("stylesheet", stylesheet);
    }



    /**
     * forces XML output rather than the textual report.
     * @param xml The state
     * @deprecated Deprecated for removal since 0.17: Use <outputStyle> with the 'xml' argument instead.
     */
    @Deprecated
    @Parameter(property = "rat.xml")
    public void setXml(final boolean xml) {
        if (xml) {
            argumentTracker.setArg("xml", null);
        } else {
            argumentTracker.removeArg("xml");
        }
    }

}
