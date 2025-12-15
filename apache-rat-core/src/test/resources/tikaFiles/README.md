Files in this directory are parsed by Tika process and verified that the proper type is returned.

the `standard` subsirectory contains files that are returned as STANDARD document types.

the `binary` subdirectory contains files that are returned as BINARY types.

the `notice` subdirectory contains files that are NOTICE types

the `archive` subdirectory contains files that are ARCHIVE types.

The `TikeProcessorTest.testTikaFiles()` automatically runs against the files in the directories.  To add a new file to test just place it in the proper directory.
