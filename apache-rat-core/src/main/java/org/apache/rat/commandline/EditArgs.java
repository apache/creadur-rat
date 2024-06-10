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

import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.rat.config.AddLicenseHeaders;

/**
 * Processes the edit arguments.
 * @since 0.17
 */
public final class EditArgs {

    /** Defines options to add licenses to files */
    private static final OptionGroup ADD = new OptionGroup()
            .addOption(Option.builder("a")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription("Use '--edit-license' instead.").get())
                    .build())
            .addOption(Option.builder("A").longOpt("addLicense")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription("Use '--edit-license' instead.").get())
                    .build())
            .addOption(Option.builder().longOpt("edit-license").desc(
                    "Add the default license header to any file with an unknown license that is not in the exclusion list. "
                            + "By default new files will be created with the license header, "
                            + "to force the modification of existing files use the --edit-overwrite option.").build()
            );

    /** Defines options to add copyright to files */
    private static final OptionGroup COPYRIGHT = new OptionGroup()
            .addOption(Option.builder("c").longOpt("copyright").hasArg()
                    .desc("The copyright message to use in the license headers, usually in the form of \"Copyright 2008 Foo\".  Only valid with --edit-license")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription("Use '--edit-copyright' instead.").get())
                    .build())
            .addOption(Option.builder().longOpt("edit-copyright").hasArg()
                    .desc("The copyright message to use in the license headers, usually in the form of \"Copyright 2008 Foo\".  Only valid with --edit-license")
                    .build());

    /** Causes file updates to overwrite existing files. */
    private static final OptionGroup OVERWRITE = new OptionGroup()
            .addOption(Option.builder("f").longOpt("force")
                    .desc("Forces any changes in files to be written directly to the source files (i.e. new files are not created).  " +
                            "Only valid with --edit-license")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription("Use '--edit-overwrite' instead.").get())
                    .build())
            .addOption(Option.builder().longOpt("edit-overwrite")
                    .desc("Forces any changes in files to be written directly to the source files (i.e. new files are not created).  " +
                            "Only valid with --edit-license")
                    .build());

    private EditArgs() {
        // do not instantiate
    }

    /**
     * Adds the options from this set of options to the options argument.
     * @param options the options to add to.
     */
    public static void addOptions(final Options options) {
        options.addOptionGroup(ADD)
                .addOptionGroup(COPYRIGHT)
                .addOptionGroup(OVERWRITE);
    }

    /**
     * Processes the edit arguments.
     * @param ctxt the context to work with.
     */
    public static void processArgs(final ArgumentContext ctxt) {
        if (ADD.getSelected() != null) {
            ctxt.getCommandLine().hasOption(ADD.getSelected());
            boolean force = OVERWRITE.getSelected() != null;
            if (force) {
                ctxt.getCommandLine().hasOption(OVERWRITE.getSelected());
            }
            ctxt.getConfiguration().setAddLicenseHeaders(force ? AddLicenseHeaders.FORCED : AddLicenseHeaders.TRUE);
            if (COPYRIGHT.getSelected() != null) {
                ctxt.getConfiguration().setCopyrightMessage(ctxt.getCommandLine().getOptionValue(COPYRIGHT.getSelected()));
            }
        }

    }

}
