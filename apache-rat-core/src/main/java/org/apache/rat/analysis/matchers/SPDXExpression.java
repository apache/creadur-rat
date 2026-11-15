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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * An SPDX license expression. WITH binds to an identifier, AND binds more
 * tightly than OR, and parentheses override precedence. Identifiers are case
 * sensitive; exceptions must be explicitly configured as part of a WITH atom.
 */
public final class SPDXExpression {
    /** A tag and its expression, bounded by the end of the line. */
    private static final Pattern TAG = Pattern.compile("SPDX-License-Identifier:([^\\r\\n]*)");
    /** Standard identifiers, legacy plus suffixes, and external license references. */
    private static final Pattern IDENTIFIER = Pattern.compile(
            "(?:DocumentRef-[A-Za-z0-9.-]+:)?LicenseRef-[A-Za-z0-9.-]+|"
                    + "(?!LicenseRef-|DocumentRef-)[A-Za-z0-9][A-Za-z0-9.-]*\\+?");
    /** An exception identifier, without the legacy plus suffix. */
    private static final Pattern EXCEPTION = Pattern.compile("[A-Za-z0-9][A-Za-z0-9.-]*");
    /** Tokens include parentheses and retain all non-whitespace text for validation. */
    private static final Pattern TOKEN = Pattern.compile("[()]|[^\\p{javaWhitespace}\\p{javaSpaceChar}\\u0085()]+");
    /** Space characters not all covered by String.stripTrailing(), including non-breaking spaces and NEL. */
    private static final Pattern SPACE = Pattern.compile("[\\p{javaSpaceChar}\\u0085]");
    /** Maximum expression length. */
    private static final int MAX_LENGTH = 16384;
    /** Maximum token count, also bounding expression tree depth. */
    private static final int MAX_TOKENS = 512;
    /** Maximum parenthesis nesting depth. */
    private static final int MAX_DEPTH = 64;

    /** The root of the parsed expression tree. */
    private final Node root;

    private SPDXExpression(final Node root) {
        this.root = root;
    }

    /**
     * Parses a complete expression; trailing text and incomplete expressions are rejected.
     * @param text the expression without the SPDX tag or comment delimiters.
     * @return the parsed expression.
     * @throws IllegalArgumentException if the expression is invalid or exceeds the parser limits.
     */
    public static SPDXExpression parse(final String text) {
        Parser parser = new Parser(text);
        Node result = parser.or(0);
        if (parser.position != parser.tokens.size()) {
            throw new IllegalArgumentException("Unexpected token in SPDX expression");
        }
        return new SPDXExpression(result);
    }

    /**
     * Gets all identifiers, treating a license WITH an exception as a single atom.
     * @return the identifiers in this expression.
     */
    public Set<String> identifiers() {
        Set<String> result = new LinkedHashSet<>();
        root.collect(result);
        return Set.copyOf(result);
    }

    /**
     * Evaluates approval against the supplied identifier policy.
     * @param approved the approval policy for identifiers.
     * @return whether a permitted licensing alternative exists.
     */
    public boolean isApproved(final Predicate<String> approved) {
        return root.approved(approved);
    }

    /**
     * Selects an approved OR branch if available, otherwise retains both branches
     * for reporting. AND always retains both requirements.
     * @param approved the approval policy for identifiers.
     * @return the identifiers needed for the selected licensing alternative.
     */
    public Set<String> select(final Predicate<String> approved) {
        Set<String> result = new LinkedHashSet<>();
        root.select(approved, result);
        return Set.copyOf(result);
    }

    /**
     * Extracts each SPDX tag on its own line, including tags in source comments.
     * Invalid tags are recorded, not silently reduced to a valid prefix.
     * @param raw the raw header text.
     * @return expressions and header text with the tags removed.
     */
    public static Header fromHeader(final String raw) {
        Matcher matcher = TAG.matcher(raw);
        List<SPDXExpression> expressions = new ArrayList<>();
        StringBuilder remaining = new StringBuilder();
        boolean invalid = false;
        while (matcher.find()) {
            String text = expressionText(matcher.group(1));
            try {
                expressions.add(parse(text));
            } catch (IllegalArgumentException e) {
                invalid = true;
            }
            matcher.appendReplacement(remaining, "");
        }
        matcher.appendTail(remaining);
        return new Header(List.copyOf(expressions), invalid, remaining.toString());
    }

    private static String expressionText(final String text) {
        String expression = SPACE.matcher(text).replaceAll(" ").stripTrailing();
        return StringUtils.removeEnd(expression, expression.endsWith("*/") ? "*/" : "-->");
    }

    /**
     * Parsed header data. Each separate SPDX tag is an additional requirement.
     * @param expressions the successfully parsed expressions.
     * @param invalid whether at least one tag was malformed.
     * @param remaining the header with all SPDX tags removed.
     */
    public record Header(List<SPDXExpression> expressions, boolean invalid, String remaining) {
    }

    private interface Node {
        boolean approved(Predicate<String> policy);
        void collect(Set<String> identifiers);
        void select(Predicate<String> policy, Set<String> identifiers);
    }

    private record Identifier(String name) implements Node {
        @Override
        public boolean approved(final Predicate<String> policy) {
            return policy.test(name);
        }

        @Override
        public void collect(final Set<String> identifiers) {
            identifiers.add(name);
        }

        @Override
        public void select(final Predicate<String> policy, final Set<String> identifiers) {
            collect(identifiers);
        }
    }

    private record Binary(Node left, Node right, boolean conjunction) implements Node {
        @Override
        public boolean approved(final Predicate<String> policy) {
            return conjunction ? left.approved(policy) && right.approved(policy)
                    : left.approved(policy) || right.approved(policy);
        }

        @Override
        public void collect(final Set<String> identifiers) {
            left.collect(identifiers);
            right.collect(identifiers);
        }

        @Override
        public void select(final Predicate<String> policy, final Set<String> identifiers) {
            if (!conjunction && left.approved(policy)) {
                left.select(policy, identifiers);
            } else if (!conjunction && right.approved(policy)) {
                right.select(policy, identifiers);
            } else {
                left.select(policy, identifiers);
                right.select(policy, identifiers);
            }
        }
    }

    private static final class Parser {
        /** The expression tokens. */
        private final List<String> tokens;
        /** The next token to consume. */
        private int position;

        Parser(final String text) {
            if (text == null || text.length() > MAX_LENGTH) {
                throw new IllegalArgumentException("Invalid SPDX expression length");
            }
            tokens = TOKEN.matcher(text).results().limit(MAX_TOKENS + 1L).map(MatchResult::group).toList();
            if (tokens.size() > MAX_TOKENS) {
                throw new IllegalArgumentException("Too many tokens in SPDX expression");
            }
        }

        private boolean accept(final String token) {
            if (position < tokens.size() && token.equals(tokens.get(position))) {
                position++;
                return true;
            }
            return false;
        }

        private Node or(final int depth) {
            Node result = and(depth);
            while (accept("OR")) {
                result = new Binary(result, and(depth), false);
            }
            return result;
        }

        private Node and(final int depth) {
            Node result = primary(depth);
            while (accept("AND")) {
                result = new Binary(result, primary(depth), true);
            }
            return result;
        }

        private Node primary(final int depth) {
            if (depth > MAX_DEPTH || position == tokens.size()) {
                throw new IllegalArgumentException("Incomplete or excessively nested SPDX expression");
            }
            if (accept("(")) {
                Node result = or(depth + 1);
                if (!accept(")")) {
                    throw new IllegalArgumentException("Unclosed parenthesis in SPDX expression");
                }
                return result;
            }
            String name = identifier(IDENTIFIER);
            if (accept("WITH")) {
                name += " WITH " + identifier(EXCEPTION);
            }
            return new Identifier(name);
        }

        private String identifier(final Pattern pattern) {
            if (position == tokens.size()) {
                throw new IllegalArgumentException("Missing SPDX identifier");
            }
            String token = tokens.get(position++);
            if (StringUtils.equalsAny(token, "AND", "OR", "WITH") || !pattern.matcher(token).matches()) {
                throw new IllegalArgumentException("Invalid SPDX identifier: " + token);
            }
            return token;
        }
    }
}
