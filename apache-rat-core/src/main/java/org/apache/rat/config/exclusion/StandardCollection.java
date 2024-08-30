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
package org.apache.rat.config.exclusion;

import org.apache.rat.config.exclusion.fileProcessors.BazaarIgnoreProcessor;
import org.apache.rat.config.exclusion.fileProcessors.CVSFileProcessor;
import org.apache.rat.config.exclusion.fileProcessors.GitFileProcessor;
import org.apache.rat.config.exclusion.fileProcessors.HgIgnoreProcessor;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public enum StandardCollection {
    ALL("All of the Standard Excludes combined.", null, null, null),
    ARCH("The files and directories created by an ARCH source code control based tool.",
            Collections.singletonList("**/.arch-ids/**"), null, null),
    BAZAAR("The files and directories created by a Bazaar source code control based tool.",
            Arrays.asList("**/.bzr/**", ".bzrignore"), null, new BazaarIgnoreProcessor()),
    BITKEEPER("The files and directories created by a Bitkeeper source code control based tool.",
            Arrays.asList("**/BitKeeper/**", "**/ChangeSet/**"), null, null),
    CVS("The files and directories created by a CVS source code control based tool.",
            Arrays.asList("**/.cvsignore",
                    "**/RCS/**", "**/SCCS/**", "**/CVS/**", "**/CVS.adm/**",
                    "**/RCSLOG/**", "**/cvslog.*", "**/tags/**", "**/TAGS/**",
                    "**/.make.state", "**/.nse_depinfo",
                    "**/*~", "**/#*", "**/.#*", "**/,*", "**/_$*", "**/*$", "**/*.old", "**/*.bak", "**/*.BAK",
                    "**/*.orig", "**/*.rej", "**/.del-*",
                    "**/*.a", "**/*.old", "**/*.o", "**/*.obj", "**/*.so", "**/*.exe",
                    "**/*.Z", "**/*.elc", "**/*.ln", "**/core"),
            null, new CVSFileProcessor()),
    DARCS("The files and directories created by a DARCS source code control based tool.",
            Arrays.asList("**/_darcs/**", "**/.darcsrepo/**", "**/-darcs-backup*", "**/.darcs-temp-mail"), null, null),
    ECLIPSE("The files and directories created by an Eclipse IDE based tool.",
            Arrays.asList(
                    ".checkstyle",//
                    ".classpath",//
                    ".factorypath",//
                    ".project", //
                    ".settings/**"),
            null, null),
    GIT("The files and directories created by GIT source code control to support GIT, also processes files listed in '.gitignore'.",
            Arrays.asList("**/.git/**", "**/.gitignore"),
            null,
            new GitFileProcessor()
    ),
    HIDDEN_DIR("The hidden directories",
            null,
            str -> TracablePathMatcher.make(() -> "HIDDEN_DIR", pth -> {
                File f = pth.toFile();
                return f.isHidden() && f.isDirectory();
            }), null
    ),
    HIDDEN_FILE("The hidden files",
            null,
            str -> TracablePathMatcher.make(() -> "HIDDEN_FILE", pth -> {
                File f = pth.toFile();
                return f.isHidden() && f.isFile();
            }), null
    ),
    IDEA("The files and directories created by an IDEA IDE based tool.",
            Arrays.asList(
                    "*.iml",
                    "*.ipr",
                    "*.iws",
                    ".idea/**"), null, null),
    MAC("The .DS_Store files MAC computer.",
            Collections.singletonList("**/.DS_Store"), null, null),
    MAVEN("The files and directories created by Maven build system based project",
            Arrays.asList(//
                    "pom.xml",
                    "target/**", //
                    "cobertura.ser", //
                    "**/MANIFEST.MF", // a MANIFEST.MF file cannot contain comment lines. In other words: It is not possible, to include a license.
                    "release.properties", //
                    ".repository", // Used by Jenkins when a Maven job uses a private repository that is "Local to the workspace"
                    "build.log", // RAT-160: until now maven-invoker-plugin runs create a build.log that is not part of a release
                    ".mvn/**", // Project configuration since Maven 3.3.1 which contains maven.config, jvm.config, extensions.xml
                    "pom.xml.releaseBackup"), null, null),
    MERCURIAL("The files and directories created by a Mercurial source code control based tool.",
            Arrays.asList("**/.hg/**", ".hgignore"), null, new HgIgnoreProcessor()), //
    MISC("The set of miscellaneous files generally left by editors and the like.",
            Arrays.asList("**/%*%", "**/._*",
                    "**/*~", "**/#*", "**/.#*", "**/,*", "**/_$*", "**/*$", "**/*.old", "**/*.bak", "**/*.BAK",
                    "**/*.orig", "**/*.rej", "**/.del-*",
                    "**/*.a", "**/*.olb", "**/*.o", "**/*.obj", "**/*.so", "**/*.exe",
                    "**/*.Z", "**/*.elc", "**/*.ln", "**/core"), null, null),
    MKS("The files and directories created by an MKS source code control based tool.",
            Collections.singletonList("**/project.pj"), null, null),
    RCS("The files and directories created by a RCS source code control based tool.",
            Collections.singletonList("**/RCS/**"), null, null),
    SCCS("The files and directories created by a SCCS source code control based tool.",
            Collections.singletonList("**/SCCS/**"), null, null),
    SERENA_DIMENSIONS_10("The files and directories created by a Serena Dimensions V10 change control system based tool.",
            Collections.singletonList("**/.metadata/**"), null, null),
    SUBVERSION("The files and directories created by a Subversion source code control based tool.",
            Collections.singletonList("**/.svn/**"), null, null),
    SURROUND_SCM("The files and directories created by a Surround SCM source code control based tool.",
            Collections.singletonList("**/.MySCMServerInfo"), null, null),
    VSS("The files and directories created by a Visual Source Safe source code control based tool.",
            Collections.singletonList("**/vssver.scc"), null, null);

    final Collection<String> patterns;
    final PathMatcherSupplier pathMatcherSupplier;
    final FileProcessor fileProcessor;
    final String desc;

    StandardCollection(String desc, Collection<String> ex, PathMatcherSupplier pathMatcherSupplier, FileProcessor fileProcessor) {
        this.desc = desc;
        this.patterns = ex == null ? Collections.emptyList() : new HashSet<>(ex);
        this.pathMatcherSupplier = pathMatcherSupplier;
        this.fileProcessor = fileProcessor;
    }

    public String desc() {
        return desc;
    }

    public boolean hasFileProcessor() {
        return fileProcessor != null;
    }

    public boolean hasPathMatchSupplier() {
        return pathMatcherSupplier != null;
    }
}
