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
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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
import org.apache.rat.analysis.matchers.SimpleRegexMatcher;
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
 *  <licenses> 
 *  <license id=id name=name >
        <notes></notes>
        <text>  </text>
        <copyright start='' end='' owner=''/>
        <spdx></spdx> 
        <and> <license>...</and>
        <or> <license>...</or> 
        <matcher_ref refid='' />
    </license>
    </licenses>
    <approved>
        <family refid=''>
    </approved>
 */

public class XMLConfigurationReader implements LicenseReader {

    enum MatcherType {
        text, copyright, spdx, any, all, matcher_ref, not, regex
    };

    enum AttributeName {
        id, name, license_ref, refid, start, end, owner, resource, derived_from, exp;
    }

    private final static String ROOT = "rat-config";
    private final static String LICENSES = "licenses";
    private final static String LICENSE = "license";
    private final static String APPROVED = "approved";
    private final static String FAMILY = "family";
    private final static String NOTE = "note";

    private Document document;
    private final Element rootElement;
    private final Element licensesElement;
    private final Element approvedElement;

    private final SortedSet<ILicense> licenses;
    private final Map<String, IHeaderMatcher> matchers;
    private final SortedSet<ILicenseFamily> licenseFamilies;
    private final SortedSet<ILicenseFamily> approvedFamilies;

    public XMLConfigurationReader() {
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("No XML parser defined", e);
        }
        rootElement = document.createElement(ROOT);
        document.appendChild(rootElement);
        licensesElement = document.createElement(LICENSES);
        rootElement.appendChild(licensesElement);
        approvedElement = document.createElement(APPROVED);
        rootElement.appendChild(approvedElement);
        licenses = new TreeSet<>((x, y) -> x.getLicenseFamily().compareTo(y.getLicenseFamily()));
        approvedFamilies = new TreeSet<>();
        licenseFamilies = new TreeSet<>();
        matchers = new HashMap<>();
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
        List<Node> lst = new ArrayList<>();
        nodeListConsumer(newDoc.getElementsByTagName(LICENSE), lst::add);
        nodeListConsumer(newDoc.getElementsByTagName(LICENSE),
                (n) -> licensesElement.appendChild(rootElement.getOwnerDocument().adoptNode(n.cloneNode(true))));
        nodeListConsumer(newDoc.getElementsByTagName(APPROVED),
                (n) -> licensesElement.appendChild(rootElement.getOwnerDocument().adoptNode(n.cloneNode(true))));

    }

    @Override
    public SortedSet<ILicenseFamily> readFamilies() {
        if (licenses.size() == 0) {
            readLicenses();
        }
        return Collections.unmodifiableSortedSet(licenseFamilies);
    }

    Map<AttributeName, String> attributes(Node node) {
        NamedNodeMap nnm = node.getAttributes();
        Map<AttributeName, String> result = new HashMap<>();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            result.put(AttributeName.valueOf(n.getNodeName()), n.getNodeValue());
        }
        return result;
    }

    private IHeaderMatcher createTextMatcher(String id, String txt) {
        boolean complex = txt.contains(" ") | txt.contains("\\t") | txt.contains("\\n") | txt.contains("\\r")
                | txt.contains("\\f") | txt.contains("\\v");
        return complex ? new FullTextMatcher(id, txt) : new SimpleTextMatcher(id, txt);
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
                    matchers.add(createTextMatcher(null, txt));
                }
            }
            return matchers;
        } catch (IOException e) {
            throw new ConfigurationException("Unable to read matching text file: " + resourceName, e);
        }
    }

    private IHeaderMatcher parseMatcher(Node matcherNode) {
        Map<AttributeName, String> attr = attributes(matcherNode);
        MatcherType type = MatcherType.valueOf(matcherNode.getNodeName());
        List<IHeaderMatcher> children;
        IHeaderMatcher result = null;
        String id = attr.get(AttributeName.id);

        switch (type) {
        case text:
            result = createTextMatcher(id, matcherNode.getTextContent().trim());
            break;

        case any:
        case all:
            if (attr.containsKey(AttributeName.resource)) {
                children = readTextResource(attr.get(AttributeName.resource));
            } else {
                children = new ArrayList<>();
                nodeListConsumer(matcherNode.getChildNodes(), x -> {
                    if (x.getNodeType() == Node.ELEMENT_NODE) {
                        children.add(parseMatcher(x));
                    }
                });
            }
            result = type == MatcherType.any ? new OrMatcher(id, children) : new AndMatcher(id, children);
            break;

        case copyright:
            result = new CopyrightMatcher(id, attr.get(AttributeName.start), attr.get(AttributeName.end),
                    attr.get(AttributeName.owner));
            break;

        case spdx:
            result = SPDXMatcherFactory.INSTANCE.create(attr.get(AttributeName.name));
            break;

        case matcher_ref:
            result = IHeaderMatcherProxy.create(attr.get(AttributeName.refid), matchers);
            break;

        case not:
            children = new ArrayList<>();
            nodeListConsumer(matcherNode.getChildNodes(), x -> {
                if (x.getNodeType() == Node.ELEMENT_NODE) {
                    children.add(parseMatcher(x));
                }
            });
            if (children.size() != 1) {
                throw new ConfigurationException("'not' type matcher requires one and only one enclosed matcher");
            }
            result = new NotMatcher(children.get(0));
            break;

        case regex:
            if (!attr.containsKey(AttributeName.exp)) {
                throw new ConfigurationException("'regex' type matcher requires an 'exp' attribute");
            }
            result = new SimpleRegexMatcher(Pattern.compile(attr.get(AttributeName.exp)));
        }
        if (attr.containsKey(AttributeName.id)) {
            matchers.put(attr.get(AttributeName.id), result);
        }
        return result;
    }

    private ILicense parseLicense(Node licenseNode) {
        Map<AttributeName, String> attributes = attributes(licenseNode);

        ILicenseFamily family = new SimpleLicenseFamily(attributes.get(AttributeName.id),
                attributes.get(AttributeName.name));
        IHeaderMatcher matcher[] = { null };
        StringBuilder notesBuilder = new StringBuilder();
        nodeListConsumer(licenseNode.getChildNodes(), x -> {
            if (x.getNodeType() == Node.ELEMENT_NODE) {
                if (x.getNodeName().equals(NOTE)) {
                    notesBuilder.append(x.getTextContent()).append("\n");
                } else {
                    matcher[0] = parseMatcher(x);
                }
            }
        });
        ILicense derivedFrom = !attributes.containsKey(AttributeName.derived_from) ? null
                : ILicenseProxy.create(attributes.get(AttributeName.derived_from), licenses);
        String notes = StringUtils.defaultIfBlank(notesBuilder.toString().trim(), null);
        return new SimpleLicense(family, matcher[0], derivedFrom, notes);
    }

    @Override
    public SortedSet<ILicense> readLicenses() {
        if (licenses.size() == 0) {
            nodeListConsumer(document.getElementsByTagName(LICENSE), x -> licenses.add(parseLicense(x)));
            licenses.stream().map(ILicense::getLicenseFamily).forEach(licenseFamilies::add);
            nodeListConsumer(document.getElementsByTagName(FAMILY), x -> approvedFamilies.add(parseFamily(x)));
            Stack<ILicense> stack = new Stack<>();
            for (ILicense license : licenses) {
                ILicense derivedFrom = license.derivedFrom();
                if (derivedFrom != null) {
                    stack.push(license);
                    while (derivedFrom != null) {
                        stack.push(derivedFrom);
                        derivedFrom = derivedFrom.derivedFrom();
                    }
                }
                while (!stack.isEmpty()) {
                    derivedFrom = stack.pop();
                    ILicenseFamily found = ILicenseFamily.search(derivedFrom.getLicenseFamily(), approvedFamilies);
                    if (found != null) {
                        while (!stack.isEmpty()) {
                            approvedFamilies.add(stack.pop().getLicenseFamily());
                        }
                    }
                }

            }
            document = null;
        }
        return Collections.unmodifiableSortedSet(licenses);
    }

    private ILicenseFamily parseFamily(Node familyNode) {
        Map<AttributeName, String> attributes = attributes(familyNode);
        if (attributes.containsKey(AttributeName.license_ref)) {
            return ILicenseFamilyProxy
                    .create(ILicenseProxy.create(attributes.get(AttributeName.license_ref), licenses));
        }
        throw new ConfigurationException("family tag requires " + AttributeName.license_ref + " attribute");
    }

    @Override
    public SortedSet<ILicenseFamily> approvedLicenseFamilies() {
        return approvedFamilies.isEmpty() ? readFamilies() : approvedFamilies;
    }
}
