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
package org.apache.rat.documentation.options;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;

public class CLIOption extends AbstractOption {

    public static String createName(final Option option) {
        return StringUtils.defaultIfBlank(option.getLongOpt(), option.getOpt());
    }

    public CLIOption(final Option option) {
        super(option, createName(option));
    }

    @Override
    public String getText() {
        StringBuilder result = new StringBuilder();
        if (option.getLongOpt() != null) {
            result.append("--").append(option.getLongOpt());
            if (option.getOpt() != null) {
                result.append(" or -").append(option.getArgs());
            }
        } else {
            result.append("-").append(option.getArgs());
        }
        return result.toString();
    }

    @Override
    protected String cleanupName(final Option option) {
        return createName(option);
    }

    @Override
    public String getExample() {
        StringBuilder sb = new StringBuilder("-");
        if (option.getLongOpt() != null) {
            sb.append("-").append(option.getLongOpt());
        } else {
            sb.append(option.getOpt());
        }
        if (option.hasArg()) {
            String argName = StringUtils.defaultIfBlank(option.getArgName(), "Arg");
            sb.append(" ").append(argName);
            if (option.hasArgs()) {
                sb.append(" [").append(argName).append("2 [").append(argName)
                        .append("3 [...]]] --");
            }
        }
        return sb.toString();
    }
}
