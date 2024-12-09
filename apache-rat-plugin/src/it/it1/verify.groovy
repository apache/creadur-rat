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
import org.apache.rat.testhelpers.XmlUtils
import org.w3c.dom.Document

import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory

private static Map<String, String> mapOf(String... parts) {
    Map<String, String> map = new HashMap<>();
    for (int i=0; i<parts.length; i+=2) {
        map.put(parts[i], parts[i+1]);
    }
    return map;
}

f = new File(basedir, 'out.xml')
assert f.exists()

Document document = XmlUtils.toDom(new FileInputStream(f));
XPath xPath = XPathFactory.newInstance().newXPath();

XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/src.apt']",
        mapOf("encoding", "ISO-8859-1", "mediaType", "text/plain", "type", "STANDARD" ));

XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/src.apt']/license[@id='MyLicense']",
        mapOf("approval", "true", "family", "YAL  ", "name", "Yet another license" ));

XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/src.apt']/license[@id='Not']",
        mapOf("approval", "true", "family", "YAL  ", "name", "Not testing" ));
