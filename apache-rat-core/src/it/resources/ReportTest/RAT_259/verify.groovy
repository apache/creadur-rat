package ReportTest.RAT_246

import org.apache.rat.testhelpers.TextUtils

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
output = new File(args[0]);
content = output.text

TextUtils.assertPatternInTarget("^Approved:\\s+9 ", content);
TextUtils.assertPatternInTarget("^Archives:\\s+1 ", content);
TextUtils.assertPatternInTarget("^Binaries:\\s+2 ", content);
TextUtils.assertPatternInTarget("^Document types:\\s+5 ", content);
TextUtils.assertPatternInTarget("^Generated:\\s+1 ", content);
TextUtils.assertPatternInTarget("^License categories:\\s+5 ", content);
TextUtils.assertPatternInTarget("^License names:\\s+6 ", content);
TextUtils.assertPatternInTarget("^Notices:\\s+2 ", content);
TextUtils.assertPatternInTarget("^Standards:\\s+8 ", content);
TextUtils.assertPatternInTarget("^Unapproved:\\s+2 ", content);
TextUtils.assertPatternInTarget("^Unknown:\\s+2 ", content);


logOutput = new File(args[1]);
log = logOutput.text

TextUtils.assertPatternNotInTarget("^ERROR:", log);
TextUtils.assertPatternNotInTarget("^WARN:", log);
