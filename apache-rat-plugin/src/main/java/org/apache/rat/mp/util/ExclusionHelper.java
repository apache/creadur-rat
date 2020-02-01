package org.apache.rat.mp.util;

import org.apache.maven.plugin.logging.Log;
import org.apache.rat.config.SourceCodeManagementSystems;
import org.codehaus.plexus.util.DirectoryScanner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
                    "release.properties", //
                    ".repository", // Used by Jenkins when a Maven job uses a private repository that is "Local to the workspace"
                    "build.log", // RAT-160: until now maven-invoker-plugin runs create a build.log that is not part of a release
                    ".mvn", // Project configuration since Maven 3.3.1 which contains maven.config, jvm.config, extensions.xml
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

    public static void addPlexusAndScmDefaults(Log log, final boolean useDefaultExcludes,
                                               final Set<String> excludeList1) {
        if (useDefaultExcludes) {
            log.debug("Adding plexus default exclusions...");
            Collections.addAll(excludeList1, DirectoryScanner.DEFAULTEXCLUDES);
            log.debug("Adding SCM default exclusions...");
            excludeList1.addAll(//
                    SourceCodeManagementSystems.getPluginExclusions());
        } else {
            log.debug("rat.useDefaultExcludes set to false. "
                    + "Plexus and SCM default exclusions will not be added");
        }
    }

    public static void addMavenDefaults(Log log, boolean useMavenDefaultExcludes,
                                        final Set<String> excludeList) {
        if (useMavenDefaultExcludes) {
            log.debug("Adding exclusions often needed by Maven projects...");
            excludeList.addAll(MAVEN_DEFAULT_EXCLUDES);
        } else {
            log.debug("rat.useMavenDefaultExcludes set to false. "
                    + "Exclusions often needed by Maven projects will not be added.");
        }
    }

    public static void addEclipseDefaults(Log log, boolean useEclipseDefaultExcludes,
                                          final Set<String> excludeList) {
        if (useEclipseDefaultExcludes) {
            log.debug("Adding exclusions often needed by projects "
                    + "developed in Eclipse...");
            excludeList.addAll(ECLIPSE_DEFAULT_EXCLUDES);
        } else {
            log.debug("rat.useEclipseDefaultExcludes set to false. "
                    + "Exclusions often needed by projects developed in "
                    + "Eclipse will not be added.");
        }
    }

    public static void addIdeaDefaults(Log log, boolean useIdeaDefaultExcludes,
                                       final Set<String> excludeList) {
        if (useIdeaDefaultExcludes) {
            log.debug("Adding exclusions often needed by projects "
                    + "developed in IDEA...");
            excludeList.addAll(IDEA_DEFAULT_EXCLUDES);
        } else {
            log.debug("rat.useIdeaDefaultExcludes set to false. "
                    + "Exclusions often needed by projects developed in "
                    + "IDEA will not be added.");
        }
    }

}
