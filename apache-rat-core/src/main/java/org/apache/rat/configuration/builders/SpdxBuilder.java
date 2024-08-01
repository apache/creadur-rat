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
package org.apache.rat.configuration.builders;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.matchers.SPDXMatcherFactory;
import org.apache.rat.config.parameters.MatcherBuilder;

/**
 * A build for SPDX matchers.
 */
@MatcherBuilder(SPDXMatcherFactory.Match.class)
public class SpdxBuilder extends AbstractBuilder {

    private String name;

    /**
     * Sets the name for the SPDX matcher.  This is the same as the identifier in the SPDX license list.
     * 
     * @param name The text that follows the colon ':' in the SPDX tag.
     * @return this builder.
     * @see <a href="https://spdx.org/licenses/">SPDX license list</a>
     */
    public SpdxBuilder setName(String name) {
        Objects.requireNonNull(name, "SPDX name must not be null");
        this.name = name;
        super.setId("SPDX:" + name);
        return this;
    }

    /**
     * Set the id for the matcher.
     * 
     * @param id the id to use.
     * @return this builder.
     */
    @Override
    public AbstractBuilder setId(String id) {
        if (StringUtils.isNotBlank(id)) {
            throw new ConfigurationException("'id' is not supported for SPDX matchers.  "
                    + "SPXD matchers always have 'SPDX:<name>' as their id");
        }
        return this;
    }

    @Override
    public SPDXMatcherFactory.Match build() {
        return SPDXMatcherFactory.INSTANCE.create(name);
    }

    @Override
    public String toString() {
        return "SpdxBuilder: " + name;
    }
}
