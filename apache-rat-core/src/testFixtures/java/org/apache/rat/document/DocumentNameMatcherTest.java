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
package org.apache.rat.document;

import java.util.function.Predicate;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.apache.rat.document.DocumentNameMatcher.MATCHES_ALL;
import static org.apache.rat.document.DocumentNameMatcher.MATCHES_NONE;

public class DocumentNameMatcherTest {

    private final static DocumentNameMatcher TRUE = new DocumentNameMatcher("T", (Predicate<DocumentName>)name -> true);
    private final static DocumentNameMatcher FALSE = new DocumentNameMatcher("F", (Predicate<DocumentName>)name -> false);
    private final static DocumentNameMatcher SOME = new DocumentNameMatcher("X", (Predicate<DocumentName>)name -> false);
    private final static DocumentName testName = DocumentName.builder().setName("testName").setBaseName("/").build();

    public static String processDecompose(DocumentNameMatcher matcher, DocumentName candidate) {
        StringBuilder sb = new StringBuilder();
        matcher.decompose(candidate).forEach(s -> sb.append(s).append("\n"));
        return sb.toString();
    }

    @Test
    public void orTest() {
        assertThat(DocumentNameMatcher.or(TRUE, FALSE).matches(testName)).as("T,F").isTrue();
        assertThat(DocumentNameMatcher.or(FALSE, TRUE).matches(testName)).as("F,T").isTrue();
        assertThat(DocumentNameMatcher.or(TRUE, TRUE).matches(testName)).as("T,T").isTrue();
        assertThat(DocumentNameMatcher.or(FALSE, FALSE).matches(testName)).as("F,F").isFalse();
    }

    @Test
    public void andTest() {
        assertThat(DocumentNameMatcher.and(TRUE, FALSE).matches(testName)).as("T,F").isFalse();
        assertThat(DocumentNameMatcher.and(FALSE, TRUE).matches(testName)).as("F,T").isFalse();
        assertThat(DocumentNameMatcher.and(TRUE, TRUE).matches(testName)).as("T,T").isTrue();
        assertThat(DocumentNameMatcher.and(FALSE, FALSE).matches(testName)).as("F,F").isFalse();
    }

    @Test
    public void matcherSetTest() {
        assertThat(DocumentNameMatcher.matcherSet(TRUE, FALSE).matches(testName)).as("T,F").isTrue();
        assertThat(DocumentNameMatcher.matcherSet(FALSE, TRUE).matches(testName)).as("F,T").isFalse();
        assertThat(DocumentNameMatcher.matcherSet(TRUE, TRUE).matches(testName)).as("T,T").isTrue();
        assertThat(DocumentNameMatcher.matcherSet(FALSE, FALSE).matches(testName)).as("F,F").isTrue();

        assertThat(DocumentNameMatcher.matcherSet(MATCHES_ALL, MATCHES_ALL)).as("All,All").isEqualTo(MATCHES_ALL);
        assertThat(DocumentNameMatcher.matcherSet(MATCHES_ALL, MATCHES_NONE)).as("All,None").isEqualTo(MATCHES_ALL);
        assertThat(DocumentNameMatcher.matcherSet(MATCHES_ALL, SOME)).as("All,X").isEqualTo(MATCHES_ALL);

        assertThat(DocumentNameMatcher.matcherSet(MATCHES_NONE, MATCHES_ALL)).as("None,All").isEqualTo(MATCHES_NONE);
        assertThat(DocumentNameMatcher.matcherSet(MATCHES_NONE, MATCHES_NONE)).as("None,None").isEqualTo(MATCHES_ALL);
        assertThat(DocumentNameMatcher.matcherSet(MATCHES_NONE, SOME).toString()).as("None,X").isEqualTo("not(X)");

        assertThat(DocumentNameMatcher.matcherSet(SOME, MATCHES_ALL).toString()).as("X,All").isEqualTo("matcherSet(X, TRUE)");
        assertThat(DocumentNameMatcher.matcherSet(SOME, MATCHES_NONE)).as("X,None").isEqualTo(MATCHES_ALL);
        assertThat(DocumentNameMatcher.matcherSet(SOME, SOME).toString()).as("X,X").isEqualTo("matcherSet(X, X)");
    }

    @Test
    void testDecompose() {
        DocumentNameMatcher matcher1 = new DocumentNameMatcher("FileFilterTest", new NameFileFilter("File.name"));
        String result = processDecompose(matcher1, testName);
        assertThat(result).contains("FileFilterTest: >>false<<").contains("  NameFileFilter(File.name)");

        DocumentNameMatcher matcher2 = new DocumentNameMatcher("MatchPatternsTest", MatchPatterns.from("/", "**/test1*", "**/*Name"));
        result = processDecompose(matcher2, testName);
        assertThat(result).contains("MatchPatternsTest: >>true<<").contains("  **/test1*: >>false<<").contains("  **/*Name: >>true<<");

        DocumentNameMatcher matcher3 = DocumentNameMatcher.matcherSet(matcher1, matcher2);
        result = processDecompose(matcher3, testName);
        assertThat(result).contains("MatchPatternsTest: >>true<<").contains("  **/test1*: >>false<<").contains("  **/*Name: >>true<<");
    }
}
