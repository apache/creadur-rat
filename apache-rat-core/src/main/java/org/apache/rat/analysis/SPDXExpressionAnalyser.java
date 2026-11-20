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
package org.apache.rat.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.rat.analysis.matchers.AbstractMatcherContainer;
import org.apache.rat.analysis.matchers.FullTextMatcher;
import org.apache.rat.analysis.matchers.SPDXExpression;
import org.apache.rat.analysis.matchers.SPDXMatcherFactory;
import org.apache.rat.api.MetaData;
import org.apache.rat.license.ILicense;

/** Applies SPDX expression alternatives without discarding independent full-text matches. */
final class SPDXExpressionAnalyser {
    private SPDXExpressionAnalyser() {
    }

    static void analyse(final IHeaders headers, final Collection<ILicense> licenses, final MetaData metadata) {
        if (!headers.raw().contains("SPDX-License-Identifier:")) {
            licenses.stream().filter(license -> license.matches(headers)).forEach(metadata::reportOnLicense);
            return;
        }
        SPDXExpression.Header header = SPDXExpression.fromHeader(headers.raw());

        // Match ordinary headers separately: a rejected SPDX alternative must not
        // hide an independently present license text.
        matching(withRaw(header.remaining()), licenses).forEach(metadata::reportOnLicense);
        Set<String> identifiers = new LinkedHashSet<>();
        header.expressions().forEach(expression -> identifiers.addAll(expression.identifiers()));
        Map<String, List<ILicense>> resolved = new HashMap<>();
        for (String identifier : identifiers) {
            IHeaders single = withRaw(header.remaining() + "\nSPDX-License-Identifier: " + identifier + "\n");
            List<ILicense> matches = matching(single, licenses).stream()
                    .filter(license -> declaresIdentifier(license.getMatcher(), identifier)).toList();
            resolved.put(identifier, matches);
        }

        Predicate<String> approved = identifier -> metadata.hasApprovalPredicate() && !resolved.get(identifier).isEmpty()
                && resolved.get(identifier).stream().allMatch(metadata::isApproved);
        for (SPDXExpression expression : header.expressions()) {
            for (String identifier : expression.select(approved)) {
                List<ILicense> matches = resolved.get(identifier);
                if (matches.isEmpty()) {
                    metadata.reportOnLicense(UnknownLicense.INSTANCE);
                } else {
                    matches.forEach(metadata::reportOnLicense);
                }
            }
        }
        if (header.invalid()) {
            metadata.reportOnLicense(UnknownLicense.INSTANCE);
        }
    }

    private static List<ILicense> matching(final IHeaders headers, final Collection<ILicense> licenses) {
        // SPDX factories cache per-document results. Each synthetic header needs a
        // fresh match pass, including matchers nested inside configured containers.
        licenses.forEach(ILicense::reset);
        return licenses.stream().filter(license -> license.matches(headers)).toList();
    }

    private static boolean declaresIdentifier(final IHeaderMatcher matcher, final String identifier) {
        if (matcher instanceof SPDXMatcherFactory.Match match) {
            return identifier.equals(match.getName());
        }
        if (matcher instanceof AbstractMatcherContainer container) {
            return container.getEnclosed().stream().anyMatch(child -> declaresIdentifier(child, identifier));
        }
        // A negated SPDX matcher is not a declaration of that identifier.
        return false;
    }

    private static IHeaders withRaw(final String raw) {
        String pruned = FullTextMatcher.prune(raw).toLowerCase(Locale.ENGLISH);
        return new IHeaders() {
            @Override
            public String raw() {
                return raw;
            }

            @Override
            public String pruned() {
                return pruned;
            }
        };
    }
}
