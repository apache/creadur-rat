/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   https://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.Defaults;
import org.apache.rat.commandline.Arg;
import org.apache.rat.utils.Log;

/**
 * A collection of options supported by the UI. This includes RAT options and UI specific options.
 * @param <T> the UIOption implementation.
 */
public class UIOptionCollection<T extends UIOption<T>> {
    /** map of ARG to the associated UpdatableOptionGroup */
    private final Map<Arg, UpdatableOptionGroup> argMap;
    /** set of RAT OptionGroups with unsupported options for this UI removed */
    private final UpdatableOptionGroupCollection supportedRatOptions;
    /** set of UI specific options */
    private final Map<Option, T> uiOptions;
    /**
     * Map of option to overridden default value. Generally applies to supported RAT options but may be
     * UI-specific options as well.
     */
    private final Map <Option, String> defaultValues;

    /**
     * Construct the UIOptionCollection from the builder.
     * @param builder the Builder to build from.
     */
    protected UIOptionCollection(final Builder<T, ?> builder) {
        Objects.requireNonNull(builder.uiOptionBuilderSupplier, "Builder.mapper");
        argMap = new TreeMap<>();
        supportedRatOptions = new UpdatableOptionGroupCollection();

        for (Arg arg : Arg.values()) {
            argMap.put(arg, supportedRatOptions.add(arg.group()));
        }

        for (Option opt : builder.unsupportedRatOptions) {
            supportedRatOptions.findGroups(opt).forEach(group -> group.disableOption(opt));
        }
        uiOptions = new HashMap<>();
        UIOption.Builder<T, ?> optBuilder = builder.uiOptionBuilderSupplier.get();
        optBuilder.optionCollection(this);
        supportedRatOptions.options().getOptions()
                .forEach(option -> uiOptions.put(option, optBuilder.option(option).build()));
        builder.uiOptions.stream().filter(option -> !uiOptions.containsKey(option))
                .forEach(option -> uiOptions.put(option, optBuilder.option(option).build()));
        defaultValues = new HashMap<>(builder.defaultValues);
    }

    /**
     * Extracts the UI based name string for an option.
     * The default implementation return the string returned by {@link ArgumentTracker#extractKey(Option)}
     *
     * @param option the option to create the name for.
     * @return the name for the option in the UICollection name, may be the same as the option name.
     */
    public String rename(final Option option) {
        return ArgumentTracker.extractKey(option);
    }

    /**
     * Checks if an {@link Arg} is selected.
     * @param arg the Arg to check.
     * @return {@code true} if the arg is selected.
     */
    public final boolean isSelected(final Arg arg) {
        UpdatableOptionGroup group = argMap.get(arg);
        return group != null && group.getSelected() != null;
    }

    /**
     * Gets the selected Option for an Arg.
     * @param arg the Arg to get the Option for..
     * @return an Optional containing the selected option, or an empty Optional if none was selected.
     */
    public final Optional<Option> getSelected(final Arg arg) {
        UpdatableOptionGroup group = argMap.get(arg);
        String s = group == null ? null : group.getSelected();
        if (s != null) {
            for (Option result : group.getOptions()) {
                if (result.getKey().equals(s)) {
                    return Optional.of(result);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Resets the groups in the Args so that they are unused and ready to detect the next set of arguments.
     */
    public void resetSelected() {
        argMap.values().forEach(uog -> {
            try {
                uog.setSelected(null);
            } catch (AlreadySelectedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Gets the collection of unsupported Options.
     * @return the Options comprising the unsupported options.
     */
    public final Options getUnsupportedOptions() {
        return supportedRatOptions.unsupportedOptions();
    }

    /**
     * Gets the UIOption instance for the Option.
     * @param option the option to find the instance of.
     * @return an Optional containing the UIOption for the option, or an empty Optional if the option is not in the UI.
     */
    public final Optional<T> getMappedOption(final Option option) {
        return Optional.ofNullable(uiOptions.get(option));
    }

    /**
     * Gets an Options that contains the Options and OptionGroups are understood by this collection.
     * OptionGroups are registered in the resulting Options object.
     * @return an Options that contains the {@link Arg} defined Option instances that are understood by this collection as well as
     * any additional custom Options.
     */
    public final Options getOptions() {
        return supportedRatOptions.options().addOptions(additionalOptions());
    }

    /**
     * Gets the Stream of UIOption implementations understood by this collection.
     * @return the Stream of UIOption implementations understood by this collection.
     */
    public final Stream<T> getMappedOptions() {
        return uiOptions.values().stream();
    }

    /**
     * Gets a map client option name to the specified UIOption implementation.
     * @return a map client option name to the specified UIOption implementation.
     */
    public final Map<String, T> getOptionMap() {
        Map<String, T> result = new TreeMap<>();
        getMappedOptions().forEach(mappedOption -> result.put(ArgumentTracker.extractKey(mappedOption.getOption()), mappedOption));
        return result;
    }

    /**
     * Gets the additional Options understood by this collection.
     * @return the additional Options understood by this collection.
     */
    public final Options additionalOptions() {
        Options options = new Options();
        uiOptions.keySet().stream()
                .filter(option -> !supportedRatOptions.contains(option))
                .forEach(options::addOption);
        return options;
    }

    /**
     * Gets the default value for the option.
     * @param option the option to lookup.
     * @return the default value or {@code null} if not set.
     */
    public final String defaultValue(final Option option) {
        return defaultValues.get(option);
    }

    /**
     * Gets the description for the Option.
     * Default implementation is based on the description in the option itself as well as the deprecated state.
     * @param option the option to get the description for.
     * @return the description of the option.
     */
    public String getDescription(final Option option) {
        StringBuilder desc = new StringBuilder();
        getMappedOption(option).ifPresent(uiOption -> {
            if (uiOption.isDeprecated()) {
                desc.append("[").append(uiOption.getDeprecated()).append("] ");
            }
            desc.append(StringUtils.defaultIfEmpty(uiOption.getDescription(), ""));
        });
        return desc.toString();
    }

    /**
     * Builder for a UIOptionCollection.
     * @param <T> the concreate type of the UIOption.
     * @param <B> the concrete type the Builder.
     */
    protected static class Builder<T extends UIOption<T>, B extends Builder<T, B>> {
        /** Set of additional UI specific options. */
        private final List<Option> uiOptions;
        /**
         * Map of option to overridden default value. Generally applies to supported RAT options but may be
         * UI-specific options as well.
         */
        private final Map <Option, String> defaultValues;
        /** The list of unsupported RAT options. */
        protected final List<Option> unsupportedRatOptions;
        /** The function to convert an option into a UIOption. */
        private final Supplier<UIOption.Builder<T, ?>> uiOptionBuilderSupplier;

        /**
         * Constructor for the UI option collection builder.
         */
        protected Builder(final Supplier<UIOption.Builder<T, ?>> uiOptionBiulderSupplier) {
            this.uiOptionBuilderSupplier = uiOptionBiulderSupplier;
            uiOptions = new ArrayList<>();
            defaultValues = new HashMap<>();
            unsupportedRatOptions = new ArrayList<>();
            defaultValue(Arg.LOG_LEVEL,  Log.Level.WARN.name());
            defaultValue(Arg.OUTPUT_ARCHIVE, Defaults.ARCHIVE_PROCESSING.name());
            defaultValue(Arg.OUTPUT_STANDARD, Defaults.STANDARD_PROCESSING.name());
            defaultValue(Arg.OUTPUT_LICENSES, Defaults.LIST_LICENSES.name());
            defaultValue(Arg.OUTPUT_FAMILIES, Defaults.LIST_FAMILIES.name());
        }

        /**
         * Returns this cast to {@code <B>} class.
         * @return this as {@code <B>} class.
         */
        protected final B self() {
            return (B) this;
        }

        /**
         * Add an Option to the collection as a UIOption.
         * @param option the Option to add.
         * @return this
         */
        public B uiOption(final Option option) {
            uiOptions.add(option);
            return self();
        }

        /**
         * Add multiple Option instances to the collection as UIOptions.
         * @param options the Option instances to add.
         * @return this
         */
        public B uiOptions(final Option... options) {
            uiOptions.addAll(Arrays.asList(options));
            return self();
        }

        /**
         * Register an Option as unsupported.
         * @param option the Option that is not be supported. This should be an option in the
         * {@link Arg} collection.
         * @return this
         */
        public B unsupported(final Option option) {
            unsupportedRatOptions.add(option);
            return self();
        }

        /**
         * Register multiple Options as unsupported.
         * Will ignore all the options associated with the specified Arg.
         * @param arg The Arg to ignore.
         * @return this
         */
        public B unsupported(final Arg arg) {
            unsupportedRatOptions.addAll(arg.group().getOptions());
            return self();
        }

        /**
         * Specify the default values for an option.
         * @param option the option to specify the default value for.
         * @param value the value for the option.
         * @return this
         */
        public B defaultValue(final Option option, final String value) {
            defaultValues.put(option, value);
            return self();
        }

        /**
         * Specify the default values for an Arg.
         * @param arg the Arg to specify the default value for.
         * @param value the value for the option.
         * @return this
         */
        public B defaultValue(final Arg arg, final String value) {
            return defaultValue(arg.option(), value);
        }
    }
}
