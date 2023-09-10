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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration2.Configuration;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.analysis.matchers.AndMatcher;
import org.apache.rat.analysis.matchers.CopyrightMatcher;
import org.apache.rat.analysis.matchers.FullTextMatcher;
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

public class XMLConfigurationReader implements Reader {

    enum MatcherType {
        text, copyright, spdx, and, or
    };

    private Document complete;
    private final Element baseElement;
    private final SortedSet<ILicense> licenses;

    public XMLConfigurationReader() {
        try {
            complete = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("No XML parser defined", e);
        }
        baseElement = complete.createElement("rat-config");
        licenses = new TreeSet<>();
    }

    @Override
    public void add(URL url) {
        try {
            read(url);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException("can not parse "+url, e);
        }

    }

    public void read(URL... urls) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        for (URL url : urls) {
            Document document = builder.parse(url.openStream());
            add(document);
        }
    }

    private void nodeListConsumer(NodeList list, Consumer<Node> consumer) {
        for (int i = 0; i < list.getLength(); i++) {
            consumer.accept(list.item(i));
        }
    }

    public void add(Document document) {
        nodeListConsumer(document.getElementsByTagName("rat-config"),
                (x) -> nodeListConsumer(x.getChildNodes(), baseElement::appendChild));
    }

    @Override
    public SortedSet<ILicenseFamily> readFamilies() {
        SortedSet<ILicenseFamily> result = new TreeSet<>();
        readLicenses().stream().map(ILicense::getLicenseFamily).forEach(result::add);
        return result;
    }

    Set<String> extractKeys(Configuration cfg) {
        Iterator<String> iter = cfg.getKeys();
        Set<String> ids = new TreeSet<>();
        while (iter.hasNext()) {
            ids.add(iter.next().split("\\.")[0]);
        }
        return ids;
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

    private IHeaderMatcher parseMatcher(Node matcherNode) {
        String hasSpaces = ".*\\s.*";
        MatcherType type = MatcherType.valueOf(matcherNode.getLocalName());
        switch (type) {
        case text:
            String txt = matcherNode.getTextContent();
            return Pattern.matches(hasSpaces, txt) ? new FullTextMatcher(txt) : new SimpleTextMatcher(txt);

        case or:
        case and:
            List<IHeaderMatcher> children = new ArrayList<>();
            nodeListConsumer(matcherNode.getChildNodes(), x -> children.add(parseMatcher(x)));
            return type == MatcherType.or ? new OrMatcher(children) : new AndMatcher(children);

        case copyright:
            Map<String, String> attr = attributes(matcherNode);
            return new CopyrightMatcher(attr.get("start"), attr.get("end"), attr.get("owner"));

        case spdx:
            return SPDXMatcherFactory.INSTANCE.create(attributes(matcherNode).get("name"));
        }
        throw new IllegalStateException(String.format("%s is not a matcher type", matcherNode.getLocalName()));
    }

    private ILicense parseLicense(Node licenseNode) {
        Map<String, String> attributes = attributes(licenseNode);

        ILicenseFamily family = new SimpleLicenseFamily(attributes.get("id"), attributes.get("name"));
        IHeaderMatcher matcher[] = { null };
        StringBuilder notesBuilder = new StringBuilder();
        nodeListConsumer(licenseNode.getChildNodes(), x -> {
            if (x.getLocalName().equals("notes")) {
                notesBuilder.append(x.getTextContent()).append("\n");
            } else {
                matcher[0] = parseMatcher(x);
            }
        });
        ILicense derivedFrom = attributes.get("derived-from") == null ? null
                : new ILicenseProxy(attributes.get("derived-from"));
        String notes = notesBuilder.length() == 0 ? null : notesBuilder.toString();
        return new SimpleLicense(family, matcher[0], derivedFrom, notes);
    }

    @Override
    public SortedSet<ILicense> readLicenses() {
        if (licenses.size() == 0) {
            nodeListConsumer(complete.getElementsByTagName("license"), x -> licenses.add(parseLicense(x)));
            complete = null;
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
                Optional<ILicense> lic = licenses.stream()
                        .filter(l -> l.getLicenseFamily().getFamilyCategory().equals(proxyId)).findFirst();
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
        public boolean matches(String line) throws RatHeaderAnalysisException {
            checkProxy();
            return wrapped.matches(line);
        }

        @Override
        public void reportFamily(Consumer<ILicenseFamily> consumer) {
            checkProxy();
            wrapped.reportFamily(consumer);
        }

        @Override
        public void extractMatcher(Consumer<IHeaderMatcher> consumer, Predicate<ILicenseFamily> comparator) {
            checkProxy();
            wrapped.extractMatcher(consumer, comparator);
        }

    }
}
