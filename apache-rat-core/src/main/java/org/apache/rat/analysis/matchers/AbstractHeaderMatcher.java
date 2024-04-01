/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.analysis.matchers;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.ConfigComponent;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;

/**
 * An abstract class to simplify IHeaderMatcher creation. This class ensures
 * that the id is set.
 */
public abstract class AbstractHeaderMatcher implements IHeaderMatcher {

    @ConfigComponent(type = Component.Type.Parameter, desc = "The id of the matcher.")
    private final String id;

    /**
     * Constructs the IHeaderMatcher with an id value. If {@code id} is null then a
     * unique random id is created.
     * 
     * @param id the Id to use.
     */
    protected AbstractHeaderMatcher(String id) {
        this.id = StringUtils.isBlank(id) ? UUID.randomUUID().toString() : id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public Description getDescription() {
        return DescriptionBuilder.build(this);
    }
}
