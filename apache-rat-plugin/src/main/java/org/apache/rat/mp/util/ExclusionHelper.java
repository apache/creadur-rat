package org.apache.rat.mp.util;

import org.apache.maven.plugin.logging.Log;
import org.apache.rat.config.SourceCodeManagementSystems;
import org.codehaus.plexus.util.AbstractScanner;
import org.codehaus.plexus.util.DirectoryScanner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.codehaus.plexus.util.AbstractScanner.DEFAULTEXCLUDES;

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

/**
 * This class encapsulates the file/directory exclusion handling of RAT.
 * Visibility is to allow testing and usage in the maven plugin itself.
 */
public final class ExclusionHelper {
    /**
     * The Maven specific default excludes.
     */
    static final List<String> MAVEN_DEFAULT_EXCLUDES = Collections
            .unmodifiableList(Arrays.asList(//
                    "target/**/*", //
                    "cobertura.ser", //
                    "**/MANIFEST.MF", // a MANIFEST.MF file cannot contain comment lines. In other words: It is not possible, to include a license.
                    "release.properties", //
                    ".repository", // Used by Jenkins when a Maven job uses a private repository that is "Local to the workspace"
                    "build.log", // RAT-160: until now maven-invoker-plugin runs create a build.log that is not part of a release
                    ".mvn/**/*", // Project configuration since Maven 3.3.1 which contains maven.config, jvm.config, extensions.xml
                    "pom.xml.releaseBackup"));

    /**
     * The Eclipse specific default excludes.
     */
    static final List<String> ECLIPSE_DEFAULT_EXCLUDES = Collections
            .unmodifiableList(Arrays.asList(
                    ".checkstyle",//
                    ".classpath",//
                    ".factorypath",//
                    ".project", //
                    ".settings/**/*"));

    /**
     * The IDEA specific default excludes.
     */
    static final List<String> IDEA_DEFAULT_EXCLUDES = Collections
            .unmodifiableList(Arrays.asList(//
                    "*.iml", //
                    "*.ipr", //
                    "*.iws", //
                    ".idea/**/*"));

    public static Set<String> addPlexusAndScmDefaults(Log log, final boolean useDefaultExcludes) {
        Set<String> excludeList = new HashSet<>();
        if (useDefaultExcludes) {
            log.debug("Adding plexus default exclusions...");
            Collections.addAll(excludeList, DEFAULTEXCLUDES);
            log.debug("Adding SCM default exclusions...");
            excludeList.addAll(SourceCodeManagementSystems.getPluginExclusions());
        } else {
            log.debug("rat.useDefaultExcludes set to false. "
                    + "Plexus and SCM default exclusions will not be added");
        }
        return excludeList;
    }

    public static Set<String> addMavenDefaults(Log log, boolean useMavenDefaultExcludes) {
        Set<String> excludeList = new HashSet<>();
        if (useMavenDefaultExcludes) {
            log.debug("Adding exclusions often needed by Maven projects...");
            excludeList.addAll(MAVEN_DEFAULT_EXCLUDES);
        } else {
            log.debug("rat.useMavenDefaultExcludes set to false. "
                    + "Exclusions often needed by Maven projects will not be added.");
        }
        return excludeList;
    }

    public static Set<String> addEclipseDefaults(Log log, boolean useEclipseDefaultExcludes) {
        Set<String> excludeList = new HashSet<>();
        if (useEclipseDefaultExcludes) {
            log.debug("Adding exclusions often needed by projects "
                    + "developed in Eclipse...");
            excludeList.addAll(ECLIPSE_DEFAULT_EXCLUDES);
        } else {
            log.debug("rat.useEclipseDefaultExcludes set to false. "
                    + "Exclusions often needed by projects developed in "
                    + "Eclipse will not be added.");
        }
        return excludeList;
    }

    public static Set<String> addIdeaDefaults(Log log, boolean useIdeaDefaultExcludes) {
        Set<String> excludeList = new HashSet<>();
        if (useIdeaDefaultExcludes) {
            log.debug("Adding exclusions often needed by projects "
                    + "developed in IDEA...");
            excludeList.addAll(IDEA_DEFAULT_EXCLUDES);
        } else {
            log.debug("rat.useIdeaDefaultExcludes set to false. "
                    + "Exclusions often needed by projects developed in "
                    + "IDEA will not be added.");
        }
        return excludeList;
    }

}
