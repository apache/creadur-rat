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
import org.apache.rat.DeprecationReporter;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;

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
 * Generated class to provide Ant support for standard RAT command line options.
 *
 * DO NOT EDIT - GENERATED FILE
 */
${class}

    private final static Map<String,String> xlateName = new HashMap<>();

    private final static List<String> unsupportedArgs = new ArrayList<>();

    private final static Map<String, String> deprecatedArgs = new HashMap<>();

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

    /*  GENERATED METHODS */

${methods}


    /*  GENERATED CLASSES */

${classes}

    /* TYPE CLASSES */

    protected static class TxtValue {
        protected TxtValue() { }

        public String value;

        public void addText(String text) {
            value = text.trim();
        }
    }

    public static class Std extends TxtValue {
        public Std() { }
    }

    public static class Expr extends TxtValue {
        public Expr() { }
    }

    public static class Cntr extends TxtValue {
        public Cntr() { }
    }

    public static class Filename extends TxtValue {
        public Filename() { }
    }

    public static class Lst extends TxtValue {
        public Lst() { }
    }
}
