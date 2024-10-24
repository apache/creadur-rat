/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.configuration.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.SortedSet;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.SimpleRegexMatcher;
import org.apache.rat.configuration.XMLConfigurationReader;
import org.apache.rat.license.ILicense;
import org.junit.jupiter.api.Test;

public class RegexBuilderTest {

    @Test
    public void xmlTest() {
        String configStr = "<rat-config>" //
                + "        <families>" //
                + "            <family id='newFam' name='my new family' />" //
                + "        </families>" //
                + "        <licenses>" //
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>" //
                + "                <regex expression='.' />" //
                + "            </license>" //
                + "        </licenses>" //
                + "    </rat-config>";

        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        SortedSet<ILicense> licenses = reader.readLicenses();
        assertEquals(1, licenses.size());
        IHeaderMatcher matcher = licenses.first().getMatcher();
        assertEquals(SimpleRegexMatcher.class, matcher.getClass());
        SimpleRegexMatcher result = (SimpleRegexMatcher) matcher;
        assertEquals(".", result.getPattern());
    }

    @Test
    public void xmlIDTest() {
        String configStr = "<rat-config>" //
                + "        <families>" //
                + "            <family id='newFam' name='my new family' />" //
                + "        </families>" //
                + "        <licenses>" //
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>" //
                + "                <regex id='foo' expression='.' />" //
                + "            </license>" //
                + "        </licenses>" //
                + "    </rat-config>";

        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        SortedSet<ILicense> licenses = reader.readLicenses();
        assertEquals(1, licenses.size());
        IHeaderMatcher matcher = licenses.first().getMatcher();
        assertEquals("foo", matcher.getId());
        assertEquals(SimpleRegexMatcher.class, matcher.getClass());
        SimpleRegexMatcher result = (SimpleRegexMatcher) matcher;
        assertEquals(".", result.getPattern());
    }
}
