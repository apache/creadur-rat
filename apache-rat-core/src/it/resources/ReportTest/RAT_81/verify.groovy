package ReportTest.RAT_81
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

import org.apache.rat.testhelpers.TextUtils
import org.apache.rat.testhelpers.XmlUtils
import org.w3c.dom.NodeList

import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory

import static org.junit.jupiter.api.Assertions.assertEquals;

doc = XmlUtils.toDom(new FileInputStream(args[0]))
XPath xPath = XPathFactory.newInstance().newXPath()

NodeList nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@name='/IBM037.txt']")
assertEquals(1, nodeList.getLength())
node = nodeList.item(0)
attributes = node.getAttributes()
assertEquals("IBM500", attributes.getNamedItem("encoding").getNodeValue())
assertEquals("text/plain", attributes.getNamedItem("mediaType").getNodeValue())
assertEquals("STANDARD", attributes.getNamedItem("type").getNodeValue())
nodeList = XmlUtils.getNodeList(node, xPath, "license")
assertEquals(1, nodeList.getLength())
node = nodeList.item(0)
attributes = node.getAttributes()
assertEquals("true", attributes.getNamedItem("approval").getNodeValue())

nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@name='/UTF8.txt']")
assertEquals(1, nodeList.getLength())
node = nodeList.item(0)
attributes = node.getAttributes()
assertEquals("ISO-8859-1", attributes.getNamedItem("encoding").getNodeValue())
assertEquals("text/plain", attributes.getNamedItem("mediaType").getNodeValue())
assertEquals("STANDARD", attributes.getNamedItem("type").getNodeValue())
nodeList = XmlUtils.getNodeList(node, xPath, "license")
assertEquals(1, nodeList.getLength())
node = nodeList.item(0)
attributes = node.getAttributes()
assertEquals("true", attributes.getNamedItem("approval").getNodeValue())

logOutput = new File(args[1])
log = logOutput.text

TextUtils.assertPatternNotInTarget("^ERROR:", log)
TextUtils.assertPatternNotInTarget("^WARN:", log)