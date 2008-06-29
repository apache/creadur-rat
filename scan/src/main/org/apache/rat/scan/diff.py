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

import xml.parsers.expat
import os
import os.path
import subprocess

class InvalidDocument(Exception):
    def __init__(self, value):
        self.value = value
        
    def __str__(self):
        return repr(self.value)
        
class Document:
    def __init__(self, dir):
        self.reset()
        self.dir = dir
        
    def __str__(self):
        return self.name
        
    def reset(self):
        self.next = None
        self.name = None
        self.md5 = None
        self.sha = None
        self.ripemd= None
        
    def __setMd5(self, value):
        if self.md5 == None:
            self.md5 = value
        else:
            self.md5 = self.md5 + value
        
    def __setSha(self, value):
        if self.sha == None:
            self.sha = value
        else:
            self.sha = self.sha + value

    def __setRipemd(self, value):
        if self.ripemd == None:
            self.ripemd = value
        else:
            self.ripemd = self.ripemd + value
        
    def __setName(self, value):
        if self.name == None:
            self.name = value
        else:
            self.name = self.name + value    
            
    def __setDir(self, value):
        if self.dir == None:
            self.dir = value
        else:
            self.dir = self.dir + value        
        
    def start_element(self, name, attrs):
        type = attrs.get('class')
        if type == 'resource':
            self.next = self.__setName
        elif type == 'md5':
            self.next = self.__setMd5
        elif type == 'sha':
            self.next = self.__setSha
        elif type == 'ripe':
            self.next = self.__setRipemd
        else:
            self.next = None
    
    def end_element(self, name):
        pass
    
    def char_data(self, data):
        if not self.next == None:
            self.next(data.strip())
        
    def match(self, documents):
        for document in documents:
            if document.name == self.name and document.dir == self.dir:
                return document
        return None 
            
    def isMissing(self, documents):
        return self.match(documents) == None
        
    def isModified(self, documents):
        document = self.match(documents)
        if document == None:
            return False
        else:
            return not (document.md5 == self.md5 and document.sha == self.sha 
                        and document.ripemd == self.ripemd)
            
    def summaryXml(self):
        return "<li class='resource'>" + self.name + '<li/>'
    
def document(dir, name, md5, sha, ripemd):
    result = Document(dir)
    result.name = name
    result.md5 = md5
    result.sha = sha
    result.ripemd = ripemd
    return result
            
class Documents:
    def __init__(self):
        self.documents = []
        self.__current = None
        self.__setter = None
        self.dir = None
        
    def append(self, document):
        self.documents.append(document)
        
    def load(self, document):
        self.documents = []
        self.__current = None
        self.on = None
        parser = xml.parsers.expat.ParserCreate()
        parser.StartElementHandler = self.start_element
        parser.EndElementHandler = self.end_element
        parser.CharacterDataHandler = self.char_data
        parser.Parse(document)
    
    def __setOn(self, value):
        if self.on == None:
            self.on = value
        else:
            self.on = self.on + value  

    def __setDir(self, value):
        if self.__dir == None:
            self.__dir = value
        else:
            self.__dir = self.__dir + value  
    
    def start_element(self, name, attrs):
        type = attrs.get("class")
        if type == 'created':
            self.__setter = self.__setOn
        if type == 'dir':
            self.__dir = None
            self.__setter = self.__setDir
        if type == 'resource':
            self.__current = Document(self.__dir)
            self.documents.append(self.__current)
            self.__current.start_element(name, attrs)
        elif not self.__current == None:
            self.__current.start_element(name, attrs)
            
    def end_element(self, name):
        if not self.__current == None:
            self.__current.end_element(name)
        self.__setter = None
        
    def char_data(self, data):
        if not self.__current == None:
            self.__current.char_data(data)
        if not self.__setter == None:
            self.__setter(data)
            
    def __iter__(self):
        for document in self.documents:
            yield(document)
            
    def isModified(self, document):
        return document.isModified(self.documents)
    
    def isMissing(self, document):
        return document.isMissing(self.documents)
            
    def compare(self, documents):
        added = filter(documents.isMissing, self)
        removed = filter(self.isMissing, documents)
        modified = filter(documents.isModified, self)
        return added, removed, modified
    
def documents(documents):
    results = Documents()
    for document in documents:
        results.append(document)
    return results

class Auditor:
    def __init__(self, basedir, prefix):
        self.basedir = basedir
        self.prefix = prefix
        
    def printSignatureChecks(self):
        for file in os.listdir(self.basedir):
            if file.endswith('.asc'):
                print file
                subprocess.Popen('gpg --verify ' + os.path.join(self.basedir, file), shell=True).wait()
                print
                
    def load(self, name):
        file = os.path.join(self.basedir, name)
        f = open(file, 'r')
        try:
            documents = Documents()
            documents.load(f.read())
            if not documents.on == name[-15:-5]:
                raise InvalidDocument('Document date does not match file date. File: ' + file)
            return documents
        finally:
            f.close()
            
    def latest(self):
        xmlDocuments = filter(lambda x:x.endswith('.html') and x.startswith(self.prefix), os.listdir(self.basedir))
        xmlDocuments.sort()
        return map(self.load, xmlDocuments[-2:])
                
    def latestDiffs(self):
        latest = self.latest()
        if len(latest)>1:
            return self.diffs(latest[1], latest[0])
        else:
            return None
        
    def toXml(self, documents):
        result = ""
        for dir, documentsInDir in dict((dir, filter(lambda doc: doc.dir == dir, documents)) for dir in set([document.dir for document in documents])).iteritems():
            result = result + "<li class='dir'>" + dir + "<ul>"
            for document in sorted(documentsInDir):
                result = result + "<li class='resource'>" + document.name + "</li>"
            result = result + "</ul></li>"
        return result
        
    def diffs(self, one, two):
        result = "<div class='diff'><h1>From <a href='"  + self.prefix + "-" + two.on + ".html' class='start-date'>"+ two.on + "</a> Till <a href='"  + self.prefix + "-" + one.on + ".html' class='end-date'>" + one.on + '</a></h1>'
        added, removed, modified = one.compare(two)
        
        result = result + "<h2>Added Resources</h2><ul class='added'>"
        result = result + self.toXml(added)
        result = result + "</ul>"
        
        result = result + "<h2>Modified Resources</h2><ul class='modified'>"
        result = result + self.toXml(modified)
        result = result + "</ul>"
        
        result = result + "<h2>Removed Resources</h2><ul class='deleted'>"
        result = result + self.toXml(removed)
        result = result + "</ul>"
        return result + '</div>' 
                
if __name__ == '__main__':
    auditor = Auditor('audit')
    auditor.printSignatureChecks()
    print auditor.latestDiffs()