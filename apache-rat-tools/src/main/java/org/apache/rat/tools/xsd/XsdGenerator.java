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

import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.MatcherBuilderTracker;
import org.apache.rat.configuration.XMLConfig;
import org.apache.rat.license.SimpleLicense;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.rat.tools.xsd.XsdWriter.Type;

public class XsdGenerator {
    private ReportConfiguration cfg;
    private XsdWriter writer;
    private static Map<ComponentType, String> typeMap = new HashMap<ComponentType, String>();

    static {
        typeMap.put(ComponentType.MATCHER, XMLConfig.MATCHER);
        typeMap.put(ComponentType.PARAMETER, "xs:string");
        typeMap.put(ComponentType.LICENSE, XMLConfig.LICENSE);
    }

    public static void main(String[] args) throws IOException {
        ReportConfiguration configuration = OptionCollection.parseCommands(args, (options) -> {});
        XsdGenerator generator = new XsdGenerator(configuration);
        generator.write();
    }

    public XsdGenerator(ReportConfiguration cfg) {
        this.cfg = cfg;
    }

    public void write() throws IOException {
        writer = new XsdWriter(new OutputStreamWriter(System.out)).init();

        writer.open(Type.ELEMENT, "name", XMLConfig.ROOT)
                .open(Type.COMPLEX).open(Type.SEQUENCE);
        writeFamilies();
        writeLicenses();
        writeApproved();
        writeMatchers();
        writer.close(Type.ELEMENT);

        writeMatcherElements();

        writer.fini();
    }

    private void writeFamilies() throws IOException {
        writer.open(Type.ELEMENT, "name", XMLConfig.FAMILIES, "maxOccurs", "1", "minOccurs", "0")
                .open(Type.COMPLEX)
                .open(Type.SEQUENCE)
                .open(Type.ELEMENT, "name", XMLConfig.FAMILY, "maxOccurs", "unbounded", "minOccurs", "0")
                .open(Type.COMPLEX)
                .attribute(XMLConfig.ATT_ID, "type", "xs:string", "use", "required")
                .attribute(XMLConfig.ATT_NAME, "type", "xs:string", "use", "required")
                .close(Type.ELEMENT)
                .close(Type.ELEMENT); // families
    }

    private void writeLicenses() throws IOException {
        Description desc = DescriptionBuilder.buildMap(SimpleLicense.class);
        List<Description> children = new ArrayList<>();
        List<Description> attributes = new ArrayList<>();
        for (Description child : desc.getChildren().values()) {
            if (XMLConfig.isLicenseChild(child.getCommonName())) {
                children.add(child);
            } else {
                if (child.getType() == ComponentType.PARAMETER) {
                    attributes.add(child);
                }
            }
        }
        writer.open(Type.ELEMENT, "name", XMLConfig.LICENSES, "maxOccurs", "1", "minOccurs", "0")
                .open(Type.COMPLEX).open(Type.SEQUENCE)
        .open(Type.ELEMENT, "name", XMLConfig.LICENSE, "maxOccurs", "unbounded", "minOccurs", "0")
                .open(Type.COMPLEX).open(Type.CHOICE, "maxOccurs", "unbounded", "minOccurs", "1");
        for (Description child : children) {
            if (child.getCommonName().equals("matcher")) {
                writer.open(Type.ELEMENT, "ref", XMLConfig.MATCHER, "maxOccurs", "1", "minOccurs", "1").close(Type.ELEMENT);
            } else {
                element(child);
            }
        }
        writer.close(Type.CHOICE);
        for (Description child : attributes) {
            attribute(child);
        }
        writer.close(Type.ELEMENT).close(Type.ELEMENT);
    }

    private void writeApproved() throws IOException {
        writer.open(Type.ELEMENT, "name", XMLConfig.APPROVED, "maxOccurs", "1", "minOccurs", "0")
                .open(Type.COMPLEX).open(Type.SEQUENCE)
                .open(Type.ELEMENT, "name", XMLConfig.FAMILY, "maxOccurs", "unbounded", "minOccurs", "0")
                .open(Type.COMPLEX)
                .attribute(XMLConfig.ATT_LICENSE_REF, "type", "xs:string", "use", "required")
                .close(Type.ELEMENT)
                .close(Type.ELEMENT);
    }

    private void writeMatchers() throws IOException {
        writer.open(Type.ELEMENT, "name", XMLConfig.MATCHERS, "maxOccurs", "1", "minOccurs", "0")
                .open(Type.COMPLEX).open(Type.SEQUENCE)
                .open(Type.ELEMENT, "name", XMLConfig.MATCHER, "maxOccurs", "unbounded", "minOccurs", "0")
                .open(Type.COMPLEX)
                .attribute(XMLConfig.ATT_CLASS_NAME, "type", "xs:string", "use", "required")
                .close(Type.ELEMENT)
                .close(Type.ELEMENT);
    }
    private void writeMatcherElements() throws IOException {
        MatcherBuilderTracker tracker = MatcherBuilderTracker.instance();
        writer.open(Type.ELEMENT, "name", XMLConfig.MATCHER, "abstract", "true").close(Type.ELEMENT);

        // matchers
        for (Class<?> clazz : tracker.getClasses()) {
            Description desc = DescriptionBuilder.buildMap(clazz);
            if (desc != null) {
                boolean hasResourceAttr = false;
                Description inline = null;
                List<Description> attributes = new ArrayList<>();
                for (Description child : desc.getChildren().values()) {
                    if (XMLConfig.isInlineNode(desc.getCommonName(), child.getCommonName())) {
                        inline = child;
                    } else {
                        hasResourceAttr |= child.getCommonName().equals(XMLConfig.ATT_RESOURCE);
                        attributes.add(child);
                    }
                }
                writer.open(Type.ELEMENT, "name", desc.getCommonName(), "substitutionGroup", XMLConfig.MATCHER)
                        .open(Type.COMPLEX);
                if (inline != null) {
                    if ("enclosed".equals(inline.getCommonName())) {
                        writer.open(Type.CHOICE).open(Type.ELEMENT, "ref", XMLConfig.MATCHER, "maxOccurs",
                                        inline.isCollection() ? "unbounded" : "1", "minOccurs", hasResourceAttr ? "0" : "1")
                                .close(Type.CHOICE);
                    } else {
                        writer.open(Type.SIMPLE).open(Type.EXTENSION, "base", "xs:string");
                    }
                }
                for (Description child : attributes) {
                    attribute(child);
                }
                writer.close(Type.ELEMENT);
            }
        }
    }

    private boolean element(Description desc) throws IOException {
        String typeName = typeMap.get(desc.getType());
        if (typeName != null) {
            writer.open(Type.ELEMENT,
                    "name", desc.getCommonName(),
                    "type", typeName,
                    "minOccurs", desc.isRequired() ? "1" : "0",
                    "maxOccurs", desc.isCollection() ? "unbounded" : "1"
                    ).close(Type.ELEMENT);
            return true;
        }
        return false;
    }

    private void attribute(Description attr) throws IOException {
        if(attr.getType() == ComponentType.PARAMETER) {
            writer.attribute(attr.getCommonName(), "form", "unqualified", "use", attr.isRequired() ? "required" : "optional");
        }
    }
}
