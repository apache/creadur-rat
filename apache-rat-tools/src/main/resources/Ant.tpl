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

${package}

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.commandline.Arg;
import org.apache.rat.utils.CasedString;
import org.apache.tools.ant.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Generated class to provide Ant support for standard Rat command line options.
 *
 * DO NOT EDIT - GENERATED FILE
 */
${class}

    private final static Map<String,String> xlateName = new HashMap<>();

    private final static List<String> unsupportedArgs = new ArrayList<>();

    static {
${static}
    }

    public static String createName(String longOpt) {
        String name = StringUtils.defaultIfEmpty(xlateName.get(longOpt), longOpt).toLowerCase(Locale.ROOT);
        return new CasedString(CasedString.StringCase.KEBAB, name).toCase(CasedString.StringCase.CAMEL);
    }

    public static List<String> unsupportedArgs() {
        return Collections.unmodifiableList(unsupportedArgs);
    }

${commonArgs}

${constructor}

    /**
     * A child element.
     */
    protected class Child {
        final String key;

        /**
         * Constructor.
         * @param key The key for this child in the CLI map.
         */
        protected Child(String key) {
            this.key = key;
        }

        /**
         * Sets the text enclosed in the child element.
         * @param arg The text enclosed in the child element.
         */
        public void addText(String arg) {
            addArg(key, arg);
        }
    }

    /*  GENERATED METHODS */

${methods}


    /*  GENERATED CLASSES */

${classes}

}
