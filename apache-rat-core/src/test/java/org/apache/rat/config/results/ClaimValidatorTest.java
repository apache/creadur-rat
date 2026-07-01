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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimValidatorTest {

    @Test
    void initialStateTest() {
        ClaimValidator validator = new ClaimValidator();
        assertThat(validator.hasErrors()).isFalse();
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            int expected = counter.getDefaultMaxValue() < 0 ? Integer.MAX_VALUE : counter.getDefaultMaxValue();
            assertThat(validator.getMax(counter)).as(() -> format("Max value '%s' is invalid", counter)).isEqualTo(expected);
            assertThat(validator.getMin(counter)).as(() -> format("Min value '%s' is invalid", counter)).isEqualTo(counter.getDefaultMinValue());
            assertThat(validator.isValid(counter, expected)).as(() -> format("max value (%s) should not be invalid", expected)).isTrue();
            assertThat(validator.isValid(counter, counter.getDefaultMinValue())).as(() -> format("min value (%s) should not be invalid",
                    counter.getDefaultMinValue())).isTrue();
        }
    }

    @Test
    void setMaxTest() {
        ClaimValidator validator = new ClaimValidator();
        int expected = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            validator.setMax(counter, expected);
            assertThat(validator.getMax(counter)).as(() -> format("'%s' value is invalid", counter)).isEqualTo(expected);
            assertThat(validator.isValid(counter, expected)).isTrue();
        }
        expected = Integer.MAX_VALUE;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            validator.setMax(counter, -1);
            assertThat(validator.getMax(counter)).as(() -> format("'%s' value is invalid", counter)).isEqualTo(expected);
            assertThat(validator.isValid(counter, expected)).isTrue();
        }
    }

    @Test
    void setMinTest() {
        ClaimValidator validator = new ClaimValidator();
        int expected = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            validator.setMin(counter, expected);
            assertThat(validator.getMin(counter)).as(() -> format("'%s' min value is invalid", counter)).isEqualTo(expected);
            assertThat(validator.isValid(counter, expected)).as(() -> format("'%s' value is invalid", counter)).isTrue();
        }
        expected = Integer.MAX_VALUE;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            validator.setMax(counter, -1);
            assertThat(validator.getMax(counter)).as(() -> format("'%s' max value is invalid", counter)).isEqualTo(expected);
            assertThat(validator.isValid(counter, expected)).as(() -> format("'%s' value is invalid", counter)).isTrue();
        }
    }

    @Test
    void isValidTest() {
        int expected = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            ClaimValidator validator = new ClaimValidator();
            assertThat(validator.hasErrors()).isFalse();
            validator.setMax(counter, expected);
            assertThat(validator.isValid(counter, expected)).as(() -> format("error with %s expected", counter)).isTrue();
            assertThat(validator.hasErrors()).as(() -> format("error with %s, counter", counter)).isFalse();
            assertThat(validator.isValid(counter, expected - 1)).as(() -> format("error with %s -1", counter)).isTrue();
            assertThat(validator.hasErrors()).as(() -> format("error with %s, counter", counter)).isFalse();
            assertThat(validator.isValid(counter, expected + 1)).as(() -> format("error with %s +1", counter)).isFalse();
            assertThat(validator.hasErrors()).as(() -> format("error with %s, counter", counter)).isTrue();
        }
    }

    private List<ClaimStatistic.Counter> getRequiredCounters() {
        return Arrays.stream(ClaimStatistic.Counter.values())
                .filter(c -> c.getDefaultMinValue() > 0)
                .collect(Collectors.toList());
    }

    @Test
    void logIssuesTest() {
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
                assertThat(log.getCaptured()).isNotEmpty();
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
    void nullValuesTest() {
        ClaimValidator underTest = new ClaimValidator();
        TestingLog log = new TestingLog();
        try {
            DefaultLog.setInstance(log);
            underTest.setMin(null, 5);
            assertThat(log.getCaptured()).contains("`null` passed as argument to setMin() -- ignoring");
            underTest.setMax(null, 10);
            assertThat(log.getCaptured()).contains("`null` passed as argument to setMax() -- ignoring");
            assertThat(underTest.getMin(null)).isZero();
            assertThat(log.getCaptured()).contains("`null` passed as argument to getMin() -- returning 0");
            assertThat(underTest.getMax(null)).isZero();
            assertThat(log.getCaptured()).contains("`null` passed as argument to getMax() -- returning 0");
        } finally {
            DefaultLog.setInstance(null);
        }

        assertThatThrownBy(() -> underTest.isValid(null, 5)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("counter must not be null.");

        assertThatThrownBy(() -> underTest.logIssues(null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("statistic must not be null.");

        assertThatThrownBy(() -> underTest.listIssues(null)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("statistic must not be null.");
    }

    @Test
    void nullCounterForMinTest() {
        ClaimValidator underTest = new ClaimValidator(false);
        // UNAPPROVED has a default min and max of 0
        underTest.setMin(ClaimStatistic.Counter.UNAPPROVED, 5);
        assertThat(underTest.getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
        assertThat(underTest.getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
    }

    @Test
    void nullCounterForMaxTest() {
        ClaimValidator underTest = new ClaimValidator(false);
        // UNAPPROVED has a default min and max of 0
        underTest.setMax(ClaimStatistic.Counter.UNAPPROVED, 5);
        assertThat(underTest.getMin(ClaimStatistic.Counter.UNAPPROVED)).isZero();
        assertThat(underTest.getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);

        underTest.setMax(ClaimStatistic.Counter.UNAPPROVED, -1);
        assertThat(underTest.getMin(ClaimStatistic.Counter.UNAPPROVED)).isZero();
        assertThat(underTest.getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void testMinHigherThanMax() {
        ClaimValidator underTest = new ClaimValidator();
        // UNAPPROVED has a default min and max of 0
        underTest.setMin(ClaimStatistic.Counter.UNAPPROVED, 5);
        assertThat(underTest.getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
        assertThat(underTest.getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
    }

    @Test
    void testMaxLowerThanMin() {
        ClaimValidator underTest = new ClaimValidator();
        // UNAPPROVED has a default min and max of 0
        underTest.setMin(ClaimStatistic.Counter.UNAPPROVED, 5);
        assertThat(underTest.getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
        assertThat(underTest.getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(5);
        underTest.setMax(ClaimStatistic.Counter.UNAPPROVED, 4);
        assertThat(underTest.getMin(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(4);
        assertThat(underTest.getMax(ClaimStatistic.Counter.UNAPPROVED)).isEqualTo(4);
    }

    @Test
    void listIssuesTest() {
        ClaimStatistic statistic = new ClaimStatistic();
        List<String> required = getRequiredCounters().stream().map(ClaimStatistic.Counter::name).toList();

        int value = 5;
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            List<String> expected = new ArrayList<>(required);
            expected.remove(counter.name());
            ClaimValidator validator = new ClaimValidator();
            validator.setMax(counter, value);
            statistic.incCounter(counter, value);
            Collection<String> actual = validator.listIssues(statistic);
            assertThat(actual).containsExactlyElementsOf(expected);
            statistic.incCounter(counter, 1);
            expected.add(counter.name());
            expected.sort(String::compareTo);
            actual = validator.listIssues(statistic);
            assertThat(actual).containsExactlyElementsOf(expected);
            statistic.incCounter(counter, -1 - value);
        }
    }

    @Test
    void verifyMinimumIsSetToZeroByDefault() {
        ClaimValidator validator = new ClaimValidator();
        validator.setMax(ClaimStatistic.Counter.IGNORED, 4711);
        assertThat(validator.getMin(ClaimStatistic.Counter.IGNORED)).isZero();
    }

    @Test
    void verifyHandlingIfMaximumIsLessThanZeroAndChangedTwice() {
        ClaimValidator validator = new ClaimValidator();
        validator.setMax(ClaimStatistic.Counter.IGNORED, -4711);
        assertThat(validator.getMax(ClaimStatistic.Counter.IGNORED)).isEqualTo(Integer.MAX_VALUE);
        validator.setMax(ClaimStatistic.Counter.IGNORED, 4711);
        assertThat(validator.getMax(ClaimStatistic.Counter.IGNORED)).isEqualTo(4711);
    }

    @Test
    void verifyHandlingIfMinAndMaxIsSetOnACounter() {
        ClaimValidator validator = new ClaimValidator();
        validator.setMin(ClaimStatistic.Counter.IGNORED, 4711);
        validator.setMax(ClaimStatistic.Counter.IGNORED, -4711);
        assertThat(validator.getMax(ClaimStatistic.Counter.IGNORED)).isEqualTo(Integer.MAX_VALUE);
        assertThat(validator.getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(4711);

        validator.setMax(ClaimStatistic.Counter.IGNORED, 4710);
        assertThat(validator.getMax(ClaimStatistic.Counter.IGNORED)).isEqualTo(4710);
        assertThat(validator.getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(4710);
    }

    @Test
    void verifyHandlingIfMinimumIsChangedTwice() {
        ClaimValidator validator = new ClaimValidator();
        validator.setMin(ClaimStatistic.Counter.IGNORED, 4711);
        assertThat(validator.getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(4711);
        validator.setMin(ClaimStatistic.Counter.IGNORED, 1);
        assertThat(validator.getMin(ClaimStatistic.Counter.IGNORED)).isEqualTo(1);
    }
}
