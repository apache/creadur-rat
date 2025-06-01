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
# Writing a new File Processor

> ## Required Knowledge
> Knowledge of the following topics is recommended:
>  * [DocumentName](document_name.html): The DocumentName class that is used to identify files.
>  * Rat [Exclude Expressions](../exclusion_expression.html): The expressions that are used to match file names


A file processor is a construct that locates files with a specific name in the directory tree and reads from them file patterns that are translated into Rat exclude expressions. These files are normally found in the file directory tree and their restrictions normally only applies to files at the same directory level as the processed file or below.  When these files are processed the result is a MatcherSet indicating the files to be explicitly included and the files to be excluded.  The include and exclude together are called a `org.apache.rat.config.exclusion.MatcherSet`. MatcherSets are build by a `org.apache.rat.config.exclusion.MatcherSet.Builder`.

## MatcherSet

The matcher set comprises two collections of patterns, one to include and one to exclude. The patters are fully qualified to the directory in which the document specified by the DocumentName is found.

The order of the Match patterns are retained.  Multiple MatcherSets may be combined into a single MatcherSet.

## AbstractFileProcessorBuilder
In many cases a file processor should process multiple files in the source tree.  For example the `.gitignore` or `.hgignore` files.  To implements a file processor that performs a walk down the source tree the `AbstractFileProcessorBuilder` is used.

The `AbstractFileProcessorBuilder` constructor takes a file name, one or more comment prefixes, and a flag to indicate whether the file name should be listed in the exclude list.  The file name is normally a file that is generally hidden on Linux systems like ".gitignore" or ".hgignore".  The `AbstractFileProcessorBuilder` will scan the directories looking for files with the specified name.  If one is found it is passed to the `process(DocumentName)` method which reads the document and returns a MatcherSet.

Classes that extend the `AbstractFileProcessorBuilder` have two main extension points: `modifyEntry(DocumentName, String)` and `process(DocumentName)`. 

### Extension Points
#### modifyEntry

The `modifyEntry` method accepts the source `DocumentName` and a non-comment string.  It is expected to process the string and return an exclude expression or null if the line does not result in an exclude expression.  The default implementation simply returns the string argument.

An example of `modifyEntry` is found in the `BazaarIgnoreBuilder` where lines that start with "RE:" are regular expressions and all other lines are standard exclude patterns.  The `BazaarIgnoreBuilder.modifyEntry` method converts "RE:" prefixed strings into the standard exclude regular expression string.

#### process

In many cases the process method does not need to be modified.  In general the process method:
 * Opens a File on the `DocumentName`
 * Reads each line in the file
 * Calls the modifyEntry on the line.
 * if the line is not null:
   * Uses the `FileProcessor.localizePattern()` to create a DocumentName for the pattern with the baseName specified as the name of the file being read.
   * Stores the new document name in the list of names being returned.
 * Repeats until all the lines in the input file have been read.
 
Classes that override the `process` method generally do so because they have some special cases.  For example the `GitIgnoreBuilder` has some specific rules about when to add wildcard paths and when the paths are literal  So a special process is required.

### Theory of Operation

The AbstractFileProcessorBuilder creates MatcherSets for each instance of the target file it finds in the source tree.  Those MatcherSets are organized into levels based on how far down the tree the target file is.  MatcherSets generated from files in the root of the tree are at level zero while files found in a subdirectory of root are are level 1, and subdirectories of subdirectories of root are at level 2 and so on.

The builder constructs a list of MatcherSets with the MatcherSets from the deepest level combined followed by the MatcherSets from the next deepest level and so on to the shallowest level.  This ensures that later files override earlier files.

If files outside the source tree need to be processed they will need to override the `process` method to add the processed files at the appropriate level.  An example of this can be seen in the `GitIgnoreBuilder` code where a global ignore file is added at level -1 because it must be processed after all the explicit included and excluded found in the source tree.
