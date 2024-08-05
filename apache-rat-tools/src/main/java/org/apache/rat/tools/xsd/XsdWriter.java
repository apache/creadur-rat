/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.tools.xsd;

import org.apache.rat.ReportConfiguration;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class XsdWriter {
    private final XmlWriter writer;

    public enum Type {
        ELEMENT("xs:element"), ATTRIBUTE("xs:attribute"),
        COMPLEX("xs:complexType"), SEQUENCE("xs:sequence"), SIMPLE("xs:simpleContent"),
        EXTENSION("xs:extension"), CHOICE("xs:choice"), COMPLEX_CONTENT("xs:complexContent");
        private String elementName;

        Type(String name) {
            elementName = name;
        }
    }

    public XsdWriter(Writer writer) {
        this.writer = new XmlWriter(writer);
    }

    public XsdWriter init() throws IOException {
        writer.startDocument()
        .openElement("xs:schema")
        .attribute("attributeFormDefault", "unqualified")
                .attribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
        return this;
    }

    public void fini() throws IOException {
        writer.closeDocument();
    }

    private void writeAttributes(String[] attributeMap) throws IOException {
        if (attributeMap != null) {
            for (int i = 0; i < attributeMap.length; i += 2) {
                writer.attribute(attributeMap[i], attributeMap[i + 1]);
            }
        }
    }

    public XsdWriter open(Type type, String... attributeMap) throws IOException {
        writer.openElement(type.elementName);
        writeAttributes(attributeMap);
        return this;
    }

    public XsdWriter attribute(String name, String... attributeMap) throws IOException {
        writer.openElement("xs:attribute").attribute("name", name);
        writeAttributes(attributeMap);
        writer.closeElement();
        return this;
    }

    public XsdWriter close(Type type) throws IOException {
        writer.closeElement(type.elementName);
        return this;
    }

}




    /*

  <xs:element name="rat-config">
    <xs:complexType>
      <xs:sequence>
        <xs:element name="families">
          <xs:complexType>
            <xs:sequence>
              <xs:element name="family" maxOccurs="unbounded" minOccurs="0">
                <xs:complexType>
                  <xs:simpleContent>
                    <xs:extension base="xs:string">
                      <xs:attribute type="xs:string" name="id" use="optional"/>
                      <xs:attribute type="xs:string" name="name" use="optional"/>
                    </xs:extension>
                  </xs:simpleContent>
                </xs:complexType>
              </xs:element>
            </xs:sequence>
          </xs:complexType>
        </xs:element>
        <xs:element name="licenses">
          <xs:complexType>
            <xs:sequence>
              <xs:element name="license" maxOccurs="unbounded" minOccurs="0">
                <xs:complexType>
                  <xs:choice maxOccurs="unbounded" minOccurs="0">
                    <xs:element name="all">
                      <xs:complexType>
                        <xs:choice maxOccurs="unbounded" minOccurs="0">
                          <xs:element name="any">
                            <xs:complexType>
                              <xs:sequence>
                                <xs:element type="xs:string" name="text" maxOccurs="unbounded" minOccurs="0"/>
                                <xs:element name="matcherRef" minOccurs="0">
                                  <xs:complexType>
                                    <xs:simpleContent>
                                      <xs:extension base="xs:string">
                                        <xs:attribute type="xs:string" name="refId"/>
                                      </xs:extension>
                                    </xs:simpleContent>
                                  </xs:complexType>
                                </xs:element>
                                <xs:element name="spdx">
                                  <xs:complexType>
                                    <xs:simpleContent>
                                      <xs:extension base="xs:string">
                                        <xs:attribute type="xs:string" name="name" use="optional"/>
                                      </xs:extension>
                                    </xs:simpleContent>
                                  </xs:complexType>
                                </xs:element>
                              </xs:sequence>
                              <xs:attribute type="xs:string" name="id" use="optional"/>
                            </xs:complexType>
                          </xs:element>
                          <xs:element name="not">
                            <xs:complexType>
                              <xs:sequence>
                                <xs:element type="xs:string" name="copyright"/>
                              </xs:sequence>
                            </xs:complexType>
                          </xs:element>
                          <xs:element name="matcherRef">
                            <xs:complexType>
                              <xs:simpleContent>
                                <xs:extension base="xs:string">
                                  <xs:attribute type="xs:string" name="refId"/>
                                </xs:extension>
                              </xs:simpleContent>
                            </xs:complexType>
                          </xs:element>
                          <xs:element type="xs:string" name="text"/>
                          <xs:element name="copyright">
                            <xs:complexType>
                              <xs:simpleContent>
                                <xs:extension base="xs:string">
                                  <xs:attribute type="xs:string" name="owner" use="optional"/>
                                  <xs:attribute type="xs:string" name="start" use="optional"/>
                                </xs:extension>
                              </xs:simpleContent>
                            </xs:complexType>
                          </xs:element>
                        </xs:choice>
                      </xs:complexType>
                    </xs:element>
                    <xs:element type="xs:string" name="note"/>
                    <xs:element name="any">
                      <xs:complexType mixed="true">
                        <xs:sequence>
                          <xs:element type="xs:string" name="text" minOccurs="0"/>
                          <xs:element name="all" minOccurs="0">
                            <xs:complexType>
                              <xs:sequence>
                                <xs:element type="xs:string" name="copyright"/>
                                <xs:element name="text">
                                  <xs:complexType>
                                    <xs:simpleContent>
                                      <xs:extension base="xs:string">
                                        <xs:attribute type="xs:string" name="id"/>
                                      </xs:extension>
                                    </xs:simpleContent>
                                  </xs:complexType>
                                </xs:element>
                              </xs:sequence>
                            </xs:complexType>
                          </xs:element>
                          <xs:element name="spdx" minOccurs="0">
                            <xs:complexType>
                              <xs:simpleContent>
                                <xs:extension base="xs:string">
                                  <xs:attribute type="xs:string" name="name" use="optional"/>
                                </xs:extension>
                              </xs:simpleContent>
                            </xs:complexType>
                          </xs:element>
                        </xs:sequence>
                        <xs:attribute type="xs:string" name="resource" use="optional"/>
                      </xs:complexType>
                    </xs:element>
                    <xs:element type="xs:string" name="text"/>
                  </xs:choice>
                  <xs:attribute type="xs:string" name="family" use="optional"/>
                  <xs:attribute type="xs:string" name="id" use="optional"/>
                  <xs:attribute type="xs:string" name="name" use="optional"/>
                </xs:complexType>
              </xs:element>
            </xs:sequence>
          </xs:complexType>
        </xs:element>
        <xs:element name="approved">
          <xs:complexType>
            <xs:sequence>
              <xs:element name="family" maxOccurs="unbounded" minOccurs="0">
                <xs:complexType>
                  <xs:simpleContent>
                    <xs:extension base="xs:string">
                        <xs:attribute type="xs:string" name="license_ref" use="optional"/>
                    </xs:extension>
                  </xs:simpleContent>
                </xs:complexType>
              </xs:element>
            </xs:sequence>
          </xs:complexType>
        </xs:element>
        <xs:element name="matchers">
          <xs:complexType>
            <xs:sequence>
              <xs:element name="matcher" maxOccurs="unbounded" minOccurs="0">
                <xs:complexType>
                  <xs:simpleContent>
                    <xs:extension base="xs:string">
                      <xs:attribute type="xs:string" name="class" use="optional"/>
                    </xs:extension>
                  </xs:simpleContent>
                </xs:complexType>
              </xs:element>
            </xs:sequence>
          </xs:complexType>
        </xs:element>
      </xs:sequence>
    </xs:complexType>
  </xs:element>
</xs:schema>

         */
