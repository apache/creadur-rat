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
package org.apache.rat.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.AndMatcher;
import org.apache.rat.analysis.matchers.CopyrightMatcher;
import org.apache.rat.analysis.matchers.FullTextMatcher;
import org.apache.rat.analysis.matchers.NotMatcher;
import org.apache.rat.analysis.matchers.OrMatcher;
import org.apache.rat.analysis.matchers.SPDXMatcherFactory;
import org.apache.rat.analysis.matchers.SimpleTextMatcher;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.SimpleLicense;
import org.apache.rat.license.SimpleLicenseFamily;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 *  <license id=id name=name >
        <notes></notes>
        <text>  </text>
        <copyright start='' end='' owner=''/>
        <spdx></spdx> 
        <and> <license>...</and>
        <or> <license>...</or> 
    </license>
 */

public class XMLConfigurationReader implements LicenseReader {

    enum MatcherType {
        text, copyright, spdx, any, all, license_ref, not
    };

    private Document document;
    private final Element baseElement;
    private final Element licensesElement;
    private final SortedSet<ILicense> licenses;

    public XMLConfigurationReader() {
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("No XML parser defined", e);
        }
        baseElement = document.createElement("rat-config");
        document.appendChild(baseElement);
        licensesElement = document.createElement("licenses");
        baseElement.appendChild(licensesElement);
        licenses = new TreeSet<>((x, y) -> x.getLicenseFamily().compareTo(y.getLicenseFamily()));
    }

    public void printDocument(OutputStream out) {
        printDocument(out, document);
    }

    public void printDocument(OutputStream out, Document document) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(document), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        } catch (TransformerException | UnsupportedEncodingException e) {
            e.printStackTrace(new PrintStream(out));
        }
    }

    @Override
    public void add(URL url) {
        read(url);
    }

    public void read(URL... urls) {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException("Unable to create DOM builder", e);
        }
        for (URL url : urls) {
            try {
                add(builder.parse(url.openStream()));
            } catch (SAXException | IOException e) {
                throw new ConfigurationException("Unable to read url: " + url, e);
            }
        }

    }

    private void nodeListConsumer(NodeList list, Consumer<Node> consumer) {
        for (int i = 0; i < list.getLength(); i++) {
            consumer.accept(list.item(i));
        }
    }

    public void add(Document newDoc) {
        printDocument(System.out, newDoc);
        List<Node> lst = new ArrayList<>();
        nodeListConsumer(newDoc.getElementsByTagName("license"), lst::add);
        nodeListConsumer(newDoc.getElementsByTagName("license"),
                (n) -> licensesElement.appendChild(baseElement.getOwnerDocument().adoptNode(n.cloneNode(true))));
    }

    @Override
    public SortedSet<ILicenseFamily> readFamilies() {
        SortedSet<ILicenseFamily> result = new TreeSet<>();
        readLicenses().stream().map(ILicense::getLicenseFamily).forEach(result::add);
        return result;
    }

    Map<String, String> attributes(Node node) {
        NamedNodeMap nnm = node.getAttributes();
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            result.put(n.getNodeName(), n.getNodeValue());
        }
        return result;
    }

    private IHeaderMatcher createTextMatcher(String txt) {
        boolean complex = txt.contains(" ") | txt.contains("\\t") | txt.contains("\\n") | txt.contains("\\r")
                | txt.contains("\\f") | txt.contains("\\v");
        return complex ? new FullTextMatcher(txt) : new SimpleTextMatcher(txt);
    }

    /**
     * Reads a text file. Each line becomes a text matcher in the resulting List.
     * 
     * @param resourceName the name of the resource to read.
     * @return a List of Matchers, one for each non empty line in the input file.
     */
    private List<IHeaderMatcher> readTextResource(String resourceName) {
        URL url = XMLConfigurationReader.class.getResource(resourceName);
        List<IHeaderMatcher> matchers = new ArrayList<>();
        try (final InputStream in = url.openStream()) {
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String txt;
            while (null != (txt = buffer.readLine())) {
                txt = txt.trim();
                if (StringUtils.isNotBlank(txt)) {
                    matchers.add(createTextMatcher(txt));
                }
            }
            return matchers;
        } catch (IOException e) {
            throw new ConfigurationException("Unable to read matching text file: " + resourceName, e);
        }
    }

    private IHeaderMatcher parseMatcher(Node matcherNode) {
        Map<String, String> attr = attributes(matcherNode);
        MatcherType type = MatcherType.valueOf(matcherNode.getNodeName());
        List<IHeaderMatcher> children;

        switch (type) {
        case text:
            return createTextMatcher(matcherNode.getTextContent().trim());

        case any:
        case all:

            if (attr.get("resource") != null) {
                children = readTextResource(attr.get("resource"));
            } else {
                children = new ArrayList<>();
                nodeListConsumer(matcherNode.getChildNodes(), x -> {
                    if (x.getNodeType() == Node.ELEMENT_NODE) {
                        children.add(parseMatcher(x));
                    }
                });
            }
            return type == MatcherType.any ? new OrMatcher(children) : new AndMatcher(children);

        case copyright:
            return new CopyrightMatcher(attr.get("start"), attr.get("end"), attr.get("owner"));

        case spdx:
            return SPDXMatcherFactory.INSTANCE.create(attr.get("name"));
        case license_ref:
            return new ILicenseProxy(attr.get("refid"));

        case not:
            children = new ArrayList<>();
            nodeListConsumer(matcherNode.getChildNodes(), x -> {
                if (x.getNodeType() == Node.ELEMENT_NODE) {
                    children.add(parseMatcher(x));
                }
            });
            if (children.size() != 1) {
                throw new ConfigurationException("'Not' type matcher requires one and only one enclosed matcher");
            }
            return new NotMatcher(children.get(0));
        }
        throw new ConfigurationException(String.format("%s is not a matcher type", matcherNode.getNodeName()));
    }

    private ILicense parseLicense(Node licenseNode) {
        Map<String, String> attributes = attributes(licenseNode);

        ILicenseFamily family = new SimpleLicenseFamily(attributes.get("id"), attributes.get("name"));
        IHeaderMatcher matcher[] = { null };
        StringBuilder notesBuilder = new StringBuilder();
        nodeListConsumer(licenseNode.getChildNodes(), x -> {
            if (x.getNodeType() == Node.ELEMENT_NODE) {
                if (x.getNodeName().equals("note")) {
                    notesBuilder.append(x.getTextContent()).append("\n");
                } else {
                    matcher[0] = parseMatcher(x);
                }
            }
        });
        ILicense derivedFrom = attributes.get("derived-from") == null ? null
                : new ILicenseProxy(attributes.get("derived-from"));
        String notes = StringUtils.defaultIfBlank(notesBuilder.toString().trim(), null);
        return new SimpleLicense(family, matcher[0], derivedFrom, notes);
    }

    @Override
    public SortedSet<ILicense> readLicenses() {
        if (licenses.size() == 0) {
            nodeListConsumer(document.getElementsByTagName("license"), x -> licenses.add(parseLicense(x)));
            document = null;
        }
        return Collections.unmodifiableSortedSet(licenses);
    }

    class ILicenseProxy implements ILicense {
        final String proxyId;
        ILicense wrapped;

        ILicenseProxy(String proxyId) {
            this.proxyId = proxyId;
        }

        private void checkProxy() {
            if (wrapped == null) {
                ILicenseFamily testLic = new SimpleLicenseFamily(proxyId, "searching proxy");
                Optional<ILicense> lic = licenses.stream().filter(l -> l.getLicenseFamily().compareTo(testLic) == 0)
                        .findFirst();
                if (!lic.isPresent()) {
                    throw new IllegalStateException(String.format("%s is not a valid family category", proxyId));
                }
                wrapped = lic.get();
            }
        }

        @Override
        public ILicenseFamily getLicenseFamily() {
            checkProxy();
            return wrapped.getLicenseFamily();
        }

        @Override
        public String getNotes() {
            checkProxy();
            return wrapped.getNotes();
        }

        @Override
        public ILicense derivedFrom() {
            checkProxy();
            return wrapped.derivedFrom();
        }

        @Override
        public String getId() {
            checkProxy();
            return wrapped.getId();
        }

        @Override
        public void reset() {
            checkProxy();
            wrapped.reset();
        }

        @Override
        public boolean matches(String line) {
            checkProxy();
            return wrapped.matches(line);
        }

        @Override
        public int compareTo(ILicense arg0) {
            return ILicense.getComparator().compare(this, arg0);
        }
    }
}
