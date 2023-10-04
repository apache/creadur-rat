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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaderMatcher.State;

public abstract class AbstractMatcherContainer extends AbstractHeaderMatcher {

    protected Collection<IHeaderMatcher> enclosed;

    public AbstractMatcherContainer(String id, Collection<? extends IHeaderMatcher> enclosed) {
        super(id);
        this.enclosed = new ArrayList<>(enclosed);
    }

    public AbstractMatcherContainer(Collection<? extends IHeaderMatcher> enclosed) {
        super(null);
        this.enclosed = new ArrayList<>(enclosed);
    }

    @Override
    public void reset() {
        enclosed.stream().forEach(x -> x.reset());
    }

    @Override
    public State finalizeState() {
        enclosed.forEach(IHeaderMatcher::finalizeState);
        return currentState();
    }
}
