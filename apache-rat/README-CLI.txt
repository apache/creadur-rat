About Apache Rat™
================

Rat audits software distributions, with a special interest in headers. 
If this isn't quite what you're looking for then take a look at the 
other products developed by Apache Creadur™, 
including Apache Whisker™ which audits and generates legal (for example LICENSE)
documents for complex software distributions.

Running from the Command Line
-----------------------------

Run from the command line with:

java -jar apache-rat-${project.version}.jar --help

This will output a help message detailing the command line options available to you.

Adding license headers
----------------------

Rat can be used to automatically add license headers to files that do not currently have them. 
Only files that are not excluded by the Rat configurations will be affected.

To add license headers use a command such as:

java -jar apache-rat-${project.version}.jar --addlicense
  --copyright "Copyright 2008 Foo" --force
  /path/to/project

This command will add the license header directly to the source files. 
If you prefer to see which files will be changed and how then remove the "--force" option.
Using multiple excludes from a file

It is common to use the Rat with the maven or ant plugins and specify a series of files to exclude
(such as a README or version control files). 
If you are using the Rat application instead of a plugin you can specify a series of regex excludes
in a file and specify that with the -E option.

java -jar apache-rat-${project.version}.jar
 -E /path/to/project/.rat-excludes
 -d /path/to/project

Command Line Options
====================
see https://creadur.apache.org/rat/apache-rat/index.html#Command_Line_Options
