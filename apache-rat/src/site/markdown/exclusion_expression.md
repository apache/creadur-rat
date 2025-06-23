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

# RAT Exclusion Expressions

RAT uses a variation on the Ant or Git exclusion syntax.

* `?` matches a single character.  For example 'ca?' will match 'cat' and 'can' but not 'call' or the abbreviation for california  (or Canada) 'CA'.
* `*` matches zero or more characters. For example 'ca*' will match 'cat', 'can', 'call' and the abbreviation for california  (or Canada) 'CA'.
* `**` matches zero or more directories.  For example '**/ca?' will match 'my/cat', 'my/can', 'the/cat', 'the/can', 'cat' and 'can'.
* `!` reverses the meaning of the expression.  Example "!ca?" will not match "cat" or "can".  See include/exclude notes below.

The table below shows an example of how the `*` and `**` differ and interact.

|             | `foo/x/y` | `b/foo` | `b/foo/x` | `a/b/foo` | `foo` | `a/b/foo/x/y` | `a/b/foo/x` | `foo/x` | `b/foo/x/y` |
| :---------: | :-------: | :-----: | :-------: | :-------: | :---: | :-----------: | :---------: | :-----: | :---------: |
| `foo`       | F         | F       | F         | F         | T     | F             | F           | F       | F           |
| `foo/*`     | F         | F       | F         | F         | F     | F             | F           | T       | F           |
| `foo/**`    | T         | F       | F         | F         | T     | F             | F           | T       | F           |
| `*/foo`     | F         | T       | F         | F         | F     | F             | F           | F       | F           |
| `*/foo/*`   | F         | F       | T         | F         | F     | F             | F           | F       | F           |
| `*/foo/**`  | F         | T       | T         | F         | F     | F             | F           | F       | T           |
| `**/foo`    | F         | T       | F         | T         | T     | F             | F           | F       | F           |
| `**/foo/*`  | F         | F       | T         | F         | F     | F             | T           | T       | F           |
| `**/foo/**` | T         | T       | T         | T         | T     | T             | T           | T       | T           |

## Inclusion/Exclusion Notes

Patterns that are excluded may be superseded by patterns that are included.

For example: the exclusion pattern 'ca*' used in conjunction with the include 'cat' would result in the file 'cat' being included but 'can', 'call' and the abbreviation for california (or Canada) 'CA' being excluded.

When processed a negated exclusion results in an inclusion, and a negated inclusion results in an exclusion.

**Once a file is explicitly included it can not be excluded.**

## Directory separation characters

Patterns may use either '/' or '\\' as the path separation character. '/' is recommended.

## Case sensitivity

The case sensitivity of the matching patterns depends upon the file system in use.  If the file system is case-sensitive then the matches are case-sensitive.
