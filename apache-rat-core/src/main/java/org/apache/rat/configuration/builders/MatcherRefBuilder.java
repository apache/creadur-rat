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

import org.apache.rat.analysis.IHeaderMatcher;

public class MatcherRefBuilder extends AbstractBuilder {
    private String referenceId;
    private Map<String, IHeaderMatcher> matchers;

    public MatcherRefBuilder setRefId(String refId) {
        this.referenceId = refId;
        return this;
    }

    public MatcherRefBuilder setMatchers(Map<String, IHeaderMatcher> matchers) {
        this.matchers = matchers;
        return this;
    }

    @Override
    public IHeaderMatcher build() {
        IHeaderMatcher result = matchers.get(referenceId);
        return result != null ? result : new IHeaderMatcherProxy(referenceId, matchers);
    }

    private class IHeaderMatcherProxy implements IHeaderMatcher {
        private final String proxyId;
        private IHeaderMatcher wrapped;
        private Map<String, IHeaderMatcher> matchers;

        private IHeaderMatcherProxy(String proxyId, Map<String, IHeaderMatcher> matchers) {
            this.proxyId = proxyId;
            this.matchers = matchers;
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
        public State matches(String line) {
            checkProxy();
            return wrapped.matches(line);
        }

        @Override
        public State currentState() {
            checkProxy();
            return wrapped.currentState();
        }

        @Override
        public State finalizeState() {
            checkProxy();
            return wrapped.finalizeState();
        }
    }

}
