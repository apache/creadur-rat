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

import org.apache.tools.ant.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Generated class to provide Ant support for standard Rat command line options.
 *
 * DO NOT EDIT - GENERATED FILE
 */
${class}
    protected final Map<String, List<String>> args = new HashMap<>();

    public static String asKey(Option option) {
        return "--" + option.getLongOpt();
    }

${constructor}

    protected List<String> args() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : args.entrySet()) {
            result.add(entry.getKey());
            result.addAll(entry.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return result;
    }

    protected void setArg(String key, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        args.put(key, values);
    }

    protected void addArg(String key, String value) {
        List<String> values = args.get(key);
        if (values == null) {
            setArg(key, value);
        } else {
            values.add(value);
        }
    }

    protected class Child {
        final String key;

        protected Child(String key) {
            this.key = key;
        }

        public void addText(String arg) {
            addArg(key, arg);
        }
    }

    /**
     * A wrapper on Option to provide access to Option info with Ant nomenclature and formatting.
     */
    public  static class AntOption {
        final Option option;
        final String name;

        AntOption(Option option, String name) {
            this.option = option;
            this.name = name;
        }

        public boolean isAttribute() {
            return (!option.hasArgs());
        }

        public boolean isElement() {
            return !isAttribute() || option.getType() != String.class;
        }

        public String getType() {
            return ((Class<?>) option.getType()).getSimpleName();
        }

        public boolean hasArg() {
            return option.hasArg();
        }

        public String longValue() {
            return "--" + option.getLongOpt();
        }
    }

    /*  GENERATED METHODS */

${methods}


    /*  GENERATED CLASSES */

${classes}

}
