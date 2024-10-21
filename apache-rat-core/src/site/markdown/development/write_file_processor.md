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


A FileProcessor is a module that locates files with a specific name in the directory tree and reads from them file patterns that are translated into Rat exclude expressions. These files are normally found in the file directory tree and their restrictions normally only applies to files at the same directory level as the processed file or below.  This type of file is implemented by the `org.apache.rat.config.exclusion.fileProcessors.DescendingFileProcessor`.

The `DescendingFileProcessor` takes a file name and one or more comment prefixes as in the constructor.  The file name is normally a file that is generally hidden on Linux systems like ".gitignore" or ".hgignore".  The `DescendingFileProcessor` will scan the directories looking for files with the specified name.  If one is found it is passed to the `process(DocumentName)` method which reads the document and returns a list of exclude expressions.

Classes that extend the `DescendingFileProcessor` have two main extension points: `modifyEntry(DocumentName, String)` and `process(DocumentName)`. 

## modifyEntry

The `modifyEntry` method accepts the source `DocumentName` and a non-comment string.  It is expected to process the string and return an exclude expression or null if the line does not result in an exclude expression.  The default implementation simply returns the string argument.

An example of `modifyEntry` is found in the `BazaarIgnoreProcessor` where lines that start with "RE:" are regular expressions and all other lines are standard exclude patterns.  The `BazaarIgnoreProcessor.modifyEntry` method converts "RE:" prefixed strings into the standard exclude regular expression string.

## process

In many cases the process method does not need to be modified.  In general the process method:
 * Opens a File on the `DocumentName`
 * Reads each line in the file
 * Calls the modifyEntry on the line.
 * if the line is not null:
   * Uses the `FileProcessor.localizePattern()` to create a DocumentName for the pattern with the baseName specified as the name of the file being read.
   * Stores the new document name in the list of names being returned.
 * Repeats until all the lines in the input file have been read.
 
Classes that override the `process` method generally do so because they have some special cases.  For example the `GitFileProcessor` has some specific rules about when to add wildcard paths and when the paths are literal  So a special process is required.
