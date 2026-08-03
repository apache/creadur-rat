/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   https://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.testhelpers;

import org.apache.commons.cli.Option;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.ui.UIOption;
import org.apache.rat.utils.CasedString;

import java.util.function.Function;

public final class BaseOption extends UIOption<BaseOption> {
    BaseOption(BaseOptionBuilder builder) {
        super(builder);
    }

    public Builder builder() {
        return new BaseOptionBuilder();
    }

    protected String cleanupName(Option option) {
        return ArgumentTracker.extractKey(option);
    }

    public String getExample() {
        return "";
    }

    public String getText() {
        return "";
    }

    public static class BaseOptionBuilder extends UIOption.Builder<BaseOption, BaseOptionBuilder> {

        @Override
        protected Function<Option, CasedString> getNameFactory() {
            return ArgumentTracker::extractName;
        }

        @Override
        protected BaseOption doBuild() {
            return new BaseOption(this);
        }
    }
}
