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

package org.apache.rat.plugin;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.rat.commandline.Arg;
import org.apache.rat.DeprecationReporter;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/* DO NOT EDIT - GENERATED FILE */

/**
 * Generated class to provide Maven support for standard RAT command line options
 */
public abstract class BaseRatMojo extends AbstractMojo {

    private static final Map<String, String> xlateName = new HashMap<>();

    private static final List<String> unsupportedArgs = new ArrayList<>();

    private static final Map<String, String> deprecatedArgs = new HashMap<>();

    static {
        xlateName.put("addLicense", "add-license");
        unsupportedArgs.add("help");
        unsupportedArgs.add("dir");
        unsupportedArgs.add("log-level");
        deprecatedArgs.put("copyright", "Use of deprecated option 'copyright'. Deprecated for removal since 0.17: Use <editCopyright> instead.");
        deprecatedArgs.put("force", "Use of deprecated option 'force'. Deprecated for removal since 0.17: Use <editOverwrite> instead.");
        deprecatedArgs.put("addLicense", "Use of deprecated option 'addLicense'. Deprecated for removal since 0.17: Use <editLicense> instead.");
        deprecatedArgs.put("licenses", "Use of deprecated option 'licenses'. Deprecated for removal since 0.17: Use <config> instead.");
        deprecatedArgs.put("no-default-licenses", "Use of deprecated option 'noDefaultLicenses'. Deprecated for removal since 0.17: Use <configurationNoDefaults> instead.");
        deprecatedArgs.put("exclude", "Use of deprecated option 'exclude'. Deprecated for removal since 0.17: Use <inputExclude> instead.");
        deprecatedArgs.put("exclude-file", "Use of deprecated option 'excludeFile'. Deprecated for removal since 0.17: Use <inputExcludeFile> instead.");
        deprecatedArgs.put("include", "Use of deprecated option 'include'. Deprecated for removal since 0.17: Use <inputInclude> instead.");
        deprecatedArgs.put("includes-file", "Use of deprecated option 'includesFile'. Deprecated for removal since 0.17: Use <inputIncludeFile> instead.");
        deprecatedArgs.put("scan-hidden-directories", "Use of deprecated option 'scanHiddenDirectories'. Deprecated for removal since 0.17: Use <inputIncludeStd> with 'HIDDEN_DIR' argument instead.");
        deprecatedArgs.put("stylesheet", "Use of deprecated option 'stylesheet'. Deprecated for removal since 0.17: Use <outputStyle> instead.");
        deprecatedArgs.put("xml", "Use of deprecated option 'xml'. Deprecated for removal since 0.17: Use <outputStyle> with the 'xml' argument instead.");
        deprecatedArgs.put("list-licenses", "Use of deprecated option 'listLicenses'. Deprecated for removal since 0.17: Use <outputLicenses> instead.");
        deprecatedArgs.put("list-families", "Use of deprecated option 'listFamilies'. Deprecated for removal since 0.17: Use <outputFamilies> instead.");
        deprecatedArgs.put("out", "Use of deprecated option 'out'. Deprecated for removal since 0.17: Use <outputFile> instead.");
    }

    /**
     * Creates a Maven name from a long option.
     * Will map excluded long options to null.
     * @param longOpt the kebab name.
     * @return The CamelCased name for Maven use.
     */
    public static String createName(String longOpt) {
        String name = xlateName.get(longOpt);
        return name != null ? name : new CasedString(CasedString.StringCase.KEBAB, longOpt).toCase(CasedString.StringCase.CAMEL);
    }

    /**
     * Creates a kebab case name from a camel case name.
     * @param camelCase the camel case name to convert.
     * @return the kebab format.
     */
    public static String toKebabForm(String camelCase) {
        return new CasedString(CasedString.StringCase.CAMEL, camelCase).toCase(CasedString.StringCase.KEBAB);
    }

    /**
     * Returns the list of unsupported args.
     * @return the list of kebab style names that are unsupported by the Maven UI.
     */
    public static List<String> unsupportedArgs() {
        return Collections.unmodifiableList(unsupportedArgs);
    }
    
    ///////////////////////// Start common Arg manipulation code

    /**
     * Sets the deprecation report method.
     */
    private static void setDeprecationReporter() {
        DeprecationReporter.setLogReporter(opt -> {
            String msg = deprecatedArgs.get(argsKey(opt));
            if (msg == null) {
                DeprecationReporter.getDefault().accept(opt);
            } else {
                DefaultLog.getInstance().warn(msg);
            }
        });
    }

    private static String argsKey(Option opt) {
        return StringUtils.defaultIfEmpty(opt.getLongOpt(), opt.getKey());
    }

    /**
     * A map of CLI-based arguments to values.
     */
    protected final Map<String, List<String>> args = new HashMap<>();

    /**
     * Gets the list of arguments prepared for the CLI code to parse.
     * @return the List of arguments for the CLI command line.
     */
    protected List<String> args() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : args.entrySet()) {
            result.add("--" + entry.getKey());
            result.addAll(entry.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return result;
    }

    private boolean validateSet(String key) {
        Arg arg = Arg.findArg(key);
        if (arg != null) {
            Option opt = arg.find(key);
            Option main = arg.option();
            if (opt.isDeprecated()) {
                args.remove(argsKey(main));
                // deprecated options must be explicitly set so let it go.
                return true;
            }
            // non-deprecated options may have default so ignore it if another option has already been set.
            for (Option o : arg.group().getOptions()) {
                if (!o.equals(main)) {
                    if (args.containsKey(argsKey(o))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Set a key and value into the argument list.
     * Replaces any existing value.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void setArg(String key, String value) {
        if (value == null || StringUtils.isNotBlank(value)) {
            if (validateSet(key)) {
                List<String> values = new ArrayList<>();
                if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
                    DefaultLog.getInstance().debug(String.format("Setting %s to '%s'", key, value));
                }
                values.add(value);
                args.put(key, values);
            }
        }
    }

    /**
     * Get the list of values for a key.
     * @param key the key for the map.
     * @return the list of values for the key or {@code null} if not set.
     */
    public List<String> getArg(String key) {
        return args.get(key);
    }

    /**
     * Add values to the key in the argument list.
     * empty values are ignored. If no non-empty values are present no change is made.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the array of values to set.
     */
    protected void addArg(String key, String[] value) {
        List<String> newValues = Arrays.stream(value).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (!newValues.isEmpty()) {
            if (validateSet(key)) {
                if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
                    DefaultLog.getInstance().debug(String.format("Adding [%s] to %s", String.join(", ", Arrays.asList(value)), key));
                }
                List<String> values = args.computeIfAbsent(key, k -> new ArrayList<>());
                values.addAll(newValues);
            }
        }
    }

    /**
     * Add a value to the key in the argument list.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void addArg(String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            if (validateSet(key)) {
                List<String> values = args.get(key);
                if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
                    DefaultLog.getInstance().debug(String.format("Adding [%s] to %s", String.join(", ", Arrays.asList(value)), key));
                }
                if (values == null) {
                    values = new ArrayList<>();
                    args.put(key, values);
                }
                values.add(value);
            }
        }
    }

    /**
     * Remove a key from the argument list.
     * @param key the key to remove from the map.
     */
    protected void removeArg(String key) {
        args.remove(key);
    }

 ///////////////////////// End common Arg manipulation code

    protected BaseRatMojo() {
        setDeprecationReporter();
    }

    /*  GENERATED METHODS */


    /**
     * The copyright message to use in the license headers. Argument should be a Arg. (See Argument Types for clarification)
     * @param copyright Copyright message to use in the license headers.
     * @deprecated Deprecated for removal since 0.17: Use &lt;editCopyright&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.Copyright")
    public void setCopyright(String copyright) {
        setArg("copyright", copyright);
    }
    /**
     * The copyright message to use in the license headers. Usually in the form of &quot;Copyright 2008 Foo&quot;.  Only valid with &lt;editLicense&gt; Argument should be a Arg. (See Argument Types for clarification)
     * @param editCopyright Copyright message to use in the license headers.
     */
    @Parameter(property = "rat.EditCopyright")
    public void setEditCopyright(String editCopyright) {
        setArg("edit-copyright", editCopyright);
    }
    /**
     * Forces any changes in files to be written directly to the source files so that new files are not created.
     * @param force The state
     * @deprecated Deprecated for removal since 0.17: Use &lt;editOverwrite&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.Force")
    public void setForce(boolean force) {
        if (force) {
            setArg("force", null);
        } else {
            removeArg("force");
        }
    }
    /**
     * Forces any changes in files to be written directly to the source files so that new files are not created. Only valid with &lt;editLicense&gt;.
     * @param editOverwrite The state
     */
    @Parameter(property = "rat.EditOverwrite")
    public void setEditOverwrite(boolean editOverwrite) {
        if (editOverwrite) {
            setArg("edit-overwrite", null);
        } else {
            removeArg("edit-overwrite");
        }
    }
    /**
     * Add the Apache-2.0 license header to any file with an unknown license that is not in the exclusion list.
     * @param addLicense The state
     * @deprecated Deprecated for removal since 0.17: Use &lt;editLicense&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.AddLicense")
    public void setAddLicense(boolean addLicense) {
        if (addLicense) {
            setArg("addLicense", null);
        } else {
            removeArg("addLicense");
        }
    }
    /**
     * Add the Apache-2.0 license header to any file with an unknown license that is not in the exclusion list. By default new files will be created with the license header, to force the modification of existing files use the &lt;editOverwrite&gt; option.
     * @param editLicense The state
     */
    @Parameter(property = "rat.EditLicense")
    public void setEditLicense(boolean editLicense) {
        if (editLicense) {
            setArg("edit-license", null);
        } else {
            removeArg("edit-license");
        }
    }
    /**
     * File names for system configuration. Arguments should be File. (See Argument Types for clarification)
     * @param config Names for system configuration.
     */
    @Parameter
    public void setConfigs(String[] config) {
        addArg("config", config);
    }
    /**
     * File names for system configuration. Arguments should be File. (See Argument Types for clarification)
     * @param config Names for system configuration.
     */
    @Parameter
    public void setConfig(String config) {
        addArg("config", config);
    }
    /**
     * File names for system configuration. Arguments should be File. (See Argument Types for clarification)
     * @param licenses Names for system configuration.
     * @deprecated Deprecated for removal since 0.17: Use &lt;config&gt; instead.
     */
    @Deprecated
    @Parameter
    public void setLicenses(String[] licenses) {
        addArg("licenses", licenses);
    }
    /**
     * File names for system configuration. Arguments should be File. (See Argument Types for clarification)
     * @param licenses Names for system configuration.
     * @deprecated Deprecated for removal since 0.17: Use &lt;config&gt; instead.
     */
    @Deprecated
    @Parameter
    public void setLicenses(String licenses) {
        addArg("licenses", licenses);
    }
    /**
     * Ignore default configuration.
     * @param configurationNoDefaults The state
     */
    @Parameter(property = "rat.ConfigurationNoDefaults")
    public void setConfigurationNoDefaults(boolean configurationNoDefaults) {
        if (configurationNoDefaults) {
            setArg("configuration-no-defaults", null);
        } else {
            removeArg("configuration-no-defaults");
        }
    }
    /**
     * Ignore default configuration.
     * @param noDefaultLicenses The state
     * @deprecated Deprecated for removal since 0.17: Use &lt;configurationNoDefaults&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.NoDefaultLicenses")
    public void setNoDefaultLicenses(boolean noDefaultLicenses) {
        if (noDefaultLicenses) {
            setArg("no-default-licenses", null);
        } else {
            removeArg("no-default-licenses");
        }
    }
    /**
     * A comma separated list of approved License IDs. These licenses will be added to the list of approved licenses. Argument should be a LicenseID. (See Argument Types for clarification)
     * @param licensesApproved Comma separated list of approved License IDs.
     */
    @Parameter(property = "rat.LicensesApproved")
    public void setLicensesApproved(String licensesApproved) {
        setArg("licenses-approved", licensesApproved);
    }
    /**
     * Name of file containing comma separated lists of approved License IDs. Argument should be a File. (See Argument Types for clarification)
     * @param licensesApprovedFile Of file containing comma separated lists of approved License IDs.
     */
    @Parameter(property = "rat.LicensesApprovedFile")
    public void setLicensesApprovedFile(String licensesApprovedFile) {
        setArg("licenses-approved-file", licensesApprovedFile);
    }
    /**
     * A comma separated list of approved license family IDs. These license families will be added to the list of approved license families. Argument should be a FamilyID. (See Argument Types for clarification)
     * @param licenseFamiliesApproved Comma separated list of approved license family IDs.
     */
    @Parameter(property = "rat.LicenseFamiliesApproved")
    public void setLicenseFamiliesApproved(String licenseFamiliesApproved) {
        setArg("license-families-approved", licenseFamiliesApproved);
    }
    /**
     * Name of file containing comma separated lists of approved family IDs. Argument should be a File. (See Argument Types for clarification)
     * @param licenseFamiliesApprovedFile Of file containing comma separated lists of approved family IDs.
     */
    @Parameter(property = "rat.LicenseFamiliesApprovedFile")
    public void setLicenseFamiliesApprovedFile(String licenseFamiliesApprovedFile) {
        setArg("license-families-approved-file", licenseFamiliesApprovedFile);
    }
    /**
     * A comma separated list of denied License IDs. These licenses will be removed from the list of approved licenses. Once licenses are removed they can not be added back. Argument should be a LicenseID. (See Argument Types for clarification)
     * @param licensesDenied Comma separated list of denied License IDs.
     */
    @Parameter(property = "rat.LicensesDenied")
    public void setLicensesDenied(String licensesDenied) {
        setArg("licenses-denied", licensesDenied);
    }
    /**
     * Name of file containing comma separated lists of the denied license IDs. These licenses will be removed from the list of approved licenses. Once licenses are removed they can not be added back. Argument should be a File. (See Argument Types for clarification)
     * @param licensesDeniedFile Of file containing comma separated lists of the denied license IDs.
     */
    @Parameter(property = "rat.LicensesDeniedFile")
    public void setLicensesDeniedFile(String licensesDeniedFile) {
        setArg("licenses-denied-file", licensesDeniedFile);
    }
    /**
     * A comma separated list of denied License family IDs. These license families will be removed from the list of approved licenses. Once license families are removed they can not be added back. Argument should be a FamilyID. (See Argument Types for clarification)
     * @param licenseFamiliesDenied Comma separated list of denied License family IDs.
     */
    @Parameter(property = "rat.LicenseFamiliesDenied")
    public void setLicenseFamiliesDenied(String licenseFamiliesDenied) {
        setArg("license-families-denied", licenseFamiliesDenied);
    }
    /**
     * Name of file containing comma separated lists of denied license IDs. These license families will be removed from the list of approved licenses. Once license families are removed they can not be added back. Argument should be a File. (See Argument Types for clarification)
     * @param licenseFamiliesDeniedFile Of file containing comma separated lists of denied license IDs.
     */
    @Parameter(property = "rat.LicenseFamiliesDeniedFile")
    public void setLicenseFamiliesDeniedFile(String licenseFamiliesDeniedFile) {
        setArg("license-families-denied-file", licenseFamiliesDeniedFile);
    }
    /**
     * The acceptable maximum number for the specified counter. A value of '-1' specifies an unlimited number. Arguments should be CounterPattern. (See Argument Types for clarification)
     * @param counterMax Acceptable maximum number for the specified counter.
     */
    @Parameter
    public void setCounterMaxs(String[] counterMax) {
        addArg("counter-max", counterMax);
    }
    /**
     * The acceptable maximum number for the specified counter. A value of '-1' specifies an unlimited number. Arguments should be CounterPattern. (See Argument Types for clarification)
     * @param counterMax Acceptable maximum number for the specified counter.
     */
    @Parameter
    public void setCounterMax(String counterMax) {
        addArg("counter-max", counterMax);
    }
    /**
     * The minimum number for the specified counter. Arguments should be CounterPattern. (See Argument Types for clarification)
     * @param counterMin Minimum number for the specified counter.
     */
    @Parameter
    public void setCounterMins(String[] counterMin) {
        addArg("counter-min", counterMin);
    }
    /**
     * The minimum number for the specified counter. Arguments should be CounterPattern. (See Argument Types for clarification)
     * @param counterMin Minimum number for the specified counter.
     */
    @Parameter
    public void setCounterMin(String counterMin) {
        addArg("counter-min", counterMin);
    }
    /**
     * A file containing file names to process. File names must use linux directory separator ('/') or none at all. File names that do not start with '/' are relative to the directory where the argument is located. Arguments should be File. (See Argument Types for clarification)
     * @param inputSource File containing file names to process.
     */
    @Parameter
    public void setInputSources(String[] inputSource) {
        addArg("input-source", inputSource);
    }
    /**
     * A file containing file names to process. File names must use linux directory separator ('/') or none at all. File names that do not start with '/' are relative to the directory where the argument is located. Arguments should be File. (See Argument Types for clarification)
     * @param inputSource File containing file names to process.
     */
    @Parameter
    public void setInputSource(String inputSource) {
        addArg("input-source", inputSource);
    }
    /**
     * Excludes files matching &lt;Expression&gt;. Arguments should be Expression. (See Argument Types for clarification)
     * @param exclude Files matching &lt;Expression&gt;.
     * @deprecated Deprecated for removal since 0.17: Use &lt;inputExclude&gt; instead.
     */
    @Deprecated
    @Parameter
    public void setExcludes(String[] exclude) {
        addArg("exclude", exclude);
    }
    /**
     * Excludes files matching &lt;Expression&gt;. Arguments should be Expression. (See Argument Types for clarification)
     * @param exclude Files matching &lt;Expression&gt;.
     * @deprecated Deprecated for removal since 0.17: Use &lt;inputExclude&gt; instead.
     */
    @Deprecated
    @Parameter
    public void setExclude(String exclude) {
        addArg("exclude", exclude);
    }
    /**
     * Excludes files matching &lt;Expression&gt;. Arguments should be Expression. (See Argument Types for clarification)
     * @param inputExclude Files matching &lt;Expression&gt;.
     */
    @Parameter
    public void setInputExcludes(String[] inputExclude) {
        addArg("input-exclude", inputExclude);
    }
    /**
     * Excludes files matching &lt;Expression&gt;. Arguments should be Expression. (See Argument Types for clarification)
     * @param inputExclude Files matching &lt;Expression&gt;.
     */
    @Parameter
    public void setInputExclude(String inputExclude) {
        addArg("input-exclude", inputExclude);
    }
    /**
     * Reads &lt;Expression&gt; entries from a file. Entries will be excluded from processing. Argument should be a File. (See Argument Types for clarification)
     * @param excludeFile &lt;Expression&gt; entries from a file.
     * @deprecated Deprecated for removal since 0.17: Use &lt;inputExcludeFile&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.ExcludeFile")
    public void setExcludeFile(String excludeFile) {
        setArg("exclude-file", excludeFile);
    }
    /**
     * Reads &lt;Expression&gt; entries from a file. Entries will be excluded from processing. Argument should be a File. (See Argument Types for clarification)
     * @param inputExcludeFile &lt;Expression&gt; entries from a file.
     */
    @Parameter(property = "rat.InputExcludeFile")
    public void setInputExcludeFile(String inputExcludeFile) {
        setArg("input-exclude-file", inputExcludeFile);
    }
    /**
     * Excludes files defined in standard collections based on commonly occurring groups. Excludes any path matcher actions but DOES NOT exclude any file processor actions. Arguments should be StandardCollection. (See Argument Types for clarification)
     * @param inputExcludeStd Files defined in standard collections based on commonly occurring groups.
     */
    @Parameter
    public void setInputExcludeStds(String[] inputExcludeStd) {
        addArg("input-exclude-std", inputExcludeStd);
    }
    /**
     * Excludes files defined in standard collections based on commonly occurring groups. Excludes any path matcher actions but DOES NOT exclude any file processor actions. Arguments should be StandardCollection. (See Argument Types for clarification)
     * @param inputExcludeStd Files defined in standard collections based on commonly occurring groups.
     */
    @Parameter
    public void setInputExcludeStd(String inputExcludeStd) {
        addArg("input-exclude-std", inputExcludeStd);
    }
    /**
     * Excludes files with sizes less than the number of bytes specified. Argument should be a Integer. (See Argument Types for clarification)
     * @param inputExcludeSize Files with sizes less than the number of bytes specified.
     */
    @Parameter(property = "rat.InputExcludeSize")
    public void setInputExcludeSize(String inputExcludeSize) {
        setArg("input-exclude-size", inputExcludeSize);
    }
    /**
     * Includes files matching &lt;Expression&gt;. Will override excluded files. Arguments should be Expression. (See Argument Types for clarification)
     * @param inputInclude Files matching &lt;Expression&gt;.
     */
    @Parameter
    public void setInputIncludes(String[] inputInclude) {
        addArg("input-include", inputInclude);
    }
    /**
     * Includes files matching &lt;Expression&gt;. Will override excluded files. Arguments should be Expression. (See Argument Types for clarification)
     * @param inputInclude Files matching &lt;Expression&gt;.
     */
    @Parameter
    public void setInputInclude(String inputInclude) {
        addArg("input-include", inputInclude);
    }
    /**
     * Includes files matching &lt;Expression&gt;. Will override excluded files. Arguments should be Expression. (See Argument Types for clarification)
     * @param include Files matching &lt;Expression&gt;.
     * @deprecated Deprecated for removal since 0.17: Use &lt;inputInclude&gt; instead.
     */
    @Deprecated
    @Parameter
    public void setIncludes(String[] include) {
        addArg("include", include);
    }
    /**
     * Includes files matching &lt;Expression&gt;. Will override excluded files. Arguments should be Expression. (See Argument Types for clarification)
     * @param include Files matching &lt;Expression&gt;.
     * @deprecated Deprecated for removal since 0.17: Use &lt;inputInclude&gt; instead.
     */
    @Deprecated
    @Parameter
    public void setInclude(String include) {
        addArg("include", include);
    }
    /**
     * Reads &lt;Expression&gt; entries from a file. Entries will override excluded files. Argument should be a File. (See Argument Types for clarification)
     * @param inputIncludeFile &lt;Expression&gt; entries from a file.
     */
    @Parameter(property = "rat.InputIncludeFile")
    public void setInputIncludeFile(String inputIncludeFile) {
        setArg("input-include-file", inputIncludeFile);
    }
    /**
     * Reads &lt;Expression&gt; entries from a file. Entries will be excluded from processing. Argument should be a File. (See Argument Types for clarification)
     * @param includesFile &lt;Expression&gt; entries from a file.
     * @deprecated Deprecated for removal since 0.17: Use &lt;inputIncludeFile&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.IncludesFile")
    public void setIncludesFile(String includesFile) {
        setArg("includes-file", includesFile);
    }
    /**
     * Includes files defined in standard collections based on commonly occurring groups. Includes any path matcher actions but DOES NOT include any file processor actions. Arguments should be StandardCollection. (See Argument Types for clarification)
     * @param inputIncludeStd Files defined in standard collections based on commonly occurring groups.
     */
    @Parameter
    public void setInputIncludeStds(String[] inputIncludeStd) {
        addArg("input-include-std", inputIncludeStd);
    }
    /**
     * Includes files defined in standard collections based on commonly occurring groups. Includes any path matcher actions but DOES NOT include any file processor actions. Arguments should be StandardCollection. (See Argument Types for clarification)
     * @param inputIncludeStd Files defined in standard collections based on commonly occurring groups.
     */
    @Parameter
    public void setInputIncludeStd(String inputIncludeStd) {
        addArg("input-include-std", inputIncludeStd);
    }
    /**
     * Scans hidden directories.
     * @param scanHiddenDirectories The state
     * @deprecated Deprecated for removal since 0.17: Use &lt;inputIncludeStd&gt; with 'HIDDEN_DIR' argument instead.
     */
    @Deprecated
    @Parameter(property = "rat.ScanHiddenDirectories")
    public void setScanHiddenDirectories(boolean scanHiddenDirectories) {
        if (scanHiddenDirectories) {
            setArg("scan-hidden-directories", null);
        } else {
            removeArg("scan-hidden-directories");
        }
    }
    /**
     * Parse SCM based exclusion files to exclude specified files and directories. This action can apply to any standard collection that implements a file processor. Arguments should be StandardCollection. (See Argument Types for clarification)
     * @param inputExcludeParsedScm SCM based exclusion files to exclude specified files and directories.
     */
    @Parameter
    public void setInputExcludeParsedScms(String[] inputExcludeParsedScm) {
        addArg("input-exclude-parsed-scm", inputExcludeParsedScm);
    }
    /**
     * Parse SCM based exclusion files to exclude specified files and directories. This action can apply to any standard collection that implements a file processor. Arguments should be StandardCollection. (See Argument Types for clarification)
     * @param inputExcludeParsedScm SCM based exclusion files to exclude specified files and directories.
     */
    @Parameter
    public void setInputExcludeParsedScm(String inputExcludeParsedScm) {
        addArg("input-exclude-parsed-scm", inputExcludeParsedScm);
    }
    /**
     * XSLT stylesheet to use when creating the report. Either an external xsl file may be specified or one of the internal named sheets. Argument should be a StyleSheet. (See Argument Types for clarification)
     * @param outputStyle Stylesheet to use when creating the report.
     */
    @Parameter(property = "rat.OutputStyle")
    public void setOutputStyle(String outputStyle) {
        setArg("output-style", outputStyle);
    }
    /**
     * XSLT stylesheet to use when creating the report. Argument should be a StyleSheet. (See Argument Types for clarification)
     * @param stylesheet Stylesheet to use when creating the report.
     * @deprecated Deprecated for removal since 0.17: Use &lt;outputStyle&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.Stylesheet")
    public void setStylesheet(String stylesheet) {
        setArg("stylesheet", stylesheet);
    }
    /**
     * forces XML output rather than the textual report.
     * @param xml The state
     * @deprecated Deprecated for removal since 0.17: Use &lt;outputStyle&gt; with the 'xml' argument instead.
     */
    @Deprecated
    @Parameter(property = "rat.Xml")
    public void setXml(boolean xml) {
        if (xml) {
            setArg("xml", null);
        } else {
            removeArg("xml");
        }
    }
    /**
     * List the defined licenses. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param outputLicenses The defined licenses.
     */
    @Parameter(property = "rat.OutputLicenses", defaultValue = "NONE")
    public void setOutputLicenses(String outputLicenses) {
        setArg("output-licenses", outputLicenses);
    }
    /**
     * List the defined licenses. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param listLicenses The defined licenses.
     * @deprecated Deprecated for removal since 0.17: Use &lt;outputLicenses&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.ListLicenses")
    public void setListLicenses(String listLicenses) {
        setArg("list-licenses", listLicenses);
    }
    /**
     * List the defined license families. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param outputFamilies The defined license families.
     */
    @Parameter(property = "rat.OutputFamilies", defaultValue = "NONE")
    public void setOutputFamilies(String outputFamilies) {
        setArg("output-families", outputFamilies);
    }
    /**
     * List the defined license families. Argument should be a LicenseFilter. (See Argument Types for clarification)
     * @param listFamilies The defined license families.
     * @deprecated Deprecated for removal since 0.17: Use &lt;outputFamilies&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.ListFamilies")
    public void setListFamilies(String listFamilies) {
        setArg("list-families", listFamilies);
    }
    /**
     * If set do not update the files but generate the reports.
     * @param dryRun The state
     */
    @Parameter(property = "rat.DryRun")
    public void setDryRun(boolean dryRun) {
        if (dryRun) {
            setArg("dry-run", null);
        } else {
            removeArg("dry-run");
        }
    }
    /**
     * Define the output file where to write a report to. Argument should be a File. (See Argument Types for clarification)
     * @param out The output file where to write a report to.
     * @deprecated Deprecated for removal since 0.17: Use &lt;outputFile&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.Out")
    public void setOut(String out) {
        setArg("out", out);
    }
    /**
     * Define the output file where to write a report to. Argument should be a File. (See Argument Types for clarification)
     * @param outputFile The output file where to write a report to.
     */
    @Parameter(property = "rat.OutputFile", defaultValue = "${project.build.directory}/rat.txt")
    public void setOutputFile(String outputFile) {
        setArg("output-file", outputFile);
    }
    /**
     * Specifies the level of detail in ARCHIVE file reporting. Argument should be a ProcessingType. (See Argument Types for clarification)
     * @param outputArchive The level of detail in ARCHIVE file reporting.
     */
    @Parameter(property = "rat.OutputArchive", defaultValue = "NOTIFICATION")
    public void setOutputArchive(String outputArchive) {
        setArg("output-archive", outputArchive);
    }
    /**
     * Specifies the level of detail in STANDARD file reporting. Argument should be a ProcessingType. (See Argument Types for clarification)
     * @param outputStandard The level of detail in STANDARD file reporting.
     */
    @Parameter(property = "rat.OutputStandard", defaultValue = "ABSENCE")
    public void setOutputStandard(String outputStandard) {
        setArg("output-standard", outputStandard);
    }
    /**
     * Print information about registered licenses.
     * @param helpLicenses The state
     */
    @Parameter(property = "rat.HelpLicenses")
    public void setHelpLicenses(boolean helpLicenses) {
        if (helpLicenses) {
            setArg("help-licenses", null);
        } else {
            removeArg("help-licenses");
        }
    }
}
