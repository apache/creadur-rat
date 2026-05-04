/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat;

import org.apache.commons.cli.Option;
import org.apache.rat.ui.UIOptionCollection;

public final class CLIOptionCollection extends UIOptionCollection<CLIOption> {
    /** The Help option */
    static final Option HELP = new Option("?", "help", false, "Print help for the RAT command line interface and exit.");

    /** The instance of the collection */
    public static final CLIOptionCollection INSTANCE = new CLIOptionCollection();

    private CLIOptionCollection() {
        super(new Builder().uiOption(HELP));
    }

    private static final class Builder extends UIOptionCollection.Builder<CLIOption, Builder> {
        private Builder() {
            super(CLIOption::new);
        }
    }
}
