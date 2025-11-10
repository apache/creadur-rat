/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.analysis.matchers.AbstractHeaderMatcher;
import org.apache.rat.analysis.matchers.AndMatcher;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.ConfigComponent;

@org.apache.rat.config.parameters.MatcherBuilder(AndMatcher.class)
public class MatcherBuilder implements IHeaderMatcher.Builder {
    public MatcherBuilder() {
    }

    @Override
    public IHeaderMatcher build() {
        return new Matcher();
    }

    @ConfigComponent(type = ComponentType.MATCHER, name = "myCustomMatcher", desc = "Custom matcher example")
    public static class Matcher extends AbstractHeaderMatcher {
        public Matcher() {
            super("MyCustomMatcher");
        }

        @Override
        public boolean matches(IHeaders headers) {
            return true;
        }
    }
}
