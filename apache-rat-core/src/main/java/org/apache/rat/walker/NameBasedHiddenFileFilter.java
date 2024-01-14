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

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.Serializable;

/**
 * This filter accepts {@code File}s that are hidden, e.g. file name starts with .
 * <p>
 * Example, showing how to print out a list of the current directory's <i>hidden</i> files:
 * </p>
 * <h2>Using Classic IO</h2>
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list(NameBasedHiddenFileFilter.HIDDEN);
 * for (String file : files) {
 *     System.out.println(file);
 * }
 * </pre>
 *
 * <h2>Using NIO</h2>
 * <pre>
 * final Path dir = Paths.get("");
 * final AccumulatorPathVisitor visitor = AccumulatorPathVisitor.withLongCounters(NameBasedHiddenFileFilter.HIDDEN);
 * //
 * // Walk one dir
 * Files.walkFileTree(dir, Collections.emptySet(), 1, visitor);
 * System.out.println(visitor.getPathCounters());
 * System.out.println(visitor.getFileList());
 * //
 * visitor.getPathCounters().reset();
 * //
 * // Walk dir tree
 * Files.walkFileTree(dir, visitor);
 * System.out.println(visitor.getPathCounters());
 * System.out.println(visitor.getDirList());
 * System.out.println(visitor.getFileList());
 * </pre>
 */
public class NameBasedHiddenFileFilter extends AbstractFileFilter implements Serializable {

    private static final long serialVersionUID = -5951069871734926741L;
	/**
     * Singleton instance of <i>hidden</i> filter.
     */
    public static final IOFileFilter HIDDEN = new NameBasedHiddenFileFilter();

    /**
     * Restrictive constructor.
     */
    protected NameBasedHiddenFileFilter() {
    }

    /**
     * Checks to see if the file is hidden, e.g. file name starts with .
     *
     * @param file the File to check
     * @return {@code true} if the file is <i>hidden</i> (file name starting with .), {@code false} else
     */
    @Override
    public boolean accept(File file) {
        return file.getName().startsWith(".");
    }

}
