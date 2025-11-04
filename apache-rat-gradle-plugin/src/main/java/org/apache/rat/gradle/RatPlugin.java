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
package org.apache.rat.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apache RAT Gradle plugin.
 *
 * <p>Apply this plugin to your project's root project.
 *
 * <p>Configurations should be configured on the {@code rat} extension of type {@link RatExtension}.
 *
 * <p>Registers the default {@code rat} task and lets the {@code check} task depend on it, if the
 * {@code check} task exists at the time when this RAT plugin is applied. Task-specific properties,
 * like the report output directory and/or report-specific output files, are configured on the task
 * and not the extension.
 *
 * <p>If multiple RAT reports with different RAT configurations are needed, register a custom task
 * of type {@link RatTask} and configure it accordingly. All {@link RatTask} RAT tasks inherit the
 * configurations from the {@code rat} extension.
 */
@SuppressWarnings({"unused", "NullableProblems"})
public abstract class RatPlugin implements Plugin<Project> {
  /** SLF4j logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(RatPlugin.class);

  @Override
  public void apply(final Project project) {
    project.getExtensions().create("rat", RatExtension.class);

    var ratTask = project.getTasks().register("rat", RatTask.class);
    RatTask.setupRatTaskConfigurations(ratTask, project);

    try {
      project.getTasks().named("check").configure(task -> task.dependsOn("rat"));
    } catch (UnknownTaskException ignore) {
      // Log at level 'INFO' to not spam people's build output.
      LOGGER.info(
          "The project '{}' does not have a 'check' task available when the Apache RAT Gradle plugin is applied. "
              + "If another plugin registers a 'check' task, consider changing the order of the plugins in the 'plugins' block. "
              + "If your build script registers a 'check' task later, consider configuring with with a 'dependsOn(\"rat\")'",
          project.getParent());
    }
  }
}
