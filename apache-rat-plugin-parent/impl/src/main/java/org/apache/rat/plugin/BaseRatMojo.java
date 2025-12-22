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


