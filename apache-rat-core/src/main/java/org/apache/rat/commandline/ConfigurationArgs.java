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
package org.apache.rat.commandline;

import java.net.MalformedURLException;

import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.rat.Defaults;

/**
 * Processes command line arguments that are specific to configuration of the build.
 * @since 0.17
 */
public final class ConfigurationArgs {

    /** group of options that read a configuraiton file */
    private static final OptionGroup CONFIGURATION = new OptionGroup()
            .addOption(Option.builder().longOpt("config").hasArgs().argName("File")
            .desc("File names or URIs for system configuration.  May be followed by multiple arguments. "
                    + "Note that '--' or a following option is required when using this parameter.")
            .build())
            .addOption(Option.builder().longOpt("licenses").hasArgs().argName("File")
            .desc("File names or URLs for license definitions.  May be followed by multiple arguments. "
                    + "Note that '--' or a following option is required when using this parameter.")
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --config").get())
            .build());

    /** group of options that skip the default configuraiton file */
    private static final OptionGroup CONFIGURATION_NO_DEFAULTS  = new OptionGroup()
            .addOption(Option.builder().longOpt("configuration-no-defaults")
                            .desc("Ignore default configuration.").build())
            .addOption(Option.builder().longOpt("no-default-licenses")
            .desc("Ignore default configuration. By default all approved default licenses are used")
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --configuration-no-defaults").get())
            .build());

//    /** Option that add approved licenses to the list */
//    private static final Option LICENSES_APPROVED = Option.builder().longOpt("licenses-approved").hasArgs().argName("LicenseID")
//            .desc("The approved License IDs.  These licenses will be added to the list of approved licenses. " +
//                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
//            .build();
//
//    /** Option that add approved licenses from a file */
//    private static final Option LICENSES_APPROVED_FILE = Option.builder().longOpt("licenses-approved-file").hasArg().argName("FileOrURI")
//            .desc("File name or URI containing the approved license IDs.")
//            .build();
//
//    /** Option that specifies approved license families */
//    private static final Option LICENSES_FAMILIES = Option.builder().longOpt("license-families").hasArgs().argName("LicenseID")
//            .desc("The approved License IDs.  These licenses will be added to the list of approved licenses. " +
//                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
//            .build();
//
//    /** Option that specifies apporoved license families from a file */
//    private static final Option LICENSES_FAMILIES_FILE = Option.builder().longOpt("license-families-file").hasArg().argName("FileOrURI")
//            .desc("File name or URI containing the approved license IDs.")
//            .build();
//
//    /** Option to remove licenses from the approved list */
//    private static final Option LICENSES_REMOVE_APPROVED = Option.builder().longOpt("licenses-remove-approved").hasArgs().argName("LicenseID")
//            .desc("The approved License IDs.  These licenses will be added to the list of approved licenses. " +
//                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
//            .build();
//
//    /** Option to read a file licenses to be removed from the approved list */
//    private static final Option LICENSES_REMOVE_APPROVED_FILE = Option.builder().longOpt("licenses-remove-approved-file").hasArg().argName("FileOrURI")
//            .desc("File name or URI containing the approved license IDs.")
//            .build();
//
//    /** Option to list license families to remove from the approved list */
//    private static final Option LICENSES_REMOVE_FAMILIES = Option.builder().longOpt("licenses-remove-families").hasArgs().argName("LicenseID")
//            .desc("The approved License IDs.  These licenses will be added to the list of approved licenses. " +
//                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
//            .build();
//
//    /** Option to read a list of license families to remove from the approved list */
//    private static final Option LICENSES_REMOVE_FAMILIES_FILE = Option.builder().longOpt("licenses-remove-families-file").hasArg().argName("FileOrURI")
//            .desc("File name or URI containing the approved license IDs.")
//            .build();

    private ConfigurationArgs() {
        // do not instantiate
    }

    /**
     * Adds the options from this set of options to the options argument.
     * @param options the options to add to.
     */
    public static void addOptions(final Options options) {
        options.addOptionGroup(CONFIGURATION)
                .addOptionGroup(CONFIGURATION_NO_DEFAULTS)
//                .addOption(LICENSES_APPROVED)
//                .addOption(LICENSES_APPROVED_FILE)
//                .addOption(LICENSES_FAMILIES)
//                .addOption(LICENSES_FAMILIES_FILE)
//                .addOption(LICENSES_REMOVE_APPROVED)
//                .addOption(LICENSES_REMOVE_APPROVED_FILE)
//                .addOption(LICENSES_REMOVE_FAMILIES)
//                .addOption(LICENSES_REMOVE_FAMILIES_FILE)
               ;
    }

    /**
     * Processes the configuration options.
     * @param ctxt the context to process.
     * @throws MalformedURLException if configuration files can not be read.
     */
    public static void processArgs(final ArgumentContext ctxt) throws MalformedURLException {
        Defaults.Builder defaultBuilder = Defaults.builder();
        if (CONFIGURATION.getSelected() != null) {
            for (String fn : ctxt.getCommandLine().getOptionValues(CONFIGURATION.getSelected())) {
                defaultBuilder.add(fn);
            }
        }
        if (CONFIGURATION_NO_DEFAULTS.getSelected() != null) {
            // display deprecation log if needed.
            ctxt.getCommandLine().hasOption(CONFIGURATION.getSelected());
            defaultBuilder.noDefault();
        }
        ctxt.getConfiguration().setFrom(defaultBuilder.build(ctxt.getLog()));
    }
}
