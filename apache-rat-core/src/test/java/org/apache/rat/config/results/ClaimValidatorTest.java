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
package org.apache.rat.config.results;

import java.util.Collection;
import java.util.Collections;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClaimValidatorTest {

   @Test
    public void initialStateTest() {
       ClaimValidator validator = new ClaimValidator();
       assertFalse(validator.hasErrors());
       for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
           int expected = counter.getDefaultMaxValue() < 0 ? Integer.MAX_VALUE : counter.getDefaultMaxValue();
           assertEquals(expected, validator.get(counter), () -> format("'%s' value is invalid", counter));
           assertTrue(validator.isValid(counter, expected));
       }
    }

    @Test
    public void setTest() {
        ClaimValidator validator = new ClaimValidator();
        int expected = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            validator.set(counter, expected);
            assertEquals(expected, validator.get(counter), () -> format("'%s' value is invalid", counter));
            assertTrue(validator.isValid(counter, expected));
        }
        expected = Integer.MAX_VALUE;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            validator.set(counter, -1);
            assertEquals(expected, validator.get(counter), () -> format("'%s' value is invalid", counter));
            assertTrue(validator.isValid(counter, expected));
        }
    }

    @Test
    public void isValidTest() {
        int expected = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            ClaimValidator validator = new ClaimValidator();
            assertFalse(validator.hasErrors());
            validator.set(counter, expected);
            assertTrue(validator.isValid(counter, expected), () -> format("error with %s expected", counter)) ;
            assertFalse(validator.hasErrors(), () -> format("error with %s, counter", counter)) ;
            assertTrue(validator.isValid(counter, expected - 1), () -> format("error with %s -1", counter)) ;
            assertFalse(validator.hasErrors(), () -> format("error with %s, counter", counter)) ;
            assertFalse(validator.isValid(counter, expected + 1), () -> format("error with %s +1", counter)) ;
            assertTrue(validator.hasErrors(), () -> format("error with %s, counter", counter)) ;
        }
    }

    @Test
    public void logIssuesTest() {
        ClaimStatistic statistic = new ClaimStatistic();
        TestingLog log = new TestingLog();
        try {
            DefaultLog.setInstance(log);

            int expected = 5;
            for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
                ClaimValidator validator = new ClaimValidator();
                validator.set(counter, expected);
                statistic.incCounter(counter, expected);
                validator.logIssues(statistic);
                assertTrue(log.getCaptured().isEmpty());
                statistic.incCounter(counter, 1);
                validator.logIssues(statistic);
                String expectedStr = format("ERROR: Unexpected count for %s, limit is 5.  Count: 6", counter);
                log.assertContains(expectedStr);
                log.clear();
                statistic.incCounter(counter, -1 - expected);
            }
        } finally {
            DefaultLog.setInstance(null);
        }
    }

    @Test
    public void listIssuesTest() {
        ClaimStatistic statistic = new ClaimStatistic();

        int expected = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            ClaimValidator validator = new ClaimValidator();
            validator.set(counter, expected);
            statistic.incCounter(counter, expected);
            Collection<String> actual = validator.listIssues(statistic);
            assertTrue(actual.isEmpty());
            statistic.incCounter(counter, 1);
            actual = validator.listIssues(statistic);
            Collection<String> expectedCollection = Collections.singletonList(counter.name());
            assertEquals(expectedCollection, actual);
            statistic.incCounter(counter, -1 - expected);
        }
    }
}
