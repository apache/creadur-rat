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
import java.util.Iterator;

import org.apache.rat.analysis.IHeaderMatcher;

public class AndMatcher extends AbstractMatcherContainer {
    private boolean[] flags = null;

    public AndMatcher(String id, Collection<? extends IHeaderMatcher> enclosed) {
        super(id, enclosed);
        flags = new boolean[enclosed.size()];
    }

    public AndMatcher(Collection<? extends IHeaderMatcher> enclosed) {
        super(enclosed);
        flags = new boolean[enclosed.size()];
    }

    @Override
    public void reset() {
        super.reset();
        flags = new boolean[enclosed.size()];
    }

    @Override
    public boolean matches(String line) {
        boolean result = true;
        Iterator<IHeaderMatcher> iter = enclosed.iterator();
        int i = 0;
        while (iter.hasNext()) {
            if (flags[i]) {
                iter.next();
            } else {
                flags[i] = iter.next().matches(line);
                result &= flags[i];
            }
            i++;
        }
        return result;
    }
}
