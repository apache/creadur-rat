/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.mp;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatCheckMojoConfigurationTest {
    @Mock
    private MavenProject mavenProject;
    @Mock
    private Model model;
    @Mock
    private Build build;
    @Mock
    private Plugin plugin;
    @Mock
    private PluginManagement pluginManagement;

    @Test
    void testRatCheckMojoConfigurationWithoutBuild() {
        RatCheckMojo mojo = new RatCheckMojo(mavenProject);
        when(mavenProject.getOriginalModel()).thenReturn(model);

        assertThat(mojo.isInheritanceDisabled()).isFalse();
    }

    @Test
    void testRatCheckMojoConfigurationWithoutPluginManagementConfigured() {
        RatCheckMojo mojo = new RatCheckMojo(mavenProject);
        when(mavenProject.getOriginalModel()).thenReturn(model);
        when(model.getBuild()).thenReturn(build);
        when(build.getPlugins()).thenReturn(Arrays.asList(plugin));
        when(build.getPluginManagement()).thenReturn(null); // ensure no pluginManagement

        assertThat(mojo.isInheritanceDisabled()).isFalse();
    }

    @Test
    void testRatCheckMojoConfigurationWithInheritanceDisabledInPluginManagement() {
        RatCheckMojo mojo = new RatCheckMojo(mavenProject);
        when(mavenProject.getOriginalModel()).thenReturn(model);
        when(model.getBuild()).thenReturn(build);
        when(build.getPlugins()).thenReturn(Arrays.asList(plugin));

        when(build.getPluginManagement()).thenReturn(pluginManagement);
        when(pluginManagement.getPlugins()).thenReturn(Arrays.asList(plugin));
        when(plugin.getGroupId()).thenReturn("org.apache.rat");
        when(plugin.getArtifactId()).thenReturn("apache-rat-plugin");
        when(plugin.getInherited()).thenReturn("false");

        assertThat(mojo.isInheritanceDisabled()).isTrue();
    }

    @Test
    void testRatCheckMojoConfigurationWithInheritanceDisabledInPluginConfiguration() {
        RatCheckMojo mojo = new RatCheckMojo(mavenProject);
        when(mavenProject.getOriginalModel()).thenReturn(model);
        when(model.getBuild()).thenReturn(build);
        when(build.getPlugins()).thenReturn(Arrays.asList(plugin));
        when(build.getPluginManagement()).thenReturn(null); // explicitly no pluginManagement
        when(plugin.getGroupId()).thenReturn("org.apache.rat");
        when(plugin.getArtifactId()).thenReturn("apache-rat-plugin");
        when(plugin.getInherited()).thenReturn("false");

        assertThat(mojo.isInheritanceDisabled()).isTrue();
    }
}
