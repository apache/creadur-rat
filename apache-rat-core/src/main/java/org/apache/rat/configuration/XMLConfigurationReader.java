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
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.BuilderParams;
import org.apache.rat.ConfigurationException;
import org.apache.rat.ImplementationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.builders.AbstractBuilder;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A class that reads the XML configuration file format.
 */
public final class XMLConfigurationReader implements LicenseReader, MatcherReader {

    private Log log;
    private Document document;
    private final Element rootElement;
    private final Element familiesElement;
    private final Element licensesElement;
    private final Element approvedElement;
    private final Element matchersElement;

    private final SortedSet<ILicense> licenses;

    private final Map<String, IHeaderMatcher> matchers;
    private final BuilderParams builderParams;
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
        rootElement = document.createElement(XMLConfig.ROOT);
        document.appendChild(rootElement);
        familiesElement = document.createElement(XMLConfig.FAMILIES);
        rootElement.appendChild(familiesElement);
        licensesElement = document.createElement(XMLConfig.LICENSES);
        rootElement.appendChild(licensesElement);
        approvedElement = document.createElement(XMLConfig.APPROVED);
        rootElement.appendChild(approvedElement);
        matchersElement = document.createElement(XMLConfig.MATCHERS);
        rootElement.appendChild(matchersElement);
        licenses = new TreeSet<>();
        licenseFamilies = new TreeSet<>();
        approvedFamilies = new TreeSet<>();
        matchers = new HashMap<>();
        builderParams = new BuilderParams() {
            @Override
            public Map<String, IHeaderMatcher> matcherMap() {
                return matchers;
            }

            @Override
            public SortedSet<ILicenseFamily> licenseFamilies() {
                return licenseFamilies;
            }
        };
    }

    @Override
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Returns the log the reader.
     * @return the log if set, if not set {@code DefaultLog.getInstance()} is returned.
     */
    public Log getLog() {
        return log == null ? DefaultLog.getInstance() : log;
    }

    @Override
    public void addLicenses(URL url) {
        read(url);
    }

    /**
     * Read xml from a reader.
     *
     * @param reader the reader to read XML from.
     */
    public void read(Reader reader) {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException("Unable to create DOM builder", e);
        }

        try {
            add(builder.parse(new InputSource(reader)));
        } catch (SAXException | IOException e) {
            throw new ConfigurationException("Unable to read inputSource", e);
        }

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
            try (InputStream inputStream = url.openStream()){
                add(builder.parse(inputStream));
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
        nodeListConsumer(newDoc.getElementsByTagName(XMLConfig.FAMILIES), nl -> nodeListConsumer(nl.getChildNodes(),
                n -> familiesElement.appendChild(rootElement.getOwnerDocument().adoptNode(n.cloneNode(true)))));
        nodeListConsumer(newDoc.getElementsByTagName(XMLConfig.LICENSE),
                n -> licensesElement.appendChild(rootElement.getOwnerDocument().adoptNode(n.cloneNode(true))));
        nodeListConsumer(newDoc.getElementsByTagName(XMLConfig.APPROVED), nl -> nodeListConsumer(nl.getChildNodes(),
                n -> approvedElement.appendChild(rootElement.getOwnerDocument().adoptNode(n.cloneNode(true)))));
        nodeListConsumer(newDoc.getElementsByTagName(XMLConfig.MATCHERS),
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

    private void callSetter(Description desc, IHeaderMatcher.Builder builder, Object value) {
        try {
            desc.setter(builder.getClass()).invoke(builder, value);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    private void processBuilderParams(Description description, IHeaderMatcher.Builder builder) {
        for (Description desc : description.childrenOfType(ComponentType.BUILD_PARAMETER)) {
            Method m = builderParams.get(desc.getCommonName());
            try {
                callSetter(desc, builder, m.invoke(builderParams));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw ImplementationException.makeInstance(e);
            }
        }
    }

    /**
     * Processes a list of children by passing each child node and the description
     * of the child (if any) to the BiPredicate. If there is not a child description
     * for the node it is ignored. If the node is processed it is removed from list
     * of children.
     * 
     * @param description the Description of the node being processed
     * @param children the child nodes of that node.
     * @param childProcessor the function that handles the processing of the child
     * node.
     */
    private void processChildren(Description description, List<Node> children,
            BiPredicate<Node, Description> childProcessor) {
        Iterator<Node> iter = children.iterator();
        while (iter.hasNext()) {
            Node child = iter.next();
            Description childDescription = description.getChildren().get(child.getNodeName());
            if (childDescription != null) {
                if (childProcessor.test(child, childDescription)) {
                    iter.remove();
                }
            }
        }
    }

    private BiPredicate<Node, Description> matcherChildNodeProcessor(AbstractBuilder builder, Description description) {
        return (child, childDescription) -> {
            switch (childDescription.getType()) {
            case LICENSE:
            case BUILD_PARAMETER:
                throw new ConfigurationException(String.format(
                        "%s may not be used as an enclosed matcher.  %s '%s' found in '%s'", childDescription.getType(),
                        childDescription.getType(), childDescription.getCommonName(), description.getCommonName()));
            case MATCHER:
                AbstractBuilder b = parseMatcher(child);
                callSetter(b.getDescription(), builder, b);
                return true;
            case PARAMETER:
                if (!XMLConfig.isInlineNode(description.getCommonName(), childDescription.getCommonName())
                        || childDescription.getChildType() == String.class) {
                    callSetter(childDescription, builder, child.getTextContent());
                } else {
                    callSetter(childDescription, builder, parseMatcher(child));
                }
                return true;
            }
            return false;
        };
    }

    private void setValue(Description description, Description childDescription, IHeaderMatcher.Builder builder,
            Node child) {
        if (childDescription.getChildType() == String.class) {
            callSetter(description, builder, child.getTextContent());
        } else {
            callSetter(description, builder, parseMatcher(child));
        }
    }

    /**
     * Process the ELEEMENT_NODEs children of the parent whos names match child
     * descriptions. All children children are processed with the childProcessor. If
     * the childProcessor handles the node it is not included in the resulting list.
     * 
     * @param description the Description of the parent node.
     * @param parent the node being processed
     * @param childProcessor the BiProcessor to handle process each child. if the
     * processor handles the child it must return {@code true}.
     * @return A Pair comprising a boolean flag indicating children were found, and
     * a list of all child nodes that were not processed by the childProcessor.
     */
    private Pair<Boolean, List<Node>> processChildNodes(Description description, Node parent,
            BiPredicate<Node, Description> childProcessor) {
        boolean foundChildren = false;
        List<Node> children = new ArrayList<>();
        // check XML child nodes.
        if (parent.hasChildNodes()) {

            nodeListConsumer(parent.getChildNodes(), (n) -> {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    children.add(n);
                }
            });
            foundChildren = !children.isEmpty();
            if (foundChildren) {
                processChildren(description, children, childProcessor);
            }
        }
        return new ImmutablePair<>(foundChildren, children);
    }

    private AbstractBuilder parseMatcher(Node matcherNode) {
        final AbstractBuilder builder = MatcherBuilderTracker.getMatcherBuilder(matcherNode.getNodeName());

        try {
            final Description description = DescriptionBuilder.buildMap(builder.builtClass());
            if (description == null) {
                throw new ConfigurationException(String.format("Unable to build description for %s", builder.builtClass()));
            }
            processBuilderParams(description, builder);

            // process the attributes
            description.setChildren(getLog(), builder, attributes(matcherNode));

            // check XML child nodes.
            Pair<Boolean, List<Node>> pair = processChildNodes(description, matcherNode,
                    matcherChildNodeProcessor(builder, description));
            boolean foundChildren = pair.getLeft();
            List<Node> children = pair.getRight();

            // check for inline nodes that can accept child nodes.
            List<Description> childDescriptions = description.getChildren().values().stream()
                    .filter(d -> XMLConfig.isInlineNode(description.getCommonName(), d.getCommonName()))
                    .collect(Collectors.toList());

            for (Description childDescription : childDescriptions) {
                if (XMLConfig.isInlineNode(description.getCommonName(), childDescription.getCommonName())) {
                    // can only process text inline if there were not child nodes.
                    if (childDescription.getChildType() == String.class) {
                        if (!foundChildren) {
                            callSetter(childDescription, builder, matcherNode.getTextContent());
                        }
                    } else {
                        Iterator<Node> iter = children.iterator();
                        while (iter.hasNext()) {
                            Node child = iter.next();
                            callSetter(childDescription, builder, parseMatcher(child));
                            iter.remove();
                        }
                    }

                } else {
                    processChildren(description, children, (child, childD) -> {
                        if (childD.getChildType().equals(description.getChildType())) {
                            setValue(childDescription, childD, builder, child);
                            return true;
                        }
                        return false;
                    });
                }

            }

            if (!children.isEmpty()) {
                children.forEach(n -> getLog().warn(String.format("unrecognised child node '%s' in node '%s'%n",
                        n.getNodeName(), matcherNode.getNodeName())));
            }

        } catch (DOMException e) {
            throw new ConfigurationException(e);
        }
        return builder.hasId() ? new IDRecordingBuilder(matchers, builder) : builder;
    }

    private BiPredicate<Node, Description> licenseChildNodeProcessor(ILicense.Builder builder,
            Description description) {
        return (child, childDescription) -> {
            switch (childDescription.getType()) {
            case LICENSE:
                throw new ConfigurationException(String.format(
                        "%s may not be enclosed in another license.  %s '%s' found in '%s'", childDescription.getType(),
                        childDescription.getType(), childDescription.getCommonName(), description.getCommonName()));
            case BUILD_PARAMETER:
                break;
            case MATCHER:
                AbstractBuilder b = parseMatcher(child);
                callSetter(b.getDescription(), builder, b);
                return true;
            case PARAMETER:
                if ((!XMLConfig.isLicenseChild(childDescription.getCommonName()))
                        || childDescription.getChildType() == String.class) {
                    callSetter(childDescription, builder, child.getTextContent());
                } else {
                    callSetter(childDescription, builder, parseMatcher(child));
                }
                return true;
            }
            return false;
        };
    }

    private ILicense parseLicense(Node licenseNode) {
        ILicense.Builder builder = ILicense.builder();
        // get the description for the builder
        Description description = builder.getDescription();
        // set the BUILDER_PARAM options from the description
        processBuilderParams(description, builder);
        // set the children from attributes.
        description.setChildren(getLog(), builder, attributes(licenseNode));
        // set children from the child nodes
        Pair<Boolean, List<Node>> pair = processChildNodes(description, licenseNode,
                licenseChildNodeProcessor(builder, description));
        List<Node> children = pair.getRight();

        // check for inline nodes that can accept child nodes.
        List<Description> childDescriptions = description.getChildren().values().stream()
                .filter(d -> XMLConfig.isLicenseInline(d.getCommonName())).collect(Collectors.toList());
        for (Description childDescription : childDescriptions) {
            Iterator<Node> iter = children.iterator();
            while (iter.hasNext()) {
                callSetter(childDescription, builder, parseMatcher(iter.next()));
                iter.remove();
            }
        }

        if (!children.isEmpty()) {
            children.forEach(n -> getLog().warn(String.format("unrecognised child node '%s' in node '%s'%n",
                    n.getNodeName(), licenseNode.getNodeName())));
        }
        return builder.build();
    }

    @Override
    public SortedSet<ILicense> readLicenses() {
        readFamilies();
        readMatcherBuilders();
        if (licenses.isEmpty()) {
            nodeListConsumer(document.getElementsByTagName(XMLConfig.LICENSE), x -> licenses.add(parseLicense(x)));
            document = null;
        }
        return Collections.unmodifiableSortedSet(licenses);
    }

    @Override
    public SortedSet<ILicenseFamily> readFamilies() {
        if (licenseFamilies.isEmpty()) {
            nodeListConsumer(document.getElementsByTagName(XMLConfig.FAMILIES),
                    x -> nodeListConsumer(x.getChildNodes(), this::parseFamily));
            nodeListConsumer(document.getElementsByTagName(XMLConfig.APPROVED),
                    x -> nodeListConsumer(x.getChildNodes(), this::parseApproved));
        }
        return Collections.unmodifiableSortedSet(licenseFamilies);
    }

    private ILicenseFamily parseFamily(Map<String, String> attributes) {
        if (attributes.containsKey(XMLConfig.ATT_ID)) {
            ILicenseFamily.Builder builder = ILicenseFamily.builder();
            builder.setLicenseFamilyCategory(attributes.get(XMLConfig.ATT_ID));
            builder.setLicenseFamilyName(
                    StringUtils.defaultIfBlank(attributes.get(XMLConfig.ATT_NAME), attributes.get(XMLConfig.ATT_ID)));
            return builder.build();
        }
        return null;
    }

    private void parseFamily(Node familyNode) {
        if (XMLConfig.FAMILY.equals(familyNode.getNodeName())) {
            ILicenseFamily result = parseFamily(attributes(familyNode));
            if (result == null) {
                throw new ConfigurationException(
                        String.format("families/family tag requires %s attribute", XMLConfig.ATT_ID));
            }
            licenseFamilies.add(result);
        }
    }

    private void parseApproved(Node approvedNode) {
        if (XMLConfig.FAMILY.equals(approvedNode.getNodeName())) {
            Map<String, String> attributes = attributes(approvedNode);
            if (attributes.containsKey(XMLConfig.ATT_LICENSE_REF)) {
                approvedFamilies.add(attributes.get(XMLConfig.ATT_LICENSE_REF));
            } else if (attributes.containsKey(XMLConfig.ATT_ID)) {
                ILicenseFamily target = parseFamily(attributes);
                licenseFamilies.add(target);
                approvedFamilies.add(target.getFamilyCategory());
            } else {
                throw new ConfigurationException(String.format("family tag requires %s or %s attribute",
                        XMLConfig.ATT_LICENSE_REF, XMLConfig.ATT_ID));
            }
        }
    }

    ////////////////////////////////////////// MatcherReader methods
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
        if (attributes.get(XMLConfig.ATT_CLASS_NAME) == null) {
            throw new ConfigurationException("matcher must have a " + XMLConfig.ATT_CLASS_NAME + " attribute");
        }
        MatcherBuilderTracker.addBuilder(attributes.get(XMLConfig.ATT_CLASS_NAME), attributes.get(XMLConfig.ATT_NAME));
    }

    @Override
    public void readMatcherBuilders() {
        nodeListConsumer(document.getElementsByTagName(XMLConfig.MATCHER), this::parseMatcherBuilder);
    }

    @Override
    public void addMatchers(URL url) {
        read(url);
    }

    ////////////// Helper classes
    /**
     * An abstract builder that delegates to another abstract builder.
     */
    static class IDRecordingBuilder extends AbstractBuilder {
        private final AbstractBuilder delegate;
        private final Map<String, IHeaderMatcher> matchers;

        IDRecordingBuilder(final Map<String, IHeaderMatcher> matchers, final AbstractBuilder delegate) {
            this.delegate = delegate;
            this.matchers = matchers;
            setId(delegate.getId());
        }

        @Override
        public IHeaderMatcher build() {
            IHeaderMatcher result = delegate.build();
            matchers.put(result.getId(), result);
            return result;
        }

        @Override
        public Class<?> builtClass() throws SecurityException {
            return delegate.builtClass();
        }

        @Override
        public Description getDescription() {
            return delegate.getDescription();
        }
    }
}
