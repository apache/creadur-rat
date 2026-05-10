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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

/**
 * An implementation of Apache Commons CLI OptionGroup that allows options to be removed (disabled).
 */
public final class UpdatableOptionGroup extends OptionGroup {
    /** The set of options to remove */
    private final Set<Option> disabledOptions = new HashSet<>();

    /**
     * Converts the group into an UpdatableOptionGroup if it is not already an instance
     * @param group the group to convert.
     * @return an UpdatableOptionGroup.
     */
    public static UpdatableOptionGroup create(final OptionGroup group) {
        return group instanceof UpdatableOptionGroup updatableOptionGroup ? updatableOptionGroup : new UpdatableOptionGroup(group);
    }

    private UpdatableOptionGroup(final OptionGroup group) {
        group.getOptions().forEach(super::addOption);
    }

    /**
     * Disable an option in the group.
     * @param option The option to disable.
     */
    public void disableOption(final Option option) {
        disabledOptions.add(option);
    }

    public boolean isEmpty() {
        return getOptions().isEmpty();
    }

    /**
     * Gets the disabled options for this group.
     * @return the set of disabled options for this group.
     */
    public Stream<Option> getDisableOptions() {
        return disabledOptions.stream();
    }
    /**
     * Reset the group so that all disabled options are re-enabled.
     */
    public void reset() {
        disabledOptions.clear();
    }

    @Override
    public Collection<Option> getOptions() {
        return super.getOptions().stream().filter(opt -> !disabledOptions.contains(opt)).toList();
    }

    @Override
    public UpdatableOptionGroup addOption(final Option option) {
        super.addOption(option);
        return this;
    }
}
