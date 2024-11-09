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

import org.apache.commons.cli.Converter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.ConfigurationException;
import org.apache.rat.report.claim.ClaimStatistic;

import static java.lang.String.format;

/**
 * Customized converters for Arg processing
 */
public final class Converters {

    private Converters() {
        // do not instantiate
    }

    /**
     * Creates a File with fully qualified name
     */
    public static final Converter<File, NullPointerException> FILE_CONVERTER = s -> new File(s).getAbsoluteFile();
    /**
     * converts the Converter pattern into a Converter, count pair.
     */
    public static final Converter<Pair<ClaimStatistic.Counter, Integer>, ConfigurationException> COUNTER_CONVERTER = arg -> {
        String[] parts = arg.split(":");
        try {
            ClaimStatistic.Counter counter = ClaimStatistic.Counter.valueOf(parts[0].toUpperCase());
            Integer limit = Integer.parseInt(parts[1]);
            return Pair.of(counter, limit);
        } catch (NumberFormatException e) {
            throw new ConfigurationException(format("'%s' is not a valid integer", parts[1]), e);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(format("'%s' is not a valid Counter", parts[0]), e);
        }
    };
}
