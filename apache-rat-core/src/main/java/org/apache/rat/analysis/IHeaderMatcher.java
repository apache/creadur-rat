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
package org.apache.rat.analysis;

import org.apache.rat.configuration.builders.AllBuilder;
import org.apache.rat.configuration.builders.AnyBuilder;
import org.apache.rat.configuration.builders.CopyrightBuilder;
import org.apache.rat.configuration.builders.MatcherRefBuilder;
import org.apache.rat.configuration.builders.NotBuilder;
import org.apache.rat.configuration.builders.RegexBuilder;
import org.apache.rat.configuration.builders.SpdxBuilder;
import org.apache.rat.configuration.builders.TextBuilder;

/**
 * Performs explicit checks against a line from the header of a file.
 * For implementations that need to check multiple lines the implementation must cache the earlier lines.
 */
public interface IHeaderMatcher {
    /**
     * The state of the matcher.
     * <ul>
     * <li>{@code t} - The matcher has located a match.</li>
     * <li>{@code f} - The matcher has determined that it will not match the
     * document.</li>
     * <li>{@code i} - The matcher can not yet determine if a matche is made or
     * not.</li>
     * </ul>
     */
    enum State {
        t("true"), f("false"), i("indeterminent");

        private final String desc;

        State(String desc) {
            this.desc = desc;
        }

        public boolean asBoolean() {
            switch (this) {
            case t : return true;
            case f : return false;
            default:
            case i : throw new IllegalStateException( "'asBoolean' should never be called on an indeterminate state");
            }
        }
        
        @Override
        public String toString() {
            return super.toString()+" "+desc;
        }
    }

    /**
     * Get the identifier for this matcher.
     * <p>All matchers must have unique identifiers</p>
     * 
     * @return the Identifier for this matcher.
     */
    String getId();

    /**
     * Resets this state {@code State.i}. 
     * If text is being cached this method should clear that cache.
     */
    void reset();

    /**
     * Attempts to match {@code line} and returns the State after
     * the match is attempted.
     * 
     * @param line next line of text, not null
     * @return the new state after the matching was attempted.
     */
    State matches(String line);

    /**
     * Gets the final state for this matcher. This is called after the EOF on the
     * input. At this point there should be no matchers in an {@code State.i} state.
     */
    State finalizeState();

    /**
     * Gets the the current state of the matcher. All matchers should be
     * in {@code State.i} at the start.
     * 
     * @return the current state of the matcher.
     */
    State currentState();

    /**
     * An IHeaderMatcher builder.
     */
    @FunctionalInterface
    interface Builder {
        /**
         * Build the IHeaderMatcher.
         * @return a new IHeaderMatcher.
         */
        IHeaderMatcher build();

        /**
         * @return an instance of the standard TextBuilder.
         * @see TextBuilder
         */
        static TextBuilder text() {
            return new TextBuilder();
        }

        /**
         * @return an instance of the standard AnyBuilder.
         * @see AnyBuilder
         */
        static AnyBuilder any() {
            return new AnyBuilder();
        }

        /**
         * @return an instance of the standard AllBuilder.
         * @see AllBuilder
         */
        static AllBuilder all() {
            return new AllBuilder();
        }

        /**
         * @return an instance of the standard CopyrightBuilder.
         * @see CopyrightBuilder
         */
        static CopyrightBuilder copyright() {
            return new CopyrightBuilder();
        }

        /**
         * @return an instance of the standard SpdxBuilder.
         * @see SpdxBuilder
         */
        static SpdxBuilder spdx() {
            return new SpdxBuilder();
        }

        /**
         * @return an instance of the standard MatcherRefBuilder.
         * @see MatcherRefBuilder
         */
        static MatcherRefBuilder matcherRef() {
            return new MatcherRefBuilder();
        }

        /**
         * @return an instance of the standard NotBuilder.
         * @see NotBuilder
         */
        static NotBuilder not() {
            return new NotBuilder();
        }

        /**
         * @return an instance of the standard RegexBuilder.
         * @see RegexBuilder
         */
        static RegexBuilder regex() {
            return new RegexBuilder();
        }
    }
}
