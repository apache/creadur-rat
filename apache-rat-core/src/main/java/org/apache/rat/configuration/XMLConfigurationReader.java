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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.FullTextMatcher;
import org.apache.rat.analysis.matchers.SimpleTextMatcher;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.builders.AbstractBuilder;
import org.apache.rat.configuration.builders.ChildContainerBuilder;
import org.apache.rat.configuration.builders.MatcherRefBuilder;
import org.apache.rat.configuration.builders.TextCaptureBuilder;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseFamilySetFactory;
import org.apache.rat.license.LicenseSetFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A class that reads the XML configuration file format.
 * <p>
 * {@code <rat-config>}<br>
 * {@code   <licenses>}<br>
 * {@code     <license id=id name=name >}<br>
 * {@code       <notes></notes>}<br>
 * {@code       <text>  </text>}<br>
 * {@code       <copyright start='' end='' owner=''/>}<br>
 * {@code       <spdx></spdx> }<br>
 * {@code       <and> <matcher/>...</and>}<br>
 * {@code       <or> <matcher/>...</or> }<br>
 * {@code       <matcher_ref refid='' />}<br>
 * {@code       <not><matcher /></not>}<br>
 * {@code     </license>}<br>
 * {@code   </licenses>}<br>
 * {@code   <approved>}<br>
 * {@code     <family refid=''>}<br>
 * {@code   </approved>}<br>
 * {@code   <matchers>}<br>
 * {@code     <matcher className=''/>}<br>
 * {@code     <matcher className=''/>}<br>
 * {@code   </matchers>}<br>
 * {@code </rat-config>}<br>
 * </p>
 */

public class XMLConfigurationReader implements LicenseReader, MatcherReader {

    public final static String ATT_ID = "id";
    public final static String ATT_NAME = "name";
    public final static String ATT_LICENSE_REF = "license_ref";
    public final static String ATT_CLASS_NAME = "class";

    public final static String ROOT = "rat-config";
    public final static String FAMILIES = "families";
    public final static String LICENSES = "licenses";
    public final static String LICENSE = "license";
    public final static String APPROVED = "approved";
    public final static String FAMILY = "family";
    public final static String NOTE = "note";
    public final static String MATCHERS = "matchers";
    public final static String MATCHER = "matcher";

    private Document document;
    private final Element rootElement;
    private final Element familiesElement;
    private final Element licensesElement;
    private final Element approvedElement;
    private final Element matchersElement;

    private final SortedSet<ILicense> licenses;
    private final Map<String, IHeaderMatcher> matchers;
    private final SortedSet<ILicenseFamily> licenseFamilies;
    private final SortedSet<String> approvedFamilies;

    /**
     * Constructs the XML configuration reader.
     */
    public XMLConfigurationReader() {
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("No XML parser defined", e);
        }
        rootElement = document.createElement(ROOT);
        document.appendChild(rootElement);
        familiesElement = document.createElement(FAMILIES);
        rootElement.appendChild(familiesElement);
        licensesElement = document.createElement(LICENSES);
        rootElement.appendChild(licensesElement);
        approvedElement = document.createElement(APPROVED);
        rootElement.appendChild(approvedElement);
        matchersElement = document.createElement(MATCHERS);
        rootElement.appendChild(matchersElement);
        licenses = LicenseSetFactory.emptyLicenseSet();
        licenseFamilies = LicenseFamilySetFactory.emptyLicenseFamilySet();
        approvedFamilies = new TreeSet<>();
        matchers = new HashMap<>();
    }

    @Override
    public void addLicenses(URL url) {
        read(url);
    }

    /**
     * Read the urls and extract the DOM information to create new objects.
     * 
     * @param urls The URLs to read.
     */
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

    /**
     * Applies the {@code consumer} to each node in the {@code list}
     * 
     * @param list the NodeList to process
     * @param consumer the consumer to apply to each node in the list.
     */
    private void nodeListConsumer(NodeList list, Consumer<Node> consumer) {
        for (int i = 0; i < list.getLength(); i++) {
            consumer.accept(list.item(i));
        }
    }

    /**
     * Merge the new document into the document that this reader processes.
     * 
     * @param newDoc the Document to merge.
     */
    public void add(Document newDoc) {
        nodeListConsumer(newDoc.getElementsByTagName(FAMILIES),
                nl -> nodeListConsumer( nl.getChildNodes(), 
                n -> familiesElement.appendChild(rootElement.getOwnerDocument().adoptNode(n.cloneNode(true)))));
        nodeListConsumer(newDoc.getElementsByTagName(LICENSE),
                n -> licensesElement.appendChild(rootElement.getOwnerDocument().adoptNode(n.cloneNode(true))));
        nodeListConsumer(newDoc.getElementsByTagName(APPROVED),
                nl -> nodeListConsumer( nl.getChildNodes(),
                n -> approvedElement.appendChild(rootElement.getOwnerDocument().adoptNode(n.cloneNode(true)))));
        nodeListConsumer(newDoc.getElementsByTagName(MATCHERS),
                n -> matchersElement.appendChild(rootElement.getOwnerDocument().adoptNode(n.cloneNode(true))));
    }

    /**
     * Get a map of Node attribute names to values.
     * 
     * @param node The node to process
     * @return the map of attributes on the node
     */
    private Map<String, String> attributes(Node node) {
        NamedNodeMap nnm = node.getAttributes();
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            result.put(n.getNodeName(), n.getNodeValue());
        }
        return result;
    }

    /**
     * Create a text matcher. Will construct a FullTextMatcher or a
     * SimpleTextMatcher depending on the complexity of the text.
     * 
     * @param id the id for the Matcher.
     * @param txt the text to match
     * @return the IHeaderMatcher that matches the text.
     */
    public static IHeaderMatcher createTextMatcher(String id, String txt) {
        boolean complex = txt.contains(" ") | txt.contains("\\t") | txt.contains("\\n") | txt.contains("\\r")
                | txt.contains("\\f") | txt.contains("\\v");
        return complex ? new FullTextMatcher(id, txt) : new SimpleTextMatcher(id, txt);
    }

    private AbstractBuilder parseMatcher(Node matcherNode) {
        AbstractBuilder builder = MatcherBuilderTracker.getMatcherBuilder(matcherNode.getNodeName());

        NamedNodeMap nnm = matcherNode.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            String methodName = "set" + StringUtils.capitalize(n.getNodeName());
            try {
                MethodUtils.invokeExactMethod(builder, methodName, n.getNodeValue());
            } catch (NoSuchMethodException e) {
                throw new ConfigurationException(
                        String.format("'%s' does not have a setter '%s' that takes a String argument",
                                matcherNode.getNodeName(), methodName));
            } catch (IllegalAccessException | InvocationTargetException | DOMException e) {
                throw new ConfigurationException(e);
            }
        }
        if (builder instanceof ChildContainerBuilder) {
            ChildContainerBuilder ccb = (ChildContainerBuilder) builder;
            nodeListConsumer(matcherNode.getChildNodes(), x -> {
                if (x.getNodeType() == Node.ELEMENT_NODE) {
                    ccb.add(parseMatcher(x));
                }
            });
        }
        if (builder instanceof TextCaptureBuilder) {
            ((TextCaptureBuilder) builder).setText(matcherNode.getTextContent().trim());
        }

        if (builder instanceof MatcherRefBuilder) {
            ((MatcherRefBuilder) builder).setMatchers(matchers);
        }

        if (builder.hasId()) {
            builder = new DelegatingBuilder(builder) {
                @Override
                public IHeaderMatcher build() {
                    IHeaderMatcher result = delegate.build();
                    matchers.put(result.getId(), result);
                    return result;
                }
            };
        }
        return builder;
    }

    private ILicense parseLicense(Node licenseNode) {
        Map<String, String> attributes = attributes(licenseNode);
        ILicense.Builder builder = ILicense.builder();
        Description desc = DescriptionBuilder.build(null)

        builder.setLicenseFamilyCategory(attributes.get(FAMILY));
        builder.setName(attributes.get(ATT_NAME));
        builder.setId(attributes.get(ATT_ID));

        StringBuilder notesBuilder = new StringBuilder();
        nodeListConsumer(licenseNode.getChildNodes(), x -> {
            if (x.getNodeType() == Node.ELEMENT_NODE) {
                if (x.getNodeName().equals(NOTE)) {
                    notesBuilder.append(x.getTextContent()).append("\n");
                } else {
                    builder.setMatcher(parseMatcher(x));
                }
            }
        });
        builder.setNotes(StringUtils.defaultIfBlank(notesBuilder.toString().trim(), null));
        return builder.build(licenseFamilies);
    }

    @Override
    public SortedSet<ILicense> readLicenses() {
        readFamilies();
        readMatcherBuilders();
        if (licenses.isEmpty()) {
            nodeListConsumer(document.getElementsByTagName(LICENSE), x -> licenses.add(parseLicense(x)));
            document = null;
        }
        return Collections.unmodifiableSortedSet(licenses);
    }
    

    @Override
    public SortedSet<ILicenseFamily> readFamilies() {
        if (licenseFamilies.isEmpty()) {
            nodeListConsumer(document.getElementsByTagName(FAMILIES),
                    x -> nodeListConsumer(x.getChildNodes(), this::parseFamily));
            nodeListConsumer(document.getElementsByTagName(APPROVED),
                    x -> nodeListConsumer(x.getChildNodes(), this::parseApproved));
        }
        return Collections.unmodifiableSortedSet(licenseFamilies);
    }
    
    private ILicenseFamily parseFamily(Map<String, String> attributes) {
        if (attributes.containsKey(ATT_ID)) {
            ILicenseFamily.Builder builder = ILicenseFamily.builder();
            builder.setLicenseFamilyCategory(attributes.get(ATT_ID));
            builder.setLicenseFamilyName(StringUtils.defaultIfBlank(attributes.get(ATT_NAME), attributes.get(ATT_ID)));
            return builder.build();
        }
        return null;
    }

    private void parseFamily(Node familyNode) {
        if (FAMILY.equals(familyNode.getNodeName())) {
            ILicenseFamily result = parseFamily(attributes(familyNode));
            if (result == null) {
                throw new ConfigurationException(String.format("families/family tag requires %s attribute", ATT_ID));
            }
            licenseFamilies.add(result);
        }
    }

    private void parseApproved(Node approvedNode) {
        if (FAMILY.equals(approvedNode.getNodeName())) {
            Map<String, String> attributes = attributes(approvedNode);
            if (attributes.containsKey(ATT_LICENSE_REF)) {
                approvedFamilies.add(attributes.get(ATT_LICENSE_REF));
            } else if (attributes.containsKey(ATT_ID)) {
                ILicenseFamily target = parseFamily(attributes);
                licenseFamilies.add(target);
                approvedFamilies.add(target.getFamilyCategory());
            } else {
                throw new ConfigurationException(
                        String.format("family tag requires %s or %s attribute", ATT_LICENSE_REF, ATT_ID));
            }
        }
    }

    @Override
    public SortedSet<String> approvedLicenseId() {
        if (licenses.isEmpty()) {
            this.readLicenses();
        }
        if (approvedFamilies.isEmpty()) {
            SortedSet<String> result = new TreeSet<>();
            licenses.stream().map(x -> x.getLicenseFamily().getFamilyCategory()).forEach(result::add);
            return result;
        }
        return Collections.unmodifiableSortedSet(approvedFamilies);
    }

    private void parseMatcherBuilder(Node classNode) {
        Map<String, String> attributes = attributes(classNode);
        if (attributes.get(ATT_CLASS_NAME) == null) {
            throw new ConfigurationException("matcher must have a " + ATT_CLASS_NAME + " attribute");
        }
        MatcherBuilderTracker.addBuilder(attributes.get(ATT_CLASS_NAME), attributes.get(ATT_NAME));
    }

    @Override
    public void readMatcherBuilders() {
        nodeListConsumer(document.getElementsByTagName(MATCHER), this::parseMatcherBuilder);
    }

    @Override
    public void addMatchers(URL url) {
        read(url);
    }

    /**
     * An abstract builder that delegates to another abstract builder.
     */
    abstract static class DelegatingBuilder extends AbstractBuilder {
        protected final AbstractBuilder delegate;

        DelegatingBuilder(AbstractBuilder delegate) {
            this.delegate = delegate;
        }
    }
}
