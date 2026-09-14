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
package org.apache.rat;

import org.apache.commons.cli.Option;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.ui.UIOption;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.CasedString;

import java.util.function.Function;

/**
 * A UIOption implementation to support testing.
 */
public class TestOption extends UIOption<TestOption> {

    private <C extends UIOptionCollection<TestOption>> TestOption(TestOptionBuilder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return option.toString() + " " + this.argumentType;
    }

    @Override
    protected String cleanupName(Option option) {
        return "clean" + option.toString();
    }

    @Override
    public String getExample() {
        return "example " + option.toString();
    }

    @Override
    public String getText() {
        return "text for " + option.toString();
    }

    /**
     * The test option builder.
     */
    public static class TestOptionBuilder extends Builder<TestOption, TestOptionBuilder> {

        @Override
        protected Function<Option, CasedString> getNameFactory() {
            return ArgumentTracker::extractName;
        }

        @Override
        protected TestOption doBuild() {
            return new TestOption(this);
        }
    }
}
