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
package org.apache.rat.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.DeprecationReporter;
import org.apache.rat.commandline.Arg;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

/**
 * Tracks arguments that are set and their values for conversion from native UI to
 * Apache commons command line values.  Native values
 */
public final class ArgumentTracker {

    /**
     * List of deprecated arguments and their deprecation notice.
     */
    private final Map<String, String> deprecatedArgs = new HashMap<>();

    /**
     * A map of CLI-based arguments to values.
     */
    private final Map<String, List<String>> args = new HashMap<>();

    /**
     * The arguments set by the UI for the current report execution.
     * @param uiOptionList the list of AbstractOption implementations for this UI.
     */
    public ArgumentTracker(final List<? extends AbstractOption<?>> uiOptionList) {
        for (AbstractOption<?> abstractOption : uiOptionList) {
            if (abstractOption.isDeprecated()) {
                deprecatedArgs.put(abstractOption.getName(),
                        String.format("Use of deprecated option '%s'. %s", abstractOption.getName(), abstractOption.getDeprecated()));
            }
        }
    }

    /**
     * Extract the core name from the option.  This is the {@link Option#getLongOpt()} if defined, otherwise
     * the {@link Option#getOpt()}.
     * @param option the commons cli option.
     * @return the common cli based name.
     */
    public static String extractKey(final Option option) {
        return StringUtils.defaultIfBlank(option.getLongOpt(), option.getOpt());
    }

    public Set<Map.Entry<String, List<String>>> entrySet() {
        return args.entrySet();
    }

    /**
     * Sets the deprecation report method.
     */
    private void setDeprecationReporter() {
        DeprecationReporter.setLogReporter(opt -> {
            String msg = deprecatedArgs.get(extractKey(opt));
            if (msg == null) {
                DeprecationReporter.getDefault().accept(opt);
            } else {
                DefaultLog.getInstance().warn(msg);
            }
        });
    }

    /**
     * Gets the list of arguments prepared for the CLI code to parse.
     * @return the List of arguments for the CLI command line.
     */
    public List<String> args() {
        final List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : args.entrySet()) {
            result.add("--" + entry.getKey());
            result.addAll(entry.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return result;
    }

    private boolean validateSet(final String key) {
        final Arg arg = Arg.findArg(key);
        if (arg != null) {
            final Option opt = arg.find(key);
            final Option main = arg.option();
            if (opt.isDeprecated()) {
                args.remove(extractKey(main));
                // deprecated options must be explicitly set so let it go.
                return true;
            }
            // non-deprecated options may have default so ignore it if another option has already been set.
            for (Option o : arg.group().getOptions()) {
                if (!o.equals(main)) {
                    if (args.containsKey(extractKey(o))) {
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
    public void setArg(final String key, final String value) {
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
    public List<String> getArg(final String key) {
        return args.get(key);
    }

    /**
     * Add values to the key in the argument list.
     * empty values are ignored. If no non-empty values are present no change is made.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the array of values to set.
     */
    public void addArg(final String key, final String[] value) {
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
    public void addArg(final String key, final String value) {
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
    public void removeArg(final String key) {
        args.remove(key);
    }
}
