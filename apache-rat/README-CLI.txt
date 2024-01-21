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

usage: java -jar apache-rat/target/apache-rat-${project.version}.jar
            [options] [DIR|TARBALL]

Available options
 -a,--addLicence                Add the default license header to any file
                                with an unknown license that is not in the
                                exclusion list. By default new files will
                                be created with the license header, to
                                force the modification of existing files
                                use the --force option.
 -A,--addLicense                Add the default license header to any file
                                with an unknown license that is not in the
                                exclusion list. By default new files will
                                be created with the license header, to
                                force the modification of existing files
                                use the --force option.
 -c,--copyright <arg>           The copyright message to use in the
                                license headers, usually in the form of
                                "Copyright 2008 Foo"
 -d,--dir                       Used to indicate source when using
                                --exclude
 -e,--exclude <expression>      Excludes files matching wildcard
                                <expression>. Note that --dir is required
                                when using this parameter. Allows multiple
                                arguments.
 -E,--exclude-file <fileName>   Excludes files matching regular expression
                                in <file> Note that --dir is required when
                                using this parameter.
 -f,--force                     Forces any changes in files to be written
                                directly to the source files (i.e. new
                                files are not created)
 -h,--help                      Print help for the RAT command line
                                interface and exit
    --licenses <arg>            File names or URLs for license definitions
    --list-approved-families    List all defined license families
    --list-licenses             List all active licenses
    --no-default-licenses       Ignore default configuration. By default
                                all approved default licenses are used
 -o,--out <arg>                 Define the output file where to write
                                report (default is System.out)
 -s,--stylesheet <arg>          XSLT stylesheet to use when creating the
                                report.  Not compatible with -x
    --scan-hidden-directories   Scan hidden directories
 -x,--xml                       Output the report in raw XML format.  Not
                                compatible with -s

