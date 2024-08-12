/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.anttasks;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.configuration.builders.CopyrightBuilder;

/**
 * Creates a Copyright matcher.
 * @deprecated use configuration file.
 */
@Deprecated // since 0.17
public class Copyright implements IHeaderMatcher.Builder {

    private final CopyrightBuilder builder = new CopyrightBuilder();
    
    public void setStart(String start) {
        builder.setStart(start);
    }

    public void setEnd(String end) {
        builder.setEnd(end);
    }

    public void setOwner(String owner) {
        builder.setOwner(owner);
    }

    @Override
    public IHeaderMatcher build() {
        return builder.build();
    }
    
}
