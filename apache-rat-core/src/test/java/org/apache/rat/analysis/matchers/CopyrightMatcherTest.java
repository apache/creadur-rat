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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.rat.analysis.IHeaderMatcher.State;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CopyrightMatcherTest {
    private final static int NAME = 0;
    private final static int TEXT = 1;
    // to be added
    private static String[] prefix = { "Copyright", "©", "(C)", "(c)" };

    private final static String[] D = { "-d", " 1990-1991" };
    private final static String[] DO = { "-d-o", " 1990-1991 an owner" };
    private final static String[] OD = { "-o-d", " an owner 1990-1991" };
    private final static String[] DOS = { "-d-o-s", " 1990 - 1991 an owner" };
    private final static String[] ODS = { "-o-d-s", " an owner 1990 - 1991" };
    private final static String[] S = { "-s", " 1990" };
    private final static String[] O = { "-o", " an owner" };
    private final static String[] OS = { "-o-s", " an owner 1990" };
    private final static String[] SO = { "-s-o", " 1990 an owner" };

    private static final int TOTAL_TESTS = prefix.length * 9;

    static Object[] startStopOwner = { "start-stop-owner", "1990", "1991", "an owner",
            new String[][] { DO, OD, DOS, ODS }, new String[][] { D, S, O, OS, SO } };
    static Object[] startOwner = { "start-owner", "1990", null, "an owner", new String[][] { OS, SO, OD, ODS },
            new String[][] { D, DO, DOS, S, O } };
    static Object[] start = { "start", "1990", null, null, new String[][] { D, DO, DOS, S, SO },
            new String[][] { OD, ODS, O, OS } };
    static Object[] owner = { "owner", null, null, "an owner", new String[][] { O, OD, ODS, OS },
            new String[][] { DO, DOS, S, D, SO } };
    static Object[] nada = { "nada", null, null, null, new String[][] { D, DO, DOS, S, SO },
            new String[][] { OD, ODS, O, OS } };

    private final CopyrightMatcher matcher;
    private final String[][] pass;
    private final String[][] fail;
    private final String testName;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(expandResults(startStopOwner), expandResults(startOwner), expandResults(start),
                expandResults(owner), expandResults(nada));
    }

    private static Object[] expandResults(Object[] source) {
        Object[] result = new Object[6];
        result[0] = source[0];
        result[1] = source[1];
        result[2] = source[2];
        result[3] = source[3];

        List<String[]> arry = new ArrayList<>();
        for (String pfx : prefix) {
            Arrays.stream((String[][]) source[4]).map(origin -> new String[] { pfx + origin[0], pfx + origin[1] })
                    .forEach(arry::add);
        }
        result[4] = arry.toArray(new String[arry.size()][2]);
        arry.clear();

        for (String pfx : prefix) {
            Arrays.stream((String[][]) source[5]).map(origin -> new String[] { pfx + origin[0], pfx + origin[1] })
                    .forEach(arry::add);
        }
        result[5] = arry.toArray(new String[arry.size()][2]);
        return result;
    }

    private static void verify(String testName, String[][] pass, String[][] fail) {
        assertEquals("Wrong number of pass/fail tests", TOTAL_TESTS, pass.length + fail.length);
        Set<String> passSet = new HashSet<String>();
        Arrays.stream(pass).forEach(s -> passSet.add(s[0]));
        Set<String> failSet = new HashSet<String>();
        Arrays.stream(fail).forEach(s -> failSet.add(s[0]));
        for (String s : passSet) {
            assertFalse(String.format("%s is in both pass and fail sets for %s", s, testName), failSet.contains(s));
        }
    }

    public CopyrightMatcherTest(String testName, String start, String stop, String owner, String[][] pass,
            String[][] fail) {
        verify(testName, pass, fail);
        matcher = new CopyrightMatcher(start, stop, owner);
        this.pass = pass;
        this.fail = fail;
        this.testName = testName;
    }

    @Test
    public void testPass() {
        for (String[] target : pass) {
            String errMsg = String.format("%s:%s failed", testName, target[NAME]);
            assertEquals(errMsg, State.t,matcher.matches(target[TEXT]));
            matcher.reset();
        }
    }

    @Test
    public void testFail() {
        for (String[] target : fail) {
            String errMsg = String.format("%s:%s passed", testName, target[NAME]);
            assertEquals(errMsg, State.i, matcher.matches(target[TEXT]));
            assertEquals(errMsg, State.f, matcher.finalizeState());
            matcher.reset();
        }
    }
}
