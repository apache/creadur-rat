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
 * Matches text headers to known licenses.
 */
public interface IHeaderMatcher {
    /**
     * The state of the matcher.
     * </ul>
     * <li>{@code t} - The matcher has located a match.</li>
     * <li>{@code f} - The matcher has determined that it will not match the
     * document.</li>
     * <li>{@code i} - The matcher can not yet determine if a matche is made or
     * not.</li>
     * </ul>
     */
    enum State {
        t("true"), f("false"), i("indeterminent");

        private String desc;

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
    }

    /**
     * Get the identifier for this matcher.
     * 
     * @return the Identifier for this matcher.
     */
    String getId();

    /**
     * Resets this matches. Subsequent calls to {@link #match} will accumulate new
     * text.
     */
    void reset();

    /**
     * Attempts to match the text after adding the line and returns the State after
     * the match is attempted.
     * 
     * @param line next line of text, not null
     * @return the new state after the matching was attempted.
     * 
     * @throws RatHeaderAnalysisException in case of internal RAT errors.
     */
    State matches(String line);

    /**
     * Gets the final state for this matcher. This is called after the EOF on the
     * input. At this point there should be no matchers in an indeterminent state.
     */
    State finalizeState();

    /**
     * Gets the the current state of the matcher. All matchers should be
     * indeterminant at the start.
     * 
     * @return the current state of the matcher.
     */
    State currentState();

    @FunctionalInterface
    interface Builder {
        IHeaderMatcher build();

        static TextBuilder text() {
            return new TextBuilder();
        }

        static AnyBuilder any() {
            return new AnyBuilder();
        }

        static AllBuilder all() {
            return new AllBuilder();
        }

        static CopyrightBuilder copyright() {
            return new CopyrightBuilder();
        }

        static SpdxBuilder spdx() {
            return new SpdxBuilder();
        }

        static MatcherRefBuilder matcherRef() {
            return new MatcherRefBuilder();
        }

        static NotBuilder not() {
            return new NotBuilder();
        }

        static RegexBuilder regex() {
            return new RegexBuilder();
        }
    }
}
