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
import java.util.function.Supplier;

import org.apache.rat.config.exclusion.fileProcessors.BazaarIgnoreProcessor;
import org.apache.rat.config.exclusion.fileProcessors.CVSFileProcessor;
import org.apache.rat.config.exclusion.fileProcessors.GitFileProcessor;
import org.apache.rat.config.exclusion.fileProcessors.HgIgnoreProcessor;
import org.apache.rat.document.impl.DocumentNameMatcherSupplier;
import org.apache.rat.document.impl.TraceableDocumentNameMatcher;
import org.apache.rat.utils.iterator.ExtendedIterator;

public enum StandardCollection {
    /**
     * All the standard excludes combined
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
            Arrays.asList("**/.bzr/**", ".bzrignore"), null, new BazaarIgnoreProcessor()),
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
            null, new CVSFileProcessor()),
    /**
     * The files and directories created by a DARCS source code control based tool.
     */
    DARCS("The files and directories created by a DARCS source code control based tool.",
            Arrays.asList("**/_darcs/**", "**/.darcsrepo/**", "**/-darcs-backup*", "**/.darcs-temp-mail"), null, null),
    /**
     * The files and directories created by an Eclipse IDE based tool.
     */
    ECLIPSE("The files and directories created by an Eclipse IDE based tool.",
            Arrays.asList(".checkstyle", ".classpath", ".factorypath", ".project", ".settings/**"),
            null, null),
    /**
     * The files and directories created by GIT source code control to support GIT, also processes files listed in '.gitignore'.
     */
    GIT("The files and directories created by GIT source code control to support GIT, also processes files listed in '.gitignore'.",
            Arrays.asList("**/.git/**", "**/.gitignore"),
            null,
            new GitFileProcessor()
    ),
    /**
     * The hidden directories. Directories with names that start with '.'
     */
    HIDDEN_DIR("The hidden directories. Directories with names that start with '.'",
            null,
            str -> TraceableDocumentNameMatcher.make(() -> "HIDDEN_DIR", documentName -> {
                File f = new File(documentName.getName());
                return f.isDirectory() && ExclusionUtils.isHidden(f);
            }), null
    ),
    /**
     * The hidden files. Directories with names that start with '.'
     */
    HIDDEN_FILE("The hidden files. Directories with names that start with '.'",
            null,
            str -> TraceableDocumentNameMatcher.make(() -> "HIDDEN_FILE", documentName -> {
                File f = new File(documentName.getName());
                return f.isFile() && ExclusionUtils.isHidden(f);
            }), null
    ),
    /**
     * The files and directories created by an IDEA IDE based tool.
     */
    IDEA("The files and directories created by an IDEA IDE based tool.",
            Arrays.asList(
                    "*.iml",
                    "*.ipr",
                    "*.iws",
                    ".idea/**"), null, null),
    /**
     * The .DS_Store files MAC computer.
     */
    MAC("The .DS_Store files MAC computer.",
            Collections.singletonList("**/.DS_Store"), null, null),
    /**
     * The files and directories created by Maven build system based project.
     */
    MAVEN("The files and directories created by Maven build system based project.",
            Arrays.asList(//
                    "target/**", //
                    "cobertura.ser", //
                    "**/MANIFEST.MF", // a MANIFEST.MF file cannot contain comment lines. In other words: It is not possible, to include a license.
                    "release.properties", //
                    ".repository", // Used by Jenkins when a Maven job uses a private repository that is "Local to the workspace"
                    "build.log", // RAT-160: until now maven-invoker-plugin runs create a build.log that is not part of a release
                    ".mvn/**", // Project configuration since Maven 3.3.1 which contains maven.config, jvm.config, extensions.xml
                    "pom.xml.releaseBackup"), null, null),
    /**
     * The files and directories created by a Mercurial source code control based tool.
     */
    MERCURIAL("The files and directories created by a Mercurial source code control based tool.",
            Arrays.asList("**/.hg/**", ".hgignore"), null, new HgIgnoreProcessor()),
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
     * The files and directories created by a RCS source code control based tool.
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
    private final DocumentNameMatcherSupplier documentNameMatcherSupplier;
    /** The FileProcessor to process the exclude file associated with this exclusion. May be null. */
    private final FileProcessor fileProcessor;
    /** The description of this collection */
    private final String desc;

    StandardCollection(final String desc, final Collection<String> patterns, final DocumentNameMatcherSupplier matcherSupplier,
                       final FileProcessor fileProcessor) {
        this.desc = desc;
        this.patterns = patterns == null ? Collections.emptyList() : new HashSet<>(patterns);
        this.documentNameMatcherSupplier = matcherSupplier;
        this.fileProcessor = fileProcessor;
    }

    /**
     * @return the description of the given collection.
     */
    public String desc() {
        return desc;
    }

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
     * @return the combined and deduped collection of patterns in the given collection.
     */
    public Collection<String> patterns() {
        Set<String> result = new HashSet<>();
        getCollections().forEach(sc -> result.addAll(sc.patterns));
        return result;
    }

    /**
     * Returns the fileProcessor if it exists.
     *
     * @return the fileProcessor if it exists, {@code null} otherwise.
     */
    public ExtendedIterator<FileProcessor> fileProcessor() {
        List<FileProcessor> lst = new ArrayList<>();
        for (StandardCollection sc : getCollections()) {
            if (sc.fileProcessor != null) {
                lst.add(sc.fileProcessor);
            }
        }
        return ExtendedIterator.create(lst.iterator());
    }

    /**
     * Returns the documentNameMatchSupplier if it exists.
     *
     * @return the documentNameMatchSupplier if it exists, {@code null} otherwise.
     */
    public DocumentNameMatcherSupplier documentNameMatcherSupplier() {
        // account for cases where this has more than one supplier.
        List<DocumentNameMatcherSupplier> lst = new ArrayList<>();
        for (StandardCollection sc : getCollections()) {
            if (sc.documentNameMatcherSupplier != null) {
                lst.add(sc.documentNameMatcherSupplier);
            }
        }
        if (lst.isEmpty()) {
            return null;
        }
        if (lst.size() == 1) {
            return lst.get(0);
        }
        Supplier<String> nameSupplier = () -> String.join(", ", ExtendedIterator.create(getCollections().iterator())
                .map(StandardCollection::name).toList());

        return dirName -> TraceableDocumentNameMatcher.make(nameSupplier, documentName -> {
            for (DocumentNameMatcherSupplier supplier : lst) {
                if (supplier.get(dirName).matches(documentName)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Returns {@code true} if the collections has a document name match supplier.
     *
     * @return {@code true} if the collections has a document name match supplier.
     */
    public boolean hasDocumentNameMatchSupplier() {
        // account for cases where this has more than one supplier.
        for (StandardCollection sc : getCollections()) {
            if (sc.documentNameMatcherSupplier != null) {
                return true;
            }
        }
        return false;
    }
}
