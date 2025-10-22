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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.rat.config.exclusion.fileProcessors.AbstractFileProcessorBuilder;
import org.apache.rat.config.exclusion.fileProcessors.BazaarIgnoreBuilder;
import org.apache.rat.config.exclusion.fileProcessors.CVSIgnoreBuilder;
import org.apache.rat.config.exclusion.fileProcessors.GitIgnoreBuilder;
import org.apache.rat.config.exclusion.fileProcessors.HgIgnoreBuilder;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.utils.ExtendedIterator;

/**
 * Collection of standard excludes.
 * HINT: In order to work recursively each entry is prefixed with {@code "**\/"}.
 */
public enum StandardCollection {
    /**
     * All the standard excludes combined.
     */
    // see getCollections() for loading
    ALL("All of the Standard Excludes combined.", null, null, null),
    /**
     * The files and directories created by an ARCH source code control based tool.
     */
    ARCH("The files and directories created by an ARCH source code control based tool.",
            Collections.singletonList("**/.arch-ids/**"), null, null),
    /**
     * The files and directories created by a Bazaar source code control based tool.
     */
    BAZAAR("The files and directories created by a Bazaar source code control based tool.",
            Arrays.asList("**/.bzr/**", "**/.bzrignore"), null, new BazaarIgnoreBuilder()),
    /**
     * The files and directories created by a Bitkeeper source code control based tool.
     */
    BITKEEPER("The files and directories created by a Bitkeeper source code control based tool.",
            Arrays.asList("**/BitKeeper/**", "**/ChangeSet/**"), null, null),
    /**
     * The files and directories created by a CVS source code control based tool.
     * @see <a href="https://www.gnu.org/software/trans-coord/manual/cvs/html_node/cvsignore.html#cvsignore">Ignoring files via cvsignore</a>
     */
    CVS("The files and directories created by a CVS source code control based tool.",
            Arrays.asList("**/.cvsignore",
                    "**/RCS/**", "**/SCCS/**", "**/CVS/**", "**/CVS.adm/**",
                    "**/RCSLOG/**", "**/cvslog.*", "**/tags/**", "**/TAGS/**",
                    "**/.make.state", "**/.nse_depinfo",
                    "**/*~", "**/#*", "**/.#*", "**/,*", "**/_$*", "**/*$", "**/*.old", "**/*.bak", "**/*.BAK",
                    "**/*.orig", "**/*.rej", "**/.del-*",
                    "**/*.a", "**/*.old", "**/*.o", "**/*.obj", "**/*.so", "**/*.exe",
                    "**/*.Z", "**/*.elc", "**/*.ln", "**/core"),
            null, new CVSIgnoreBuilder()),
    /**
     * The files and directories created by a DARCS source code control based tool.
     */
    DARCS("The files and directories created by a DARCS source code control based tool.",
            Arrays.asList("**/_darcs/**", "**/.darcsrepo/**", "**/-darcs-backup*", "**/.darcs-temp-mail"), null, null),
    /**
     * The files and directories created by an Eclipse IDE based tool.
     */
    ECLIPSE("The files and directories created by an Eclipse IDE based tool.",
            Arrays.asList("**/.checkstyle", "**/.classpath", "**/.factorypath",
                    "**/.project", "**/.settings/**", "**/.externalToolBuilders/**", "**/bin/**"),
            null, null),
    /**
     * The files and directories created by GIT source code control to support GIT, also processes files listed in '.gitignore'
     * and (unless RAT_NO_GIT_GLOBAL_IGNORE is specified) the global gitignore.
     */
    GIT("The files and directories created by GIT source code control to support GIT, also processes files listed in '.gitignore' " +
        "and (unless RAT_NO_GIT_GLOBAL_IGNORE is specified) the global gitignore.",
            Arrays.asList("**/.git/**", "**/.gitignore"),
            null,
            new GitIgnoreBuilder()
    ),
    /**
     * The hidden directories. Directories with names that start with {@code .}
     */
    HIDDEN_DIR("The hidden directories. Directories with names that start with '.'",
            null,
            new DocumentNameMatcher("HIDDEN_DIR", new Predicate<DocumentName>() {
                @Override
                public boolean test(final DocumentName documentName) {
                    File file = documentName.asFile();
                    return file.isDirectory() && ExclusionUtils.isHidden(documentName.getShortName());
                }
                @Override
                public String toString() {
                    return "HIDDEN_DIR";
                }
            }), null
    ),
    /**
     * The hidden files. Directories with names that start with {@code .}
     */
    HIDDEN_FILE("The hidden files. Directories with names that start with '.'",
            null,
            new DocumentNameMatcher("HIDDEN_FILE", new Predicate<DocumentName>() {
                @Override
                public boolean test(final DocumentName documentName) {
                    File file = documentName.asFile();
                    return file.isFile() && ExclusionUtils.isHidden(documentName.getShortName());
                }
                @Override
                public String toString() {
                    return "HIDDEN_FILE";
                }
            }), null
    ),
    /**
     * The files and directories created by an IDEA IDE based tool.
     */
    IDEA("The files and directories created by an IDEA IDE based tool.",
            Arrays.asList("**/*.iml", "**/*.ipr", "**/*.iws", "**/.idea/**"), null, null),
    /**
     * The {@code .DS_Store} files on Mac computers.
     */
    MAC("The .DS_Store files on Mac computers.",
            Collections.singletonList("**/.DS_Store"), null, null),
    /**
     * The files and directories created by Maven build system based project.
     */
    MAVEN("The files and directories created by Maven build system based project.",
            Arrays.asList(
                    "**/target/**", //
                    "**/cobertura.ser", //
                    "**/MANIFEST.MF", // a MANIFEST.MF file cannot contain comment lines. In other words: It is not possible, to include a license.
                    "**/release.properties", //
                    "**/.repository", // Used by Jenkins when a Maven job uses a private repository that is "Local to the workspace"
                    "**/build.log", // RAT-160: until now maven-invoker-plugin runs create a build.log that is not part of a release
                    "**/.mvn/**", // Project configuration since Maven 3.3.1 which contains maven.config, jvm.config, extensions.xml
                    "**/pom.xml.releaseBackup"), null, null),
    /**
     * The files and directories created by a Mercurial source code control based tool.
     */
    MERCURIAL("The files and directories created by a Mercurial source code control based tool.",
            Arrays.asList("**/.hg/**", "**/.hgignore"), null, new HgIgnoreBuilder()),
    /**
     * The set of miscellaneous files generally left by editors and the like.
     */
    MISC("The set of miscellaneous files generally left by editors and the like.",
            Arrays.asList("**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*"),
            null, null),
    /**
     * The files and directories created by an MKS source code control based tool.
     */
    MKS("The files and directories created by an MKS source code control based tool.",
            Collections.singletonList("**/project.pj"), null, null),
    /**
     * The files and directories created by an RCS source code control based tool.
     */
    RCS("The files and directories created by a RCS source code control based tool.",
            Collections.singletonList("**/RCS/**"), null, null),
    /**
     * The files and directories created by a SCCS source code control based tool.
     */
    SCCS("The files and directories created by a SCCS source code control based tool.",
            Collections.singletonList("**/SCCS/**"), null, null),
    /**
     * The files and directories created by a Serena Dimensions V10 change control system based tool.
     */
    SERENA_DIMENSIONS_10("The files and directories created by a Serena Dimensions V10 change control system based tool.",
            Collections.singletonList("**/.metadata/**"), null, null),
    /**
     * A standard collection of generally accepted patterns to ignore.
     */
    // see getCollections() for loading
    STANDARD_PATTERNS("A standard collection of generally accepted patterns to ignore.", null, null, null),
    /**
     * A standard collection of SCMs.
     */
    // see getCollections() for loading
    STANDARD_SCMS("A standard collection of SCMs", null, null, null),
    /**
     * The files and directories created by a Subversion source code control based tool.
     */
    SUBVERSION("The files and directories created by a Subversion source code control based tool.",
            Collections.singletonList("**/.svn/**"), null, null),
    /**
     * The files and directories created by a Surround SCM source code control based tool.
     */
    SURROUND_SCM("The files and directories created by a Surround SCM source code control based tool.",
            Collections.singletonList("**/.MySCMServerInfo"), null, null),
    /**
     * The files and directories created by a Visual Source Safe source code control based tool.
     */
    VSS("The files and directories created by a Visual Source Safe source code control based tool.",
            Collections.singletonList("**/vssver.scc"), null, null);

    /** The collections of patterns to be excluded. May be empty.*/
    private final Collection<String> patterns;
    /** A document name matcher supplier to create a document name matcher. May be null */
    private final DocumentNameMatcher staticDocumentNameMatcher;
    /** The AbstractFileProcessorBuilder to process the exclude file associated with this exclusion. May be {@code null}. */
    private final AbstractFileProcessorBuilder fileProcessorBuilder;
    /** The description of this collection */
    private final String desc;

    StandardCollection(final String desc, final Collection<String> patterns, final DocumentNameMatcher documentNameMatcher,
                       final AbstractFileProcessorBuilder fileProcessorBuilder) {
        this.desc = desc;
        this.patterns = patterns == null ? Collections.emptyList() : new HashSet<>(patterns);
        this.staticDocumentNameMatcher = documentNameMatcher;
        this.fileProcessorBuilder = fileProcessorBuilder;
    }

    /**
     * @return the description of the given collection.
     */
    public String desc() {
        return desc;
    }

    /**
     * Handles aggregate StandardCollections (e.g. ALL) by generating the set of StandardCollection objects that
     * comprise this StandardCollection.
     * @return the set of StandardCollection objects that comprise this StandardCollection.
     */
    private Set<StandardCollection> getCollections() {
        Set<StandardCollection> result = new HashSet<>();
        switch (this) {
            case ALL:
                for (StandardCollection sc : StandardCollection.values()) {
                    if (sc != ALL) {
                        result.add(sc);
                    }
                }
                break;
            case STANDARD_PATTERNS:
                result.addAll(Arrays.asList(MISC, CVS, RCS, SCCS, VSS, MKS, SUBVERSION, ARCH, BAZAAR, SURROUND_SCM, MAC,
                        SERENA_DIMENSIONS_10, MERCURIAL, GIT, BITKEEPER, DARCS));
                break;
            case STANDARD_SCMS:
                result.addAll(Arrays.asList(SUBVERSION, GIT, BAZAAR, MERCURIAL, CVS));
                break;

            default:
                result.add(this);
        }
        return result;
    }

    /**
     * Returns combined and deduped collection of patterns.
     * @return the combined and deduped collection of patterns in the given collection.
     */
    public Set<String> patterns() {
        Set<String> result = new HashSet<>();
        getCollections().forEach(sc -> result.addAll(sc.patterns));
        return result;
    }

    /**
     * Returns the fileProcessor if it exists.
     *
     * @return the fileProcessor if it exists, {@code null} otherwise.
     */
    public ExtendedIterator<AbstractFileProcessorBuilder> fileProcessorBuilder() {
        List<AbstractFileProcessorBuilder> lst = new ArrayList<>();
        for (StandardCollection sc : getCollections()) {
            if (sc.fileProcessorBuilder != null) {
                lst.add(sc.fileProcessorBuilder);
            }
        }
        return ExtendedIterator.create(lst.iterator());
    }

    /**
     * Returns the documentNameMatchSupplier if it exists.
     *
     * @return the documentNameMatchSupplier if it exists, {@code null} otherwise.
     */
    public DocumentNameMatcher staticDocumentNameMatcher() {
        // account for cases where this has more than one supplier.
        List<DocumentNameMatcher> lst = new ArrayList<>();
        for (StandardCollection sc : getCollections()) {
            if (sc.staticDocumentNameMatcher != null) {
                lst.add(sc.staticDocumentNameMatcher);
            }
        }
        if (lst.isEmpty()) {
            return null;
        }
        if (lst.size() == 1) {
            return lst.get(0);
        }

        return new DocumentNameMatcher(name() + " static DocumentNameMatchers",  DocumentNameMatcher.or(lst));
    }

    /**
     * Returns {@code true} if the collections has a document name match supplier.
     *
     * @return {@code true} if the collections has a document name match supplier.
     */
    public boolean hasStaticDocumentNameMatcher() {
        // account for cases where this has more than one supplier.
        for (StandardCollection sc : getCollections()) {
            if (sc.staticDocumentNameMatcher != null) {
                return true;
            }
        }
        return false;
    }
}
