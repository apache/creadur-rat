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
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rat.Defaults;
import org.apache.rat.commandline.Arg;
import org.apache.rat.utils.Log;

/**
 * A collection of options supported by the UI.  This includes RAT options and UI specific options.
 * @param <T> the AbstractOption implementation.
 */
public class UIOptionCollection<T extends UIOption<T>> {
    /** map of ARG to the associated UpdatableOptionGroup */
    private final Map<Arg, UpdatableOptionGroup> argMap;
    /** set of RAT OptionGroups with unsupported options for this UI removed */
    private final UpdatableOptionGroupCollection supportedRatOptions;
    /** set of UI specific options */
    private final Map<Option, T> uiOptions;
    /**
     * Map of option to overridden default value.  Generally applies to supported rat options but may be ui
     * specific options as well
     */
    private final Map <Option, String> defaultValues;

    /**
     * The function to generate a concrete BaseOption instance.
     */
    private final BiFunction<UIOptionCollection<T>, Option, T> mapper;

    /**
     * Construct the UIOptionCollection from the builder.
     * @param builder the builder to build from.
     */
    protected UIOptionCollection(final Builder<T, ?> builder) {
        Objects.requireNonNull(builder.mapper, "Builder.mapper");
        argMap = new TreeMap<>();
        mapper = builder.mapper;
        supportedRatOptions = new UpdatableOptionGroupCollection();

        for (Arg arg : Arg.values()) {
            argMap.put(arg, supportedRatOptions.add(arg.group()));
        }

        for (Option opt : builder.unsupportedRatOptions) {
            supportedRatOptions.findGroups(opt).forEach(group -> group.disableOption(opt));
        }
        uiOptions = new HashMap<>();
        supportedRatOptions.options().getOptions()
                .forEach(option -> uiOptions.put(option, mapper.apply(this, option)));
        builder.uiOptions.stream().filter(option -> !uiOptions.containsKey(option))
                .forEach(option -> uiOptions.put(option, mapper.apply(this, option)));
        defaultValues = new HashMap<>(builder.defaultValues);
    }

    /**
     * Checks if an Arg is selected.
     * @param arg the Arg to check.
     * @return {@code true} if the arg is selected.
     */
    public final boolean isSelected(final Arg arg) {
        UpdatableOptionGroup group = argMap.get(arg);
        return group != null && group.getSelected() != null;
    }

    /**
     * Gets the selected Option for the arg.
     * @param arg the arg to check.
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
     * Gets the collection of unsupported Options.
     * @return the Options comprised for the unsupported options.
     */
    public final Options getUnsupportedOptions() {
        return supportedRatOptions.unsupportedOptions();
    }

    /**
     * Gets the UiOption instance for the Option.
     * @param option the option to find the instance of.
     * @return an UIOption instance that wraps the option.
     */
    public final Optional<T> getMappedOption(final Option option) {
        return Optional.ofNullable(uiOptions.get(option));
    }

    /**
     * Gets an Options that contains the RAT Arg defined Option instances that are understood by this collection.
     * OptionGroups are registered in the resulting Options object.
     * @return an Options that contains the RAT Arg defined Option instances that are understood by this collection.
     */
    public final Options getOptions() {
        return supportedRatOptions.options().addOptions(additionalOptions());
    }

    /**
     * Gets the Stream of AbstractOption implementations  understood by this collection.
     * @return the Stream of AbstractOption implementations understood by this collection.
     */
    public final Stream<T> getMappedOptions() {
        return uiOptions.values().stream();
    }

    /**
     * Gets a map client option name to specified AbstractOption implementation.
     * @return a map client option name to specified AbstractOption implementation
     */
    public final Map<String, T> getOptionMap() {
        Map<String, T> result = new TreeMap<>();
        getMappedOptions().forEach(mappedOption -> result.put(ArgumentTracker.extractKey(mappedOption.getOption()), mappedOption));
        return result;
    }

    /**
     * Gets the additional options understood by this collection.
     * @return the additional options understood by this collection.
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
     * Builder for a BaseOptionCollection.
     * @param <T> the concreate type of the BaseOption.
     * @param <S> the concrete type being built.
     */
    protected static class Builder<T extends UIOption<T>, S extends Builder<T, S>> {
        /** set of additional UI specific options */
        private final List<Option> uiOptions;
        /**
         * Map of option to overridden default value.  Generally applies to supported rat options but may be ui
         * specific options as well
         */
        private final Map <Option, String> defaultValues;
        /** The list of unsupported Rat options */
        protected final List<Option> unsupportedRatOptions;
        /** The function to convert an option into a UIOption. */
        private BiFunction<UIOptionCollection<T>, Option, T> mapper;

        /**
         * Constructor for the builder.
         */
        protected Builder() {
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
         * build the UIOptionCollection.
         * @return the UIOptionCollection.
         */
        public UIOptionCollection<T> build() {
            return new UIOptionCollection<>(this);
        }

        /**
         * Returns this cast to {@code <S>} class.
         * @return this as {@code <S>} class.
         */
        protected final S self() {
            return (S) this;
        }

        /**
         * Set the mapper for the builder.
         * @param mapper the function to convert an option into a UIOption ({@code <T>} object).
         * @return this
         */
        public S mapper(final BiFunction<UIOptionCollection<T>, Option, T> mapper) {
            this.mapper = mapper;
            return self();
        }

        /**
         * Add a UI option to the collection.
         * @param uiOption the UI Option to add.
         * @return this
         */
        public S uiOption(final Option uiOption) {
            uiOptions.add(uiOption);
            return self();
        }

        /**
         * Add a UI options to the collection.
         * @param uiOption the UIOptions ({@code <T>} objects) to add.
         * @return this
         */
        public S uiOptions(final Option... uiOption) {
            uiOptions.addAll(Arrays.asList(uiOption));
            return self();
        }

        /**
         * Register an option as unsupported.
         * @param option the option that is not be supported.  This should be an option in the
         * {@link Arg} collection.
         * @return this
         */
        public S unsupported(final Option option) {
            unsupportedRatOptions.add(option);
            return self();
        }

        /**
         * Register multiple options as unsupported.
         * Will ignore all the options associated with the specified Arg.
         * @param arg The Arg to ignore.
         * @return this
         */
        public S unsupported(final Arg arg) {
            unsupportedRatOptions.addAll(arg.group().getOptions());
            return self();
        }

        /**
         * Specify the default values for an option.
         * @param option the option to specify the default value for.
         * @param value the value for the option.
         * @return this
         */
        public S defaultValue(final Option option, final String value) {
            defaultValues.put(option, value);
            return self();
        }

        /**
         * Specify the default values for an Arg.
         * @param arg the Arg to specify the default value for.
         * @param value the value for the option.
         * @return this
         */
        public S defaultValue(final Arg arg, final String value) {
            return defaultValue(arg.option(), value);
        }
    }
}
