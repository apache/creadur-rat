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

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;

/** RAT task-specific properties. */
public interface RatTaskProperties extends RatOptionsTaskBase {

  /**
   * The default output directory for RAT reports. The default value is the project's build
   * directory plus {@code reports} plus the task name, for example {@code build/reports/rat}.
   */
  @OutputDirectory
  DirectoryProperty getReportOutputDirectory();

  /**
   * The RAT XML report output file. Defaults to {@code rat-report.txt} in {@link
   * #getReportOutputDirectory()}.
   */
  @OutputFile
  RegularFileProperty getRatXmlFile();

  /**
   * The RAT text report output file. Defaults to {@code rat-report.txt} in {@link
   * #getReportOutputDirectory()}.
   */
  @OutputFile
  RegularFileProperty getRatTxtFile();

  /**
   * The RAT HTML report output file. Defaults to {@code rat-report.txt} in {@link
   * #getReportOutputDirectory()}.
   */
  @OutputFile
  RegularFileProperty getRatHtmlFile();

  default void applyTaskPropertiesConventions(RatTaskProperties from) {
    getReportOutputDirectory().convention(from.getReportOutputDirectory());
    getRatTxtFile().convention(from.getRatTxtFile());
    getRatXmlFile().convention(from.getRatXmlFile());
    getRatHtmlFile().convention(from.getRatHtmlFile());
  }
}
