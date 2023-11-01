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
import java.util.Objects;

import org.apache.rat.analysis.IHeaderMatcher;

/**
 * A class that implements IHeaderMatcher as a collection of other IHeaderMatchers.
 */
public abstract class AbstractMatcherContainer extends AbstractHeaderMatcher {

    protected final Collection<IHeaderMatcher> enclosed;

    /**
     * Constructs the abstract matcher container.
     * If the {@code id} is not set then a unique random identifier is created.
     * The {@code enclosed} collection is preserved in a new collection that retains the order of
     * of the original collection.
     * @param id The id for the matcher.
     * @param enclosed the collection of enclosed matchers.  
     */
    public AbstractMatcherContainer(String id, Collection<? extends IHeaderMatcher> enclosed) {
        super(id);
        Objects.requireNonNull(enclosed, "The collection of IHeaderMatcher may not be null");
        this.enclosed = new ArrayList<>(enclosed);
    }

    /**
     * Constructs the abstract matcher container with a unique random id.
     * The {@code enclosed} collection is preserved in a new collection that retains the order of
     * of the original collection.
     * @param enclosed the collection of enclosed matchers.  
     */
    public AbstractMatcherContainer(Collection<? extends IHeaderMatcher> enclosed) {
        this(null, enclosed);
    }

    @Override
    public void reset() {
        enclosed.forEach(IHeaderMatcher::reset);
    }
}
