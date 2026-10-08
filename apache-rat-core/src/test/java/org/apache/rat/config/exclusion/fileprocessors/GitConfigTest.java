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

import java.io.File;

import org.apache.rat.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GitConfig}.
 */
public class GitConfigTest {

    @TempDir
    private File tempDir;

    private File write(final String name, final String... lines) {
        File file = new File(tempDir, name);
        return FileUtils.writeFile(file.getParentFile(), file.getName(), lines);
    }

    private GitConfig newConfig(final File xdg, final File user, final String home) {
        return new GitConfig(xdg, user, home);
    }

    private GitConfig newConfig(final File xdg, final File user, final String home, final File cwd) {
        return new GitConfig(xdg, user, home, cwd);
    }

    @Test
    public void testCoreExcludesFileFromUserConfig() {
        File ignore = write("ignore.txt", "*.log");
        File userConfig = write("user.gitconfig",
                "[core]",
                "excludesFile = " + ignore.getAbsolutePath());
        GitConfig config = newConfig(null, userConfig, tempDir.toString());
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testCoreExcludesFileKeyIsCaseInsensitive() {
        File ignore = write("ignore.txt", "*.log");
        File userConfig = write("user.gitconfig",
                "[core]",
                "ExCludesFiLe = " + ignore.getAbsolutePath());
        GitConfig config = newConfig(null, userConfig, tempDir.toString());
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testCoreExcludesFileQuotedValue() {
        File ignore = write("ignore file.txt", "*.log");
        File userConfig = write("user.gitconfig",
                "[core]",
                "excludesFile = \"" + ignore.getAbsolutePath() + "\"");
        GitConfig config = newConfig(null, userConfig, tempDir.toString());
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testWindowsStylePathKeepsBackslashes() {
        File userConfig = write("user.gitconfig",
                "[core]",
                "excludesFile = \"some\\dir\\ignore.txt\"");
        GitConfig config = newConfig(null, userConfig, tempDir.toString(), tempDir);
        // unknown escape sequences keep the backslash, matching git behavior
        assertThat(config.coreExcludesFile())
                .hasValue(new File(tempDir, "some\\dir\\ignore.txt"));
    }

    @Test
    public void testCoreExcludesFileComments() {
        File ignore = write("ignore.txt", "*.log");
        File userConfig = write("user.gitconfig",
                "# a comment",
                "; another comment",
                "[core]",
                "excludesFile = " + ignore.getAbsolutePath());
        GitConfig config = newConfig(null, userConfig, tempDir.toString());
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testLastValueWins() {
        File ignore = write("ignore.txt", "*.log");
        File ignored2 = write("ignore2.txt", "*.tmp");
        File userConfig = write("user.gitconfig",
                "[core]",
                "excludesFile = " + ignore.getAbsolutePath(),
                "excludesFile = " + ignored2.getAbsolutePath());
        GitConfig config = newConfig(null, userConfig, tempDir.toString());
        assertThat(config.coreExcludesFile()).hasValue(ignored2);
    }

    @Test
    public void testUserConfigOverridesXdgConfig() {
        File xdgIgnore = write("xdg/ignore.txt", "*.log");
        File userIgnore = write("user/ignore.txt", "*.tmp");
        File xdgConfig = write("xdg/config",
                "[core]",
                "excludesFile = " + xdgIgnore.getAbsolutePath());
        File userConfig = write("user/.gitconfig",
                "[core]",
                "excludesFile = " + userIgnore.getAbsolutePath());
        GitConfig config = newConfig(xdgConfig, userConfig, tempDir.toString());
        // user config wins because the last value wins
        assertThat(config.coreExcludesFile()).hasValue(userIgnore);
    }

    @Test
    public void testTildeExpansion() {
        File ignore = write("home/ignore.txt", "*.log");
        File userConfig = write("user.gitconfig",
                "[core]",
                "excludesFile = ~/ignore.txt");
        File homeDir = new File(tempDir, "home");
        GitConfig config = newConfig(null, userConfig, homeDir.toString());
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testRelativePathResolvedAgainstCwd() {
        File ignore = write("ignore.txt", "*.log");
        File userConfig = write("user.gitconfig",
                "[core]",
                "excludesFile = ignore.txt");
        GitConfig config = newConfig(null, userConfig, tempDir.toString(), tempDir);
        // git resolves relative paths against the current working directory
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testIncludePath() {
        File ignore = write("include/ignore.txt", "*.log");
        File included = write("include/included.conf",
                "[core]",
                "excludesFile = " + ignore.getAbsolutePath());
        File userConfig = write("user.gitconfig",
                "[include]",
                "path = include/included.conf");
        GitConfig config = newConfig(null, userConfig, tempDir.toString());
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testIncludePathRelativeToIncludingFile() {
        File ignore = write("dir/ignore.txt", "*.log");
        File included = write("sub/included.conf",
                "[core]",
                "excludesFile = dir/ignore.txt");
        File userConfig = write("user.gitconfig",
                "[include]",
                "path = sub/included.conf");
        File cwd = tempDir;
        GitConfig config = newConfig(null, userConfig, tempDir.toString(), cwd);
        // the relative excludesFile inside the included file is resolved against the CWD (git behavior)
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testIncludePathTilde() {
        File ignore = write("home/ignore.txt", "*.log");
        File included = write("home/included.conf",
                "[core]",
                "excludesFile = ~/ignore.txt");
        File userConfig = write("user.gitconfig",
                "[include]",
                "path = ~/included.conf");
        File homeDir = new File(tempDir, "home");
        GitConfig config = newConfig(null, userConfig, homeDir.toString());
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testIncludePathRecursive() {
        File ignore = write("include/ignore.txt", "*.log");
        File inner = write("include/inner.conf",
                "[core]",
                "excludesFile = " + ignore.getAbsolutePath());
        File outer = write("include/outer.conf",
                "[include]",
                "path = inner.conf");
        File userConfig = write("user.gitconfig",
                "[include]",
                "path = include/outer.conf");
        GitConfig config = newConfig(null, userConfig, tempDir.toString());
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testIncludePathCycle() {
        File ignore = write("ignore.txt", "*.log");
        File userConfig = write("user.gitconfig",
                "[include]",
                "path = user.gitconfig",
                "[core]",
                "excludesFile = " + ignore.getAbsolutePath());
        GitConfig config = newConfig(null, userConfig, tempDir.toString());
        // cycle must be detected, the value from the including file still applies
        assertThat(config.coreExcludesFile()).hasValue(ignore);
    }

    @Test
    public void testSubsectionsIgnored() {
        File ignore = write("ignore.txt", "*.log");
        File userConfig = write("user.gitconfig",
                "[core \"sub\"]",
                "excludesFile = " + ignore.getAbsolutePath());
        GitConfig config = newConfig(null, userConfig, tempDir.toString());
        assertThat(config.coreExcludesFile()).isEmpty();
    }

    @Test
    public void testMissingFileReturnsConfiguredFile() {
        File userConfig = write("user.gitconfig",
                "[core]",
                "excludesFile = nonexistent/ignore.txt");
        GitConfig config = newConfig(null, userConfig, tempDir.toString(), tempDir);
        // git does not fall back to the default when core.excludesFile is configured
        assertThat(config.coreExcludesFile()).hasValue(new File(tempDir, "nonexistent/ignore.txt"));
    }

    @Test
    public void testNoExcludesFileFallsBackToDefault() {
        File defaultIgnore = write("config/git/ignore", "*.log");
        File xdgConfig = write("config/git/config",
                "[core]",
                "# no excludesFile");
        GitConfig config = newConfig(xdgConfig, null, tempDir.toString());
        assertThat(config.coreExcludesFile()).hasValue(defaultIgnore);
    }
}
