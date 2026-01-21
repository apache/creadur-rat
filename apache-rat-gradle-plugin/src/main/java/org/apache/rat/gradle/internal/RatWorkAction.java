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
package org.apache.rat.gradle.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FileDocument;
import org.apache.rat.gradle.RatTask;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.walker.DirectoryWalker;
import org.gradle.api.GradleException;
import org.gradle.workers.WorkAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actual RAT checks and report generation happens via this Gradle work action, which is called from
 * {@link RatTask} using classloader isolation.
 */
public abstract class RatWorkAction implements WorkAction<RatWorkParameters> {

  /** SLF4j logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(RatWorkAction.class);

  @Override
  public void execute() {

    deleteFiles();

    DefaultLog.setInstance(new Slf4jLogBridge(LOGGER));

    RatOptionsToConfiguration ratArguments = new RatOptionsToConfiguration(getParameters());

    ReportConfiguration config = getConfiguration(ratArguments);

    logLicenses(config.getLicenses(LicenseSetFactory.LicenseFilter.ALL));
    try {
      Reporter reporter = new Reporter(config);

      config.setStyleSheet(RatTask.class.getResource("/org/apache/rat/plain-rat.xsl"));
      config.setOut(getParameters().getRatTxtFile().get().getAsFile());
      reporter.output();

      config.setStyleSheet(RatTask.class.getResource("/org/apache/rat/html.xsl"));
      config.setOut(getParameters().getRatHtmlFile().get().getAsFile());
      reporter.output();

      config.setStyleSheet(RatTask.class.getResource("/org/apache/rat/xml.xsl"));
      config.setOut(getParameters().getRatXmlFile().get().getAsFile());
      reporter.output();

      StringWriter summaryWriter = new StringWriter();
      reporter.writeSummary(summaryWriter);

      check(reporter, config);
    } catch (Exception e) {
      throw new GradleException(e.getMessage(), e);
    }
  }

  private void deleteFiles() {
    try {
      Files.deleteIfExists(getParameters().getRatXmlFile().getAsFile().get().toPath());
      Files.deleteIfExists(getParameters().getRatTxtFile().getAsFile().get().toPath());
      Files.deleteIfExists(getParameters().getRatHtmlFile().getAsFile().get().toPath());

      try (Stream<Path> pathStream =
          Files.walk(getParameters().getReportOutputDirectory().getAsFile().get().toPath())) {
        Iterator<Path> iter =
            pathStream
                .sorted(Comparator.comparingInt(p -> p.toString().length()).reversed())
                .iterator();
        while (iter.hasNext()) {
          Files.deleteIfExists(iter.next());
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to delete RAT report directory or files", e);
    }
  }

  protected void logLicenses(final Collection<ILicense> licenses) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("The following {} licenses are activated:", licenses.size());
      for (ILicense license : licenses) {
        LOGGER.debug("* {}", license);
      }
    }
  }

  protected void check(final Reporter reporter, final ReportConfiguration config) throws Exception {
    ClaimStatistic statistics = reporter.getClaimsStatistic();
    reporter.writeSummary(DefaultLog.getInstance().asWriter());
    if (config.getClaimValidator().hasErrors()) {
      config.getClaimValidator().logIssues(statistics);
      if (!config
          .getClaimValidator()
          .isValid(
              ClaimStatistic.Counter.UNAPPROVED,
              statistics.getCounter(ClaimStatistic.Counter.UNAPPROVED))) {
        try {
          ByteArrayOutputStream buffer = new ByteArrayOutputStream();
          reporter.output(StyleSheets.UNAPPROVED_LICENSES.getStyleSheet(), () -> buffer);
          LOGGER.error(new String(buffer.toByteArray(), StandardCharsets.UTF_8));
        } catch (Exception e) {
          LOGGER.error("Unable to print the files with unapproved licenses to the console.", e);
        }
      }

      String msg =
          String.format(
              "Counter(s) %s exceeded minimum or maximum values.\nSee RAT reports:\n- '%s'\n- '%s'\n- '%s'",
              String.join(", ", config.getClaimValidator().listIssues(statistics)),
              getParameters().getRatHtmlFile().get().getAsFile().toURI(),
              getParameters().getRatTxtFile().get().getAsFile().toURI(),
              getParameters().getRatXmlFile().get().getAsFile().toURI());

      throw new Exception(msg);
    }
  }

  // see org.apache.rat.mp.AbstractRatMojo.getConfiguration
  protected ReportConfiguration getConfiguration(final RatOptionsToConfiguration ratArguments) {
    try {
      File basedir = getParameters().getProjectBaseDir().getAsFile().get();

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("RAT configuration options:");
        for (Map.Entry<String, List<String>> entry : ratArguments.getArgsMap().entrySet()) {
          LOGGER.debug(" * {} {}}", entry.getKey(), String.join(", ", entry.getValue()));
        }
      }

      boolean helpLicenses = !ratArguments.getValues(Arg.HELP_LICENSES).isEmpty();
      ratArguments.removeKey(Arg.HELP_LICENSES);

      ReportConfiguration config =
          OptionCollection.parseCommands(
              basedir,
              ratArguments.args().toArray(new String[0]),
              ignore -> LOGGER.warn("Help option not supported"),
              true);

      DocumentName dirName = DocumentName.builder(basedir).build();
      config.addSource(
          new DirectoryWalker(
              new FileDocument(dirName, basedir, config.getDocumentExcluder(dirName))));

      if (helpLicenses) {
        Writer w = new OutputStreamWriter(System.out);
        new org.apache.rat.help.Licenses(config, w).printHelp();
      }
      return config;
    } catch (Exception e) {
      throw new GradleException("Failed to build RAT ReportConfiguration", e);
    }
  }
}
