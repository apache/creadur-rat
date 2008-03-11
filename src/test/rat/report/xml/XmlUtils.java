/*
 * Copyright 2006 Robert Burrell Donkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package rat.report.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public final class XmlUtils {

    public static final boolean isWellFormedXml(final String string) throws Exception {
        return isWellFormedXml(new StringBufferInputStream(string));
    }

        

    public static final boolean isWellFormedXml(final InputStream in) throws Exception {
        boolean result = true;
        try {
            toDom(in);
        } catch (SAXException e) {
            System.out.println(e);
            e.printStackTrace();
            result = false;
        }
        return result;
    }
    
    public static final Document toDom(final InputStream in) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document result;
        result = builder.parse(in);
        return result;
    }
}
