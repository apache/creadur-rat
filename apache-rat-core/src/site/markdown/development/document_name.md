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
# Document Names

Rat must be able to distinguish files in multiple file systems; most notably Windows&#174; and Linux&#174;. In addition, we want Rat to produce reports that are comparable across the platforms. To achieve these goals the `org.apache.rat.document.impl.DocumentName` class was developed.

All documents in the Rat system have a base directory. Different UIs will set the base directory differently. For example:

 * The command line sets the base directory to the directory or archive  specified on the command line.
 * Ant UI sets the base directory to the directory where the `build.xml` file is located.
 * Maven UI sets the base directory to the directory where the project is located.
 * Other UIs may define the base directory as it fits the underlying build system.

When reporting a document Rat will report the path from the base directory to the file; the path relative to the base directory.

The `DocumentName` comprises:

* `name`: The fully qualified file name as provided by the underlying file system.
* `baseName`: The fully qualified base directory name as provided by the underlying file system.
* `dirSeparator`: The directory separator string used by the underlying file system.
* `isCaseSensitive`: The case-sensitive flag for the underlying file system.

The class `DocumentName` also provides a static value that identifies the case-sensitivity of the underlying operating system.

`DocumentName` also provides methods to:

* Resolve a name: This method takes a path relative to the DocumentName and creates a new DocumentName instance with the same baseName, dirSeparator and case-sensitivity flag is the original DocumentName.
* Localize a name: This method returns the path from the baseName to the file.  It utilizes the dirSeparator to separate the directories.
* Localize with separator: This method returns the path from the baseName to the file but utilizes a specified string as the directory separator.
* Short name: This method returns the last segment of the name.


## Notes
 * `DocumentName` implements 
    * `equals()`, 
    * `hashCode()`
    * `Comparable<DocumentName>`.
 * Base directories within the system are identified with DocumentNames that have the `name` and `baseName` set to the same underlying file system value.
