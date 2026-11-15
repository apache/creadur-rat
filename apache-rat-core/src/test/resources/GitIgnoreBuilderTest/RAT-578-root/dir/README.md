If `git config --global core.excludesfile $PWD/../excludes.txt` is executed in this current directory and `git config --global core.excludesfile` verifies that the `../excludes.txt` in this directory is the global core.excludes file. 
Then 
```
git check-ignore --no-index --verbose `find ..`
```
will yield
```
$PWD/excludes.txt:1:*.md        ./root.md
$PWD/excludes.txt:1:*.md        ./dir/ignored.md
$dir/.gitignore:2:!README.md    ./dir/README.md
```
The `GitItnoreBuilderTest` verifies this. 

If the core.excludes is set to "" (clearing the value) then the files are not ignored.
