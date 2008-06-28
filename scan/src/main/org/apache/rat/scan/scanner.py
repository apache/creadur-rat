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
        result = " <document dir='" + self.dir + "' name='" + self.name + "' >\n"
        result = result + "  <md5>" + self.md5() + "</md5>\n"
        result = result + "  <sha512>" + self.sha() + "</sha512>\n"
        result = result + "  <ripemd160>" + self.ripe() + "</ripemd160>\n"
        result = result + " </document>\n"
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
        result = "<?xml version='1.0'?>\n<documents basedir='" + self.basedir + "' at='"
        if self.at == None:
            result = result + datetime.datetime.utcnow().isoformat() 
        else:
            result = result + self.at
        result = result + "'>\n"
        for root, dirs, files in os.walk(self.basedir):
            for name in files:
                ext = splitext(name)[1]
                if name == 'KEYS':
                    pass
                elif not (ext == '.sha1' or ext == '.md5' or ext=='.sha' or ext == '.asc'):
                    document = Document(root, name)
                    result = result + document.toXml();
            if '.svn' in dirs:
                dirs.remove('.svn')
        result = result + "</documents>"
        return result

def scanIncubatorReleases():
    scanner = Scanner('/www/www.apache.org/dist/incubator')
    print scanner.scan()
    scanner = Scanner('/www/archive.apache.org/dist/incubator')
    print scanner.scan()
    
if __name__ == '__main__':
    scanIncubatorReleases()