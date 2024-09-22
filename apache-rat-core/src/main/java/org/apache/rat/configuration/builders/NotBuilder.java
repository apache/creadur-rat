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

import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.NotMatcher;
import org.apache.rat.config.parameters.MatcherBuilder;

/**
 * A builder for the NotMatcher.
 */
@MatcherBuilder(NotMatcher.class)
public class NotBuilder extends ChildContainerBuilder {

    @Override
    public NotMatcher build() {
        if (children.isEmpty()) {
            throw new ConfigurationException("'not' type matcher requires one and only one enclosed matcher");
        }
        return new NotMatcher(getId(), children.get(0).build());
    }

    @Override
    public String toString() {
        return String.format("NotBuilder: %s", !children.isEmpty() ? children.get(0) : null);
    }
    
    /**
     * Sets the enclosed matcher.  Prior to this call the builder is invalid and the {@code build()} will fail.
     * @param enclosed The matcher to negate.
     * @return this.
     */
    public NotBuilder setEnclosed(IHeaderMatcher.Builder enclosed) {
        if (children.isEmpty()) {
            children.add(enclosed);
        } else {
            children.set(0, enclosed);
        }
       return this;
    }
}
