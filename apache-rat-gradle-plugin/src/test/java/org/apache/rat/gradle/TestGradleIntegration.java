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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * Gradle build integration tests.
 *
 * <p>Not many functional tests in this class, as integration tests are (by nature) rather slow.
 *
 * <p>Test build directories intentionally use Groovy instead of Kotlin-Script, because the latter
 * requires some expensive preparation steps (Gradle API compilation for Kotlin). That test can be
 * removed, as it is not strictly necessary.
 *
 * <p>The Gradle test-kit automatically "wires" the plugins in the current project to the test
 * builds, a version reference is not needed, it is actually incorrect to specify one in the test
 * builds.
 */
@ExtendWith(SoftAssertionsExtension.class)
public class TestGradleIntegration {
  @InjectSoftAssertions SoftAssertions soft;

  @TempDir Path projectDir;

  @BeforeEach
  void setup(TestInfo testInfo) throws Exception {
    String testCaseDir =
        format(
            "/%s/%s",
            testInfo.getTestClass().get().getSimpleName(),
            testInfo.getTestMethod().get().getName());
    Path templateDir =
        Paths.get(
            Objects.requireNonNull(
                    TestGradleIntegration.class.getResource(testCaseDir),
                    "Test case Gradle project resource directory " + testCaseDir + " not found")
                .toURI());

    Files.walkFileTree(
        templateDir,
        new FileVisitor<Path>() {
          @Override
          public @NotNull FileVisitResult preVisitDirectory(
              Path dir, @NotNull BasicFileAttributes attrs) throws IOException {
            Path relativePath = templateDir.relativize(dir);
            Path target = projectDir.resolve(relativePath);
            if (!Files.isDirectory(target)) {
              Files.createDirectory(target);
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public @NotNull FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs)
              throws IOException {
            Path relativePath = templateDir.relativize(file);
            Path target = projectDir.resolve(relativePath);
            Files.copy(file, target);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public @NotNull FileVisitResult visitFileFailed(Path file, @NotNull IOException exc)
              throws IOException {
            return FileVisitResult.TERMINATE;
          }

          @Override
          public @NotNull FileVisitResult postVisitDirectory(Path dir, @Nullable IOException exc)
              throws IOException {
            return FileVisitResult.CONTINUE;
          }
        });
  }

  @Test
  public void defaultSettings() {
    assertThat(ratOutcome(standardGradleRunner().build()))
        .matches(TestGradleIntegration::isSuccess);

    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.html")).isNotEmptyFile();
  }

  /** Verify that the {@code rat} task is run as a dependency of the {@code check} task. */
  @Test
  public void withJava() {
    assertThat(
            ratOutcome(
                createGradleRunner("--build-cache", "--info", "--stacktrace", "check").build()))
        .matches(TestGradleIntegration::isSuccess);

    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.html")).isNotEmptyFile();
  }

  /**
   * Verify that the {@code rat} task's XML report can be consumed by another task in the same
   * Gradle project.
   *
   * <p>Dependency chain is:
   *
   * <ul>
   *   <li>Task {@code other} declares a task-input from the {@code ratXmlReport} Gradle
   *       configuration.
   *   <li>The {@code ratXmlReport} Gradle configuration is configured with a single artifact for
   *       the XML report file, built by the {@code rat} task.
   * </ul>
   *
   * <p>Executing the task {@code other} therefore depends on the {@code rat} task, and the XML
   * report file must be available.
   */
  @Test
  public void consumeXmlReportInCustomTask() {
    BuildResult buildResult =
        createGradleRunner("--build-cache", "--info", "--stacktrace", "other").build();
    assertThat(ratOutcome(buildResult)).matches(TestGradleIntegration::isSuccess);
    assertThat(buildResult.task(":other").getOutcome()).matches(TestGradleIntegration::isSuccess);

    // We run the 'other' task, which has no "direct" task dependency (aka dependsOn()) to rat.
    // The 'rat' task has to be run.

    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.html")).isNotEmptyFile();
  }

  /** Verify that the {@code rat} task's XML report can be used as a "copy source". */
  @Test
  public void consumeXmlReportInCopyTask() {
    BuildResult buildResult =
        createGradleRunner("--build-cache", "--info", "--stacktrace", "copyXml").build();
    assertThat(ratOutcome(buildResult)).matches(TestGradleIntegration::isSuccess);
    assertThat(buildResult.task(":copyXml").getOutcome()).matches(TestGradleIntegration::isSuccess);

    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.html")).isNotEmptyFile();

    soft.assertThat(projectDir.resolve("build/copied/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/copied/rat-report.txt")).doesNotExist();
    soft.assertThat(projectDir.resolve("build/copied/rat-report.html")).doesNotExist();
  }

  /** Verify that the {@code rat} task's XML report can be used as a "copy source". */
  @Test
  public void consumeAllReportsInCopyTask() {
    BuildResult buildResult =
        createGradleRunner("--build-cache", "--info", "--stacktrace", "copyAll").build();
    assertThat(ratOutcome(buildResult)).matches(TestGradleIntegration::isSuccess);
    assertThat(buildResult.task(":copyAll").getOutcome()).matches(TestGradleIntegration::isSuccess);

    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.html")).isNotEmptyFile();

    soft.assertThat(projectDir.resolve("build/copied/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/copied/rat-report.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/copied/rat-report.html")).isNotEmptyFile();
  }

  /** Verify that the {@code rat} task's XML report can be used as a "copy source". */
  @Test
  public void consumeXmlReportInOtherProject() {
    BuildResult buildResult =
        createGradleRunner("--build-cache", "--info", "--stacktrace", ":a:copyXml").build();
    assertThat(ratOutcome(buildResult)).matches(TestGradleIntegration::isSuccess);
    assertThat(buildResult.task(":a:copyXml").getOutcome())
        .matches(TestGradleIntegration::isSuccess);

    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.html")).isNotEmptyFile();

    soft.assertThat(projectDir.resolve("a/build/copied/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("a/build/copied/rat-report.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("a/build/copied/rat-report.html")).isNotEmptyFile();
  }

  @Test
  public void kotlinDefaultSettings() {
    assertThat(ratOutcome(standardGradleRunner().build()))
        .matches(TestGradleIntegration::isSuccess);

    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("build/reports/rat/rat-report.html")).isNotEmptyFile();
  }

  @Test
  public void customOutputDirectory() {
    assertThat(ratOutcome(standardGradleRunner().build()))
        .matches(TestGradleIntegration::isSuccess);

    soft.assertThat(projectDir.resolve("build/reports/rat")).doesNotExist();

    soft.assertThat(projectDir.resolve("custom/output/rat-report.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("custom/output/rat-report.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("custom/output/rat-report.html")).isNotEmptyFile();
  }

  @Test
  public void customOutputFiles() {
    assertThat(ratOutcome(standardGradleRunner().build()))
        .matches(TestGradleIntegration::isSuccess);

    soft.assertThat(projectDir.resolve("build/reports/rat")).doesNotExist();

    // Not great that this directory is created, but it doesn't hurt at all.
    // This assertion isn't really needed and can be changed or removed when necessary.
    soft.assertThat(projectDir.resolve("custom")).isEmptyDirectory();

    soft.assertThat(projectDir.resolve("tech/that.xml")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("my/output.txt")).isNotEmptyFile();
    soft.assertThat(projectDir.resolve("my/output.txt")).isNotEmptyFile();
  }

  @Test
  public void badLicenseFailsBuild() {
    assertThat(ratOutcome(standardGradleRunner().buildAndFail())).isSameAs(TaskOutcome.FAILED);
  }

  private static boolean isSuccess(TaskOutcome outcome) {
    // All these outcomes represent a successful task execution.
    // SUCCESS -> task successfully executed
    // UP_TO_DATE -> task inputs match the inputs of a previous successful execution, output is
    // up-to-date
    // FROM_CACHE -> task output was loaded from the build cache using the inputs of the task
    // (Note: the latter two are currently impossible, see RatTask constructor)
    return outcome == TaskOutcome.SUCCESS
        || outcome == TaskOutcome.UP_TO_DATE
        || outcome == TaskOutcome.FROM_CACHE;
  }

  private static TaskOutcome ratOutcome(BuildResult result) {
    return result.task(":rat").getOutcome();
  }

  private GradleRunner standardGradleRunner(String... args) {
    return createGradleRunner("--build-cache", "--info", "--stacktrace", "rat");
  }

  private GradleRunner createGradleRunner(String... args) {
    return GradleRunner.create()
        .withPluginClasspath()
        .withProjectDir(projectDir.toFile())
        .withArguments(args)
        .withDebug(true)
        .forwardOutput();
  }
}
