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

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.inspector.AbstractInspector;
import org.apache.rat.inspector.Inspector;
import org.apache.rat.inspector.Inspector.Type;

/**
 * A matcher that performs a logical {@code AND} across all the contained matchers.
 */
public class AndMatcher extends AbstractMatcherContainer {

    /**
     * Constructs the AndMatcher with the specified id and enclosed collection.
     * @param id the to use.  If null or an empty string a unique random id will be created.
     * @param enclosed the enclosed collection.
     */
    public AndMatcher(String id, Collection<? extends IHeaderMatcher> enclosed) {
        super(id, enclosed);
    }

    /**
     * Constructs the AndMatcher with the a unique random id and the enclosed collection.
     * @param enclosed the enclosed collection.
     */
    public AndMatcher(Collection<? extends IHeaderMatcher> enclosed) {
        this(null, enclosed);
    }

    @Override
    public State currentState() {
        State dflt = State.t;
        for (IHeaderMatcher matcher : enclosed) {
            switch (matcher.currentState()) {
            case f:
                return State.f;
            case i:
                dflt = State.i;
                break;
            default:
                // do nothing
                break;
            }
        }
        return dflt;
    }

    @Override
    public State matches(String line) {
        enclosed.stream().filter(x -> x.currentState() == State.i).forEach(x -> x.matches(line));
        return currentState();
    }

    @Override
    public State finalizeState() {
        enclosed.forEach(IHeaderMatcher::finalizeState);
        return currentState();
    }
    
    @Override
    public Inspector getInspector() {
        return AbstractInspector.matcher("all", getId(), enclosed.stream().map(IHeaderMatcher::getInspector).collect(Collectors.toList()));
    }
}
