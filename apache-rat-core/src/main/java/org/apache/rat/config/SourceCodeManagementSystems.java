package org.apache.rat.config;

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
import java.util.ArrayList;
import java.util.List;

public enum SourceCodeManagementSystems {
    SUBVERSION(".svn", null), //
    GIT(".git", ".gitignore"), //
    BAZAAR(".bzr", ".bzrignore"), //
    MERCURIAL(".hg", ".hgignore"), //
    CVS("CVS", ".cvsignore")
    //
    ;

    /**
     * Technical directory of that SCM which contains SCM internals.
     */
    private final String directory;
    /**
     * If there is a external way to configure files to be ignored: name of this
     * file, <code>null</code> otherwise.
     */
    private final String ignoreFile;

    SourceCodeManagementSystems(String directory, String ignoreFile) {
        this.directory = directory;
        this.ignoreFile = ignoreFile;
    }

    /**
     * If an ignore file exists it's added as
     * 
     * <pre>
     * *&frasl;.scm&frasl;*
     * </pre>
     * 
     * . Otherwise the technical directory of the SCM is added as
     * 
     * <pre>
     * **&frasl;.scmignore
     * </pre>
     * 
     * to be used as exclusion during RAT runs.
     * 
     * @return list of excludes if the current SCM is used.
     */
    public List<String> getExclusions() {
        List<String> excludes = new ArrayList<>(2);

        if (hasIgnoreFile()) {
            excludes.add("**/" + ignoreFile);
        }
        excludes.add("*/" + directory + "/*");

        return excludes;
    }

    public boolean hasIgnoreFile() {
        return ignoreFile != null && ignoreFile.length() != 0;
    }

    /**
     * Calls {@link #getExclusions()} on each SCM to generate a global list of
     * exclusions to be used during RAT runs.
     * 
     * @return the global list of exclusions usable for all known SCM.
     */
    public static List<String> getPluginExclusions() {
        List<String> pluginExclusions = new ArrayList<>();

        for (SourceCodeManagementSystems scm : values()) {
            pluginExclusions.addAll(scm.getExclusions());
        }

        return pluginExclusions;
    }

    /**
     * Maybe <code>null</code>, check before with
     * @see #hasIgnoreFile()
     *
     * @return the ignore file of the SCM.
     */
    public String getIgnoreFile() {
        return ignoreFile;
    }
}