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

import java.util.regex.Pattern;

import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.matchers.SimpleRegexMatcher;
import org.apache.rat.config.parameters.MatcherBuilder;

/**
 * A builder for the regex matcher.
 */
@MatcherBuilder(SimpleRegexMatcher.class)
public class RegexBuilder extends AbstractBuilder {

    private Pattern pattern;

    /**
     * Sets the regex expression. This method compiles the string into a pattern and
     * may throw any exception thrown by the {@code Pattern.compile(String)} method.
     * @param exp the expression as a string.
     * @return this builder for chaining.
     * @see Pattern#compile(String)
     */
    public RegexBuilder setExpr(String exp) {
        this.pattern = exp == null ? null : Pattern.compile(exp);
        return this;
    }

    @Override
    public SimpleRegexMatcher build() {
        if (null == pattern) {
            throw new ConfigurationException("'regex' type matcher requires an expression");
        }
        return new SimpleRegexMatcher(getId(), pattern);
    }

    @Override
    public String toString() {
        return String.format("RegexBuilder: %s", pattern == null ? null : pattern.pattern());
    }

}
