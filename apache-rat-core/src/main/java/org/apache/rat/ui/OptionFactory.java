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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rat.commandline.Arg;

/**
 * Factory to produce AbstractOption implementations as well as list available commons CLI options.
 */
public final class OptionFactory {
    /** A Configuration for the base options */
    private static final Config<AbstractOption.BaseOption> BASE_CONFIG = new Config<>(null, AbstractOption.BaseOption::new, new Options());

    private OptionFactory() {
        // do not instantiate.
    }

    /**
     * Gets a sorted the of command line options.
     * @return the list of command line options.
     */
    public static List<Option> baseOptions() {
        return baseOptions(BASE_CONFIG);
    }

    /**
     * Gets the list of commons CLI options.
     * @param config The OptionFactory configuration.
     * @return a sorted list of command line options.
     */
    public static List<Option> baseOptions(final Config<?> config) {
        List<Option> lst = new ArrayList<>(Arg.getOptions(config.additionalOptions).getOptions());
        if (config.filter != null) {
            lst.removeIf(config.filter);
        }
        lst.sort(Comparator.comparing(AbstractOption::extractBaseName));
        return lst;
    }

    /**
     * Gets a map client option name to specified AbstractOption implementation.
     * @param config the OptionFactory configuraiton.
     * @return a map client option name to specified AbstractOption implementation
     * @param <T> the AbstractOption implementation.
     */
    public static <T extends AbstractOption<T>> Map<String, T> getOptionMap(final Config<T> config) {
        Map<String, T> result = new TreeMap<>();
        getOptionList(config).forEach(option -> result.put(AbstractOption.extractBaseName(option.getOption()), option));
        return result;
    }

    /**
     * Gets a stream of AbstractOption implementations.
     * @param config The Configuration for the factory.
     * @return a stream of AbstractOption implementations.
     * @param <T> the AbstractOption implementation.
     */
    public static <T extends AbstractOption<T>> Stream<T> getOptionList(final Config<T> config) {
        return Arg.getOptions(config.additionalOptions).getOptions().stream().filter(config.getFilter()).map(config.mapper);
    }

    /**
     * The Configuration for a factory to produce AbstractOption implementations.
     * @param <T> the AbstractOption implementation.
     */
    public static final class Config<T extends AbstractOption<T>> {
        /** The filter to remove unsupported options */
        private final Predicate<Option> filter;
        /** Function to map commons CLI Option to T */
        private final Function<Option, T> mapper;
        /** Additional commons CLI Options to add to the result */
        private final Options additionalOptions;

        /**
         * @param filter The predicate to remove original options. May be {@code null} to include all options.
         * @param mapper The function to map commons CLI options to AbstractOption implementation.
         * @param additionalOptions Additional options added for the UI.
         */
        public Config(final Predicate<Option> filter, final Function<Option, T> mapper, final Options additionalOptions) {
            this.filter = filter;
            this.mapper = mapper;
            this.additionalOptions = additionalOptions;
        }

        /**
         * Get the filter.  If the filter is null return a filter that alwasy returns false.
         * @return the predicate for the filter.
         */
        private Predicate<Option> getFilter() {
            return  filter == null ? x -> false : filter;
        }
    }
}
