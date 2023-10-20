<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
# RAT Architecture notes.

## Source definition

In this document source files are the files that RAT is checking.  They can be individual source files, document files,
configuration files, archives, etc.  The use of the term "source" does not specify application source files per. se.

## License definitions

Licenses comprise a unique ID, a name, and a series of tests.

 * The uniqueID and name are defined in the interface `ILicenseFamiy`
 * The tests are defined in the interface `IHeaderMatcher`
 * If any of the tests are matched then the license is detected.
 * tests may be multiplexing tests that containt more than one test.  Multiplexing tests are either
 	  * `AND` tests where all the the sub tests must be true for the test to be true; or
 	  * `OR` tests where one the the sub-tests being true makes the entire test true.
 
 
 When source files are tested for the presence of a license all the license checks are performed in parallel.
 
 ## Tests
 
 There are several implementations of tests
 
 ### Text
 
 Text is specified, the complete text must be located in the source file.
 
 ### SPDX
 
 The SPDX keyword must be found within the source file.  SPDX keyword have the form `SPDX-License-Identifier:\\s([A-Za-z0-9\\.\\-]+)`
 when defining the SPDX matcher onlyt the variable alpha-numeric phrase may be specified (eg the 'Apache-2.0' in the SPDX
 identifier 'SPDX-License-Identifier: Apache-2.0`
 
 ### Copyright
 
 The copyright must match a specific date(range) and owner.
 
 ### Multiplex
 
 Multiplex tests comprise several tests.  How the test is evaluated depends upon the type.
 
 #### ANY-Multiplex (aka OR-Multiplex)
 If any of the tests comprising the ANY-Multiplex test pass then the ANY-Multiplex test passes.
 
 #### ALL-Multiplex (aka AND-Multiplex)
Only if all of the tests comprising the ALL-Multiplex test pass will the ALL-Multiplex test pass.
 
 ### License test
 All licenses are defined as ANY-Multiplex tests.  Licenses therefore be uses as tests within other tests.  However, this
 only makes sense in the context of an ALL-Multiplex test.
