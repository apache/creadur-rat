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
package org.apache.rat.tools;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.help.AbstractHelp;
import org.apache.rat.help.AbstractHelp;
import org.apache.rat.help.Licenses;

import static java.lang.String.format;

/**
 * Generates text based documentation for Licenses, LicenceFamilies, and Matchers.
 * Utilizes the same command line as the CLI based Report client so that additional licenses, etc. can be added.
 */
public final class Documentation {

    private Documentation() {
        // do not instantiate.
    }
    /**
     * Creates the documentation. Writes to the output specified by the -o or --out option. Defaults to System.out.
     * @param args the arguments. Try --help for help.
     * @throws IOException on error
     */
    public static void main(final String[] args) throws IOException {


        ReportConfiguration config = OptionCollection.parseCommands(args, Documentation::printUsage, true);
        if (config != null) {
            try (Writer writer = config.getWriter().get()) {
                new Licenses(config, writer).output();
            }
        }
    }

    private static void printUsage(final Options opts) {
        HelpFormatter f = new HelpFormatter();
        f.setOptionComparator(OptionCollection.OPTION_COMPARATOR);
        f.setWidth(AbstractHelp.HELP_WIDTH);
        String header = "\nAvailable options";
        String footer = "";
        String cmdLine = format("java -jar apache-rat/target/apache-rat-CURRENT-VERSION.jar %s",
                Documentation.class.getName());
        f.printHelp(cmdLine, header, opts, footer, false);
        System.exit(0);
    }
}
