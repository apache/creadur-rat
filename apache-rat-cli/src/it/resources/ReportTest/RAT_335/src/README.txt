Note the output when running in the real commandline version of git

# Files that must be ignored (dropping the gitignore matches outside of this test tree)
$ git check-ignore --no-index --verbose $(find . -type f|sort) | fgrep 'apache-rat-plugin/src/test/resources/unit/RAT-335-GitIgnore/'


apache-rat-plugin/src/test/resources/unit/RAT-335-GitIgnore/dir1/.gitignore:1:*.txt     ./dir1/dir1.txt
apache-rat-plugin/src/test/resources/unit/RAT-335-GitIgnore/dir1/.gitignore:3:file1.log ./dir1/file1.log
apache-rat-plugin/src/test/resources/unit/RAT-335-GitIgnore/.gitignore:1:*.md   ./dir2/dir2.md
apache-rat-plugin/src/test/resources/unit/RAT-335-GitIgnore/.gitignore:4:*.log  ./dir3/dir3.log
apache-rat-plugin/src/test/resources/unit/RAT-335-GitIgnore/.gitignore:1:*.md   ./root.md
