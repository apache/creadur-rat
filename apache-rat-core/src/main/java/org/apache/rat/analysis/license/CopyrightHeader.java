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

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData.Datum;

import java.util.regex.Pattern;

/**
 * Matches a typical Copyright header line only based on a regex pattern
 * which allows for one (starting) year or year range, and a
 * configurable copyright owner.
 * <br>
 * <br>The matching is done case insensitive<br>
 * <br>
 * Example supported Copyright header lines, using copyright owner &quot;FooBar&quot;
 * <ul>
 * <li>* Copyright 2010 FooBar. *</li>
 * <li>* Copyright 2010-2012 FooBar. *</li>
 * <li>*copyright 2012 foobar*</li>
 * </ul>
 * <p>Note also that the copyright owner is appended to the regex pattern, so
 * can support additional regex but also requires escaping where needed,<br>
 * e.g. use &quot;FooBar \(www\.foobar\.com\)&quot; for matching &quot;FooBar (www.foobar.com)&quot;</p>
 *
 * @since Rat 0.9
 */
public class CopyrightHeader extends BaseLicense implements IHeaderMatcher {

    public static final String COPYRIGHT_PREFIX_PATTERN_DEFN = ".*Copyright [0-9]{4}(\\-[0-9]{4})? ";

    private Pattern copyrightPattern;
    private String copyrightOwner;
    private boolean copyrightMatch = false;

    public CopyrightHeader() {
    }

    protected CopyrightHeader(Datum licenseFamilyCategory, Datum licenseFamilyName, String notes) {
        super(licenseFamilyCategory, licenseFamilyName, notes);
    }

    protected CopyrightHeader(Datum licenseFamilyCategory, Datum licenseFamilyName, String notes, String copyrightOwner) {
        this(licenseFamilyCategory, licenseFamilyName, notes);
        setCopyrightOwner(copyrightOwner);
    }

    // Called by ctor, so must not be overridden
    public final void setCopyrightOwner(String copyrightOwner) {
        this.copyrightOwner = copyrightOwner;
        this.copyrightPattern = Pattern.compile(COPYRIGHT_PREFIX_PATTERN_DEFN + copyrightOwner + ".*", Pattern.CASE_INSENSITIVE);
    }

    public String getCopyRightOwner() {
        return copyrightOwner;
    }

    public boolean hasCopyrightPattern() {
        return copyrightPattern != null;
    }

    protected boolean isCopyrightMatch() {
        return copyrightMatch;
    }

    protected boolean matchCopyright(String s) {
        if (!copyrightMatch) {
            copyrightMatch = copyrightPattern.matcher(s).matches();
        }
        return copyrightMatch;
    }

    public boolean match(Document subject, String s) throws RatHeaderAnalysisException {
        if (!copyrightMatch && matchCopyright(s)) {
            reportOnLicense(subject);
        }
        return copyrightMatch;
    }

    public void reset() {
        copyrightMatch = false;
    }
}
