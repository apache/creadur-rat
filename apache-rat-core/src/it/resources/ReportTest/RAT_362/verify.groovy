package ReportTest.RAT_335

import org.apache.rat.testhelpers.XmlUtils
import org.apache.rat.utils.DefaultLog
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

DefaultLog.instance.warn(content)

Document document = XmlUtils.toDom(new FileInputStream(args[0]))
XPath xPath = XPathFactory.newInstance().newXPath()

List<String> ignoredFiles = new ArrayList<>(Arrays.asList(
        "/.gitignore",
        "/foo.md",
        "/src.md"));

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
        mapOf("count", "3" ))
