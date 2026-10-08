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
import java.util.Optional;
import java.util.Set;

import org.apache.rat.api.EnvVar;

/**
 * Reads the global git configuration files to resolve the file configured
 * for {@code core.excludesFile}.
 * <p>
 * The configuration files are read in git order: {@code $XDG_CONFIG_HOME/git/config}
 * first and {@code ~/.gitconfig} second. The last value found for
 * {@code core.excludesFile} wins.
 * </p>
 * <p>
 * {@code [include] path} directives are supported and the included files are expanded
 * in place, recursively. The relative paths of included files are resolved against the
 * directory of the including file, {@code ~/} is expanded to the home directory and a
 * depth limit of {@value #MAX_INCLUDE_DEPTH} is enforced like git does.
 * </p>
 * <p>
 * {@code includeIf} directives, the system configuration ({@code /etc/gitconfig}) and the
 * repository configuration ({@code .git/config}) are not supported.
 * </p>
 * <p>
 * When {@code core.excludesFile} is not configured the default location next to the XDG
 * configuration ({@code $XDG_CONFIG_HOME/git/ignore} or {@code $HOME/.config/git/ignore})
 * is used.
 * </p>
 */
public class GitConfig {
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
    /** The home directory prefix of a path. */
    private static final String HOME_PREFIX = "~/";
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

    /**
     * Creates a GitConfig reading the configuration files from the environment.
     */
    public GitConfig() {
        this(xdgConfigFile(), userConfigFile(), EnvVar.HOME.getValue());
    }

    /**
     * Creates a GitConfig reading the given configuration files.
     * @param xdgConfig the XDG configuration file or {@code null} if there is none.
     * @param userConfig the user configuration file or {@code null} if there is none.
     * @param home the home directory used for {@code ~/} expansion.
     */
    GitConfig(final File xdgConfig, final File userConfig, final String home) {
        this(xdgConfig, userConfig, home, new File(System.getProperty("user.dir")));
    }

    /**
     * Creates a GitConfig reading the given configuration files.
     * @param xdgConfig the XDG configuration file or {@code null} if there is none.
     * @param userConfig the user configuration file or {@code null} if there is none.
     * @param home the home directory used for {@code ~/} expansion.
     * @param cwd the directory against which relative paths are resolved.
     */
    GitConfig(final File xdgConfig, final File userConfig, final String home, final File cwd) {
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
        parseFile(xdgConfig, configured, includeChain, 0);
        parseFile(userConfig, configured, includeChain, 0);
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
                if (trailingBackslashes(current) % 2 == 1) {
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
     * Resolves an include path.
     * @param value the raw value of the {@code include.path} key.
     * @param includingFile the file that contains the include directive.
     * @return the file to include or an empty Optional if it can not be resolved.
     */
    private Optional<File> resolveInclude(final String value, final File includingFile) {
        if (value.startsWith(HOME_PREFIX)) {
            return expandHome(value);
        }
        if (value.startsWith("~")) {
            return Optional.empty();
        }
        File path = new File(value);
        if (!path.isAbsolute() && includingFile.getParentFile() != null) {
            path = new File(includingFile.getParentFile(), value);
        }
        return Optional.of(path);
    }

    /**
     * Resolves a {@code core.excludesFile} value.
     * @param value the raw value of the {@code core.excludesFile} key.
     * @return the configured file or an empty Optional if it can not be resolved.
     */
    private Optional<File> resolveExcludesFile(final String value) {
        if (value.startsWith(HOME_PREFIX)) {
            return expandHome(value);
        }
        if (value.startsWith("~")) {
            return Optional.empty();
        }
        File path = new File(value);
        if (!path.isAbsolute()) {
            // git resolves relative paths against the current working directory
            path = new File(cwd, value);
        }
        return Optional.of(path);
    }

    /**
     * Expands a {@code ~/} prefixed value against the home directory.
     * @param value the value to expand.
     * @return the expanded file or an empty Optional if no home directory is defined.
     */
    private Optional<File> expandHome(final String value) {
        if (home == null || home.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new File(home, value.substring(HOME_PREFIX.length())));
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
        String home = EnvVar.HOME.getValue();
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
        String home = EnvVar.HOME.getValue();
        return new File(home == null ? "" : home, ".gitconfig");
    }

    /**
     * A parsed section header.
     * @param name the lower cased section name.
     * @param subsection the subsection name or {@code null}.
     */
    private record Section(String name, String subsection) {
    }
}
