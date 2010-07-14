                            Release Audit Tool
                            ==================

Apache RAT is an effort undergoing incubation at The Apache Software Foundation (ASF), 
sponsored by the Incubator PMC. Incubation is required of all newly accepted projects 
until a further review indicates that the infrastructure, communications, and decision 
making process have stabilized in a manner consistent with other successful ASF projects.
While incubation status is not necessarily a reflection of the completeness or stability 
of the code, it does indicate that the project has yet to be fully endorsed by the ASF.
      
Release Audit Tool (RAT) is a tool to improve accuracy and efficiency when checking
releases. It is heuristic in nature: making guesses about possible problems. It
will produce false positives and cannot find every possible issue with a release.
It's reports require interpretation.

RAT was developed in response to a need felt in the Apache Incubator to be able to
review releases for the most common faults less labour intensively. It is therefore
highly tuned to the Apache style of releases.

RAT is intended to be self documenting: reports should include introductory material
describing their function. Building RAT describes how to run RAT. Running RAT 
describes the options available. These release notes describe the current state of
RAT.

A good way to use RAT is to through the source. This allows the code base to be
easily patched for example to add new generated file matchers. The main jar is 
runnable and self-documenting. This jar is available as a standard alone binary.

RAT includes a task library for Ant 1.7. This allows RAT reports to be run against
a wide variety of resources. See ant-task-examples.xml. To use the Ant tasks, 
Apache Ant 1.7 is required. See http://ant.apache.org/.

For Maven builds, the plugin is recommended.

In response to demands from project quality tool developers, RAT is available as a 
library (rat-lib jar) suitable for inclusion in tools. Note that binary compatibility 
is not gauranteed between 0.x releases. The XML output format is not yet in it's 
final form and so library users are recommended to either use the supplied 
stylesheets or keep in close touch with the code.

RAT is in Incubation at Apache: http://incubator.apache.org/rat

Artifacts
=========
 TODO:

