/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat;

import java.io.File;

import org.apache.commons.cli.Options;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.help.Help;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * The CLI based configuration object for report generation.
 */
public final class Report {

    /**
     * Processes the command line and builds a configuration and executes the
     * report.
     *
     * @param args the arguments.
     * @throws Exception on error.
     */
    public static void main(final String[] args) throws Exception {
        DefaultLog.getInstance().info(new VersionInfo().toString());

        if (args == null || args.length == 0) {
            DefaultLog.getInstance().info("Please use the \"--help\" option to see a " +
                    "list of valid commands and options, as you did not provide any arguments.");
            System.exit(0);
        }

        ReportConfiguration configuration = OptionCollection.parseCommands(new File("."), args, Report::printUsage);
        if (configuration != null) {
            configuration.validate(DefaultLog.getInstance()::error);
            Reporter reporter = new Reporter(configuration);
            reporter.output();
            reporter.writeSummary(DefaultLog.getInstance().asWriter());

            if (configuration.getClaimValidator().hasErrors()) {
                configuration.getClaimValidator().logIssues(reporter.getClaimsStatistic());
                throw new RatDocumentAnalysisException(format("Issues with %s",
                        String.join(", ",
                                configuration.getClaimValidator().listIssues(reporter.getClaimsStatistic()))));
            }
        }
    }

    /**
     * Prints the usage message on {@code System.out}.
     * @param opts The defined options.
     */
    private static void printUsage(final Options opts) {
        new Help(System.out).printUsage(opts);
    }

    private Report() {
        // do not instantiate
    }
}
