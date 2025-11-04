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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.gradle.internal.RatWorkParameters;
import org.apache.rat.report.claim.ClaimStatistic;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
public class TestConventions {
  @InjectSoftAssertions SoftAssertions soft;

  /**
   * Verify that the conventions mapping from the {@link RatExtension} to a {@code RatTask} down to
   * the {@link RatWorkParameters} works.
   *
   * <p>Does not exercise all options/properties, but one of each type.
   */
  @Test
  public void conventionsMapping() {
    var project = ProjectBuilder.builder().build();
    project.getPluginManager().apply("org.apache.rat");

    var extension = project.getExtensions().getByType(RatExtension.class);

    // Property<Boolean> (no arg options)
    extension.getConfigurationNoDefaults().set(true);
    extension.getDryRun().set(false);

    extension.getEditCopyright().set("my copyright");

    extension.getCounterMins().put(ClaimStatistic.Counter.APPROVED, 42);
    extension.getCounterMins().put(ClaimStatistic.Counter.UNAPPROVED, 666);

    extension.getInputIncludes().addAll("one", "two", "three");

    extension
        .getInputExcludeParsedScms()
        .addAll(StandardCollection.GIT, StandardCollection.SUBVERSION);

    extension.getLicensesApprovedFile().set(project.file("approved.txt"));

    extension.getConfigs().from(project.file("my-config.xml"), project.file("my-other-config.xml"));

    var customTaskProvider = project.getTasks().register("customRat", RatTask.class);
    var customTask = customTaskProvider.get();

    var ratTaskProvider = project.getTasks().named("rat", RatTask.class);
    ratTaskProvider.configure(
        task -> {
          task.getConfigurationNoDefaults().set(false);
          task.getDryRun().set(true);

          task.getEditCopyright().set("other copyright");

          task.getCounterMins().put(ClaimStatistic.Counter.APPROVED, 666);
          task.getCounterMins().put(ClaimStatistic.Counter.UNAPPROVED, 42);

          task.getInputIncludes().set(List.of("1", "2", "3"));

          task.getInputExcludeParsedScms()
              .set(List.of(StandardCollection.CVS, StandardCollection.MERCURIAL));

          task.getLicensesApprovedFile().set(project.file("more-approved.txt"));

          // `.from()` is "additive", would need to "unset" the convention first to just have the
          // files below.
          // Example: `task.getConfigs().unsetConvention();`
          task.getConfigs()
              .from(project.file("more-config.xml"), project.file("more-other-config.xml"));
        });

    var ratTask = ratTaskProvider.get();

    // The 'customTask' has no properties on its own instance.
    // All properties default to their convention, the extension object's properties.

    soft.assertThat(customTask.getConfigurationNoDefaults())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, true);
    soft.assertThat(customTask.getDryRun())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, false);
    soft.assertThat(customTask.getEditCopyright())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, "my copyright");
    soft.assertThat(customTask.getCounterMins())
        .extracting(MapProperty::isPresent, MapProperty::get)
        .containsExactly(
            true,
            Map.of(ClaimStatistic.Counter.APPROVED, 42, ClaimStatistic.Counter.UNAPPROVED, 666));
    soft.assertThat(customTask.getInputIncludes())
        .extracting(ListProperty::isPresent, ListProperty::get)
        .containsExactly(true, List.of("one", "two", "three"));
    soft.assertThat(customTask.getInputExcludeParsedScms())
        .extracting(ListProperty::isPresent, ListProperty::get)
        .containsExactly(true, List.of(StandardCollection.GIT, StandardCollection.SUBVERSION));
    soft.assertThat(customTask.getLicensesApprovedFile().getAsFile().get())
        .hasFileName("approved.txt");
    soft.assertThat(customTask.getConfigs().getFiles())
        .map(File::getName)
        .containsExactlyInAnyOrder("my-config.xml", "my-other-config.xml");

    // same assertions for the 'customTask' task

    var customTaskWorkParameters = project.getObjects().newInstance(RatWorkParameters.class);
    customTask.applyWorkParameters(customTaskWorkParameters);
    soft.assertThat(customTaskWorkParameters.getProjectBaseDir().getAsFile().get())
        .isEqualTo(project.getProjectDir());
    soft.assertThat(
            customTaskWorkParameters
                .getRatXmlFile()
                .getAsFile()
                .get()
                .toString()
                .replace('\\', '/'))
        .endsWith("build/reports/customRat/rat-report.xml");
    soft.assertThat(
            customTaskWorkParameters
                .getRatTxtFile()
                .getAsFile()
                .get()
                .toString()
                .replace('\\', '/'))
        .endsWith("build/reports/customRat/rat-report.txt");
    soft.assertThat(
            customTaskWorkParameters
                .getRatHtmlFile()
                .getAsFile()
                .get()
                .toString()
                .replace('\\', '/'))
        .endsWith("build/reports/customRat/rat-report.html");

    soft.assertThat(customTaskWorkParameters.getConfigurationNoDefaults())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, true);
    soft.assertThat(customTaskWorkParameters.getDryRun())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, false);
    soft.assertThat(customTaskWorkParameters.getEditCopyright())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, "my copyright");
    soft.assertThat(customTaskWorkParameters.getCounterMins())
        .extracting(MapProperty::isPresent, MapProperty::get)
        .containsExactly(
            true,
            Map.of(ClaimStatistic.Counter.APPROVED, 42, ClaimStatistic.Counter.UNAPPROVED, 666));
    soft.assertThat(customTaskWorkParameters.getInputIncludes())
        .extracting(ListProperty::isPresent, ListProperty::get)
        .containsExactly(true, List.of("one", "two", "three"));
    soft.assertThat(customTaskWorkParameters.getInputExcludeParsedScms())
        .extracting(ListProperty::isPresent, ListProperty::get)
        .containsExactly(true, List.of(StandardCollection.GIT, StandardCollection.SUBVERSION));
    soft.assertThat(customTaskWorkParameters.getLicensesApprovedFile().getAsFile().get())
        .hasFileName("approved.txt");
    soft.assertThat(customTaskWorkParameters.getConfigs().getFiles())
        .map(File::getName)
        .containsExactlyInAnyOrder("my-config.xml", "my-other-config.xml");

    // The 'ratTask' has custom property values.

    soft.assertThat(ratTask.getConfigurationNoDefaults())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, false);
    soft.assertThat(ratTask.getDryRun())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, true);
    soft.assertThat(ratTask.getEditCopyright())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, "other copyright");
    soft.assertThat(ratTask.getCounterMins())
        .extracting(MapProperty::isPresent, MapProperty::get)
        .containsExactly(
            true,
            Map.of(ClaimStatistic.Counter.APPROVED, 666, ClaimStatistic.Counter.UNAPPROVED, 42));
    soft.assertThat(ratTask.getInputIncludes())
        .extracting(ListProperty::isPresent, ListProperty::get)
        .containsExactly(true, List.of("1", "2", "3"));
    soft.assertThat(ratTask.getInputExcludeParsedScms())
        .extracting(ListProperty::isPresent, ListProperty::get)
        .containsExactly(true, List.of(StandardCollection.CVS, StandardCollection.MERCURIAL));
    soft.assertThat(ratTask.getLicensesApprovedFile().getAsFile().get())
        .hasFileName("more-approved.txt");
    soft.assertThat(ratTask.getConfigs().getFiles())
        .map(File::getName)
        .containsExactlyInAnyOrder(
            "my-config.xml", "my-other-config.xml",
            "more-config.xml", "more-other-config.xml");

    // same assertions for the 'rat' task

    var ratTaskWorkParameters = project.getObjects().newInstance(RatWorkParameters.class);
    ratTask.applyWorkParameters(ratTaskWorkParameters);
    soft.assertThat(ratTask.getProjectBaseDir().getAsFile().get())
        .isEqualTo(project.getProjectDir());
    soft.assertThat(ratTask.getRatXmlFile().getAsFile().get().toString().replace('\\', '/'))
        .endsWith("build/reports/rat/rat-report.xml");
    soft.assertThat(ratTask.getRatXmlFile().getAsFile().get().toString().replace('\\', '/'))
        .endsWith("build/reports/rat/rat-report.xml");
    soft.assertThat(ratTask.getRatTxtFile().getAsFile().get().toString().replace('\\', '/'))
        .endsWith("build/reports/rat/rat-report.txt");
    soft.assertThat(ratTask.getRatHtmlFile().getAsFile().get().toString().replace('\\', '/'))
        .endsWith("build/reports/rat/rat-report.html");

    soft.assertThat(ratTaskWorkParameters.getConfigurationNoDefaults())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, false);
    soft.assertThat(ratTaskWorkParameters.getDryRun())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, true);
    soft.assertThat(ratTaskWorkParameters.getEditCopyright())
        .extracting(Property::isPresent, Property::get)
        .containsExactly(true, "other copyright");
    soft.assertThat(ratTaskWorkParameters.getCounterMins())
        .extracting(MapProperty::isPresent, MapProperty::get)
        .containsExactly(
            true,
            Map.of(ClaimStatistic.Counter.APPROVED, 666, ClaimStatistic.Counter.UNAPPROVED, 42));
    soft.assertThat(ratTaskWorkParameters.getInputIncludes())
        .extracting(ListProperty::isPresent, ListProperty::get)
        .containsExactly(true, List.of("1", "2", "3"));
    soft.assertThat(ratTaskWorkParameters.getInputExcludeParsedScms())
        .extracting(ListProperty::isPresent, ListProperty::get)
        .containsExactly(true, List.of(StandardCollection.CVS, StandardCollection.MERCURIAL));
    soft.assertThat(ratTaskWorkParameters.getLicensesApprovedFile().getAsFile().get())
        .hasFileName("more-approved.txt");
    soft.assertThat(ratTaskWorkParameters.getConfigs().getFiles())
        .map(File::getName)
        .containsExactlyInAnyOrder(
            "my-config.xml", "my-other-config.xml",
            "more-config.xml", "more-other-config.xml");
  }
}
