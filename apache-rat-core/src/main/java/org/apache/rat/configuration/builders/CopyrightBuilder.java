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

import org.apache.rat.analysis.matchers.CopyrightMatcher;
import org.apache.rat.config.parameters.MatcherBuilder;

/**
 * A copyright builder.
 */
@MatcherBuilder(CopyrightMatcher.class)
public class CopyrightBuilder extends AbstractBuilder {
    private String start;
    private String end;
    private String owner;

    /**
     * Sets the start date.
     * @param start the start date for the copyright
     * @return this for chaining
     */
    public CopyrightBuilder setStart(String start) {
        this.start = start;
        return this;
    }

    /**
     * Sets the end date.
     * @param end the End data for the copyright.
     * @return this for chaining.
     */
    public CopyrightBuilder setEnd(String end) {
        this.end = end;
        return this;
    }

    /**
     * Sets the owner.
     * @param owner the owner for the copyright
     * @return this for chaining.
     */
    public CopyrightBuilder setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public CopyrightMatcher build() {
        return new CopyrightMatcher(getId(), start, end, owner);
    }

    @Override
    public String toString() {
        return String.format("Copyright Builder: s:%s e:%s o:%s", start, end, owner);
    }
}
