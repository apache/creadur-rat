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


public class ClaimValidator {
    private final ConcurrentHashMap<ClaimStatistic.Counter, Integer> limits = new ConcurrentHashMap<>();
    private boolean hasErrors;

    public ClaimValidator() {
    }

    public boolean hasErrors() {
        return hasErrors;
    }
    public void set(ClaimStatistic.Counter counter, int value) {
        limits.put(counter, value);
    }

    public int get(ClaimStatistic.Counter counter) {
        Integer result = limits.get(counter);
        return result == null ? 0 : result;
    }

    public boolean isValid(ClaimStatistic.Counter counter, int count) {
        boolean result = true;
        if (limits.containsKey(counter)) {
            switch (counter) {
                case UNAPPROVED:
                case GENERATED:
                case UNKNOWN:
                    result = limits.get(counter) >= count;
                    hasErrors |= !result;
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public void logIssues(ClaimStatistic statistic) {
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            if (!isValid(counter, statistic.getCounter(counter))) {
                DefaultLog.getInstance().error(format("Unexpected count for %s, limit is %s.  Count: %s", counter,
                        limits.get(counter), statistic.getCounter(counter)));
            }
        }
    }

    public Collection<String> listIssues(ClaimStatistic statistic) {
        List<String> result = new ArrayList<>();
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            if (!isValid(counter, statistic.getCounter(counter))) {
                result.add(counter.toString());
            }
        }
        return result;
    }
}
