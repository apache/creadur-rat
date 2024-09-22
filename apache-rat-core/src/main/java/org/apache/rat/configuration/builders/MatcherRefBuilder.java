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

import java.util.Map;

import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.ConfigComponent;
import org.apache.rat.config.parameters.MatcherBuilder;

/**
 * A reference matching Matcher builder.
 * <p>
 * This class stores a matcher id as a reference to the matcher. It also has a
 * map of matcher ids to the matcher instances. When {@code build()} is called the matcher
 * reference is looked up in the map. If it is found then its value is returned
 * from the {@code build()} call. If the reference is not located then a
 * IHeaderMatcherProxy is returned. the IHeaderMatcherProxy is resolved in a
 * later configuration construction phase.
 */
@MatcherBuilder(MatcherRefBuilder.IHeaderMatcherProxy.class)
public class MatcherRefBuilder extends AbstractBuilder {
    private String referenceId;
    private Map<String, IHeaderMatcher> matchers;

    /** the reference id attribute */
    public static final String ATT_REF_ID = "refId";

    /**
     * Constructs the MatcherReferenceBuilder using the provided reference id.
     * 
     * @param refId the reverence to the matcher id.
     * @return this builder for chaining.
     */
    public MatcherRefBuilder setRefId(String refId) {
        this.referenceId = refId;
        return this;
    }

    /**
     * Set the Map of matcher ids to matcher instances.
     * 
     * @param matchers the Map of ids to instances.
     * @return this builder for chaining.
     */
    public MatcherRefBuilder setMatcherMap(Map<String, IHeaderMatcher> matchers) {
        this.matchers = matchers;
        return this;
    }

    @Override
    public IHeaderMatcher build() {
        if (matchers == null) {
            throw new ConfigurationException("'matchers' not set");
        }
        IHeaderMatcher result = matchers.get(referenceId);
        return result != null ? result : new IHeaderMatcherProxy(referenceId, matchers);
    }

    @Override
    public String toString() {
        return "MathcerRefBuilder: " + referenceId;
    }

    /**
     * A class that is a proxy to the actual matcher. It retrieves the actual
     * matcher from the map of matcher ids to matcher instances on the first use of
     * the matcher. This allows earlier read matchers to reference later constructed
     * matchers as long as all the matchers are constructed before the earlier one
     * is used.
     */
    @ConfigComponent(type = ComponentType.MATCHER, name = "matcherRef", desc = "A pointer to another Matcher")
    public static class IHeaderMatcherProxy implements IHeaderMatcher {

        @ConfigComponent(type = ComponentType.PARAMETER, name = "refId", desc = "Reference to an existing matcher", required = true)
        private final String proxyId;
        private IHeaderMatcher wrapped;

        @ConfigComponent(type = ComponentType.BUILD_PARAMETER, name = "matcherMap", desc = "Map of matcher names to matcher instances")
        private Map<String, IHeaderMatcher> matchers;

        /**
         * Constuctor. The matchers map should be a reference to an object that will be
         * updated by later processing of matcher definitions.
         * 
         * @param proxyId the id of the matcher to find.
         * @param matchers a mapping of matchers that have been found.
         */
        public IHeaderMatcherProxy(String proxyId, Map<String, IHeaderMatcher> matchers) {
            this.proxyId = proxyId;
            this.matchers = matchers;
        }

        /**
         * Get the reference ID that this proxy is using.
         * 
         * @return the reference id that points to the actual matcher.
         */
        public String getRefId() {
            return proxyId;
        }

        private void checkProxy() {
            if (wrapped == null) {
                wrapped = matchers.get(proxyId);
                if (wrapped == null) {
                    throw new IllegalStateException(String.format("%s is not a valid matcher id", proxyId));
                }
                matchers = null;
            }
        }

        @Override
        public String getId() {
            checkProxy();
            return wrapped.getId();
        }

        @Override
        public void reset() {
            checkProxy();
            wrapped.reset();
        }

        @Override
        public boolean matches(IHeaders header) {
            checkProxy();
            return wrapped.matches(header);
        }
    }

}
