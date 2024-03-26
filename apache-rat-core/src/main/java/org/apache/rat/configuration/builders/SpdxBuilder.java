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

import org.apache.rat.analysis.matchers.SPDXMatcherFactory;

/**
 * A build for SPDX matchers.
 */
public class SpdxBuilder extends AbstractBuilder {

    private String name;

    /**
     * sets the name for the SPDX matcher
     * @param name The text that follows the colon ':' in the SPDX tag.
     * @return this builder for chaining.
     */
    public SpdxBuilder setName(String name) {
        Objects.requireNonNull(name, "spdx name must not be null");
        this.name = name;
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
