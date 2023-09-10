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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.analysis.license.CopyrightHeader;
import org.apache.rat.analysis.license.FullTextMatchingLicense;
import org.apache.rat.analysis.license.SimplePatternBasedLicense;
import org.apache.rat.analysis.matchers.AndMatcher;
import org.apache.rat.analysis.matchers.FullTextMatcher;
import org.apache.rat.analysis.matchers.OrMatcher;
import org.apache.rat.analysis.matchers.SPDXMatcher;
import org.apache.rat.analysis.matchers.SPDXMatcherFactory;
import org.apache.rat.analysis.matchers.SimpleTextMatcher;
import org.apache.rat.api.MetaData;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.SimpleLicense;
import org.apache.rat.license.SimpleLicenseFamily;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    
    
    enum MatcherType { text, copyright, spdx, and, or };
    
    private final Document complete;
    private final Element baseElement;
    private final SortedSet<ILicense> licenses;
    
    public XMLConfigurationReader() {
        try {
            complete = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException( "No XML parser defined", e);
        }
        baseElement = complete.createElement("rat-config");
        licenses = new TreeSet<>();
    }

    @Override
    public void add(URL url) {
        try {
            read(url);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    public void read(URL... urls) throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        for (URL url : urls) {
            Document document = builder.parse(url.openStream());
            add(document);
        }
    }
    
    private void nodeListConsumer(NodeList list, Consumer<Node> consumer) {
        for (int i=0; i<list.getLength(); i++) {
            consumer.accept(list.item(i));
        }
    }

    public void add(Document document) {
        nodeListConsumer(document.getElementsByTagName("rat-config"),
                (x) -> nodeListConsumer( x.getChildNodes(), baseElement::appendChild));
    }
    
    

    @Override
    public SortedSet<ILicenseFamily> readFamilies() {
        SortedSet<ILicenseFamily> result = new TreeSet<>();
        readLicenses().stream().map( ILicense::getLicenseFamily).forEach( result::add );
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
    
    Map<String,String> attributes(Node node) {
        NamedNodeMap nnm = node.getAttributes();
        Map<String,String> result = new HashMap<>();
        for (int i=0; i<nnm.getLength(); i++) {
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
            nodeListConsumer( matcherNode.getChildNodes(), x -> children.add(parseMatcher(x)));
            return type==MatcherType.or ? new OrMatcher( children ) : new AndMatcher( children );
            
        case copyright:
            Map<String,String> attr = attributes(matcherNode);
            return new CopyrightMatcher(attr.get('start'), attr.get('end'), attr.get('owner'));
            
        case spdx:
            return SPDXMatcherFactory.INSTANCE.create(attributes(matcherNode).get("name"));
        }
        
    }
    
    private ILicense parseLicense(Node licenseNode)
    {
       Map<String,String> attributes = attributes(licenseNode);
       
       ILicenseFamily family = new SimpleLicenseFamily( attributes.get("id"), attributes.get("name"));
       IHeaderMatcher matcher[] = {null};
       StringBuilder notesBuilder = new StringBuilder();
       nodeListConsumer( licenseNode.getChildNodes(), x -> { 
           if (x.getLocalName().equals("notes")) {
               notesBuilder.append(x.getTextContent()).append("\n");
           } else {
               matcher[0] = parseMatcher(x);
           }
       });
       ILicense derivedFrom = attributes.get("derived-from")==null?null:new ILicenseProxy(attributes.get("derived-from"));
       String notes = notesBuilder.length() == 0 ? null : notesBuilder.toString();
       return new SimpleLicense( family, matcher[0], derivedFrom, notes);
    }
    
    @Override
    public SortedSet<ILicense> readLicenses() {
        if (licenses.size() == 0) {
            nodeListConsumer( complete.getElementsByTagName("license"), x -> licenses.add( parseLicense(x)));
            complete = null;
        }
        return Collections.unmodifiableSortedSet(licenses);
        /*
        SortedSet<ILicenseFamily> families = readFamilies();
        Collection<IHeaderMatcher> result = new ArrayList<>();
        List<IHeaderMatcher> licenses = new ArrayList<>();
        Configuration licenseConfig = configuration.subset("license");
        for (String id : extractKeys(licenseConfig)) {
            Configuration lic = licenseConfig.subset(id);
            ILicenseFamily licenseFamily = ILicenseFamily.searchSet(families, lic.getString("family"));
            if (licenseFamily == null) {
                throw new IllegalArgumentException(
                        String.format("license %s uses family %s which is missing", id, lic.getString("family")));
            }
            Set<String> keys = extractKeys(lic);
            String notes = lic.getString("notes", "");
            if (keys.contains("fullText")) {
                licenses.add(new FullTextMatchingLicense(id, licenseFamily, notes, lic.getString("fullText")));
            }
            if (keys.contains("copyright")) {
                licenses.add(new CopyrightHeader(id, licenseFamily, notes, lic.getString("copyright")));
            }
            if (keys.contains("text")) {
                Configuration text = lic.subset("text");
                Set<String> txtKeys = extractKeys(text);
                List<String> texts = txtKeys.stream().map(text::getString).collect(Collectors.toList());
                licenses.add(
                        new SimplePatternBasedLicense(id, licenseFamily, notes, texts.toArray(new String[texts.size()])));
            }
            if (keys.contains("spdx")) {
                licenses.add(SPDXMatcher.INSTANCE.register(id, licenseFamily, notes, lic.getString("spdx")));
            }
            if (licenses.size() == 1) {
                result.add(licenses.get(0));
                licenses.clear();
            } else if (licenses.size() > 1) {
                result.add(new OrMatcher(id, licenses));
                licenses = new ArrayList<>();
            }
        }
        return licenses;
        */
    }
    
    class ILicenseProxy implements ILicense {
        final String proxyId;
        ILicense wrapped;
        
        ILicenseProxy(String proxyId) {
            this.proxyId = proxyId;
        }
        
        private void checkProxy() {
            if (wrapped == null) {
                wrapped = complete.stream().
            }
        }

        public ILicenseFamily getLicenseFamily() {
            checkProxy();
            return wrapped.getLicenseFamily();
        }

        public String getNotes() {
            checkProxy();
            return wrapped.getNotes();
        }

        public ILicense derivedFrom() {
            checkProxy();
            return wrapped.derivedFrom();
        }

        public String getId() {
            checkProxy();
            return wrapped.getId();
        }

        public void reset() {
            checkProxy();
            wrapped.reset();
        }

        public boolean match(MetaData metadata, String line) throws RatHeaderAnalysisException {
            checkProxy();
            return wrapped.match(metadata, line);
        }

        public void reportFamily(Consumer<ILicenseFamily> consumer) {
            checkProxy();
            wrapped.reportFamily(consumer);
        }

        public void extractMatcher(Consumer<IHeaderMatcher> consumer, Predicate<ILicenseFamily> comparator) {
            checkProxy();
            wrapped.extractMatcher(consumer, comparator);
        }
        
    }
}
