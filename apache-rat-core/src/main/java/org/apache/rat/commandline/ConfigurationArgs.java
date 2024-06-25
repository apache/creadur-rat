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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;

/**
 * Processes command line arguments that are specific to configuration of the build.
 * @since 0.17
 */
public final class ConfigurationArgs {

    /** group of options that read a configuraiton file */
    private static final OptionGroup CONFIGURATION = new OptionGroup()
            .addOption(Option.builder().longOpt("config").hasArgs().argName("File")
            .desc("File names for system configuration.  May be followed by multiple arguments. "
                    + "Note that '--' or a following option is required when using this parameter.")
            .build())
            .addOption(Option.builder().longOpt("licenses").hasArgs().argName("File")
            .desc("File names for system configuration.  May be followed by multiple arguments. "
                    + "Note that '--' or a following option is required when using this parameter.")
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --config").get())
            .build());

    /** group of options that skip the default configuration file */
    private static final OptionGroup CONFIGURATION_NO_DEFAULTS  = new OptionGroup()
            .addOption(Option.builder().longOpt("configuration-no-defaults")
                            .desc("Ignore default configuration.").build())
            .addOption(Option.builder().longOpt("no-default-licenses")
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --configuration-no-defaults").get())
            .build());

    /** Option that add approved licenses to the list */
    private static final Option LICENSES_APPROVED = Option.builder().longOpt("licenses-approved").hasArgs().argName("LicenseID")
            .desc("The approved License IDs.  These licenses will be added to the list of approved licenses. " +
                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
            .build();

    /** Option that add approved licenses from a file */
    private static final Option LICENSES_APPROVED_FILE = Option.builder().longOpt("licenses-approved-file").hasArg().argName("File")
            .desc("Name of file containing the approved license IDs.")
            .type(File.class)
            .build();

    /** Option that specifies approved license families */
    private static final Option FAMILIES_APPROVED = Option.builder().longOpt("license-families-approved").hasArgs().argName("FamilyID")
            .desc("The approved License Family IDs.  These licenses families will be added to the list of approved licenses families " +
                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
            .build();

    /** Option that specifies approved license families from a file */
    private static final Option FAMILIES_APPROVED_FILE = Option.builder().longOpt("license-families-approved-file").hasArg().argName("File")
            .desc("Name of file containing the approved family IDs.")
            .type(File.class)
            .build();

    /** Option to remove licenses from the approved list */
    private static final Option LICENSES_DENIED = Option.builder().longOpt("licenses-denied").hasArgs().argName("LicenseID")
            .desc("The approved License IDs.  These licenses will be added to the list of approved licenses. " +
                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
            .build();

    /** Option to read a file licenses to be removed from the approved list */
    private static final Option LICENSES_DENIED_FILE = Option.builder().longOpt("licenses-denied-file").hasArg().argName("File")
            .desc("Name of File containing the approved license IDs.")
            .type(File.class)
            .build();

    /** Option to list license families to remove from the approved list */
    private static final Option FAMILIES_DENIED = Option.builder().longOpt("license-families-denied").hasArgs().argName("FamilyID")
            .desc("The denied License family IDs.  These license families will be removed from the list of approved licenses. " +
                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
            .build();

    /** Option to read a list of license families to remove from the approved list */
    private static final Option FAMILIES_DENIED_FILE = Option.builder().longOpt("license-families-denied-file").hasArg().argName("File")
            .desc("Name of file containing the denied license IDs.")
            .type(File.class)
            .build();

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
                .addOption(LICENSES_APPROVED)
                .addOption(LICENSES_APPROVED_FILE)
                .addOption(FAMILIES_APPROVED)
                .addOption(FAMILIES_APPROVED_FILE)
                .addOption(LICENSES_DENIED)
                .addOption(LICENSES_DENIED_FILE)
                .addOption(FAMILIES_DENIED)
                .addOption(FAMILIES_DENIED_FILE)
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

        if (ctxt.getCommandLine().hasOption(FAMILIES_APPROVED)) {
            for (String cat : ctxt.getCommandLine().getOptionValues(FAMILIES_APPROVED)) {
                ctxt.getConfiguration().addApprovedLicenseCategory(cat);
            }
        }
        if (ctxt.getCommandLine().hasOption(FAMILIES_APPROVED_FILE)) {
            try {
                File f = ctxt.getCommandLine().getParsedOptionValue(FAMILIES_APPROVED_FILE);
                try (InputStream in = new FileInputStream(f)) {
                    ctxt.getConfiguration().addApprovedLicenseCategories(IOUtils.readLines(in, StandardCharsets.UTF_8));
                }
            } catch (IOException | ParseException e) {
                throw new ConfigurationException(e);
            }
        }
        if (ctxt.getCommandLine().hasOption(FAMILIES_DENIED)) {
            for (String cat : ctxt.getCommandLine().getOptionValues(FAMILIES_DENIED)) {
                ctxt.getConfiguration().removeApprovedLicenseCategory(cat);
            }
        }
        if (ctxt.getCommandLine().hasOption(FAMILIES_DENIED_FILE)) {
            try {
                File f = ctxt.getCommandLine().getParsedOptionValue(FAMILIES_DENIED_FILE);
                try (InputStream in = new FileInputStream(f)) {
                    ctxt.getConfiguration().removeApprovedLicenseCategories(IOUtils.readLines(in, StandardCharsets.UTF_8));
                }
            } catch (IOException | ParseException e) {
                throw new ConfigurationException(e);
            }
        }

        if (ctxt.getCommandLine().hasOption(LICENSES_APPROVED)) {
            for (String id : ctxt.getCommandLine().getOptionValues(LICENSES_APPROVED)) {
                ctxt.getConfiguration().addApprovedLicenseId(id);
            }
        }
        if (ctxt.getCommandLine().hasOption(LICENSES_APPROVED_FILE)) {
            try {
                File f = ctxt.getCommandLine().getParsedOptionValue(LICENSES_APPROVED_FILE);
                try (InputStream in = new FileInputStream(f)) {
                    ctxt.getConfiguration().addApprovedLicenseIds(IOUtils.readLines(in, StandardCharsets.UTF_8));
                }
            } catch (IOException | ParseException e) {
                throw new ConfigurationException(e);
            }
        }
        if (ctxt.getCommandLine().hasOption(LICENSES_DENIED)) {
            for (String id : ctxt.getCommandLine().getOptionValues(LICENSES_DENIED)) {
                ctxt.getConfiguration().removeApprovedLicenseId(id);
            }
        }
        if (ctxt.getCommandLine().hasOption(LICENSES_DENIED_FILE)) {
            try {
                File f = ctxt.getCommandLine().getParsedOptionValue(LICENSES_DENIED_FILE);
                try (InputStream in = new FileInputStream(f)) {
                    ctxt.getConfiguration().removeApprovedLicenseIds(IOUtils.readLines(in, StandardCharsets.UTF_8));
                }
            } catch (IOException | ParseException e) {
                throw new ConfigurationException(e);
            }
        }
    }
}
