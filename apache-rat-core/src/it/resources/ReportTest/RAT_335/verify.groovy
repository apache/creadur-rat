package ReportTest.RAT_335

import org.apache.rat.testhelpers.XmlUtils
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.NodeList

import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory

import static org.assertj.core.api.Assertions.assertThat

private static Map<String, String> mapOf(String... parts) {
    Map<String, String> map = new HashMap<>()
    for (int i = 0; i < parts.length; i += 2) {
        map.put(parts[i], parts[i+1])
    }
    return map
}
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
output = new File(args[0])
content = output.text

//Map<ClaimStatistic.Counter, String> data = new HashMap<>()
//data.put(ClaimStatistic.Counter.APPROVED, "2")
//data.put(ClaimStatistic.Counter.ARCHIVES, "0")
//data.put(ClaimStatistic.Counter.BINARIES, "0")
//data.put(ClaimStatistic.Counter.DOCUMENT_TYPES, "3")
//data.put(ClaimStatistic.Counter.IGNORED, "6")
//data.put(ClaimStatistic.Counter.LICENSE_CATEGORIES, "2")
//data.put(ClaimStatistic.Counter.LICENSE_NAMES, "2")
//data.put(ClaimStatistic.Counter.NOTICES, "1")
//data.put(ClaimStatistic.Counter.STANDARDS, "6")
//data.put(ClaimStatistic.Counter.UNAPPROVED, "4")
//data.put(ClaimStatistic.Counter.UNKNOWN, "4")

Document document = XmlUtils.toDom(new FileInputStream(args[0]))
XPath xPath = XPathFactory.newInstance().newXPath()

//for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
//    String xpath = String.format("/rat-report/statistics/statistic[@name='%s']", counter.displayName())
//    Map<String, String> map = mapOf("approval",
//            counter == ClaimStatistic.Counter.UNAPPROVED ? "false" : "true",
//            "count", data.get(counter),
//            "description", counter.getDescription())
//    XmlUtils.assertAttributes(document, xPath, xpath, map)
//}

//// license categories
//XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseCategory[@name='?????']",
//        mapOf("count", "4" ))
//
//XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseCategory[@name='AL   ']",
//        mapOf("count", "2" ))
//
//// license names
//XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseName[@name='Apache License Version 2.0']",
//        mapOf("count", "2" ))
//
//XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseName[@name='Unknown license']",
//        mapOf("count", "4" ))



//Note the output when running in the real commandline version of git
//
//# Files that must be ignored (dropping the gitignore matches outside of this test tree)
//$ git check-ignore --no-index --verbose $(find . -type f|sort)
//
// apache-rat-core/src/it/resources/ReportTest/RAT_335/src/dir1/.gitignore:2:!dir1.md	./dir1/dir1.md
// apache-rat-core/src/it/resources/ReportTest/RAT_335/src/dir1/.gitignore:1:*.txt	./dir1/dir1.txt
// apache-rat-core/src/it/resources/ReportTest/RAT_335/src/dir1/.gitignore:3:file1.log	./dir1/file1.log
// .gitignore:20:**/.gitignore	./dir1/.gitignore
// apache-rat-core/src/it/resources/ReportTest/RAT_335/src/.gitignore:1:*.md	./dir2/dir2.md
// apache-rat-core/src/it/resources/ReportTest/RAT_335/src/.gitignore:4:*.log	./dir3/dir3.log
// apache-rat-core/src/it/resources/ReportTest/RAT_335/src/.gitignore:7:!file*.log	./dir3/file3.log
//         .gitignore:20:**/.gitignore	./.gitignore
// apache-rat-core/src/it/resources/ReportTest/RAT_335/src/.gitignore:1:*.md	./root.md

/* list of excluded files:

./dir1/dir1.txt
./dir1/file1.log
./dir1/.gitignore
./dir2/dir2.md
./dir3/dir3.log
./.gitignore
./root.md

 */

List<String> ignoredFiles = new ArrayList<>(Arrays.asList(
        "/dir1/dir1.txt",
        "/dir1/file1.log",
        "/dir1/.gitignore",
        "/dir2/dir2.md",
        "/dir3/dir3.log",
        "/.gitignore",
        "/root.md"))

NodeList nodeList = XmlUtils.getNodeList(document, xPath, "/rat-report/resource[@type='IGNORED']")
for (int i = 0 ; i < nodeList.getLength(); i++) {
    NamedNodeMap attr = nodeList.item(i).getAttributes()
    String s = attr.getNamedItem("name").getNodeValue()
    assertThat(ignoredFiles).contains(s)
    ignoredFiles.remove(s)
}

assertThat(ignoredFiles).isEmpty()

// Document types
XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='IGNORED']",
        mapOf("count", "6" ))

XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='NOTICE']",
        mapOf("count", "1" ))

XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='STANDARD']",
        mapOf("count", "6" ))

/*
TextUtils.assertPatternInTarget("^  Approved:\\s+8 ", content)
TextUtils.assertPatternInTarget("^  Archives:\\s+1 ", content)
TextUtils.assertPatternInTarget("^  Binaries:\\s+2 ", content)
TextUtils.assertPatternInTarget("^  Document types:\\s+5 ", content)
TextUtils.assertPatternInTarget("^  Ignored:\\s+1 ", content)
TextUtils.assertPatternInTarget("^  License categories:\\s+4 ", content)
TextUtils.assertPatternInTarget("^  License names:\\s+5", content)
TextUtils.assertPatternInTarget("^  Notices:\\s+2 ", content)
TextUtils.assertPatternInTarget("^  Standards:\\s+8 ", content)
TextUtils.assertPatternInTarget("^  Unapproved:\\s+2 ", content)
TextUtils.assertPatternInTarget("^  Unknown:\\s+2 ", content)

logOutput = new File(args[1])
log = logOutput.text

TextUtils.assertPatternNotInTarget("^ERROR:", log)
TextUtils.assertPatternNotInTarget("^WARN:", log)
*/
