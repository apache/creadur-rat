package ReportTest.RAT_14

import org.apache.rat.OptionCollection
import org.apache.rat.ReportConfiguration
import org.apache.rat.Reporter
import org.apache.rat.report.claim.ClaimStatistic
import org.apache.rat.testhelpers.TextUtils
import org.apache.rat.utils.DefaultLog

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

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
output = new File(args[0])
content = output.text

TextUtils.assertPatternInTarget("^Approved:\\s+3 ", content)
TextUtils.assertPatternInTarget("^Archives:\\s+2 ", content)
TextUtils.assertPatternInTarget("^Binaries:\\s+2 ", content)
TextUtils.assertPatternInTarget("^Document types:\\s+5 ", content)
TextUtils.assertPatternInTarget("^Generated:\\s+1 ", content)
TextUtils.assertPatternInTarget("^License categories:\\s+3 ", content)
TextUtils.assertPatternInTarget("^License names:\\s+3 ", content)
TextUtils.assertPatternInTarget("^Notices:\\s+1 ", content)
TextUtils.assertPatternInTarget("^Standards: \\s+5 ", content)
TextUtils.assertPatternInTarget("^Unapproved:\\s+3 ", content)
TextUtils.assertPatternInTarget("^Unknown:\\s+3 ", content)

logOutput = new File(args[1])
log = logOutput.text

TextUtils.assertPatternInTarget("^INFO:\\s+Approved:\\s+3\$", log)
TextUtils.assertPatternInTarget("^INFO:\\s+Archives:\\s+2\$", log)
TextUtils.assertPatternInTarget("^INFO:\\s+Binaries:\\s+2\$", log)
TextUtils.assertPatternInTarget("^INFO:\\s+Document types:\\s+5\$", log)
TextUtils.assertPatternInTarget("^INFO:\\s+Generated:\\s+1\$", log)
TextUtils.assertPatternInTarget("^INFO:\\s+License categories:\\s+3\$", log)
TextUtils.assertPatternInTarget("^INFO:\\s+License names:\\s+3\$", log)
TextUtils.assertPatternInTarget("^INFO:\\s+Notices:\\s+1\$", log)
TextUtils.assertPatternInTarget("^INFO:\\s+Standards: \\s+5\$", log)
TextUtils.assertPatternInTarget("^INFO:\\s+Unapproved:\\s+3\$", log)
TextUtils.assertPatternInTarget("^INFO:\\s+Unknown:\\s+3\$", log)

// test without generating output.
File src = new File(output.getParent(), "src")
String[] myArgs = new String[4]
myArgs[0] = "--counter-max"
myArgs[1] = "UNAPPROVED:-1"
myArgs[2] = "--"
myArgs[3] = src.getAbsolutePath()

ReportConfiguration configuration = OptionCollection.parseCommands(myArgs, { opts -> })
assertNotNull(configuration)
configuration.validate(DefaultLog.getInstance().&error)
Reporter reporter = new Reporter(configuration)
ClaimStatistic statistic = reporter.execute()

assertEquals(3, statistic.getCounter(ClaimStatistic.Counter.APPROVED))
assertEquals(2, statistic.getCounter(ClaimStatistic.Counter.ARCHIVES))
assertEquals(2, statistic.getCounter(ClaimStatistic.Counter.BINARIES))
assertEquals(5, statistic.getCounter(ClaimStatistic.Counter.DOCUMENT_TYPES))
assertEquals(1, statistic.getCounter(ClaimStatistic.Counter.GENERATED))
assertEquals(3, statistic.getCounter(ClaimStatistic.Counter.LICENSE_CATEGORIES))
assertEquals(3, statistic.getCounter(ClaimStatistic.Counter.LICENSE_NAMES))
assertEquals(1, statistic.getCounter(ClaimStatistic.Counter.NOTICES))
assertEquals(5, statistic.getCounter(ClaimStatistic.Counter.STANDARDS))
assertEquals(3, statistic.getCounter(ClaimStatistic.Counter.UNAPPROVED))
assertEquals(3, statistic.getCounter(ClaimStatistic.Counter.UNKNOWN))