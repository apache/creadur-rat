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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/* DO NOT EDIT - GENERATED FILE */

/**
 * Generated class to provide Maven support for standard Rat command line options
 */
${class}
    /**
     * A map of CLI based arguments to values.
     */
    protected final Map<String, List<String>> args = new HashMap<>();

    private final static Map<String,String> xlateName = new HashMap<>();;

    static {
${static}
    }

    public static String createName(String longOpt) {
        String name = xlateName.get(longOpt);
        return name != null ? name : new CasedString(CasedString.StringCase.KEBAB, longOpt).toCase(CasedString.StringCase.CAMEL);
    }

${constructor}

    /**
     * Gets the list of arguments prepared for the CLI code to parse.
     * @return the List of arguments for the CLI command line.
     */
    protected List<String> args() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : args.entrySet()) {
            result.add(entry.getKey());
            result.addAll(entry.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * Set a key and value into the argument list.
     * Replaces any existing value.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void setArg(String key, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        args.put(key, values);
    }

    /**
     * Get the list of values for a key.
     * @param key the key for the map.
     * @return the list of values for the key or {@code null} if not set.
     */
    public List<String> getArg(String key) {
        return args.get(key);
    }

    /**
     * Add an value to the key in the argument list.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void addArg(String key, String value) {
        List<String> values = args.get(key);
        if (values == null) {
            setArg(key, value);
        } else {
            values.add(value);
        }
    }

    /**
     * remove a key from the argument list.
     * @param key the key to remove from the map.
     */
    protected void removeArg(String key) {
        args.remove(key);
    }


    /*  GENERATED METHODS */


${methods}
}
