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
package org.apache.rat.config.exclusion.fileprocessors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.rat.api.EnvVar;
import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentName.FSInfo;
import org.apache.rat.document.DocumentNameMatcher;

import static org.apache.rat.config.exclusion.ExclusionUtils.NEGATION_PREFIX;

/**
 * Processes the {@code .gitignore} file.
 * @see <a href='https://git-scm.com/docs/gitignore'>.gitignore documentation</a>
 */
public class GitIgnoreBuilder extends AbstractFileProcessorBuilder {
    /** The name of the file we read from */
    private static final String IGNORE_FILE = ".gitignore";
    /** The comment prefix */
    private static final String COMMENT_PREFIX = "#";
    /** An escaped comment in the .gitignore file. (Not a comment) */
    private static final String ESCAPED_COMMENT = "\\#";
    /** An escaped negation in the .gitignore file. (Not a negation) */
    private static final String ESCAPED_NEGATION = "\\!";
    /** The slash string */
    private static final String SLASH = "/";

    /**
     * Constructs a file processor that processes a {@code .gitignore} file and ignores any lines starting with {@value #COMMENT_PREFIX}.
     */
    public GitIgnoreBuilder() {
        super(IGNORE_FILE, COMMENT_PREFIX, true);
    }

    private MatcherSet processGlobalIgnore(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName root, final DocumentName globalGitIgnore) {
        final MatcherSet.Builder matcherSetBuilder = new MatcherSet.Builder();
        final List<String> iterable = new ArrayList<>();
        ExclusionUtils.asIterator(globalGitIgnore.asFile(), commentFilter)
                .map(entry -> modifyEntry(matcherSetConsumer, globalGitIgnore, entry).orElse(null))
                .filter(Objects::nonNull)
                .map(entry -> ExclusionUtils.qualifyPattern(root, entry))
                .forEachRemaining(iterable::add);

        Set<String> included = new HashSet<>();
        Set<String> excluded = new HashSet<>();
        MatcherSet.Builder.segregateList(excluded, included, iterable);
        DocumentName displayName = DocumentName.builder(root).setName("global gitignore").build();
        matcherSetBuilder.addExcluded(displayName, excluded);
        matcherSetBuilder.addIncluded(displayName, included);
        return matcherSetBuilder.build();
    }

    @Override
    protected MatcherSet process(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName root, final DocumentName documentName) {
      if (root.equals(documentName.getBaseDocumentName())) {
          Optional<File> globalGitIgnore = globalGitIgnore();
          List<MatcherSet> matcherSets = new ArrayList<>();
          matcherSets.add(super.process(matcherSetConsumer, root, documentName));
          if (globalGitIgnore.isPresent()) {
              LevelBuilder levelBuilder = getLevelBuilder(Integer.MAX_VALUE);
              DocumentName ignore = DocumentName.builder(globalGitIgnore.get()).build();
              matcherSets.add(processGlobalIgnore(levelBuilder::add, root, ignore));
          }
          return MatcherSet.merge(matcherSets);
      } else {
          return super.process(matcherSetConsumer, root, documentName);
      }
    }

    /**
     * Convert the string entry.
     * If the string ends with a slash an {@link DocumentNameMatcher#and} is constructed from a directory check and the file
     * name matcher. In this case an empty Optional is returned.
     * If the string starts with {@value ExclusionUtils#NEGATION_PREFIX} then the entry is placed in the include list, otherwise
     * the entry is placed in the exclude list and the name of the check returned.
     * @param documentName The name of the document being processed.
     * @param entry The entry from the document
     * @return and Optional containing the name of the matcher.
     */
    @Override
    protected Optional<String> modifyEntry(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName documentName, final String entry) {
        // An optional prefix "!" which negates the pattern;
        boolean prefix = entry.startsWith(NEGATION_PREFIX);
        String pattern = prefix || entry.startsWith(ESCAPED_COMMENT) || entry.startsWith(ESCAPED_NEGATION) ?
                entry.substring(1) : entry;

        // If there is a separator at the beginning or middle (or both) of the pattern, then
        // the pattern is relative to the directory level of the particular .gitignore file itself.
        // Otherwise, the pattern may also match at any level below the .gitignore level.
        int slashPos = pattern.indexOf(SLASH);
        // no slash or at end already
        if (slashPos == -1 || slashPos == pattern.length() - 1) {
            pattern = "**/" + pattern;
        }
        if (slashPos == 0) {
            pattern = pattern.substring(1);
        }
        // If there is a separator at the end of the pattern then the pattern will only match directories,
        // otherwise the pattern can match both files and directories.
        if (pattern.endsWith(SLASH)) {
            pattern = pattern.substring(0, pattern.length() - 1);
            String name = prefix ? NEGATION_PREFIX + pattern : pattern;
            DocumentName matcherPattern = DocumentName.builder(documentName).setName(name.replace(SLASH, documentName.getDirectorySeparator()))
                    .build();
            DocumentNameMatcher matcher = DocumentNameMatcher.and(new DocumentNameMatcher("isDirectory", File::isDirectory),
                    new DocumentNameMatcher(name, MatchPatterns.from(matcherPattern.localized(documentName.getDirectorySeparator()))));

            MatcherSet.Builder builder = new MatcherSet.Builder();
            if (prefix) {
                builder.addIncluded(matcher);
            } else {
                builder.addExcluded(matcher);
            }
            matcherSetConsumer.accept(builder.build());
            return Optional.empty();
        }
        return Optional.of(prefix ? NEGATION_PREFIX + pattern : pattern);
    }

    /**
     * The global gitignore file to process, based on the
     * {@link EnvVar#RAT_NO_GIT_GLOBAL_IGNORE} and {@link EnvVar#RAT_GIT_GLOBAL_IGNORE}
     * environment variables, the git configuration ({@code core.excludesFile} including
     * {@code [include] path} directives and {@code GIT_CONFIG_GLOBAL}) and the
     * {@link EnvVar#XDG_CONFIG_HOME} environment variable.
     * <p>
     * The global ignore file is read exactly once, anchored at the scan {@code root}, and its
     * relative {@code core.excludesFile} path is resolved against the process working directory.
     * It is not re-resolved when the scan descends into subdirectories, matching git where
     * patterns from {@code core.excludesFile} are treated as if specified at the root of the
     * working tree.
     * </p>
     * <p>
     * {@link EnvVar#RAT_NO_GIT_GLOBAL_IGNORE} takes precedence over {@link EnvVar#RAT_GIT_GLOBAL_IGNORE}.
     * When {@link EnvVar#RAT_GIT_GLOBAL_IGNORE} is {@code false} or an explicit path, the scan result does
     * not depend on the machine the audit runs on, which is recommended for reproducible CI/release builds.
     * </p>
     */
    protected Optional<File> globalGitIgnore() {
        if (EnvVar.RAT_NO_GIT_GLOBAL_IGNORE.isSet()) {
            return Optional.empty();
        }
        return resolveGlobalGitIgnore(EnvVar.RAT_GIT_GLOBAL_IGNORE.getValue());
    }

    /**
     * Resolves the global ignore file honoring the {@code RAT_GIT_GLOBAL_IGNORE} override value.
     * <p>
     * The override is a three-state value: a {@code "false"} disables the global ignore, a
     * {@code "true"} (or an unset variable) falls back to the git configuration resolution, and
     * any other value is treated as an explicit global ignore path that, when it exists, is used
     * as is so the scan result does not depend on the machine the audit runs on.
     * </p>
     * <p>
     * Because the three states share the same string value, the value may not be both a boolean
     * keyword and a file name: a file that literally is named {@code true} or {@code false}
     * cannot be pinned with this variable.
     * </p>
     * @param override the raw value of the {@code RAT_GIT_GLOBAL_IGNORE} environment variable or
     *        {@code null} if it is not set.
     * @return the resolved global ignore file or an empty Optional if none must be used.
     */
    static Optional<File> resolveGlobalGitIgnore(final String override) {
        if (override != null) {
            String value = override.strip();
            if ("false".equalsIgnoreCase(value)) {
                return Optional.empty();
            }
            if (!"true".equalsIgnoreCase(value)) {
                // an explicit path always uses that file if it exists; no fall back to the git config
                File file = new File(value);
                return file.isFile() ? Optional.of(file) : Optional.empty();
            }
        }
        // a configured but missing file yields an empty ignore (no fall back to the default),
        // matching git behavior
        return new GitConfig().coreExcludesFile().filter(File::isFile);
    }

    /**
     * Reads the global git configuration files to resolve the file configured
     * for {@code core.excludesFile}.
     * <p>
     * It is a {@code protected static} nested type so that it stays independently unit-testable
     * while keeping the git-config reading logic co-located with the {@link GitIgnoreBuilder} that
     * consumes it.
     * </p>
     * <p>
     * The configuration files are read in git order: {@code $XDG_CONFIG_HOME/git/config}
     * first and {@code ~/.gitconfig} second. The last value found for
     * {@code core.excludesFile} wins.
     * </p>
     * <p>
     * {@code [include] path} directives are supported and the included files are expanded
     * in place, recursively. The relative paths of included files are resolved against the
     * directory of the including file, {@code ~/} (or {@code ~\}) is expanded to the home
     * directory and a depth limit of {@value #MAX_INCLUDE_DEPTH} is enforced like git does.
     * </p>
     * <p>
     * {@code includeIf} directives, the system configuration ({@code /etc/gitconfig}), the
     * {@code GIT_CONFIG_SYSTEM} variable and the repository configuration ({@code .git/config}) are
     * not supported. The {@code GIT_CONFIG_GLOBAL} variable, when set, replaces the
     * {@code $HOME/.gitconfig} and {@code $XDG_CONFIG_HOME/git/config} files.
     * </p>
     * <p>
     * The home directory used for {@code ~} expansion is resolved from the {@code user.home} system
     * property. On modern JVMs this matches the operating system home directory on every platform,
     * so no platform specific environment variable lookup is needed.
     * </p>
     * <p>
     * When {@code core.excludesFile} is not configured the default location next to the XDG
     * configuration ({@code $XDG_CONFIG_HOME/git/ignore} or {@code $HOME/.config/git/ignore})
     * is used.
     * </p>
     */
    protected static final class GitConfig {
        /** The name of the 'core' section. */
        private static final String CORE_SECTION = "core";
        /** The name of the 'include' section. */
        private static final String INCLUDE_SECTION = "include";
        /** The key of the core section holding the global ignore file. */
        private static final String EXCLUDES_FILE_KEY = "excludesfile";
        /** The key of the include section holding the file to include. */
        private static final String INCLUDE_PATH_KEY = "path";
        /** The maximum depth of nested includes, matching the git limit. */
        private static final int MAX_INCLUDE_DEPTH = 10;
        /** The comment marker for a full line comment. */
        private static final String COMMENT_PREFIX = "#";
        /** The alternate comment marker for a full line comment. */
        private static final String SEMICOLON_COMMENT_PREFIX = ";";
        /** The opening quote of a quoted value. */
        private static final String QUOTE = "\"";
        /** The escape character of a quoted value. */
        private static final String ESCAPE = "\\";

        /** The XDG configuration file. */
        private final File xdgConfig;
        /** The user configuration file. */
        private final File userConfig;
        /** The home directory or {@code null} when not defined. */
        private final String home;
        /** The directory against which relative paths are resolved. */
        private final File cwd;
        /** The default global ignore file, next to the XDG configuration or {@code null}. */
        private final File defaultIgnore;
        /** The file configured by the {@code GIT_CONFIG_GLOBAL} environment variable or {@code null}. */
        private final File globalConfig;
        /** The file system info of the default file system used to build paths. */
        private final FSInfo fsInfo = FSInfo.getDefault();
        /** The home directory prefix for this file system (e.g. {@code ~/} on Unix, {@code ~\} on Windows). */
        private final String homePrefix = "~" + fsInfo.dirSeparator();
        /** The alternate home directory prefix using the other separator, for portable git configs. */
        private final String homeAlternatePrefix = "~" + (fsInfo.dirSeparator().equals("/") ? "\\" : "/");

        /**
         * Creates a GitConfig reading the configuration files from the environment.
         */
        public GitConfig() {
            this(gitConfigGlobalFile(), xdgConfigFile(), userConfigFile(), homeDir(),
                    new File(System.getProperty("user.dir")));
        }

        /**
         * Creates a GitConfig reading the given configuration files.
         * @param xdgConfig the XDG configuration file or {@code null} if there is none.
         * @param userConfig the user configuration file or {@code null} if there is none.
         * @param home the home directory used for {@code ~} expansion.
         */
        GitConfig(final File xdgConfig, final File userConfig, final String home) {
            this(null, xdgConfig, userConfig, home, new File(System.getProperty("user.dir")));
        }

        /**
         * Creates a GitConfig reading the given configuration files.
         * @param xdgConfig the XDG configuration file or {@code null} if there is none.
         * @param userConfig the user configuration file or {@code null} if there is none.
         * @param home the home directory used for {@code ~} expansion.
         * @param cwd the directory against which relative paths are resolved.
         */
        GitConfig(final File xdgConfig, final File userConfig, final String home, final File cwd) {
            this(null, xdgConfig, userConfig, home, cwd);
        }

        /**
         * Creates a GitConfig reading the given configuration files.
         * @param globalConfig the effective global config file (from {@code GIT_CONFIG_GLOBAL})
         *        or {@code null} to use the XDG and user configuration files.
         * @param xdgConfig the XDG configuration file or {@code null} if there is none.
         * @param userConfig the user configuration file or {@code null} if there is none.
         * @param home the home directory used for {@code ~} expansion.
         * @param cwd the directory against which relative paths are resolved.
         */
        GitConfig(final File globalConfig, final File xdgConfig, final File userConfig, final String home, final File cwd) {
            this.globalConfig = globalConfig;
            this.xdgConfig = xdgConfig;
            this.userConfig = userConfig;
            this.home = home;
            this.cwd = cwd;
            File parent = xdgConfig == null ? null : xdgConfig.getAbsoluteFile().getParentFile();
            this.defaultIgnore = parent == null ? null : new File(parent, "ignore");
        }

        /**
         * Resolves the global git ignore file.
         * <p>
         * If {@code core.excludesFile} is configured in one of the git configuration files the configured
         * file is returned - even if it does not exist, matching git behavior where a configured file
         * disables the default location. Otherwise the default location is returned if it exists.
         * </p>
         * @return the configured or default global git ignore file or an empty Optional if neither exists.
         */
        public Optional<File> coreExcludesFile() {
            List<String> configured = new ArrayList<>();
            Set<File> includeChain = new HashSet<>();
            if (globalConfig != null) {
                // when GIT_CONFIG_GLOBAL is set, neither ~/.gitconfig nor $XDG_CONFIG_HOME/git/config are read
                parseFile(globalConfig, configured, includeChain, 0);
            } else {
                parseFile(xdgConfig, configured, includeChain, 0);
                parseFile(userConfig, configured, includeChain, 0);
            }
            if (!configured.isEmpty()) {
                String value = configured.get(configured.size() - 1);
                return resolveExcludesFile(value);
            }
            return defaultIgnore != null && defaultIgnore.isFile() ? Optional.of(defaultIgnore) : Optional.empty();
        }

        /**
         * Parses a configuration file, expanding {@code [include] path} directives in place.
         * @param file the file to parse or {@code null} if there is none.
         * @param excludesFileValues the collected raw {@code core.excludesFile} values in order.
         * @param includeChain the canonical files of the current include chain to detect cycles.
         * @param depth the current include depth.
         */
        private void parseFile(final File file, final List<String> excludesFileValues, final Set<File> includeChain, final int depth) {
            if (file == null || depth > MAX_INCLUDE_DEPTH) {
                return;
            }
            final File canonical = canonical(file);
            if (!canonical.isFile() || !includeChain.add(canonical)) {
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                String section = "";
                String subsection = null;
                String pending = null;
                String line;
                while ((line = reader.readLine()) != null) {
                    String current = pending == null ? line : pending + line;
                    if ((trailingBackslashes(current) & 1) == 1) {
                        pending = current.substring(0, current.length() - 1);
                        continue;
                    }
                    pending = null;
                    current = current.strip();
                    if (current.isEmpty() || current.startsWith(COMMENT_PREFIX) || current.startsWith(SEMICOLON_COMMENT_PREFIX)) {
                        continue;
                    }
                    if (current.startsWith("[")) {
                        Section parsedSection = parseSection(current);
                        if (parsedSection != null) {
                            section = parsedSection.name();
                            subsection = parsedSection.subsection();
                        }
                        continue;
                    }
                    int separator = current.indexOf('=');
                    if (separator < 0) {
                        continue;
                    }
                    String key = current.substring(0, separator).strip().toLowerCase(Locale.ROOT);
                    String value = current.substring(separator + 1).strip();
                    if (key.isEmpty() || value.isEmpty()) {
                        continue;
                    }
                    String unquoted = unquote(value);
                    if (CORE_SECTION.equals(section) && subsection == null && EXCLUDES_FILE_KEY.equals(key)) {
                        excludesFileValues.add(unquoted);
                    } else if (INCLUDE_SECTION.equals(section) && subsection == null && INCLUDE_PATH_KEY.equals(key)) {
                        resolveInclude(unquoted, file).ifPresent(included -> parseFile(included, excludesFileValues, includeChain, depth + 1));
                    }
                }
            } catch (IOException e) {
                // unreadable configuration files are ignored like git does
            } finally {
                includeChain.remove(canonical);
            }
        }

        /**
         * Parses a section header.
         * @param line the line starting with '['.
         * @return the parsed section or {@code null} if the line is not a valid section header.
         */
        private Section parseSection(final String line) {
            int end = line.indexOf(']');
            if (end < 0) {
                return null;
            }
            String inner = line.substring(1, end).strip();
            int quote = inner.indexOf(QUOTE);
            String name;
            String subsection = null;
            if (quote >= 0) {
                name = inner.substring(0, quote).strip();
                int close = inner.indexOf(QUOTE, quote + 1);
                if (close >= 0) {
                    subsection = inner.substring(quote + 1, close);
                }
            } else {
                int dot = inner.indexOf('.');
                if (dot >= 0) {
                    name = inner.substring(0, dot).strip();
                    subsection = inner.substring(dot + 1).strip();
                } else {
                    name = inner;
                }
            }
            return name.isEmpty() ? null : new Section(name.toLowerCase(Locale.ROOT), subsection);
        }

        /**
         * Resolves an include path relative to the directory of the including file.
         * @param value the raw value of the {@code include.path} key.
         * @param includingFile the file that contains the include directive.
         * @return the file to include or an empty Optional if it can not be resolved.
         */
        private Optional<File> resolveInclude(final String value, final File includingFile) {
            File base = includingFile == null ? null : includingFile.getParentFile();
            return resolve(value, base);
        }

        /**
         * Resolves a {@code core.excludesFile} value against the current working directory.
         * @param value the raw value of the {@code core.excludesFile} key.
         * @return the configured file or an empty Optional if it can not be resolved.
         */
        private Optional<File> resolveExcludesFile(final String value) {
            return resolve(value, cwd);
        }

        /**
         * Resolves a path value, expanding a leading {@code ~} followed by a separator against the
         * home directory and otherwise resolving relative paths against the given base directory.
         * <p>
         * Both a forward slash and a back slash are accepted after the {@code ~} because git
         * configuration files are portable: the same {@code ~/.gitconfig} may use either separator
         * regardless of the host operating system.
         * </p>
         * @param value the raw configured value.
         * @param base the directory against which a relative (non-tilde) path is resolved, or
         *        {@code null} if relative paths must be left as-is.
         * @return the resolved file or an empty Optional if the value can not be resolved.
         */
        private Optional<File> resolve(final String value, final File base) {
            if (isHomePrefixed(value)) {
                return expandHome(value);
            }
            if (value.startsWith("~")) {
                return Optional.empty();
            }
            File path = new File(value);
            if (!path.isAbsolute() && base != null) {
                path = new File(base, value);
            }
            return Optional.of(path);
        }

        /**
         * Determines if the value starts with a home directory prefix for this file system or the
         * alternate separator, so portable git configuration files resolve on any host.
         * @param value the value to check.
         * @return {@code true} if the value starts with a home directory prefix.
         */
        private boolean isHomePrefixed(final String value) {
            return value.startsWith(homePrefix) || value.startsWith(homeAlternatePrefix);
        }

        /**
         * Expands a home directory prefixed value ({@code ~} followed by a separator) against the
         * home directory.
         * @param value the value to expand.
         * @return the expanded file or an empty Optional if no home directory is defined.
         */
        private Optional<File> expandHome(final String value) {
            if (home == null || home.isEmpty()) {
                return Optional.empty();
            }
            String rest = value.startsWith(homePrefix) ? value.substring(homePrefix.length())
                    : value.substring(homeAlternatePrefix.length());
            return Optional.of(new File(home, rest));
        }

        /**
         * Counts the number of trailing backslashes of a line.
         * @param line the line to check.
         * @return the number of trailing backslashes.
         */
        private int trailingBackslashes(final String line) {
            int count = 0;
            for (int i = line.length() - 1; i >= 0 && line.charAt(i) == '\\'; i--) {
                count++;
            }
            return count;
        }

        /**
         * Unquotes a value if it is quoted, resolving the git escape sequences.
         * @param value the raw value.
         * @return the unquoted value.
         */
        private String unquote(final String value) {
            if (!value.startsWith(QUOTE) || value.length() < 2) {
                return value;
            }
            StringBuilder result = new StringBuilder(value.length() - 2);
            int i = 1;
            while (i < value.length()) {
                char c = value.charAt(i);
                if (c == ESCAPE.charAt(0) && i + 1 < value.length()) {
                    i++;
                    char escaped = value.charAt(i);
                    switch (escaped) {
                        case 'n':
                            result.append('\n');
                            break;
                        case 't':
                            result.append('\t');
                            break;
                        case 'b':
                            result.append('\b');
                            break;
                        case '"':
                            result.append('"');
                            break;
                        case '\\':
                            result.append('\\');
                            break;
                        default:
                            // git keeps the backslash for unknown escape sequences
                            result.append('\\').append(escaped);
                            break;
                    }
                } else if (c == '"') {
                    break;
                } else {
                    result.append(c);
                }
                i++;
            }
            return result.toString();
        }

        /**
         * Gets the canonical file.
         * @param file the file to canonicalize.
         * @return the canonical file or the absolute file if canonicalization failed.
         */
        private File canonical(final File file) {
            try {
                return file.getCanonicalFile();
            } catch (IOException e) {
                return file.getAbsoluteFile();
            }
        }

        /**
         * Gets the XDG configuration file from the environment.
         * @return the XDG configuration file.
         */
        private static File xdgConfigFile() {
            String xdgConfigHome = EnvVar.XDG_CONFIG_HOME.getValue();
            String home = homeDir();
            File base = xdgConfigHome != null && !xdgConfigHome.isEmpty()
                    ? new File(xdgConfigHome)
                    : new File(home == null ? "" : home, ".config");
            return new File(new File(base, "git"), "config");
        }

        /**
         * Gets the user configuration file from the environment.
         * @return the user configuration file.
         */
        private static File userConfigFile() {
            String home = homeDir();
            return new File(home == null ? "" : home, ".gitconfig");
        }

        /**
         * Gets the file configured by the {@code GIT_CONFIG_GLOBAL} environment variable.
         * <p>When set, neither {@code $HOME/.gitconfig} nor {@code $XDG_CONFIG_HOME/git/config} are
         * read (matching git). Git treats the value as a single file path.</p>
         * @return the global config file or {@code null} if the variable is not set.
         */
        private static File gitConfigGlobalFile() {
            String value = EnvVar.GIT_CONFIG_GLOBAL.getValue();
            return value == null || value.isEmpty() ? null : new File(value);
        }

        /**
         * Resolves the user home directory from the {@code user.home} system property.
         * <p>On modern JVMs this equals the operating system home directory on every platform,
         * so no platform specific environment variable lookup is needed.</p>
         * @return the home directory or {@code null} if not defined.
         */
        private static String homeDir() {
            return System.getProperty("user.home");
        }

        /**
         * A parsed section header.
         * @param name the lower cased section name.
         * @param subsection the subsection name or {@code null}.
         */
        private record Section(String name, String subsection) {
        }
    }
}
