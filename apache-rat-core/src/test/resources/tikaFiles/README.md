# Introduction

Files in this directory are parsed by Tika process and verified that the proper type is returned.

* the `standard` subdirectory contains files that are returned as STANDARD document types.

* the `binary` subdirectory contains files that are returned as BINARY types.

* the `notice` subdirectory contains files that are NOTICE types

* the `archive` subdirectory contains files that are ARCHIVE types.

The `TikaProcessorTest.testTikaFiles()` automatically runs against the files in the directories. To add a new file to test just place it in the proper directory.

## Scripts and DTDs (RAT-582 / RAT-592)

`TikaProcessorTest.envScriptsAreScanned_RAT582()` tests `#!/usr/bin/env python3`,
`#!/usr/bin/env perl`, and `#!/usr/bin/env node` using in-memory documents rather than additional
fixtures with the same MIME type. These variants deliberately exercise Tika's generic shell-script
magic, which overrides the filename extension. They must still be scanned as STANDARD files.
