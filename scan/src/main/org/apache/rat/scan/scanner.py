#########################################################################################
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#########################################################################################

import os
import hashlib
import datetime
from os.path import join, splitext

class Document:
    '''
    A document scanned
    '''
    def __init__(self, dir, name, uri=dir):
        self.dir = dir
        self.name = name
        self.loaded = False
        self.uri = uri
    
    def __load(self):
        if not self.loaded:
            path = join(self.dir, self.name)
            file = open(path, mode='rb')
            try:
                md5 = hashlib.md5()
                sha = hashlib.sha512()
                ripe = hashlib.new('ripemd160')
                        
                more = True
                while more:
                    content = file.read(4048)
                    if content == '':
                        more = False
                    else:
                        md5.update(content)
                        sha.update(content)
                        ripe.update(content)
                        
                self.__md5 = md5.hexdigest()
                self.__sha = sha.hexdigest()
                self.__ripe = ripe.hexdigest()
            finally:
                file.close()
            self.loaded = True

    def md5(self):
        self.__load()
        return self.__md5

    def ripe(self):
        self.__load()
        return self.__ripe

    def sha(self):
        self.__load()
        return self.__sha
    
    def toXml(self):
        result = "<li><a name='" + join(self.dir, self.name) + "' class='resource'>" + self.name + "</a><dl>"
        result = result + "<dt>md5</dt><dd class='md5'>" + self.md5() + "</dd>"
        result = result + "<dt>sha</dt><dd class='sha'>" + self.sha() + "</dd>"
        result = result + "<dt>ripe</dt><dd class='ripe'>" + self.ripe() + "</dd>"
        result = result + "</dl></li>"
        return result
    
class Scanner:
    '''
    Scans a directory tree.
    Calculates sums.
    Outputs xml to stdout.
    '''
    def __init__(self, basedir, at=None):
        self.basedir = basedir
        self.at = at
        
    def scan(self):
        result = """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head><title>Audited On """

        if self.at == None:
            created = datetime.datetime.utcnow().isoformat() 
        else:
            created = self.at
        result = result + created + """/title></head>
<body>
<p>
Audit conducted on <span class='created'>"""
        result = result + created + """</span> scanned directories
root at <span class='base-dir'>"""
        result = result + self.basedir + """</span>
</p><p>
Artifacts by directory:
</p>
<ul>"""
        for root, dirs, files in os.walk(self.basedir):
            result = result + "<li><span class='dir'>" + root + "</span><ul>"
            for name in files:
                ext = splitext(name)[1]
                if name == 'KEYS':
                    pass
                elif not (ext == '.sha1' or ext == '.md5' or ext=='.sha' or ext == '.asc'):
                    document = Document(root, name)
                    result = result + document.toXml();
            result = result + "</ul>"
            if '.svn' in dirs:
                dirs.remove('.svn')
        result = result + """</ul>
</body>
</html>"""
        return result

def scanIncubatorReleases():
    scanner = Scanner('/www/www.apache.org/dist/incubator')
    print scanner.scan()
    scanner = Scanner('/www/archive.apache.org/dist/incubator')
    print scanner.scan()
    
if __name__ == '__main__':
    scanIncubatorReleases()