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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.rat.analysis.IHeaderMatcher.State;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

    static Arguments startStopOwner = Arguments.of("start-stop-owner", "1990", "1991", "an owner",
            expandResults( DO, OD, DOS, ODS) , expandResults( D, S, O, OS, SO ));
    static Arguments startOwner = Arguments.of( "start-owner", "1990", null, "an owner", 
            expandResults( OS, SO, OD, ODS ), expandResults( D, DO, DOS, S, O ) );
    static Arguments start = Arguments.of("start", "1990", null, null, expandResults(D, DO, DOS, S, SO ),
            expandResults(OD, ODS, O, OS ) );
    static Arguments owner = Arguments.of("owner", null, null, "an owner", expandResults( O, OD, ODS, OS ),
expandResults( DO, DOS, S, D, SO ) );
    static Arguments nada = Arguments.of("nada", null, null, null, expandResults( D, DO, DOS, S, SO ),
            expandResults( OD, ODS, O, OS ) );

    
    
    public static Stream<Arguments> parameterProvider() {
        return Stream.of( startStopOwner, startOwner, start, owner, nada);
    }

    private static String[][] expandResults( String[]...args) {
        List<String[]> arry = new ArrayList<>();
        for (String pfx : prefix) {
          Arrays.stream(args).map(origin -> new String[] { pfx + origin[0], pfx + origin[1] })
                  .forEach(arry::add);
        }
        return arry.toArray(new String[arry.size()][2]);
    }

    private static void verify(String testName, String[][] pass, String[][] fail) {
        assertEquals(TOTAL_TESTS, pass.length + fail.length, "Wrong number of pass/fail tests");
        Set<String> passSet = new HashSet<String>();
        Arrays.stream(pass).forEach(s -> passSet.add(s[0]));
        Set<String> failSet = new HashSet<String>();
        Arrays.stream(fail).forEach(s -> failSet.add(s[0]));
        for (String s : passSet) {
            assertFalse(failSet.contains(s), ()->String.format("%s is in both pass and fail sets for %s", s, testName));
        }
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testPass(String testName, String start, String stop, String owner, String[][] pass,
            String[][] fail) {
        verify(testName, pass, fail);
        CopyrightMatcher matcher = new CopyrightMatcher(start, stop, owner);
        for (String[] target : pass) {
            assertEquals(State.i, matcher.currentState(), ()->String.format("%s:%s failed", testName, target[NAME]));
            assertEquals(State.t, matcher.matches(target[TEXT]), ()->String.format("%s:%s failed", testName, target[NAME]));
            matcher.reset();
            assertEquals(State.i, matcher.currentState(),()->String.format("%s:%s failed", testName, target[NAME]));
        }
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testFail(String testName, String start, String stop, String owner, String[][] pass,
            String[][] fail) {
        verify(testName, pass, fail);
        CopyrightMatcher matcher = new CopyrightMatcher(start, stop, owner);
        for (String[] target : fail) {
            assertEquals( State.i, matcher.currentState(), ()->String.format("%s:%s passed", testName, target[NAME]));
            assertEquals( State.i, matcher.matches(target[TEXT]), ()->String.format("%s:%s passed", testName, target[NAME]));
            assertEquals( State.f, matcher.finalizeState(), ()->String.format("%s:%s passed", testName, target[NAME]));
            matcher.reset();
            assertEquals( State.i, matcher.currentState(), ()->String.format("%s:%s passed", testName, target[NAME]));
        }
    }
}
