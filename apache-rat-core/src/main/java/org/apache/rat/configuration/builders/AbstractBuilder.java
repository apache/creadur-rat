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

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.analysis.IHeaderMatcher;

/**
 * An abstract IHeaderMatcher.Builder.
 */
public abstract class AbstractBuilder implements IHeaderMatcher.Builder {

    private String id;

    /**
     * Protected empty constructor.
     */
    protected AbstractBuilder() {
    }

    /**
     * Set the id for the matcher.
     * @param id the id to use.
     * @return this builder for chaining.
     */
    public final AbstractBuilder setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return {@code true} if the id is not null and not blank.
     */
    public final boolean hasId() {
        return !StringUtils.isBlank(id);
    }
    
    /**
     * @return the id as specified in the builder.
     */
    protected String getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return String.format( "%s with id %s", this.getClass(), id);
    }

}