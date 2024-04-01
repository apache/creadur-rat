/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.configuration.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;
import java.util.SortedSet;

import org.apache.rat.ConfigurationException;
import org.apache.rat.configuration.XMLConfigurationReader;
import org.apache.rat.license.ILicense;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;

public class CopyrightBuilderTest {

    @Test
    public void xmlTest() {
        String configStr = "<rat-config>"
                + "        <families>"
                + "            <family id='newFam' name='my new family' />"
                + "        </families>"
                + "        <licenses>"
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>"
                + "                <copyright/>"
                + "            </license>"
                + "        </licenses>"
                + "    </rat-config>"
                + "";
        
        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        SortedSet<ILicense> licenses = reader.readLicenses();
        assertEquals(1, licenses.size());
    }
    
    @Test
    public void xmlOwnerTest() {
        String configStr = "<rat-config>"
                + "        <families>"
                + "            <family id='newFam' name='my new family' />"
                + "        </families>"
                + "        <licenses>"
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>"
                + "                <copyright owner='someone'/>"
                + "            </license>"
                + "        </licenses>"
                + "    </rat-config>"
                + "";
        
        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        SortedSet<ILicense> licenses = reader.readLicenses();
        assertEquals(1, licenses.size());
    }
    
    @Test
    public void xmlStartTest() {
        String configStr = "<rat-config>"
                + "        <families>"
                + "            <family id='newFam' name='my new family' />"
                + "        </families>"
                + "        <licenses>"
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>"
                + "                <copyright start='1989'/>"
                + "            </license>"
                + "        </licenses>"
                + "    </rat-config>"
                + "";
        
        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        SortedSet<ILicense> licenses = reader.readLicenses();
        assertEquals(1, licenses.size());
    }
    
    @Test
    public void xmlStartOwnerTest() {
        String configStr = "<rat-config>"
                + "        <families>"
                + "            <family id='newFam' name='my new family' />"
                + "        </families>"
                + "        <licenses>"
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>"
                + "                <copyright start='1989' owner='somebody'/>"
                + "            </license>"
                + "        </licenses>"
                + "    </rat-config>"
                + "";
        
        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        SortedSet<ILicense> licenses = reader.readLicenses();
        assertEquals(1, licenses.size());
    }
    
    @Test
    public void xmlEndTest() {
        String configStr = "<rat-config>"
                + "        <families>"
                + "            <family id='newFam' name='my new family' />"
                + "        </families>"
                + "        <licenses>"
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>"
                + "                <copyright start='1989' end='1990'/>"
                + "            </license>"
                + "        </licenses>"
                + "    </rat-config>"
                + "";
        
        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        SortedSet<ILicense> licenses = reader.readLicenses();
        assertEquals(1, licenses.size());
    }
    
    @Test
    public void xmlEndOwnerTest() {
        String configStr = "<rat-config>"
                + "        <families>"
                + "            <family id='newFam' name='my new family' />"
                + "        </families>"
                + "        <licenses>"
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>"
                + "                <copyright start='1989' stop='1990' owner='somebody'/>"
                + "            </license>"
                + "        </licenses>"
                + "    </rat-config>"
                + "";
        
        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        SortedSet<ILicense> licenses = reader.readLicenses();
        assertEquals(1, licenses.size());
    }
    
    @Test
    public void xmlEndNoStartTest() {
        String configStr = "<rat-config>"
                + "        <families>"
                + "            <family id='newFam' name='my new family' />"
                + "        </families>"
                + "        <licenses>"
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>"
                + "                <copyright stop='1990'/>"
                + "            </license>"
                + "        </licenses>"
                + "    </rat-config>"
                + "";
        
        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        assertThrows(ConfigurationException.class, ()-> reader.readLicenses());
    }
    
    @Test
    public void xmlNonNumericStartTest() {
        String configStr = "<rat-config>"
                + "        <families>"
                + "            <family id='newFam' name='my new family' />"
                + "        </families>"
                + "        <licenses>"
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>"
                + "                <copyright start='now'/>"
                + "            </license>"
                + "        </licenses>"
                + "    </rat-config>"
                + "";
        
        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        assertThrows(ConfigurationException.class, ()-> reader.readLicenses());
    }
    
    @Test
    public void xmlNonNumericStopTest() {
        String configStr = "<rat-config>"
                + "        <families>"
                + "            <family id='newFam' name='my new family' />"
                + "        </families>"
                + "        <licenses>"
                + "            <license family='newFam' id='EXAMPLE' name='Example License'>"
                + "                <copyright start='1990' stop='now'/>"
                + "            </license>"
                + "        </licenses>"
                + "    </rat-config>"
                + "";
        
        XMLConfigurationReader reader = new XMLConfigurationReader();
        reader.read(new StringReader(configStr));
        assertThrows(ConfigurationException.class, ()-> reader.readLicenses());
    }
}
