package org.apache.rat.config.exclusion.plexus;

/*
 * Copyright The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

import static java.lang.String.format;
@SuppressWarnings({"checkstyle:RegexpSingleLine", "checkstyle:JavadocVariable"})
/**
 * <p>Describes a match target for SelectorUtils.</p>
 *
 * <p>Significantly more efficient than using strings, since re-evaluation and re-tokenizing is avoided.</p>
 *
 * <p>Based on code from plexus-utils.</p>
 *
 * @author Kristian Rosenvold
 * @see <a href="https://github.com/codehaus-plexus/plexus-utils/blob/master/src/main/java/org/codehaus/plexus/util/MatchPattern.java">
 * plexus-utils MatchPattern</a>
 */
public final class MatchPattern {
    private final String source;

    private final String regexPattern;

    private final String separator;

    private final String[] tokenized;

    private final char[][] tokenizedChar;

    public MatchPattern(final String source, final String separator) {
        regexPattern = SelectorUtils.isRegexPrefixedPattern(source)
                ? source.substring(
                SelectorUtils.REGEX_HANDLER_PREFIX.length(),
                source.length() - SelectorUtils.PATTERN_HANDLER_SUFFIX.length())
                : null;
        this.source = SelectorUtils.isAntPrefixedPattern(source)
                ? source.substring(
                SelectorUtils.ANT_HANDLER_PREFIX.length(),
                source.length() - SelectorUtils.PATTERN_HANDLER_SUFFIX.length())
                : source;
        this.separator = separator;
        tokenized = tokenizePathToString(this.source, separator);
        tokenizedChar = new char[tokenized.length][];
        for (int i = 0; i < tokenized.length; i++) {
            tokenizedChar[i] = tokenized[i].toCharArray();
        }
    }

    public boolean matchPath(final String str, final boolean isCaseSensitive) {
        if (regexPattern != null) {
            return str.matches(regexPattern);
        } else {
            return SelectorUtils.matchAntPathPattern(this, str, separator, isCaseSensitive);
        }
    }

    boolean matchPath(final String str, final char[][] strDirs, final boolean isCaseSensitive) {
        boolean result;
        if (regexPattern != null) {
            result = str.matches(regexPattern);
        } else {
            result = SelectorUtils.matchAntPathPattern(getTokenizedPathChars(), strDirs, isCaseSensitive);
        }
        if (result && DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
            DefaultLog.getInstance().debug(format("%s match %s -> %s", this, str, result));
        }
        return result;
    }

    public boolean matchPatternStart(final String str, final boolean isCaseSensitive) {
        if (regexPattern != null) {
            // FIXME: ICK! But we can't do partial matches for regex, so we have to reserve judgement until we have
            // a file to deal with, or we can definitely say this is an exclusion...
            return true;
        } else {
            String altStr = str.replace('\\', '/');

            return SelectorUtils.matchAntPathPatternStart(this, str, File.separator, isCaseSensitive)
                    || SelectorUtils.matchAntPathPatternStart(this, altStr, "/", isCaseSensitive);
        }
    }

    public String[] getTokenizedPathString() {
        return tokenized;
    }

    public char[][] getTokenizedPathChars() {
        return tokenizedChar;
    }

    public boolean startsWith(final String string) {
        return source.startsWith(string);
    }

    @Override
    public String toString() {
        return Arrays.asList(tokenized).toString();
    }

    public String source() {
        return source;
    }

    public static String[] tokenizePathToString(final String path, final String separator) {
        List<String> ret = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(path, separator);
        while (st.hasMoreTokens()) {
            ret.add(st.nextToken());
        }
        return ret.toArray(new String[0]);
    }

    static char[][] tokenizePathToCharArray(final String path, final String separator) {
        String[] tokenizedName = tokenizePathToString(path, separator);
        char[][] tokenizedNameChar = new char[tokenizedName.length][];
        for (int i = 0; i < tokenizedName.length; i++) {
            tokenizedNameChar[i] = tokenizedName[i].toCharArray();
        }
        return tokenizedNameChar;
    }
}
