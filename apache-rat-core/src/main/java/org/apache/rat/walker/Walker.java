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

package org.apache.rat.walker;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.rat.report.IReportable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * Abstract walker.
 */
public abstract class Walker implements IReportable {

    protected final File file;
    protected final String name;

    protected final FilenameFilter filesToIgnore;

    protected static FilenameFilter regexFilter(final Pattern pattern) {
        return (dir, name) -> {
            final boolean result;
            if (pattern == null) {
                result = false;
            } else {
                result = pattern.matcher(name).matches();
            }
            return result;
        };
    }
 
    protected final boolean isNotIgnored(final File file) {
        return !filesToIgnore.accept(file.getParentFile(), file.getName());
    }

    public Walker(File file, final FilenameFilter filter) {
        this(file.getPath(), file, filter);
    }

    protected Walker(final String name, final File file, final FilenameFilter filesToIgnore) {
        this.name = name;
        this.file = file;
        this.filesToIgnore = filesToIgnore == null ? FalseFileFilter.FALSE : filesToIgnore;
    }

}
