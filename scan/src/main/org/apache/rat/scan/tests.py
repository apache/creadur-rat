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

import scanner
import unittest
import diff
import xml.parsers.expat

TEST_BASE_DIR="../../../../../test/org/apache/rat/scan/"
SCANNER_OUT="""<div class='audit'>\n<p>\nAt <span class=\'at\'>NOW</span> started to scan root <span class=\'base-dir\'>../../../../../test/org/apache/rat/scan/scanner/</span>\n</p><p>\nArtifacts by directory:\n</p>\n<ul><li><span class=\'dir\'>../../../../../test/org/apache/rat/scan/scanner/</span><ul><li><a name=\'../../../../../test/org/apache/rat/scan/scanner/HenryV.txt\' class=\'resource\'>HenryV.txt</a><dl><dt>md5</dt><dd class=\'md5\'>c81f4cd3b2203ae869b8c6acea6bf73c</dd><dt>sha</dt><dd class=\'sha\'>2ae73f5cfe7943a7d51b46e653948af7f067a6b01e61c827201c8e17b9231956f48b3e8e0da64e822ca9fdeb7a62f5af623406e2dbb9b39a8dabf569d2046402</dd><dt>ripe</dt><dd class=\'ripe\'>4b0a5f9317e0d3165ea4982f90e7266a553d8353</dd></dl></li><li><a name=\'../../../../../test/org/apache/rat/scan/scanner/RichardIII.txt\' class=\'resource\'>RichardIII.txt</a><dl><dt>md5</dt><dd class=\'md5\'>911bade3f0bcdb652f1331fb19d7bf07</dd><dt>sha</dt><dd class=\'sha\'>3fd5d26bbbea1dfddeeab642bffd0d7fbdc6c4ed0d06faae3283e1e7b220d943200048630663c6a33e7cbd26fceab585920cd77d3481ce4dfa209b5000ccd4de</dd><dt>ripe</dt><dd class=\'ripe\'>30ddc78d8bf08ef52ec8a7a8f7553c27931a6d0d</dd></dl></li></ul><li><span class=\'dir\'>../../../../../test/org/apache/rat/scan/scanner/sub</span><ul></ul><li><span class=\'dir\'>../../../../../test/org/apache/rat/scan/scanner/sub/deep</span><ul><li><a name=\'../../../../../test/org/apache/rat/scan/scanner/sub/deep/Hamlet.txt\' class=\'resource\'>Hamlet.txt</a><dl><dt>md5</dt><dd class=\'md5\'>1ccce242df4a39d25057aebed53be182</dd><dt>sha</dt><dd class=\'sha\'>2b92d82dcd9db3a3142f1bd1522d0a3818555edfb3fd579d80e3b7ebc67adb8fb73db0185ccdc72704294005fb3830529e8962715f2dbfa7da0bb0553abb573a</dd><dt>ripe</dt><dd class=\'ripe\'>307a14094c28da7a46f894ed10eb732fb4d0f199</dd></dl></li><li><a name=\'../../../../../test/org/apache/rat/scan/scanner/sub/deep/JuliusCaesar\' class=\'resource\'>JuliusCaesar</a><dl><dt>md5</dt><dd class=\'md5\'>0ef9754818b94baecca3596b43eb0753</dd><dt>sha</dt><dd class=\'sha\'>54184034009fc6b4e0dadfb0e14a1bad9c4c03791a982c4a7111dc7e4596164c1ca3dc49ec37c6081daf2bcd59d73a6c085beb1203b667066b77b58731f72460</dd><dt>ripe</dt><dd class=\'ripe\'>0aa5485c5b892be83910fae34c304c1ac41240ec</dd></dl></li></ul></ul>\n</div>"""

class ReadXmlTestCase(unittest.TestCase):
    
    def testReadOne(self):
        document = diff.Document("a directory")
        parser = xml.parsers.expat.ParserCreate()
        parser.StartElementHandler = document.start_element
        parser.EndElementHandler = document.end_element
        parser.CharacterDataHandler = document.char_data
        parser.Parse("""<?xml version="1.0"?>
<li><a name='a directory/a name' class='resource'>a name</a>
<dl>
<dt>md5</dt>
<dd class='md5'>MD5 SUM</dd>
<dt>sha</dt>
<dd class='sha'>SHA SUM</dd>
<dt>ripe</dt>
<dd class='ripe'>RIPEMD</dd>
</dl>
</li>
""", 1)
        
        self.assertEqual('a name', document.name)
        self.assertEqual('MD5 SUM', document.md5)
        self.assertEqual('SHA SUM', document.sha)
        self.assertEqual('RIPEMD', document.ripemd)
        
    def testLoad(self):
        documents = diff.Documents()
        documents.load("""
<div class='audit'>
<p>
Audit conducted on <span class='created'>2008-01-22</span> scanned directories
root at <span class='base-dir'>/www/whatever</span>
</p><p>
Artifacts by directory:
</p>
<ul>
<li><span class='dir'>a directory</span>
<ul>
<li><a name='a directory/a name' class='resource'>a name</a>
<dl>
<dt>md5</dt>
<dd class='md5'>MD5 SUM</dd>
<dt>sha</dt>
<dd class='sha'>SHA SUM</dd>
<dt>ripe</dt>
<dd class='ripe'>RIPEMD</dd>
</dl>
</li>
</ul>
</li>
<li><span class='dir'>another directory</span>
<ul>
<li><a name='another directory/another name' class='resource'>another name</a>
<dl>
<dt>md5</dt>
<dd class='md5'>ANOTHER MD5 SUM</dd>
<dt>sha</dt>
<dd class='sha'>ANOTHER SHA SUM</dd>
<dt>ripe</dt>
<dd class='ripe'>ANOTHER RIPEMD</dd>
</dl>
</li>
</ul>
</li>
</ul>
</div>
 """)
        
        self.assert_(not documents.documents == None)
        self.assertEqual('2008-01-22', documents.on)
        self.assertEqual(2, len(documents.documents))
        document = documents.documents[0]
        self.assertEqual('a directory', document.dir)
        self.assertEqual('a name', document.name)
        self.assertEqual('MD5 SUM', document.md5)
        self.assertEqual('SHA SUM', document.sha)
        self.assertEqual('RIPEMD', document.ripemd)
        document = documents.documents[1]
        self.assertEqual('another directory', document.dir)
        self.assertEqual('another name', document.name)
        self.assertEqual('ANOTHER MD5 SUM', document.md5)
        self.assertEqual('ANOTHER SHA SUM', document.sha)
        self.assertEqual('ANOTHER RIPEMD', document.ripemd)
        
class DiffTestCase(unittest.TestCase):
    def setUp(self):
        self.documents = []
        self.documents.append(diff.document("dir", "name", "md5", "sha", "ripemd"))
        self.document2 = diff.document("dir", "name2", "2md5", "2sha", "2ripemd")
        self.documents.append(self.document2)
        self.documents.append(diff.document("dirA", "name", "Amd5", "Asha", "Aripemd"))
        self.documents.append(diff.document("dirA", "nameB", "Bmd5", "Bsha", "Bripemd"))
        
    def testIsMissing(self):
        document = diff.document("dir", "name", "md5", "sha", "ripemd")
        self.assertEquals(False, document.isMissing(self.documents))
        document = diff.document("dirA", "name", "Amd5", "Asha", "Aripemd")
        self.assertEquals(False, document.isMissing(self.documents))      
        document = diff.document("dirC", "name", "md5", "sha", "ripemd")
        self.assertEquals(True, document.isMissing(self.documents))
        document = diff.document("dir", "nameB", "md5", "sha", "ripemd")
        self.assertEquals(True, document.isMissing(self.documents))
        
    def testIsModified(self):
        document = diff.document("dir", "name", "md5", "sha", "ripemd")
        self.assertEquals(False, document.isModified(self.documents))
        document = diff.document("dirA", "name", "Amd5", "Asha", "Aripemd")
        self.assertEquals(False, document.isModified(self.documents))      
        document = diff.document("dirC", "name", "md5", "sha", "ripemd")
        self.assertEquals(False, document.isModified(self.documents))
        document = diff.document("dir", "nameB", "md5", "sha", "ripemd")
        self.assertEquals(False, document.isModified(self.documents))
        document = diff.document("dir", "name", "Amd5", "sha", "ripemd")
        self.assertEquals(True, document.isModified(self.documents))
        document = diff.document("dir", "name", "md5", "Qsha", "ripemd")
        self.assertEquals(True, document.isModified(self.documents))
        document = diff.document("dir", "name", "md5", "sha", "Tripemd")
        self.assertEquals(True, document.isModified(self.documents))
        
    def testCompareEmpty(self):
        emptyDocuments = diff.Documents()
        documents = diff.documents(self.documents)
        added, removed, modified = documents.compare(emptyDocuments)
        self.assert_(not added == None)
        self.assert_(not removed == None)
        self.assert_(not modified == None)
        self.assertEquals(4, len(added))
        self.assertEquals(0, len(modified))
        self.assertEquals(0, len(removed))
        added, removed, modified = emptyDocuments.compare(documents)
        self.assert_(not added == None)
        self.assert_(not removed == None)
        self.assert_(not modified == None)
        self.assertEquals(0, len(added))
        self.assertEquals(0, len(modified))
        self.assertEquals(4, len(removed))
        
    def testCompareDiffering(self):
        documents = diff.documents(self.documents)
        
        differentsDocuments = diff.Documents()
        modifiedDocument = diff.document("dir", "name", "NOT", "NOT", "NOT")
        newDocument = diff.document("anotherdir", "anothername", "NOT", "NOT", "NOT")
        differentsDocuments.append(modifiedDocument)
        differentsDocuments.append(newDocument)
        differentsDocuments.append(diff.document("dirA", "name", "Amd5", "Asha", "Aripemd"))
        differentsDocuments.append(diff.document("dirA", "nameB", "Bmd5", "Bsha", "Bripemd"))
        
        added, removed, modified = differentsDocuments.compare(documents)
        self.assert_(not added == None)
        self.assert_(not removed == None)
        self.assert_(not modified == None)
        self.assertEquals(1, len(modified))
        self.assertEquals(1, len(added))
        self.assertEquals(1, len(removed))
        self.assertEquals(modifiedDocument, modified[0])
        self.assertEquals(newDocument, added[0])
        self.assertEquals(self.document2, removed[0])
        
class ScanDocumentTest(unittest.TestCase):
    def setUp(self):
        self.document = scanner.Document(TEST_BASE_DIR, "Sample.txt", "uri")
    
    def testSums(self):
        self.assertEquals("c81f4cd3b2203ae869b8c6acea6bf73c", self.document.md5())
        self.assertEquals("4b0a5f9317e0d3165ea4982f90e7266a553d8353", self.document.ripe())
        self.assertEquals("2ae73f5cfe7943a7d51b46e653948af7f067a6b01e61c827201c8e17b9231956f48b3e8e0da64e822ca9fdeb7a62f5af623406e2dbb9b39a8dabf569d2046402", self.document.sha())

    def testXml(self):
        self.assertEquals("<li><a name='../../../../../test/org/apache/rat/scan/Sample.txt' class='resource'>Sample.txt</a><dl><dt>md5</dt><dd class='md5'>c81f4cd3b2203ae869b8c6acea6bf73c</dd><dt>sha</dt><dd class='sha'>2ae73f5cfe7943a7d51b46e653948af7f067a6b01e61c827201c8e17b9231956f48b3e8e0da64e822ca9fdeb7a62f5af623406e2dbb9b39a8dabf569d2046402</dd><dt>ripe</dt><dd class='ripe'>4b0a5f9317e0d3165ea4982f90e7266a553d8353</dd></dl></li>", self.document.toXml())
        
class ScanScannerTest(unittest.TestCase):

    def setUp(self):
        self.scanner = scanner.Scanner(TEST_BASE_DIR + "scanner/", "NOW")
        
    def testScan(self):
        self.assertEquals(SCANNER_OUT, self.scanner.scan())