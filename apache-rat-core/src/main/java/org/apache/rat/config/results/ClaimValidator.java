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
package org.apache.rat.config.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * Validates the ClaimStatistic results meet the specified requirements.
 */
public final class ClaimValidator {
    /**
     * The map of counter limits.
     */
    private final ConcurrentHashMap<ClaimStatistic.Counter, Integer> limits = new ConcurrentHashMap<>();
    /**
     * {@code true} if errors were detected in the claim.
     */
    private boolean hasErrors;

    /**
     * Constructor.
     */
    public ClaimValidator() {
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            limits.put(counter, counter.getDefaultMaxValue() < 0 ? Integer.MAX_VALUE : counter.getDefaultMaxValue());
        }
    }

    /**
     * Returns {@code true} if any validation failed.
     * @return {@code true} if any validation failed.
     */
    public boolean hasErrors() {
        return hasErrors;
    }

    /**
     * Sets the limit for the specified counter.
     * @param counter the counter to set the limit for.
     * @param value the value to set. A negative value specifies no maximum value.
     */
    public void set(final ClaimStatistic.Counter counter, final int value) {
        if (value < 0) {
            limits.put(counter, Integer.MAX_VALUE);
        } else {
            limits.put(counter, value);
        }
    }

    /**
     * Gets the limit for the specific counter.
     * @param counter the counter to get the limit for.
     * @return the limit for the counter or 0 if not set.
     */
    public int get(final ClaimStatistic.Counter counter) {
        Integer result = limits.get(counter);
        return result == null ? 0 : result;
    }

    /**
     * Determines if the specified count is within the limits for the counter.
     * @param counter The counter to check.
     * @param count the limit to check.
     * @return {@code true} if the count is within the limits, {@code false} otherwise.
     */
    public boolean isValid(final ClaimStatistic.Counter counter, final int count) {
        boolean result = limits.get(counter) >= count;
        hasErrors |= !result;
        return result;
    }

    /**
     * Logs all the invalid values as errors.
     * @param statistic The statistics that contain the run values.
     */
    public void logIssues(final ClaimStatistic statistic) {
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            if (!isValid(counter, statistic.getCounter(counter))) {
                DefaultLog.getInstance().error(format("Unexpected count for %s, limit is %s.  Count: %s", counter,
                        limits.get(counter), statistic.getCounter(counter)));
            }
        }
    }

    /**
     * Creates a list of items that have issues.
     * @param statistic The statistics that contain the run values.
     * @return a collection of counter names that are invalid.
     */
    public Collection<String> listIssues(final ClaimStatistic statistic) {
        List<String> result = new ArrayList<>();
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            if (!isValid(counter, statistic.getCounter(counter))) {
                result.add(counter.toString());
            }
        }
        return result;
    }
}
