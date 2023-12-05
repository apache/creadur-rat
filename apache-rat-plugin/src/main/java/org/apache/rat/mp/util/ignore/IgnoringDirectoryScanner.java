package org.apache.rat.mp.util.ignore;

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

import org.codehaus.plexus.util.DirectoryScanner;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class IgnoringDirectoryScanner extends DirectoryScanner {

    public IgnoringDirectoryScanner() {
        super();
    }

    List<IgnoreMatcher> ignoreMatcherList = new ArrayList<>();

    public void addIgnoreMatcher(IgnoreMatcher ignoreMatcher) {
        ignoreMatcherList.add(ignoreMatcher);
    }

    private boolean matchesAnIgnoreMatcher(String name) {
        for (IgnoreMatcher ignoreMatcher : ignoreMatcherList) {
            if (ignoreMatcher.isIgnoredFile(name).orElse(FALSE) == TRUE) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isExcluded(String name) {
        if (matchesAnIgnoreMatcher(name)) {
            return true;
        }
        return super.isExcluded(name);
    }

    @Override
    protected boolean isExcluded(String name, String[] tokenizedName) {
        if (matchesAnIgnoreMatcher(name)) {
            return true;
        }
        return super.isExcluded(name, tokenizedName);
    }

    @Override
    protected boolean isExcluded(String name, char[][] tokenizedName) {
        if (matchesAnIgnoreMatcher(name)) {
            return true;
        }
        return super.isExcluded(name, tokenizedName);
    }
}
