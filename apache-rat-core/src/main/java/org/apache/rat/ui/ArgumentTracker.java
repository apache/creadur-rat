/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.DeprecationReporter;
import org.apache.rat.commandline.Arg;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

/**
 * Tracks Arg values that are set and their values for conversion from native UI to
 * Apache Commons command line values.
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
     * The arguments understood by the UI for the current report execution.
     * @param optionCollection The AbstractOptionCollection for this UI.
     */
    public ArgumentTracker(final UIOptionCollection<?> optionCollection) {
        for (UIOption<?> abstractOption : optionCollection.getMappedOptions().toList()) {
            if (abstractOption.isDeprecated()) {
                deprecatedArgs.put(abstractOption.getName(),
                        String.format("Use of deprecated option '%s'. %s", abstractOption.getName(), abstractOption.getDeprecated()));
            }
        }
        setDeprecationReporter();
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

    /**
     * Generates the CasedString for the specified option.
     * @param option the option to extract the name for.
     * @return the CasedString in KEBAB format.
     */
    public static CasedString extractName(final Option option) {
        return new CasedString(CasedString.StringCase.KEBAB, ArgumentTracker.extractKey(option));
    }

    /**
     * Sets the deprecation report method in the Apache Commons CLI processes.
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
            result.addAll(entry.getValue().stream().filter(Objects::nonNull).toList());
        }
        return result;
    }

    /**
     * Applies the consumer to each arg and list in turn.
     */
    public void apply(final BiConsumer<String, List<String>> consumer) {
        args.forEach((key, value) -> consumer.accept(key, new ArrayList<>(value)));
    }

    /**
     * Validate that the option is defined in Args and has not already been set.
     * This check will verify tha only one of the keys in the group can be set.
     * @param key the key to check
     * @return true if the key may be set.
     */
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
    public void setArg(final UIOption<?> key, final String value) {
        setArg(key.keyValue(), value);
    }

    /**
     * Set a key and value into the argument list.
     * Replaces any existing value.
     * @param trackerKey the key for the map.
     * @param value the value to set.
     */
    public void setArg(final String trackerKey, final String value) {
        if (value == null || StringUtils.isNotBlank(value)) {
            if (validateSet(trackerKey)) {
                Option ratOption = Arg.findArg(trackerKey).find(trackerKey);
                if (ratOption.hasArg()) {
                    List<String> values = new ArrayList<>();
                    if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
                        DefaultLog.getInstance().debug(String.format("Setting %s to '%s'", trackerKey, value));
                    }
                    values.add(value);
                    args.put(trackerKey, values);
                } else {
                    DefaultLog.getInstance().warn(String.format("Key '%s' does not accept arguments.", trackerKey));
                }
            } else {
                DefaultLog.getInstance().warn(String.format("Key '%s' is unknown", trackerKey));
            }
        }
    }

    /**
     * Get the list of values for a key.
     * @param key the key for the map.
     * @return the list of values for the key or {@code null} if not set.
     */
    public Optional<List<String>> getArg(final String key) {
        return Optional.ofNullable(args.get(key));
    }

    /**
     * Add values to the key in the argument list.
     * empty values are ignored. If no non-empty values are present no change is made.
     * If the key does not exist, adds it.
     * @param option the option to add values for.
     * @param value the array of values to set.
     */
    public void addArg(final UIOption<?> option, final String... value) {
        addArg(option.keyValue(), value);
    }

    /**
     * Add values to the key in the argument list.
     * empty values are ignored. If no non-empty values are present no change is made.
     * If the key does not exist, adds it.
     * @param trackerKey the key add values for.
     * @param value the array of values to set.
     */
    public void addArg(final String trackerKey, final String... value) {
        List<String> newValues = Arrays.stream(value).filter(StringUtils::isNotBlank).toList();
        if (newValues.isEmpty()) {
            return;
        }
        if (!validateSet(trackerKey)) {
            DefaultLog.getInstance().warn(String.format("Key '%s' is unknown", trackerKey));
            return;
        }
        Option ratOption = Arg.findArg(trackerKey).find(trackerKey);
        if (!ratOption.hasArgs()) {
            DefaultLog.getInstance().warn(String.format("Key '%s' does not accept %sarguments.", trackerKey,
                    ratOption.hasArg() ? "more that one " : ""));
        }
        if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
            DefaultLog.getInstance().debug(String.format("Adding [%s] to %s", String.join(", ", Arrays.asList(value)), trackerKey));
        }
        List<String> values = args.computeIfAbsent(trackerKey, k -> new ArrayList<>());
        values.addAll(newValues);
    }

    /**
     * Remove a key from the argument list.
     * @param option the option to remove the key for.
     */
    public void removeArg(final UIOption<?> option) {
        args.remove(option.keyValue());
    }

    /**
     * Remove a key from the argument list.
     * @param trackerKey the key remove.
     */
    public void removeArg(final String trackerKey) {
        args.remove(trackerKey);
    }
}
