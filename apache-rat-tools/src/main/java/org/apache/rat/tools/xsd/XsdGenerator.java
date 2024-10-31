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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.MatcherBuilderTracker;
import org.apache.rat.configuration.XMLConfig;
import org.apache.rat.license.SimpleLicense;
import org.apache.rat.tools.xsd.XsdWriter.Type;

/**
 * Generates the XSD for a configuration.
 */
public class XsdGenerator {
    /** The configuration to generate the XSD for. */
    private final ReportConfiguration cfg;
    /** The XsdWriter being written to. */
    private XsdWriter writer;
    /** A map of component type to XML element name / property type */
    private static final Map<ComponentType, String> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put(ComponentType.MATCHER, XMLConfig.MATCHER);
        TYPE_MAP.put(ComponentType.PARAMETER, "xs:string");
        TYPE_MAP.put(ComponentType.LICENSE, XMLConfig.LICENSE);
    }

    /**
     * Command line that accepts standard RAT CLI command line options and generates an XSD from the
     * configuration.
     * @param args the arguments for RAT CLI.
     * @throws IOException on IO errors.
     * @throws TransformerException if the XSD can not be pretty printed.
     */
    public static void main(final String[] args) throws IOException, TransformerException {
        ReportConfiguration configuration = OptionCollection.parseCommands(args, options -> {
        });
        XsdGenerator generator = new XsdGenerator(configuration);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try (InputStream in = generator.getInputStream();
             InputStream styleIn = StyleSheets.XML.getStyleSheet().get()) {
            transformer = tf.newTransformer(new StreamSource(styleIn));
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new StreamSource(in),
                    new StreamResult(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)));
        }
    }

    /**
     * Constructs an XsdGenerator for the structures in the configuration.
     * @param cfg the configuration to generate the XSD for.
     */
    public XsdGenerator(final ReportConfiguration cfg) {
        this.cfg = cfg;
    }

    /**
     * Create an input stream from the output of the generator.
     * @return an InputStream that contains the output of the generator.
     * @throws IOException on output errors.
     */
    public InputStream getInputStream() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Writer writer = new OutputStreamWriter(baos)) {
            write(writer);
            return new ByteArrayInputStream(baos.toByteArray());
        }
    }

    /**
     * Writes the XSD to the output.
     * @param output the output to write to.
     * @throws IOException on write error.
     */
    public void write(final Writer output) throws IOException {
        writer = new XsdWriter(output).init();

        writer.open(Type.ELEMENT, "name", XMLConfig.ROOT)
                .open(Type.COMPLEX).open(Type.SEQUENCE);
        writeFamilies();
        writeLicenses();
        writeApproved();
        writeMatchers();
        writer.close(Type.ELEMENT);

        writeMatcherElements();

        writer.finish();
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

    private boolean element(final Description desc) throws IOException {
        String typeName = TYPE_MAP.get(desc.getType());
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

    private void attribute(final Description attr) throws IOException {
        if (attr.getType() == ComponentType.PARAMETER) {
            writer.attribute(attr.getCommonName(), "form", "unqualified", "use", attr.isRequired() ? "required" : "optional");
        }
    }
}
