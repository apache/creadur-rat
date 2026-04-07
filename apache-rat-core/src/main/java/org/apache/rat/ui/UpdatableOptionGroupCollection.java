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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * A collection of UpdatableOptionGroups.
 */
public class UpdatableOptionGroupCollection {
    /** the contained UpdatableOptionGroups */
    private final List<UpdatableOptionGroup> updatableOptionGroups;

    /**
     * Creates an empty collection.
     */
    public UpdatableOptionGroupCollection() {
        updatableOptionGroups = new ArrayList<>();
    }

    /**
     * Adds an OptionGroup to the collection.  If the OptionGroup is not an UpdatableOptionGroup
     * it is converted first.
     * @param optionGroup an OptionGroup to add.
     * @return the UpdatableOptionGroup that was added.
     */
    public UpdatableOptionGroup add(final OptionGroup optionGroup) {
        UpdatableOptionGroup uog = UpdatableOptionGroup.create(optionGroup);
        updatableOptionGroups.add(uog);
        return uog;
    }

    /**
     * Gets an Options object from this collection.
     * @return an Options object.
     */
    public Options options() {
        Options result = new Options();
        updatableOptionGroups.forEach(result::addOptionGroup);
        return result;
    }

    /**
     * Gets ll the UpdatableOptionGroups that the option is in.
     * @param option the option to searhc for.
     * @return the stream of UpdatableOptionGroups the option is in.
     */
    public Stream<UpdatableOptionGroup> findGroups(final Option option) {
        return updatableOptionGroups.stream().filter(og -> og.getOptions().contains(option));
    }

    /**
     * Gets the set of removed Options from the collection.
     * @return the set of removed options.
     */
    public Set<Option> removedOptions() {
        Set<Option> result = new HashSet<>();
        updatableOptionGroups.forEach(uog -> uog.getDisableOptions().forEach(result::add));
        return result;
    }

    /**
     * Gets the unsupported options
     * If multiple options from the a group are disabled they will be added to the
     * options in a group together.
     * @return the Options object containing all the unsupported options.
     */
    public Options unsupportedOptions() {
        Options result = new Options();
        for (UpdatableOptionGroup uog : updatableOptionGroups) {
            OptionGroup group = new OptionGroup();
            uog.getDisableOptions().forEach(group::addOption);
            result.addOptionGroup(group);
        }
        return result;
    }

    /**
     * Returns true if the option is in any of the groups.
     * @param option the option.
     * @return {@code true} if the option is in any of the groups.
     */
    public boolean contains(final Option option) {
        return updatableOptionGroups.stream().anyMatch(og -> og.getOptions().contains(option));
    }
}
