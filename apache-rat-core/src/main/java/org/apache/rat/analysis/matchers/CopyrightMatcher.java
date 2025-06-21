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
package org.apache.rat.analysis.matchers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.ConfigComponent;

/**
 * Matches a typical Copyright header line only based on a regex pattern which
 * allows for one (starting) year or year range, and a configurable copyright
 * owner. <br>
 * <br>
 * The matching is done case insensitive<br>
 * <br>
 * Example supported Copyright header lines, using copyright owner
 * &quot;FooBar&quot;
 * <ul>
 * <li>Copyright 2010 FooBar.</li>
 * <li>Copyright 2010-2012 FooBar.</li>
 * <li>copyright 2012 foobar</li>
 * </ul>
 * <p>
 * Note also that the copyright owner is appended to the regex pattern and so
 * can support additional regex but also requires escaping where needed,<br>
 * e.g. use &quot;FooBar \\(www\\.foobar\\.com\\)&quot; or &quot;FooBar
 * \\Q(www.foobar.com)\\E&quot; to match &quot;FooBar (www.foobar.com)&quot;
 * </p>
 * <p>
 * The matcher also accepts "(C)", "(c)", and "©" in place of (or in addition
 * to) the "Copyright" or "copyright" keyword
 * </p>
 */
@ConfigComponent(type = ComponentType.MATCHER, name = "copyright",
        desc = "A matcher that matches Copyright text. " +
                "Uses regular expressions and so should only be used when looking for copyrights with specific " +
                "patterns that are not caught by a standard text matcher. This matcher will match \"(C)\", \"copyright\", " +
                "or \"©\". (text is not case sensitive). It will also match things like Copyright (c) joe 1995 as well " +
                "as Copyright (C) 1995 joe and Copyright (C) joe 1995.")
public class CopyrightMatcher extends AbstractHeaderMatcher {
    /** String to build a pattern to match the various recognized copyright symbols */
    private static final String COPYRIGHT_SYMBOL_DEFN = "\\([Cc]\\)|©|\\&[Cc][Oo][Pp][Yy]\\;";
    /** String to build a pattern to match symbols or word "copyright" */
    private static final String COPYRIGHT_PATTERN_DEFN = "(\\b)?" + COPYRIGHT_SYMBOL_DEFN + "|Copyright\\b";
    /** The compiled pattern from {@link #COPYRIGHT_PATTERN_DEFN} */
    private static final Pattern COPYRIGHT_PATTERN = Pattern.compile(COPYRIGHT_PATTERN_DEFN);
    /** The string to build a Pattern to match a copyright with a single part (date or name) */
    private static final String ONE_PART = "\\s+((" + COPYRIGHT_SYMBOL_DEFN + ")\\s+)?%s";
    /** The string to build a Pattern to match a copyright iwth both name and date */
    private static final String TWO_PART = "\\s+((" + COPYRIGHT_SYMBOL_DEFN + ")\\s+)?%s,?\\s+%s";
    /** Format string to build a pattern to match two dates */
    private static final String DOUBLE_DATE_FMT = "%s\\s*-\\s*%s";
    /** String to build pattern to match an arbitrary date (year) */
    private static final String ARBITRARY_DATE = "[0-9]{4}";
    /** The built Pattern for matching "Copyright date owner" */
    private final Pattern dateOwnerPattern;
    /** The built pattern for matching "Copyright owner date" */
    private final Pattern ownerDatePattern;
    /** The start date of the copyright. May be null. */
    @ConfigComponent(type = ComponentType.PARAMETER, desc = "The initial year of the copyright if any")
    private final String start;
    /** The end date of the copyright. May be null. */
    @ConfigComponent(type = ComponentType.PARAMETER, desc = "The last year the copyright.  Only valid with 'start'")
    private final String end;
    /** The owner of the copyright. May be null */
    @ConfigComponent(type = ComponentType.PARAMETER, desc = "The owner of the copyright")
    private final String owner;

    /**
     * Constructs the CopyrightMatcher with the specified start, stop and owner
     * strings and a unique random id.
     *
     * @param start the start date for the copyright, may be null.
     * @param end the stop date for the copyright, may be null. May not be
     * specified if start is not specified.
     * @param owner the owner of the copyright, may be null.
     */
    public CopyrightMatcher(final String start, final String end, final String owner) {
        this(null, start, end, owner);
    }

    private static void assertNumber(final String label, final String value) {
        try {
            if (StringUtils.isNotEmpty(value)) {
                Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            throw new ConfigurationException(String.format("'%s' must be numeric (value provided: '%s')", label, value));
        }
    }
    /**
     * Constructs the CopyrightMatcher with the specified start, stop and owner
     * strings.
     *
     * @param id the id for the matcher.
     * @param start the start date for the copyright, may be null.
     * @param end the end date for the copyright, may be null. May not be
     * specified if start is not specified.
     * @param owner the owner of the copyright. may be null.
     */
    public CopyrightMatcher(final String id, final String start, final String end, final String owner) {
        super(id);
        if (StringUtils.isBlank(start) && !StringUtils.isBlank(end)) {
            throw new ConfigurationException("'end' may not be set if 'start' is not set.");
        }
        assertNumber("start", start);
        assertNumber("end", end);
        this.start = start;
        this.end = end;
        this.owner = owner;
        String dateDefn = "";
        if (StringUtils.isNotEmpty(start)) {
            if (StringUtils.isNotEmpty(end)) {
                dateDefn = String.format(DOUBLE_DATE_FMT, this.start, this.end);
            } else {
                dateDefn = this.start;
            }
        }
        if (StringUtils.isEmpty(owner)) {
            // no owner
            if (StringUtils.isEmpty(dateDefn)) {
                dateDefn = ARBITRARY_DATE;
            }
            dateOwnerPattern = Pattern.compile(String.format(ONE_PART, dateDefn));
            ownerDatePattern = null;
        } else {
            if (StringUtils.isEmpty(dateDefn)) {
                dateDefn = String.format(DOUBLE_DATE_FMT, "(((" + ARBITRARY_DATE, ")?" + ARBITRARY_DATE + "))?");
                dateOwnerPattern = Pattern.compile(String.format(TWO_PART, dateDefn, owner));
                ownerDatePattern = Pattern.compile(String.format(ONE_PART, owner));
            } else {
                dateOwnerPattern = Pattern.compile(String.format(TWO_PART, dateDefn, owner));
                ownerDatePattern = Pattern.compile(String.format(TWO_PART, owner, dateDefn));
            }
        }
    }

    /**
     * Gets the start date of the copyright.
     * @return the start date of the copyright or {@code null} if not set
     */
    public String getStart() {
        return start;
    }

    /**
     * Gets the end date of the copyright.
     * @return the end date of the copyright or {@code null} if not set
     */
    public String getEnd() {
        return end;
    }

    /**
     * Gets the owner of the copyright.
     * @return the owner of the copyright or {@code null} if not set
     */
    public String getOwner() {
        return owner;
    }

    @Override
    public boolean matches(final IHeaders headers) {
        String lowerLine = headers.raw().toLowerCase();
        if (lowerLine.contains("copyright") || lowerLine.contains("(c)") || lowerLine.contains("©") ||
                lowerLine.contains("&copy;")) {
            Matcher matcher = COPYRIGHT_PATTERN.matcher(headers.raw());
            if (matcher.find()) {
                String buffer = headers.raw().substring(matcher.end());
                matcher = dateOwnerPattern.matcher(buffer);
                if (matcher.find() && matcher.start() == 0) {
                    return true;
                }
                if (ownerDatePattern != null) {
                    matcher = ownerDatePattern.matcher(buffer);
                    return matcher.find() && matcher.start() == 0;
                }
            }
        }
        return false;
    }
}
