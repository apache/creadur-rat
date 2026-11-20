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
package org.apache.rat.analysis.matchers;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SPDXExpressionTest {
    private static final Set<String> APPROVED = Set.of("Apache-2.0", "MIT");

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "Apache-2.0; true; Apache-2.0",
            "Apache-2.0 OR GPL-3.0-only; true; Apache-2.0",
            "GPL-3.0-only OR Apache-2.0; true; Apache-2.0",
            "Apache-2.0 AND GPL-3.0-only; false; Apache-2.0,GPL-3.0-only",
            "(Apache-2.0 OR MIT); true; Apache-2.0",
            "Apache-2.0 OR MIT AND GPL-3.0-only; true; Apache-2.0",
            "(Apache-2.0 OR MIT) AND GPL-3.0-only; false; Apache-2.0,GPL-3.0-only",
            "GPL-3.0-only AND MIT OR Apache-2.0; true; Apache-2.0",
            "(GPL-3.0-only AND MIT) OR (Apache-2.0 AND MIT); true; Apache-2.0,MIT",
            "GPL-3.0-only OR LicenseRef-Private; false; GPL-3.0-only,LicenseRef-Private",
            "MIT AND MIT; true; MIT"
    })
    void evaluatesAndSelectsAlternatives(String text, boolean approved, String selected) {
        SPDXExpression expression = SPDXExpression.parse(text);
        assertThat(expression.isApproved(APPROVED::contains)).isEqualTo(approved);
        assertThat(expression.select(APPROVED::contains)).containsExactlyInAnyOrder(selected.split(","));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "  ( Apache-2.0\tOR\tMIT )  ",
            "(Apache-2.0 OR MIT)",
            "\u00a0(\u2003Apache-2.0\u00a0OR\u2003MIT\u00a0)\u2003",
            "\r\n(Apache-2.0\nOR\r\nMIT)\t"
    })
    void acceptsWhitespace(String text) {
        SPDXExpression expression = SPDXExpression.parse(text);
        assertThat(expression.identifiers()).containsExactlyInAnyOrder("Apache-2.0", "MIT");
        assertThat(expression.isApproved(APPROVED::contains)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"\u001c", "\u0085", "\u00a0", "\u2007", "\u202f"})
    void preservesExtendedWhitespaceWhenTokenizingAndRemovingCommentDelimiters(String whitespace) {
        String text = "(" + whitespace + "Apache-2.0" + whitespace + "OR" + whitespace + "MIT)";
        assertThat(SPDXExpression.parse(text).identifiers()).containsExactlyInAnyOrder("Apache-2.0", "MIT");
        SPDXExpression.Header header = SPDXExpression.fromHeader(
                "/* SPDX-License-Identifier:" + whitespace + text + " */" + whitespace);
        assertThat(header.invalid()).isFalse();
        assertThat(header.expressions()).hasSize(1);
        assertThat(header.expressions().get(0).identifiers()).containsExactlyInAnyOrder("Apache-2.0", "MIT");
    }

    @Test
    void parenthesesRemainSeparateTokensWithoutSurroundingWhitespace() {
        SPDXExpression expression = SPDXExpression.parse("(MIT)AND(Apache-2.0)");
        assertThat(expression.identifiers()).containsExactlyInAnyOrder("Apache-2.0", "MIT");
        assertThat(expression.isApproved(APPROVED::contains)).isTrue();
    }

    @Test
    void removesOnlyOneClosingCommentDelimiter() {
        assertThat(SPDXExpression.fromHeader("SPDX-License-Identifier: MIT -->*/").invalid()).isTrue();
        assertThat(SPDXExpression.fromHeader("SPDX-License-Identifier: MIT */-->").invalid()).isTrue();
    }

    @Test
    void exceptionsAndExtendedIdentifiersAreNotReducedToBaseLicenses() {
        SPDXExpression expression = SPDXExpression.parse(
                "GPL-2.0-only\tWITH   Classpath-exception-2.0 OR GPL-2.0+");
        assertThat(expression.identifiers()).containsExactlyInAnyOrder(
                "GPL-2.0-only WITH Classpath-exception-2.0", "GPL-2.0+");
        assertThat(expression.isApproved("GPL-2.0-only"::equals)).isFalse();
        assertThat(expression.isApproved("GPL-2.0-only WITH Classpath-exception-2.0"::equals)).isTrue();
        assertThat(SPDXExpression.parse("DocumentRef-External:LicenseRef-Custom").identifiers())
                .containsExactly("DocumentRef-External:LicenseRef-Custom");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "MIT", "GPL-2.0+", "LicenseRef-Custom", "LicenseRef-Custom.1-2",
            "DocumentRef-Upstream:LicenseRef-Custom.1-2", "LicenseReference", "DocumentReference",
            "licenseref-Custom+"
    })
    void distinguishesStandardIdentifiersFromReservedReferencePrefixes(String identifier) {
        assertThat(SPDXExpression.parse(identifier).identifiers()).containsExactly(identifier);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "", " ", "()", "AND", "OR", "WITH", "MIT AND", "MIT OR", "MIT WITH",
            "AND MIT", "MIT MIT", "MIT or Apache-2.0", "MIT XOR Apache-2.0", "MIT++",
            "MIT/Apache-2.0", "(MIT", "MIT)", "MIT OR ()", "MIT AND OR Apache-2.0",
            "(MIT OR Apache-2.0) WITH Exception", "MIT WITH Exception WITH Other",
            "MIT WITH (Exception)", "MIT WITH Exception+", "MIT trailing prose",
            "MIT;", "MIT || Apache-2.0", "DocumentRef-External:MIT", "LicenseRef-",
            "LicenseRef-Custom+", "DocumentRef-External", "MIT\u200bOR\u200bApache-2.0",
            "DocumentRef-:LicenseRef-Custom", "DocumentRef-Upstream:LicenseRef-",
            "DocumentRef-Upstream:LicenseRef-Custom+", "LicenseRef-Custom:MIT",
            "DocumentRef-Upstream:LicenseRef-Custom:Other"
    })
    void rejectsMalformedExpressions(String text) {
        assertThrows(IllegalArgumentException.class, () -> SPDXExpression.parse(text));
    }

    @Test
    void boundsParserWork() {
        String excessivelyNested = "(".repeat(66) + "MIT" + ")".repeat(66);
        String tooManyTokens = "MIT OR ".repeat(260) + "MIT";
        String tooLong = "M".repeat(16385);
        assertThrows(IllegalArgumentException.class, () -> SPDXExpression.parse(excessivelyNested));
        assertThrows(IllegalArgumentException.class, () -> SPDXExpression.parse(tooManyTokens));
        assertThrows(IllegalArgumentException.class, () -> SPDXExpression.parse(tooLong));
    }

    @Test
    void extractsSeparateTagsAndRemovesOnlyTheirText() {
        String raw = """
                // header\r
                /* SPDX-License-Identifier:  (Apache-2.0 OR MIT) */\r
                <!-- SPDX-License-Identifier:\tGPL-3.0-only -->\r
                // footer\r
                """;
        SPDXExpression.Header header = SPDXExpression.fromHeader(raw);
        assertThat(header.invalid()).isFalse();
        assertThat(header.expressions()).hasSize(2);
        assertThat(header.expressions().get(0).identifiers()).containsExactlyInAnyOrder("Apache-2.0", "MIT");
        assertThat(header.expressions().get(1).identifiers()).containsExactly("GPL-3.0-only");
        assertThat(header.remaining()).contains("// header", "// footer").doesNotContain("SPDX-License-Identifier:");
    }

    @Test
    void incompleteTagCannotConsumeNextLineOrHideAValidTag() {
        SPDXExpression.Header header = SPDXExpression.fromHeader(
                "SPDX-License-Identifier:\nMIT\nSPDX-License-Identifier: Apache-2.0\n");
        assertThat(header.invalid()).isTrue();
        assertThat(header.expressions()).hasSize(1);
        assertThat(header.expressions().get(0).identifiers()).containsExactly("Apache-2.0");
        assertThat(header.remaining()).contains("MIT\n");
    }
}
