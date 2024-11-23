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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
           assertEquals(expected, validator.getMax(counter), () -> format("Max value '%s' is invalid", counter));
           assertEquals(counter.getDefaultMinValue(), validator.getMin(counter), () -> format("Min value '%s' is invalid", counter));
           assertTrue(validator.isValid(counter, expected), () -> format("max value (%s) should not be invalid", expected));
           assertTrue(validator.isValid(counter, counter.getDefaultMinValue()), () -> format("min value (%s) should not be invalid",
                   counter.getDefaultMinValue()));
       }
    }

    @Test
    public void setMaxTest() {
        ClaimValidator validator = new ClaimValidator();
        int expected = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            validator.setMax(counter, expected);
            assertEquals(expected, validator.getMax(counter), () -> format("'%s' value is invalid", counter));
            assertTrue(validator.isValid(counter, expected));
        }
        expected = Integer.MAX_VALUE;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            validator.setMax(counter, -1);
            assertEquals(expected, validator.getMax(counter), () -> format("'%s' value is invalid", counter));
            assertTrue(validator.isValid(counter, expected));
        }
    }

    @Test
    public void setMinTest() {
        ClaimValidator validator = new ClaimValidator();
        int expected = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            validator.setMin(counter, expected);
            assertEquals(expected, validator.getMin(counter), () -> format("'%s' value is invalid", counter));
            assertTrue(validator.isValid(counter, expected));
        }
        expected = Integer.MAX_VALUE;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            validator.setMax(counter, -1);
            assertEquals(expected, validator.getMax(counter), () -> format("'%s' value is invalid", counter));
            assertTrue(validator.isValid(counter, expected));
        }
    }

    @Test
    public void isValidTest() {
        int expected = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            ClaimValidator validator = new ClaimValidator();
            assertFalse(validator.hasErrors());
            validator.setMax(counter, expected);
            assertTrue(validator.isValid(counter, expected), () -> format("error with %s expected", counter)) ;
            assertFalse(validator.hasErrors(), () -> format("error with %s, counter", counter)) ;
            assertTrue(validator.isValid(counter, expected - 1), () -> format("error with %s -1", counter)) ;
            assertFalse(validator.hasErrors(), () -> format("error with %s, counter", counter)) ;
            assertFalse(validator.isValid(counter, expected + 1), () -> format("error with %s +1", counter)) ;
            assertTrue(validator.hasErrors(), () -> format("error with %s, counter", counter)) ;
        }
    }

    private List<ClaimStatistic.Counter> getRequiredCounters() {
        return Arrays.stream(ClaimStatistic.Counter.values())
                .filter(c -> c.getDefaultMinValue() > 0)
                .collect(Collectors.toList());
    }

    @Test
    public void logIssuesTest() {
        ClaimStatistic statistic = new ClaimStatistic();
        TestingLog log = new TestingLog();
        try {
            DefaultLog.setInstance(log);
            Map<ClaimStatistic.Counter, String> required = new HashMap<>();
            getRequiredCounters().forEach(counter ->
                    required.put(counter, format("ERROR: Unexpected count for %s, limit is [%s,%s].  Count: 0",
                            counter, counter.getDefaultMinValue(),
                            counter.getDefaultMaxValue() == -1 ? Integer.MAX_VALUE : counter.getDefaultMaxValue())));

            int expected = 5;
            for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
                ClaimValidator validator = new ClaimValidator();
                validator.setMax(counter, expected);
                statistic.incCounter(counter, expected);
                validator.logIssues(statistic);
                assertFalse(log.getCaptured().isEmpty());
                required.entrySet().stream().filter(e -> e.getKey() != counter)
                        .map(Map.Entry::getValue).forEach(log::assertContains);
                if (required.entrySet().contains(counter)) {
                    log.assertNotContains(required.get(counter));
                }
                statistic.incCounter(counter, 1);
                validator.logIssues(statistic);
                String expectedStr = format("ERROR: Unexpected count for %s, limit is [%s,5].  Count: 6", counter,
                        counter.getDefaultMinValue());
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
        List<String> required = getRequiredCounters().stream().map(ClaimStatistic.Counter::name).collect(Collectors.toList());

        int value = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            List<String> expected = new ArrayList<>(required);
            expected.remove(counter.name());
            ClaimValidator validator = new ClaimValidator();
            validator.setMax(counter, value);
            statistic.incCounter(counter, value);
            Collection<String> actual = validator.listIssues(statistic);
            assertEquals(expected, actual);
            statistic.incCounter(counter, 1);
            expected.add(counter.name());
            expected.sort(String::compareTo);
            actual = validator.listIssues(statistic);
            assertEquals(expected, actual);
            statistic.incCounter(counter, -1 - value);
        }
    }
}
