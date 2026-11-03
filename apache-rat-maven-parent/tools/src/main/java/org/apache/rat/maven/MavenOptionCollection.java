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
package org.apache.rat.maven;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.cli.Option;
import org.apache.rat.commandline.Arg;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.CasedString;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;

/**
 * The collection of MavenOptions equivalent to the CLI options
 * with any unsupported options removed.
 */
@DefaultKey("MavenOptions")
@ValidScope({"application"})
public final class MavenOptionCollection extends UIOptionCollection<MavenOption> {
    /**
     * Mapping of standard name to non-conflicting name.
     */
    private static final Map<String, String> RENAME_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("addLicense", "add-license");
        RENAME_MAP = map;
    }

    /**
     * Gets the map of renamed Options indexed by original option name.
     * @return the map of renamed Options indexed by original option name.
     */
    public static Map<String, String> getRenameMap() {
        return new TreeMap<>(RENAME_MAP);
    }

    /**
     * Create an instance.
     */
    public MavenOptionCollection() {
        this(new Builder());
    }

    /**
     * Create the MavenOptionsCollection from a builder.
     * @param builder the builder to use.
     */
    protected MavenOptionCollection(final MavenOptionCollection.Builder builder) {
        super(builder);
    }

    @Override
    public String rename(final Option option) {
        String baseKey = ArgumentTracker.extractKey(option);
        String key = RENAME_MAP.getOrDefault(baseKey, baseKey);
        CasedString name = new CasedString(CasedString.StringCase.KEBAB, key).as(CasedString.StringCase.PASCAL);
        if (option.hasArgs()) {
            String[] segments = name.getSegments();
            int pos = segments.length - 1;
            String seg =  segments[pos];
            if (!(seg.endsWith("s") || seg.equals("approved") || seg.equals("denied"))) {
                segments[pos] = seg + "s";
            }
        }
        return name.toString();
    }

    /**
     * The Builder for the MavenOptionCollection.
     */
    public static final class Builder extends UIOptionCollection.Builder<MavenOption, Builder> {
        public Builder() {
            super(MavenOption.MavenOptionBuilder::new);
            Arg.getOptions().getOptions()
                    .stream().filter(o -> Objects.isNull(o.getLongOpt()))
                    .forEach(this::unsupported);
            unsupported(Arg.DIR).unsupported(Arg.LOG_LEVEL);
        }

        MavenOptionCollection build() {
            return new MavenOptionCollection(this);
        }
    }
}
