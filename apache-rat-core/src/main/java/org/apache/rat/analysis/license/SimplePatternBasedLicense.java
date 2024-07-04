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
package org.apache.rat.analysis.license;

import java.util.Arrays;

import org.apache.rat.DeprecationReporter;
import org.apache.rat.configuration.builders.AbstractBuilder;
import org.apache.rat.configuration.builders.AnyBuilder;
import org.apache.rat.configuration.builders.TextBuilder;
import org.apache.rat.license.ILicense;


/**
 * @since Rat 0.8
 * @deprecated Use new configuration options
 */
@Deprecated // Since 0.16
@DeprecationReporter.Info(since = "0.16", forRemoval = true, use = "new configuration options")
public class SimplePatternBasedLicense  extends BaseLicense {
    private String[] patterns;


    public SimplePatternBasedLicense() {
        DeprecationReporter.logDeprecated(SimplePatternBasedLicense.class);
    }

    
    public String[] getPatterns() {
        return patterns;
    }

    public void setPatterns(String[] pPatterns) {
        patterns = pPatterns;
    }

    private AbstractBuilder getMatcher() {
        if (patterns.length == 1) {
            return new TextBuilder().setSimpleText(patterns[0]);
        } else {
            AnyBuilder any = new AnyBuilder();
            Arrays.stream(patterns).map(s -> new TextBuilder().setSimpleText(s)).forEach(b-> any.addEnclosed(b));
            return any;
        }
    }

    @Override
    public ILicense.Builder getLicense() {
        return ILicense.builder()
        .setFamily(getLicenseFamilyCategory())
        .setName(getLicenseFamilyName())
        .setMatcher( getMatcher() )
        .setNote(getNotes());
    }
}
